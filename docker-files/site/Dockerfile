# the builder
FROM fiscoorg/wedpr-jupyter-image:v3.0.0 as builder

LABEL maintainer service@webank.com

WORKDIR /

ARG SOURCE_BRANCH
ENV DEBIAN_FRONTEND=noninteractive \
    SOURCE=${SOURCE_BRANCH:-main}

RUN git clone  https://github.com/WeBankBlockchain/WeDPR.git -b ${SOURCE} \
    && cd WeDPR && bash gradlew clean build

# the web_builder
FROM node:14.17.0 as web_builder

LABEL maintainer service@webank.com

WORKDIR /

ARG SOURCE_BRANCH
ENV DEBIAN_FRONTEND=noninteractive \
    SOURCE=${SOURCE_BRANCH:-main}

# generated web package in /WeDPR/wedpr-web/dist path
RUN git clone  https://github.com/WeBankBlockchain/WeDPR.git -b ${SOURCE}  \
    && cd WeDPR/wedpr-web && npm install && npm run build:pro


# the wedpr-worker
FROM fiscoorg/wedpr-jupyter-image:v3.0.0 as wedpr-jupyter-worker
LABEL maintainer service@webank.com

RUN mkdir -p /data/home/wedpr
COPY --from=builder /WeDPR/wedpr-worker/dist/ /data/home/wedpr/wedpr-worker
ENTRYPOINT ["/bin/bash", "/data/home/wedpr/wedpr-worker/start.sh", "true"]

# the wedpr-pir
FROM fiscoorg/wedpr-image:v3.0.0 as wedpr-pir
LABEL maintainer service@webank.com

RUN mkdir -p /data/home/wedpr
COPY --from=builder /WeDPR/wedpr-pir/dist/ /data/app/wedpr-pir
ENTRYPOINT ["/bin/bash", "/data/home/wedpr/wedpr-pir/start.sh", "true"]

# the wedpr-site
FROM fiscoorg/wedpr-image:v3.0.0 as wedpr-site
LABEL maintainer service@webank.com

# install the requirements
COPY depends/requirements.txt /root/requirements.txt
# install the requirements
RUN pip install --no-cache-dir -i https://pypi.mirrors.ustc.edu.cn/simple/ -r root/requirements.txt

# TODO: use the non-test pip after uploaded

RUN pip install --no-cache-dir -i https://test.pypi.org/simple/ wedpr_mpc_generator

# install nginx
RUN apt-get update && apt-get install --no-install-recommends -y nginx

RUN mkdir -p /data/home/wedpr
COPY --from=builder /WeDPR/wedpr-site/dist/ /data/home/wedpr/wedpr-site

# copy the web package
COPY --from=web_builder /WeDPR/wedpr-web/dist /data/home/wedpr/wedpr-site/web

ENTRYPOINT ["/bin/bash", "/data/home/wedpr/wedpr-site/start.sh", "true", "&&", "/usr/sbin/nginx"]
