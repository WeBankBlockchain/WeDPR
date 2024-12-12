#!/bin/sh
SHELL_FOLDER=$(cd $(dirname $0);pwd)
cd ${SHELL_FOLDER}

if [[ $# -lt 5 ]] ; then
    echo "Usage: bash ${0} DB_IP DB_PORT DB_USER DB_PASSWORD DB_NAME"
    echo "eg: sh ${0} 127.0.0.1 3306 root ppc_pass ppc"
    exit 1
fi

IP="${1}"
PORT="${2}"
#dbUser
DBUSER="${3}"
#dbPass
PASSWD="${4}"
#dbName
DBNAME="${5}"

#connect to database then execute init
cat wedpr_sql.list | mysql --user=$DBUSER --password=$PASSWD --host=$IP --database=$DBNAME --port=$PORT
