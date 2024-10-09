#!/bin/bash
SHELL_FOLDER=$(cd $(dirname $0);pwd)
cd ${SHELL_FOLDER}

# JAVA_HOME="/nemo/jdk8"
APP_MAIN=com.webank.wedpr.pir.main.PirApplication
LOG_DIR=.
SERVER_NAME="PPCS-PIR"
CLASSPATH='conf/:apps/*:lib/*'


STATUS_STARTING="Starting"
STATUS_RUNNING="Running"
STATUS_STOPPED="Stopped"
start_success_log="start.*success"


if [ "${JAVA_HOME}" = "" ];then
    JAVA_HOME=/nemo/jdk8u382-b05
    echo "JAVA_HOME has not been configured, set  to default: ${JAVA_HOME}"
fi

LOG_INFO()
{
    local content=${1}
    echo -e "\033[32m${content}\033[0m"
}

LOG_ERROR()
{
    local content=${1}
    echo -e "\033[31m${content}\033[0m"
}

app_pid(){
    ps aux |grep "$APP_MAIN" | grep "${SHELL_FOLDER}/" | awk -F' ' '{print $2}'
}

JAVA_OPTS=" -Dfile.encoding=UTF-8"
JAVA_OPTS+=" -Djava.security.egd=file:/dev/./urandom"
JAVA_OPTS+=" -Xmx256m -Xms256m -Xmn128m -Xss512k -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m"
JAVA_OPTS+=" -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${SHELL_FOLDER}/heap_error.log"
JAVA_OPTS+=" -XX:+UseG1GC -Xloggc:${LOG_DIR}/logs/${SERVER_NAME}-gc.log -XX:+PrintGCDateStamps"
JAVA_OPTS+=" -DserviceName=${SERVER_NAME}"
run_app()
{
    nohup $JAVA_HOME/bin/java $JAVA_OPTS -cp $CLASSPATH $APP_MAIN > start.out 2>&1 &
}

app_status()
{
    if [ ! -z "$(app_pid)" ]; then
        if [ ! -z "$(grep -i "${start_success_log}" start.out)" ]; then
            echo ${STATUS_RUNNING}
        else
            echo ${STATUS_STARTING}
        fi
    else
        echo ${STATUS_STOPPED}
    fi
}

before_start()
{
    local status=$(app_status)
    case ${status} in
        ${STATUS_STARTING})
            LOG_ERROR "${APP_MAIN} is starting, pid is $(app_pid)"
            exit 0
            ;;
        ${STATUS_RUNNING})
            LOG_ERROR "${APP_MAIN} is running, pid is $(app_pid)"
            exit 0
            ;;
        ${STATUS_STOPPED})
            # do nothing
            ;;
        *)
            exit 1
            ;;
    esac
}

start(){
    rm -f start.out
    run_app
    LOG_INFO "${APP_MAIN} booting up .."
    try_times=45
    i=0
    while [ $i -lt ${try_times} ]
    do
        sleep 1
        local status=$(app_status)

        case ${status} in
            ${STATUS_STARTING})
                echo -e "\033[32m.\033[0m\c"
                ;;
            ${STATUS_RUNNING})
                break
                ;;
            ${STATUS_STOPPED})
                break
                ;;
            *)
                exit 1
                ;;
        esac

        ((i=i+1))
    done
    echo ""
}

after_start()
{
    local status=$(app_status)
    case ${status} in
        ${STATUS_STARTING})
            kill $(app_pid)
            LOG_ERROR "Exceed waiting time. Killed. Please try to start ${APP_MAIN}:${app_pid} again"
            exit 1
            ;;
        ${STATUS_RUNNING})
            LOG_INFO "${APP_MAIN}:${app_pid} start successfully!"
            if [ "${LOG_DIR}" != "." ];then
              rm -rf logs
              ln -s ${LOG_DIR}/logs/ppcs-modeladm logs
            fi
            ;;
        ${STATUS_STOPPED})
            LOG_ERROR "${APP_MAIN} start failed"
            LOG_ERROR "See logs for details"
            exit 1
            ;;
        *)
            exit 1
            ;;
    esac
}

before_start
start
after_start
