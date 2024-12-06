#!/bin/bash
echo "========= BUILD IMAGES for WEDPR, BRANCH: ${1} ========="
echo "* build image: wedpr-jupyter-worker"
docker build --target wedpr-jupyter-worker --build-arg SOURCE_BRANCH=${1} -t wedpr-jupyter-worker .
echo "* build image: wedpr-jupyter-worker success"


echo "* build image: wedpr-pir"
docker build --target wedpr-pir --build-arg SOURCE_BRANCH=${1} -t wedpr-pir .
echo "* build image: wedpr-pir success"

echo "* build image: wedpr-site"
docker build --target wedpr-site --build-arg SOURCE_BRANCH=${1} -t wedpr-site .
echo "* build image: wedpr-site success"

echo "========= BUILD IMAGES for WEDPR, BRANCH: ${1} success ========="