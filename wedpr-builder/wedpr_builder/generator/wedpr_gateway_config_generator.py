#!/usr/bin/python
# -*- coding: UTF-8 -*-
import os
import json
from wedpr_builder.common import utilities
from wedpr_builder.config.wedpr_deploy_config import WeDPRDeployConfig
from wedpr_builder.generator.binary_generator import BinaryGenerator
from wedpr_builder.generator.cert_generator import CertGenerator
from wedpr_builder.generator.shell_script_generator import ShellScriptGenerator


class WeDPRGatewayConfigGenerator:
    """
    the ppc-gateway-config-generator
    """

    def __init__(self, config: WeDPRDeployConfig, output_dir: str):
        self.config = config
        self.output_dir = output_dir
        self.binary_name = utilities.ConfigInfo.ppc_gateway_binary_name
        self.service_type = utilities.ServiceInfo.gateway_service_type

    def generate_gateway_config(self):
        utilities.print_badge("* generate gateway config, deploy_dir: %s" %
                              self.config.env_config.deploy_dir)
        # generate the ca
        ret = CertGenerator.generate_ca_cert(
            self.config.gateway_sm_ssl, self.__generate_ca_cert_path__())
        if ret is False:
            utilities.log_error(
                "* generate ca-cert config for %s failed" % self.config.env_config.deploy_dir)
            return False
        connection_config = {}
        connection_config["nodes"] = []
        connection_config_path_list = []
        for agency_config in self.config.agency_list.values():
            gateway_config = agency_config.gateway_config
            ret = self.__generate_single_gateway_config__(
                gateway_config, connection_config, connection_config_path_list)
            if ret is False:
                return False
        self.__generate_connection_info__(
            connection_config_path_list, connection_config)
        utilities.print_badge(
            "* generate gateway config success, deploy_dir: %s" % self.config.env_config.deploy_dir)
        return True

    def __generate_single_gateway_config__(self, gateway_config, connection_config, connection_config_path_list):
        # load the config from tpl_config_path
        utilities.log_info("* generate config for ppc-gateway")
        for ip_str in gateway_config.deploy_ip:
            ip_array = ip_str.split(":")
            ip = ip_array[0]
            # generate the shell scripts for the given ip
            ret = ShellScriptGenerator.generate_ip_shell_scripts(
                self.__generate_ip_shell_scripts_output_path__(gateway_config.agency_name, ip), "start_all.sh", "stop_all.sh")
            if ret is False:
                return False
            node_count = 1
            if len(ip_array) >= 2:
                node_count = int(ip_array[1])
            for node_index in range(node_count):
                node_name = "node" + str(node_index)
                connection_config_path_list.append(os.path.join(self.__generate_node_path__(
                    gateway_config.agency_name, ip, node_name), "nodes.json"))
                utilities.print_badge(
                    "* generate config for ppc-gateway %s.%s, deploy_ip: %s" % (gateway_config.agency_name, node_name, ip))
                config_content = utilities.load_config(
                    utilities.ConfigInfo.gateway_config_tpl_path)
                # load the common config
                self.__generate_common_config__(gateway_config, config_content)
                # load the gateway config
                listen_port = gateway_config.listen_port + node_index
                connection_config["nodes"].append(ip + ":" + str(listen_port))
                grpc_listen_port = gateway_config.grpc_listen_port + node_index
                self.__generate_gateway_config_content__(
                    gateway_config, config_content, listen_port, grpc_listen_port)
                # generate the binary
                binary_path = os.path.join(
                    self.config.env_config.binary_path, self.binary_name)
                dst_binary_path = os.path.join(
                    self.__generate_ip_shell_scripts_output_path__(
                        gateway_config.agency_name, ip),
                    self.binary_name)
                ret = BinaryGenerator.generate_binary(
                    binary_path, dst_binary_path)
                if ret is False:
                    return False
                # generate the ini config
                ini_config_output_path = os.path.join(
                    self.__generate_node_path__(gateway_config.agency_name, ip, node_name), utilities.ConfigInfo.config_ini_file)
                ret = utilities.store_config(
                    config_content, "ini", ini_config_output_path, "config.ini")
                if ret is False:
                    utilities.log_error(
                        "* generate config for ppc-gateway, ip: %s failed" % (ip))
                    return False
                # generate the node config
                ret = CertGenerator.generate_node_cert(self.config.gateway_sm_ssl, self.__generate_ca_cert_path__(
                ), self.__generate_conf_output_path__(gateway_config.agency_name, ip, node_name))
                if ret is False:
                    utilities.log_error(
                        "* generate config for ppc-gateway failed for generate the node config failed")
                    return False
                ret = ShellScriptGenerator.generate_node_shell_scripts(self.__generate_node_path__(
                    gateway_config.agency_name, ip, node_name), self.binary_name)
                if ret is False:
                    return False
                utilities.print_badge("* generate config for ppc-gateway%s success" %
                                      (node_name))
        utilities.log_info("* generate config for ppc-gateway success")
        return True

    def __generate_connection_info__(self, connection_config_path_list, connection_config):
        connection_str = json.dumps(connection_config)
        for path in connection_config_path_list:
            ret = utilities.store_config(
                connection_str, "json", path, "nodes.json")
            if ret is False:
                utilities.log_error(
                    "* generate nodes.json failed, path: %s" % path)
                return False
        return True

    def __generate_ca_cert_path__(self):
        return os.path.join(self.output_dir, self.service_type, "ca")

    def __generate_node_path__(self, agency_name, ip, node_name):
        return os.path.join(self.output_dir, agency_name, ip, self.service_type, node_name)

    def __generate_conf_output_path__(self, agency_name, ip, node_name):
        node_path = self.__generate_node_path__(agency_name, ip, node_name)
        return os.path.join(node_path, "conf")

    def __generate_ip_shell_scripts_output_path__(self, agency_name, ip):
        return os.path.join(self.output_dir, agency_name, ip, self.service_type)

    def __generate_common_config__(self, config, config_content):
        """
        generate the common config
        """
        section = "agency"
        # the agency
        config_content[section]["id"] = config.agency_name
        config_content["gateway"]["holding_msg_minutes"] = str(
            config.holding_msg_minutes)

    def __generate_gateway_config_content__(self, config, config_content, listen_port, grpc_listen_port):
        """
        generate the gateway config
        """
        section = "gateway"
        # the listen_ip
        config_content[section]["listen_ip"] = config.listen_ip
        # the listen port
        config_content[section]["listen_port"] = str(listen_port)
        # the thread count
        config_content[section]["thread_count"] = str(config.thread_count)
        # sm ssl
        config_content[section]["sm_ssl"] = utilities.convert_bool_to_str(
            self.config.gateway_sm_ssl)
        # disable ssl
        config_content[section]["disable_ssl"] = utilities.convert_bool_to_str(
            self.config.gateway_disable_ssl)
        section = "transport"
        config_content[section]["listen_ip"] = config.grpc_listen_ip
        # the listen port
        config_content[section]["listen_port"] = str(grpc_listen_port)
