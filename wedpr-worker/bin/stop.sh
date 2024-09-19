#!/bin/bash
SHELL_FOLDER=$(cd $(dirname $0);pwd)

# JAVA_HOME="/nemo/jdk8"
APP_MAIN="com.webank.wedpr.worker.main.WorkerApplication"


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

get_app_pid(){
    ps aux |grep "$APP_MAIN" | grep "${SHELL_FOLDER}/" | grep -v grep | awk -F' ' '{print $2}'
}

app_pid=$(get_app_pid)
if [ -z "${app_pid}" ]; then
    LOG_ERROR "${APP_MAIN} isn't running!"
    exit 0
fi
LOG_INFO "Begin to stop  ${APP_MAIN}:${app_pid}"
kill ${app_pid}

try_times=10
i=0
while [ $i -lt ${try_times} ]
do
    app_pid=$(get_app_pid)
    if [ -z "${app_pid}" ]; then
        LOG_INFO "Stop ${APP_MAIN}:${app_pid} successfully"
        exit 0
    fi
    sleep 0.5

    ((i=i+1))
done

LOG_ERROR "Exceed waiting time. Please try again to stop ${APP_MAIN}:${app_pid}"
exit 1
