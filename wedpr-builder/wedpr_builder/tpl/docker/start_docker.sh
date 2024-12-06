#!/bin/bash
SHELL_FOLDER=$(cd $(dirname $0);pwd)

cd ${SHELL_FOLDER}

docker start ${WEDPR_DOCKER_NAME}