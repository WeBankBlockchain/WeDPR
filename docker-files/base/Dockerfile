FROM ubuntu:18.04

# install python and java
RUN apt-get update && apt-get install -y tzdata language-pack-zh* \
    && ln -fs /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone  \
    && locale-gen zh_CN.UTF-8  \
    && apt-get install -y  git wget tar unzip lzip vim nasm python3.8 python3-pip python3.8-dev perl graphviz openjdk-8-jdk gcc g++ build-essential m4 tcpdump net-tools gdb  \
    && rm -f /usr/bin/python && ln -s /usr/bin/python3.8 /usr/bin/python  \
    && rm -f /usr/bin/python3 && ln -s /usr/bin/python3.8 /usr/bin/python3  \
    && ln -s /usr/bin/pip3 /usr/bin/pip
# upgrade the pip
RUN python -m pip install --upgrade pip