#!/bin/bash
SHELL_FOLDER=$(cd $(dirname $0);pwd)

cd ${SHELL_FOLDER}

docker stop ${WEDPR_DOCKER_NAME}