#!/bin/bash
SHELL_FOLDER=$(cd $(dirname $0);pwd)
LOG_INFO() {
    content=${1}
    echo -e "\033[32m[INFO] ${content}\033[0m"
}
cd ${SHELL_FOLDER}

LOG_INFO "Ready to destory docker: ${WEDPR_DOCKER_NAME}, Please enter 'Y' to confirm"
read -r confirm
if [[ "${confirm}" == "Y" || "${confirm}" == "y" ]]; then
  LOG_INFO "Begin to destroy docker: ${WEDPR_DOCKER_NAME}"
  docker stop ${WEDPR_DOCKER_NAME}
  docker rm ${WEDPR_DOCKER_NAME}
  LOG_INFO "Destroy docker: ${WEDPR_DOCKER_NAME} success"
else
  LOG_INFO "Exit without destory docker ${WEDPR_DOCKER_NAME}"
fi
