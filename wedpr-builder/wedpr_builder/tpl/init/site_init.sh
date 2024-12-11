#!/bin/bash
SHELL_FOLDER=$(cd $(dirname $0);pwd)

LOG_INFO() {
    content=${1}
    echo -e "\033[32m[INFO] ${content}\033[0m"
}

cd ${SHELL_FOLDER}

db_init_script="db/scripts/init/db_init.sh"

LOG_INFO "* Begin to init database: ${MYSQL_DATABASE}, MYSQL_HOST: ${MYSQL_HOST}, PORT: ${MYSQL_PORT}, USER: ${MYSQL_USER}"

bash ${db_init_script} ${MYSQL_HOST} ${MYSQL_PORT} ${MYSQL_USER} ${MYSQL_PASSWORD} ${MYSQL_DATABASE}

LOG_INFO "* Init database success: ${MYSQL_DATABASE}, MYSQL_HOST: ${MYSQL_HOST}, PORT: ${MYSQL_PORT}, USER: ${MYSQL_USER}"
