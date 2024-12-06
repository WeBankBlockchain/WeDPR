# the builder
FROM wedpr-jupyter-image:latest as builder

LABEL maintainer service@webank.com

WORKDIR /

ARG SOURCE_BRANCH
ENV DEBIAN_FRONTEND=noninteractive \
    SOURCE=${SOURCE_BRANCH:-main}

RUN git clone  https://github.com/WeBankBlockchain/WeDPR.git -b ${SOURCE} \
    && cd WeDPR && bash gradlew clean build

# the wedpr-worker
FROM wedpr-jupyter-image:latest as wedpr-jupyter-worker
LABEL maintainer service@webank.com

RUN mkdir -p /data/home/wedpr
COPY --from=builder /WeDPR/wedpr-worker/dist/ /data/home/wedpr/wedpr-worker
ENTRYPOINT ["/bin/bash", "/data/home/wedpr/wedpr-worker/start.sh", "true"]

# the wedpr-pir
FROM wedpr-image:latest as wedpr-pir
LABEL maintainer service@webank.com

RUN mkdir -p /data/home/wedpr
COPY --from=builder /WeDPR/wedpr-pir/dist/ /data/app/wedpr-pir
ENTRYPOINT ["/bin/bash", "/data/home/wedpr/wedpr-pir/start.sh", "true"]

# the wedpr-site
FROM wedpr-image:latest as wedpr-site
LABEL maintainer service@webank.com

# install the requirements
COPY depends/requirements.txt /root/requirements.txt
# install the requirements
RUN pip install --no-cache-dir -i https://pypi.mirrors.ustc.edu.cn/simple/ -r root/requirements.txt

# TODO: use the non-test pip after uploaded
RUN pip install --no-cache-dir -i https://test.pypi.org/simple/ wedpr_mpc_generator

RUN mkdir -p /data/home/wedpr
COPY --from=builder /WeDPR/wedpr-site/dist/ /data/home/wedpr/wedpr-site
# TODO: fetch the web package from github tag
ENTRYPOINT ["/bin/bash", "/data/home/wedpr/wedpr-site/start.sh", "true"]
