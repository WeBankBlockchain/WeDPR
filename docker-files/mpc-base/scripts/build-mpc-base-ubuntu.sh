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


install_spdz_deps() {
    # 安装基本依赖
    apt update
    apt install -y wget dos2unix vim automake make yasm git libboost-dev libboost-thread-dev libssl-dev libtool m4 python3 texinfo yasm libgmp-dev libsodium-dev
    # 安装特定版本的GCC（类似于CentOS中的devtoolset）
    apt install -y software-properties-common
    add-apt-repository ppa:ubuntu-toolchain-r/test
    apt update
    apt install -y g++-11 g++-11-multilib

    # 配置环境变量以使用新安装的GCC版本
    echo "export CC=/usr/bin/gcc-11" >> ~/.bashrc
    echo "export CXX=/usr/bin/g++-11" >> ~/.bashrc
    source ~/.bashrc
    ln -sf /usr/bin/g++-11 /usr/bin/g++
    ln -sf /usr/bin/gcc-11 /usr/bin/gcc
    g++ -v

    # 安装OpenSSL 1.1（类似于CentOS中的openssl11）
    apt install -y openssl libssl-dev
    # Ubuntu中不需要创建符号链接，因为软件包会自动处理版本问题
    # openssl rand -writerand /root/.rnd 命令在Ubuntu中同样适用
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
    g++-11 -v
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
