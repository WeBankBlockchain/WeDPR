FROM wedpr-jupyter-image:latest

RUN mkdir -p /data/app/
COPY dist.tgz /data/app/dist.tgz

RUN cd /data/app/ && tar -xvf dist.tgz && mv dist wedpr-worker && rm -rf dist.tgz

ENTRYPOINT ["/bin/bash","/data/app/wedpr-worker/start.sh","true"]