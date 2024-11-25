#!/bin/bash

LANG=en_US.UTF-8

mpc_work_dir=/data/app/wedpr/wedpr-mpc-node/

copy_wedpr_mpc_cert(){
    wedpr_mpc_cert_path=/data/app/wedpr/scripts/wedpr-mpc/Player-Data
    wedpr_mpc_no_gateway_cert_path=/data/app/wedpr/scripts/wedpr-mpc-no-gateway/Player-Data
    if [ ! -f ${mpc_work_dir}/conf/ca.crt ];then
        cd /data/app/wedpr/scripts/
        tar xf mpc-node-conf.tar.gz
        cp -r conf ${mpc_work_dir}/
    fi
    if [ ! -f ${wedpr_mpc_cert_path}/P0.pem ];then
        cd /data/app/wedpr/scripts/
        mkdir -p ${wedpr_mpc_cert_path}
        mkdir -p ${wedpr_mpc_no_gateway_cert_path}
        tar xf ssl_cert.tar.gz
        cp -r ssl_cert/* ${wedpr_mpc_cert_path}/
        cp -r ssl_cert/* ${wedpr_mpc_no_gateway_cert_path}/
    fi
}

wedpr_mpc_start() {
    copy_wedpr_mpc_cert
    cd ${mpc_work_dir}
    bash start.sh
}

wedpr_mpc_stop() {
    cd ${mpc_work_dir}
    bash stop.sh
}

case "$1" in
  start)
    wedpr_mpc_start
    ;;
  stop)
    wedpr_mpc_stop
    ;;
  restart)
    wedpr_mpc_stop
    wedpr_mpc_start
    ;;
  *)
    echo "Usage: /etc/init.d/wedpr-mpc {start|stop|restart}"
    exit 1
esac
exit 0
