FROM wedpr-image:latest

ENV LANG zh_CN.UTF-8
ENV LANGUAGE zh_CN.UTF-8
ENV LC_ALL zh_CN.UTF-8

RUN mkdir -p /home/share/.jupyter/

COPY depends/jupyter_lab_config.py /home/share/.jupyter/
COPY depends/authenticator-1.0.0.dev0.tar.gz /root
COPY depends/requirements.txt /root/requirements.txt

RUN chmod -R 777  /home/share/

# install jupyter
#RUN apt-get install -y libzmq3-dev && pip install pyzmq -i https://pypi.mirrors.ustc.edu.cn/simple/
RUN pip install --no-cache-dir -i https://pypi.mirrors.ustc.edu.cn/simple/ -r root/requirements.txt
RUN pip install --no-cache-dir -i https://pypi.mirrors.ustc.edu.cn/simple/ root/authenticator-1.0.0.dev0.tar.gz