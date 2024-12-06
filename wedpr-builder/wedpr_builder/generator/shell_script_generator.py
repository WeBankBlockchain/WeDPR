#!/usr/bin/python
# -*- coding: UTF-8 -*-
from wedpr_builder.common import utilities
from wedpr_builder.common import constant
import os


class ShellScriptGenerator:
    """
    generate the shell-scripts
    """
    def generate_ip_shell_scripts(
            script_output_dir: str,
            start_shell_script_name,
            stop_shell_script_name):
        start_all_path = os.path.join(
            script_output_dir, start_shell_script_name)
        utilities.mkdir(script_output_dir)
        if os.path.exists(start_all_path) is False:
            utilities.log_debug(
                "* generate shell script, dst: %s" % start_all_path)
            # start_all.sh
            command = "cp %s %s" % (
                constant.ConfigInfo.start_all_tpl_path, start_all_path)
            (result, output) = utilities.execute_command_and_getoutput(command)
            if result is False:
                utilities.log_error(
                    "* generate %s failed, error: %s" % (start_all_path, output))
                return False
        stop_all_path = os.path.join(
            script_output_dir, stop_shell_script_name)
        if os.path.exists(stop_all_path) is False:
            # tars stop_all.sh
            command = "cp %s %s" % (
                constant.ConfigInfo.stop_all_tpl_path, stop_all_path)
            (result, output) = utilities.execute_command_and_getoutput(command)
            if result is False:
                utilities.log_error(
                    "* generate %s failed, error: %s" % (stop_all_path, output))
                return False
        utilities.log_debug(
            "* generate_ip_shell_scripts success, output: %s" % script_output_dir)
        return True

    def __update_binary__(file_path, output_path, binary_name):
        command = "cp %s %s" % (file_path, output_path)
        (ret, _) = utilities.execute_command_and_getoutput(command)
        if ret is False:
            return False
        updated_config = ""
        with open(output_path, 'r', encoding='UTF-8') as file:
            updated_config = file.read()
            updated_config = updated_config.replace(
                "@BINARY_NAME@", binary_name)
        with open(output_path, 'w', encoding='UTF-8') as file:
            file.write(updated_config)
        return True

    def generate_node_shell_scripts(script_output_dir, binary_name):
        utilities.log_debug("* generate shell scripts for %s, dst: %s" %
                            (binary_name, script_output_dir))
        utilities.mkdir(script_output_dir)
        # the start.sh
        output_path = os.path.join(script_output_dir, "start.sh")
        ret = ShellScriptGenerator.__update_binary__(
            constant.ConfigInfo.start_tpl_path, output_path, binary_name)
        if ret is False:
            utilities.log_error(
                "generate_node_shell_scripts %s error" % output_path)
            return False
        # the stop.sh
        output_path = os.path.join(script_output_dir, "stop.sh")
        ret = ShellScriptGenerator.__update_binary__(
            constant.ConfigInfo.stop_tpl_path, output_path, binary_name)
        if ret is False:
            utilities.log_error(
                "generate_node_shell_scripts %s error" % output_path)
            return False
        return True
