#!/bin/bash

set -e
LANG=en_US.UTF-8

LOG_INFO() {
    local content=${1}
    echo -e "\033[32m[INFO] ${content}\033[0m"
}

LOG_ERROR() {
    local content=${1}
    echo -e "\033[31m[ERROR] ${content}\033[0m"
}

update_yum_repo() {
    yum repolist
    yum clean all
    yum makecache
    yum install epel-release -y
    rm -rf /etc/yum.repos.d/CentOS-SCLo-scl-rh.repo.rpmnew
    rm -rf /etc/yum.repos.d/CentOS-SCLo-sclo.repo
    yum repolist
    yum clean all
    yum makecache
}


install_spdz_deps() {
    yum install epel-release wget dos2unix vim which automake make yasm git boost-devel boost-thread openssl-devel libtool m4 python3 texinfo yasm gmp-devel libsodium-devel -y

    yum -y install centos-release-scl
    yum -y install devtoolset-11-gcc devtoolset-11-gcc-c++ devtoolset-11-binutils
    echo "source /opt/rh/devtoolset-11/enable" >> ~/.bashrc
    source ~/.bashrc
    g++ -v

    yum -y install openssl11
    ln -s /usr/bin/openssl11 /usr/bin/openssl
    openssl rand -writerand /root/.rnd
}

cp_wedpr_node_script() {
    dos2unix /data/app/wedpr/scripts/*.sh
    chmod +x /data/app/wedpr/scripts/wedpr-mpc-node.sh
    cp /data/app/wedpr/scripts/wedpr-mpc-node.sh /etc/init.d/
}

install_cmake(){
    cd /usr/local/lib/
    wget https://cmake.org/files/v3.21/cmake-3.21.4.tar.gz
    tar -xf cmake-3.21.4.tar.gz
    cd cmake-3.21.4
    g++ -v
    ./configure
    make -j4
    make install
    rm -rf /usr/local/lib/cmake-3.21.4.tar.gz
}

install_spdz() {
    cd /data/app/wedpr/scripts/
    git clone https://github.com/WeDPR-Team/MP-SPDZ.git
    cp -r MP-SPDZ wedpr-mpc-no-gateway
    cp -r MP-SPDZ wedpr-mpc

    cd /data/app/wedpr/scripts/wedpr-mpc-no-gateway
    git checkout ppc-2.0.0-no-gateway
    do_compile_spdz


    cd /data/app/wedpr/scripts/wedpr-mpc
    git checkout ppc-2.0.0
    do_compile_spdz

    cd /data/app/wedpr/scripts
    rm -rf MP-SPDZ
}

do_compile_spdz(){
      echo CXX=g++ >> CONFIG.mine
      echo AVX_OT=0 >> CONFIG.mine
  #    echo USE_NTL=1 > CONFIG.mine
      make setup
      sed -i "s/std::aligned_alloc/aligned_alloc/g" ./local/include/boost/asio/detail/memory.hpp
      make -j4 replicated-ring-party.x
      make -j4 hemi-party.x
      make -j4 shamir-party.x
      make -j4 mascot-party.x
      make -j4 sy-rep-ring-party.x
      strip mascot-party.x replicated-ring-party.x shamir-party.x hemi-party.x sy-rep-ring-party.x


      mkdir Player-Data
      tar xf /data/app/wedpr/scripts/ssl_cert.tar.gz
      cp -r  ssl_cert/* Player-Data/

      rm -rf ./git ./GC ./Machines ./deps ./Math ./Processor ./FHEOffline ./OT ./Networking ./FHE ./ECDSA
      rm -rf BMR ExternalIO Yao Utils bin doc  Dockerfile Makefile License.txt README.md CHANGELOG.md azure-pipelines.yml setup.py Tools 
}

install_mpc_nodes() {
    cd /data/app/wedpr/
    tar -xf wedpr-mpc-node.tar.gz
    rm -rf wedpr-mpc-node.tar.gz

    chmod +x /data/app/wedpr/wedpr-mpc-node/wedpr-mpc
    chmod +x /data/app/wedpr/wedpr-mpc-node/*.sh
    dos2unix /data/app/wedpr/wedpr-mpc-node/*.sh
}

update_yum_repo
echo "update_yum_repo ok"
install_spdz_deps
echo "install_spdz_deps ok"
cp_wedpr_node_script
echo "cp_wedpr_node_script ok"
install_cmake
echo "install_cmake ok"
install_spdz
echo "install_spdz ok"
#install_mpc_nodes
#echo "install_mpc_nodes ok"
