FROM wedpr-image:latest

ENV LANG zh_CN.UTF-8
ENV LANGUAGE zh_CN.UTF-8
ENV LC_ALL zh_CN.UTF-8

COPY depends/authenticator-1.0.0.dev0.tar.gz /root
COPY depends/requirements.txt /root/requirements.txt

# install jupyter
#RUN apt-get install -y libzmq3-dev && pip install pyzmq -i https://pypi.mirrors.ustc.edu.cn/simple/
RUN pip install -i https://pypi.mirrors.ustc.edu.cn/simple/ -r root/requirements.txt
RUN pip install -i https://pypi.mirrors.ustc.edu.cn/simple/ root/authenticator-1.0.0.dev0.tar.gz