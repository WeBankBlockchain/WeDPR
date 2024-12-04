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

RUN mkdir -p /data/app/
COPY --from=builder /WeDPR/wedpr-worker/dist/ /data/app/wedpr-worker
ENTRYPOINT ["/bin/bash"]

# the wedpr-pir
FROM wedpr-image:latest as wedpr-pir
LABEL maintainer service@webank.com

RUN mkdir -p /data/app/
COPY --from=builder /WeDPR/wedpr-pir/dist/ /data/app/wedpr-pir
ENTRYPOINT ["/bin/bash"]

# the wedpr-site
FROM wedpr-image:latest as wedpr-site
LABEL maintainer service@webank.com

RUN mkdir -p /data/app/
COPY --from=builder /WeDPR/wedpr-site/dist/ /data/app/wedpr-site
# TODO: fetch the web package from github tag
ENTRYPOINT ["/bin/bash"]