#!/bin/bash

# set -e
LANG=en_US.UTF-8

LOG_INFO() {
    local content=${1}
    echo -e "\033[32m[INFO] ${content}\033[0m"
}

LOG_ERROR() {
    local content=${1}
    echo -e "\033[31m[ERROR] ${content}\033[0m"
}
install_python3() {
	cd /usr/local/lib
	tar -zxf Python-3.8.3.tgz
	cd Python-3.8.3
	./configure --prefix=/usr/local/lib/python3
	make && make install
	rm -f /usr/bin/python && ln -s /usr/local/lib/python3/bin/python3.8 /usr/bin/python
	rm -f /usr/bin/python3 && ln -s /usr/local/lib/python3/bin/python3.8 /usr/bin/python3
	ln -s /usr/local/lib/python3/bin/pip3 /usr/bin/pip
	rm -rf Python-3.8.3.tgz
    sed -i 's|#!/usr/bin/python|#!/usr/bin/python2.7|g' /usr/bin/yum
	sed -i 's|#! /usr/bin/python|#!/usr/bin/python2.7|g' /usr/libexec/urlgrabber-ext-down
}

cp_wedpr_node_script() {
    dos2unix /data/app/wedpr/scripts/*.sh
    dos2unix /data/app/wedpr/scripts/wedpr-mpc.sh
    chmod +x /data/app/wedpr/scripts/wedpr-mpc.sh
    cp /data/app/wedpr/scripts/wedpr-mpc.sh /etc/init.d/
}

install_wedpr_mpc_node() {
    cd /data/app/wedpr/scripts/
    tar -xf MP-SPDZ-x86.tar.gz
    tar -xf MP-SPDZ-x86-no-gateway.tar.gz
    mv MP-SPDZ-x86 wedpr-mpc
    mv MP-SPDZ-x86-no-gateway wedpr-mpc-no-gateway

    cd /data/app/wedpr/scripts/wedpr-mpc
    echo USE_NTL = 1 > CONFIG.mine
    strip mascot-party.x replicated-ring-party.x shamir-party.x hemi-party.x sy-rep-ring-party.x
    cd local/lib/
    rm -rf libmpir.so.23 libmpirxx.so.8
    ln -s libmpir.so.23.0.3 libmpir.so.23
    ln -s libmpirxx.so.8.4.3 libmpirxx.so.8

    cd /data/app/wedpr/scripts/wedpr-mpc-no-gateway
    echo USE_NTL = 1 > CONFIG.mine
    strip mascot-party.x replicated-ring-party.x shamir-party.x hemi-party.x sy-rep-ring-party.x
    cd local/lib/
    rm -rf libmpir.so.23 libmpirxx.so.8
    ln -s libmpir.so.23.0.3 libmpir.so.23
    ln -s libmpirxx.so.8.4.3 libmpirxx.so.8

    cd /data/app/wedpr/scripts
    rm -rf MP-SPDZ-x86.tar.gz MP-SPDZ-x86-no-gateway.tar.gz
}

install_mpc_nodes() {
    cd /data/app/wedpr/
    tar -xf wedpr-mpc-node.tar.gz
    rm -rf wedpr-mpc-node.tar.gz

    chmod +x /data/app/wedpr/wedpr-mpc-node/wedpr-mpc
    chmod +x /data/app/wedpr/wedpr-mpc-node/*.sh
    dos2unix /data/app/wedpr/wedpr-mpc-node/*.sh
}

install_python3
echo "install_python3 ok"
cp_wedpr_node_script
echo "cp_wedpr_node_script ok"
install_wedpr_mpc_node
echo "install_wedpr_mpc_node ok"
install_mpc_nodes
echo "install_mpc_nodes ok"

