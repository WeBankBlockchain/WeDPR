#!/usr/bin/python
# -*- coding: UTF-8 -*-
from wedpr_builder.common import utilities
from wedpr_builder.common import constant
from wedpr_builder.generator.wedpr_gateway_config_generator import WeDPRGatewayConfigGenerator
from wedpr_builder.generator.wedpr_node_config_generator import WeDPRNodeConfigGenerator
from wedpr_builder.config.wedpr_deploy_config import WeDPRDeployConfig
from wedpr_builder.config.wedpr_deploy_config import ComponentSwitch
from wedpr_builder.generator.wedpr_service_generator import WedprSiteServiceGenerator
from wedpr_builder.generator.wedpr_service_generator import WedprPirServiceGenerator
from wedpr_builder.generator.wedpr_service_generator import WedprJupyterWorkerServiceGenerator
from wedpr_builder.generator.wedpr_service_generator import WedprModelServiceGenerator
from argparse import RawTextHelpFormatter
import sys
import toml
import os
import argparse


def parse_command():
    help_info = "examples:\n * generate node config:\t " \
                "python3 build_wedpr.py -t wedpr-node\n " \
                "* generate gateway config:\t python3 build_wedpr.py -t wedpr-gateway\n " \
                "* generate wedpr-site config:\t python3 build_wedpr.py -t wedpr-site\n " \
                "* generate wedpr-pir config:\t python3 build_wedpr.py -t wedpr-pir\n" \
                "* generate wedpr-model service config:\t python3 build_wedpr.py -t wedpr-model\n " \
                "* generate gateway config:\t python3 build_wedpr.py -o genconfig -c config.toml -t wedpr-gateway -d wedpr-generated\n " \
                "* generate node config:\t python3 build_wedpr.py -o genconfig -c config.toml -t wedpr-node -d wedpr-generated"
    parser = argparse.ArgumentParser(
        prog=sys.argv[0], description=help_info, formatter_class=RawTextHelpFormatter, add_help=True)

    # the command option, now support genconfig/extend
    help_info = "[Optional] specify the command: \n* supported command list: %s\n" % (
        ''.join(constant.CommandInfo.supported_command_list))
    parser.add_argument("-o", '--operation', help=help_info,
                        required=False, default=constant.CommandInfo.generate_config)

    # config option
    help_info = "[Optional] the config file, default is config.toml\n"
    parser.add_argument(
        "-c", "--config", help=help_info, default="config.toml")
    # the output option
    help_info = "[Optional] the output path, default is pp-generated\n"
    parser.add_argument(
        "-d", "--output", help=help_info, default="wedpr-generated")

    # the type option
    supported_service_type_str = ', '.join(
        constant.ServiceInfo.supported_service_type)
    help_info = "[Required] the service type:\n* now support: %s \n" % (
        supported_service_type_str)
    parser.add_argument("-t", "--type", help=help_info, default="")
    args = parser.parse_args()
    return args


def generate_node_config(args, toml_config):
    """
    generate the node config
    """
    service_type = args.type
    utilities.log_debug(f"generate config for the wedpr {service_type}")
    # check the type
    if service_type not in constant.ServiceInfo.supported_service_type:
        utilities.log_error("The service type must be " +
                            ', '.join(constant.ServiceInfo.supported_service_type))
        sys.exit(-1)
    # the node config generator
    if service_type == constant.ServiceInfo.node_service_type:
        utilities.log_debug(f"generate config for the wedpr {service_type}")
        component_switch = ComponentSwitch(node_must_exists=True)
        config = WeDPRDeployConfig(toml_config, component_switch)
        node_generator = WeDPRNodeConfigGenerator(config, args.output)
        ret = node_generator.generate_node_config()
        if ret is False:
            sys.exit(-1)
    # the gateway config generator
    if service_type == constant.ServiceInfo.gateway_service_type:
        component_switch = ComponentSwitch(gateway_must_exists=True)
        config = WeDPRDeployConfig(toml_config, component_switch)
        gateway_generator = WeDPRGatewayConfigGenerator(config, args.output)
        ret = gateway_generator.generate_gateway_config()
        if ret is False:
            sys.exit(-1)
    # the site config generator
    if service_type == constant.ServiceInfo.wedpr_site_service:
        # default generate the jupyter config
        component_switch = ComponentSwitch(
            site_must_exists=True, jupyter_must_exists=True)
        config = WeDPRDeployConfig(toml_config, component_switch)
        jupyter_generator = WedprJupyterWorkerServiceGenerator(
            config, args.output)
        jupyter_generator.generate_config()
        # Note: generate site config after jupyter config generate success
        site_generator = WedprSiteServiceGenerator(config, args.output)
        site_generator.generate_config()
    # the pir service generator
    if service_type == constant.ServiceInfo.wedpr_pir_service:
        component_switch = ComponentSwitch(pir_must_exists=True)
        config = WeDPRDeployConfig(toml_config, component_switch)
        pir_generator = WedprPirServiceGenerator(config, args.output)
        pir_generator.generate_config()
    # the model service generator
    if service_type == constant.ServiceInfo.wedpr_model_service:
        component_switch = ComponentSwitch(model_must_exists=True)
        config = WeDPRDeployConfig(toml_config, component_switch)
        model_service_generator = WedprModelServiceGenerator(
            config, args.output)
        model_service_generator.generate_config()
    # Note: the jupyter config is generated with the site config
    """
    # the jupyter worker config generator
    if service_type == constant.ServiceInfo.wedpr_jupyter_worker_service:
        component_switch = ComponentSwitch(jupyter_must_exists=True)
        config = WeDPRDeployConfig(toml_config, component_switch)
        jupyter_generator = WedprJupyterWorkerServiceGenerator(
            config, args.output)
        jupyter_generator.generate_config()
    """


def execute_command(args):
    # check the config path
    if os.path.exists(args.config) is False:
        utilities.log_error("The config file %s not found!" % args.config)
        sys.exit(-1)
    # load the toml config
    toml_config = toml.load(args.config)
    # check the command
    command = args.operation
    if command not in constant.CommandInfo.supported_command_list:
        utilities.log_error("The command must be " +
                            ', '.join(constant.CommandInfo.supported_command_list))
        sys.exit(-1)
    if command == constant.CommandInfo.generate_config:
        generate_node_config(args, toml_config)
        return
    # TODO: implement expand
    if command == constant.CommandInfo.extend:
        utilities.log_error("unimplemented command %s" % command)
