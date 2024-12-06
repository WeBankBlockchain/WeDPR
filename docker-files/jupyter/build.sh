#!/bin/bash

echo "* build image: wedpr-jupyter-image"

docker build -t wedpr-jupyter-image .

echo "* build image: wedpr-jupyter-image success"