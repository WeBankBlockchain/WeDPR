#!/usr/bin/python
# -*- coding: UTF-8 -*-
import sys
import os
import subprocess
import logging
import configparser
import random
import string


logging.basicConfig(format='%(message)s',
                    level=logging.INFO)


def log_error(error_msg):
    logging.error("\033[31m%s \033[0m" % error_msg)


def log_info(error_msg):
    logging.info("\033[32m%s \033[0m" % error_msg)


def format_info(info):
    return ("\033[32m%s \033[0m" % info)


def log_debug(error_msg):
    logging.debug("%s" % error_msg)


def get_item_value(config, key, default_value, must_exist, desc):
    if key in config:
        return config[key]
    if must_exist:
        raise Exception("the value for %s.%s must be set" % (desc, key))
    return default_value


def get_value(config, section, key, default_value, must_exist):
    if section in config and key in config[section]:
        return config[section][key]
    if must_exist:
        raise Exception("the value for %s must be set" % key)
    return default_value


def execute_command_and_getoutput(command):
    status, output = subprocess.getstatusoutput(command)
    if status != 0:
        log_error(
            "execute command %s failed, error message: %s" % (command, output))
        return (False, output)
    return (True, output)


def execute_command(command):
    (ret, result) = execute_command_and_getoutput(command)
    return ret


def mkdir(path):
    if not os.path.exists(path):
        os.makedirs(path)


def removeDir(path):
    if os.path.exists(path):
        os.removedirs(path)


def mkfiledir(filepath):
    parent_dir = os.path.abspath(os.path.join(filepath, ".."))
    if os.path.exists(parent_dir) is False:
        mkdir(parent_dir)


def convert_bool_to_str(value):
    if value is True:
        return "true"
    return "false"


def print_split_info():
    log_info("=========================================================")


def print_badge(badge):
    log_info("----------- %s -----------" % badge)


def file_must_exist(file_path):
    if not os.path.exists(file_path):
        log_error("%s does not exist, please check" % file_path)
        sys.exit(-1)


def store_config(config_content, config_type, config_path, desc):
    """
    store the generated genesis config content for given node
    """
    if os.path.exists(config_path):
        log_error("* store %s config for %s failed for the config %s already exists." %
                  (config_type, desc, config_path))
        return False
    log_info("* store %s config for %s\n\t path: %s" %
             (config_type, desc, config_path))

    if os.path.exists(os.path.dirname(config_path)) is False:
        mkdir(os.path.dirname(config_path))

    with open(config_path, 'w') as configFile:
        if isinstance(config_content, str):
            configFile.write(config_content)
        elif isinstance(config_content, bytes):
            configFile.write(config_content)
        else:
            config_content.write(configFile)
        log_info("* store %s config for %s success" %
                 (config_type, desc))
    return True


def load_config(file_path):
    # load the config from tpl_config_path
    config_content = configparser.ConfigParser(
        comment_prefixes='/', allow_no_value=True)
    # to distinguish uppercase/lowercase letters
    config_content.optionxform = str
    config_content.read(file_path)
    return config_content


def generate_random_str(size: int = 8):
    return ''.join(random.sample(string.ascii_letters + string.digits, size))

# substitute configurations
# TODO: support macos


def substitute_configurations(config_properities: {}, config_file: str):
    for config_key in config_properities.keys():
        command = f"sed -i \'s/${{config_key}}/{config_properities.get(config_key)}\/g' {config_file}"
        log_debug(f"* to execute command: {command}")
        (ret, output) = execute_command_and_getoutput(command)
        if ret is False:
            raise Exception(
                f"Execute command: {command} failed for reason {output}")
