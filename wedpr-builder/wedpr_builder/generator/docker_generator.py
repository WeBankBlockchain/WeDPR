#!/usr/bin/python
# -*- coding: UTF-8 -*-
import os
from wedpr_builder.common import utilities
from wedpr_builder.common import constant


class DockerGenerator:
    def __init__(self, node_path, docker_tpl_path):
        self.node_path = node_path
        self.docker_tpl_path = docker_tpl_path

    @staticmethod
    def generate_exec_all_shell_scripts(path, target_shell_name, called_shell_name):
        target_shell_path = os.path.join(path, target_shell_name)
        utilities.mkdir(path)
        command = f"cp {constant.ConfigInfo.exec_all_tpl_path} {target_shell_path}"
        (ret, output) = utilities.execute_command_and_getoutput(command)
        if ret is False:
            raise Exception(
                f"Generate shell script {target_shell_name} failed, error: {output}")
        props = {}
        props.update(
            {constant.ConfigProperities.BASE_SHELL_SCRIPT_NAME: called_shell_name})
        utilities.substitute_configurations(props, target_shell_path)

    @staticmethod
    def generate_shell_scripts(path):
        DockerGenerator.generate_exec_all_shell_scripts(
            path, constant.ConfigInfo.create_all_dockers_shell,
            constant.ConfigInfo.create_docker_shell)
        DockerGenerator.generate_exec_all_shell_scripts(
            path, constant.ConfigInfo.start_all_dockers_shell,
            constant.ConfigInfo.start_docker_shell)
        DockerGenerator.generate_exec_all_shell_scripts(
            path, constant.ConfigInfo.stop_all_dockers_shell,
            constant.ConfigInfo.stop_docker_shell)

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
