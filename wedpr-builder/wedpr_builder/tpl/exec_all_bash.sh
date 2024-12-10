#!/bin/bash
dirpath="$(cd "$(dirname "$0")" && pwd)"
cd "${dirpath}"

dirs=($(ls -l ${dirpath} | awk '/^d/ {print $NF}'))
for dir in ${dirs[*]}
do
    if [[ -f "${dirpath}/${dir}/${BASE_SHELL_SCRIPT_NAME}" ]];then
        echo "try to start ${dir}"
        bash ${dirpath}/${dir}/${BASE_SHELL_SCRIPT_NAME}
    fi
done
wait