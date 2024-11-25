#!/bin/bash

set -e
LANG=en_US.UTF-8

ROOT=$(pwd)

update_yum_source(){
    cd /etc/yum.repos.d
    rm -rf CentOS-Base.repo
    cp /data/app/scripts/CentOS-Base.repo .
    cat CentOS-Base.repo
    # mv CentOS-Base.repo CentOS-Base.repo.backup
    # wget -O /etc/yum.repos.d/CentOS-Base.repo https://repo.huaweicloud.com/repository/conf/CentOS-7-reg.repo 
    yum clean all
    yum makecache
    yum update -y
}

install_basic_depends() {
	yum -y remove openssl
	yum -y install epel-release 
    yum -y install gcc-c++ libstdc++-devel gdb lzip m4 dos2unix git wget tar unzip lzip vim nasm python-devel perl graphviz make automake autoconf mysql-devel postgresql-devel yasm texinfo ntl-devel tcpdump net-tools
	yum -y install openssl11 
	ln -s /usr/bin/openssl11 /usr/bin/openssl
    openssl rand -writerand /root/.rnd
}

install_cmake() {
    cd /usr/local/lib/ 
    tar -zxf cmake-3.21.4-linux-x86_64.tar.gz
    ln -sf /usr/local/lib/cmake-3.21.4-linux-x86_64/bin/* /usr/bin/
    rm -rf cmake-3.21.4-linux-x86_64.tar.gz
    chmod -R 755 /data/app/scripts/
    cd ${ROOT}
}

install_libsodium() {
    cd ${ROOT}
    # wget https://download.libsodium.org/libsodium/releases/libsodium-1.0.18.tar.gz
    tar -zxf libsodium-1.0.18.tar.gz 
    cd libsodium-1.0.18
    ./configure
    make -j4 && make install
    ldconfig
    cd ${ROOT}
}

install_mpir_offline() {
    cd ${ROOT}
    # wget http://mpir.org/mpir-3.0.0.zip
    unzip mpir-3.0.0.zip
    cd mpir-3.0.0
    ./configure --enable-cxx
    make -j4 && make install
    echo 'export LD_LIBRARY_PATH="/usr/local/lib/"' >> ~/.bashrc
    source  ~/.bashrc
    cd ${ROOT}
}

install_gmp() {
    cd ${ROOT}
    # wget https://gmplib.org/download/gmp/gmp-6.1.2.tar.lz
    lzip -d gmp-6.1.2.tar.lz
    tar -xf gmp-6.1.2.tar
    cd gmp-6.1.2
    ./configure
    make -j4 && make check && make install
    cd ${ROOT}
}

install_ntl() {
    cd ${ROOT}
    # wget https://shoup.net/ntl/ntl-10.5.0.tar.gz
    tar -zxf ntl-10.5.0.tar.gz
    cd ntl-10.5.0/src
    ./configure
    make -j4 && make check && make install
    cd ${ROOT}
}

install_boost() {
	cd ${ROOT}
	# wget https://boostorg.jfrog.io/artifactory/main/release/1.65.1/source/boost_1_65_1.tar.gz
    tar -zxf boost_1_65_1.tar.gz && cd boost_1_65_1/
    ./bootstrap.sh && ./b2 install --prefix=/opt/boost/
	cp -r /opt/boost/include/boost /usr/include
	cp -r /opt/boost/lib/* /usr/lib64 
    cd ${ROOT}
}

install_libstdc() {
	cd ${ROOT}
	unzip libstdc.so_.6.0.26.zip
	cp libstdc++.so.6.0.26 /lib64
	rm -rf /lib64/libstdc++.so.6
	ln -s /lib64/libstdc++.so.6.0.26 /lib64/libstdc++.so.6
	cd ${ROOT}
}

install_deps() {
    cd ${ROOT}
	install_basic_depends
    install_cmake
	install_libstdc
	install_boost
    install_libsodium
	install_mpir_offline
    install_gmp
    install_ntl
    cd ${ROOT}
}

update_yum_source
echo "update_yum_source ok"
install_deps
echo "install_deps ok"
rm -rf /tmp/*
rm -rf /data/app/scripts
