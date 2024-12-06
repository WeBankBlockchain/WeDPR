#!/usr/bin/python
# -*- coding: UTF-8 -*-
import os
from wedpr_builder.common import utilities
from wedpr_builder.common import constant


class DockerGenerator:
    def __init__(self, node_path, docker_tpl_path):
        self.node_path = node_path
        self.docker_tpl_path = docker_tpl_path

    def generate_config(self, config_properities: dict):
        # copy the docker tpl
        command = f"cp {self.docker_tpl_path}/* {self.node_path}"
        (ret, output) = utilities.execute_command_and_getoutput(command)
        if ret is False:
            raise Exception(f"* Copy docker tpl file from {self.docker_tpl_path} "
                            f"to {self.node_path} failed for {output}")
        # substitute the properties
        for file in constant.ConfigInfo.docker_file_list:
            file_path = os.path.join(self.node_path, file)
            utilities.substitute_configurations(config_properities, file_path)
