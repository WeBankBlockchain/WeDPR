#!/usr/bin/python
# -*- coding: UTF-8 -*-
import os
import sys
from wedpr_builder.common import utilities
from wedpr_builder.common import constant
from wedpr_builder.generator.binary_generator import BinaryGenerator
from wedpr_builder.generator.cert_generator import CertGenerator
from wedpr_builder.generator.shell_script_generator import ShellScriptGenerator
from wedpr_builder.config.wedpr_deploy_config import WeDPRDeployConfig
from wedpr_builder.config.wedpr_deploy_config import AgencyConfig
from wedpr_builder.config.wedpr_deploy_config import NodeConfig


class WeDPRNodeConfigGenerator:
    """
    the ppc-node-config-generator
    """

    def __init__(self, config: WeDPRDeployConfig, output_dir: str):
        self.config = config
        self.output_dir = os.path.join(
            output_dir, self.config.env_config.deploy_dir)
        self.binary_name = constant.ConfigInfo.ppc_node_binary_name
        self.service_type = constant.ServiceInfo.node_service_type

    def __generate_ip_shell_scripts__(self, agency_name, ip):
        if self.config.env_config.docker_mode is True:
            return True
        return ShellScriptGenerator.generate_ip_shell_scripts(
            self.__generate_ip_output_path__(agency_name, ip),
            "start_all.sh", "stop_all.sh")

    def generate_node_config(self):
        utilities.print_badge("* generate_node_config")
        # generate the ca cert for rpc
        ret = CertGenerator.generate_ca_cert(
            self.config.rpc_sm_ssl, self.__generate_ca_cert_path__())
        if ret is False:
            utilities.log_error(
                "* generate_node_config failed for generate ca error")
            return False
        for agency_config in self.config.agency_list.values():
            for node_config in agency_config.node_list.values():
                for ip_str in node_config.deploy_ip:
                    if ret is False:
                        return False
                    ip_array = ip_str.split(":")
                    ip = ip_array[0]
                    # generate the shell scripts for the given ip
                    ret = self.__generate_ip_shell_scripts__(
                        node_config.agency_name, ip)
                    node_count = 1
                    if len(ip_array) >= 2:
                        node_count = int(ip_array[1])
                    for node_index in range(node_count):
                        node_name = "node" + str(node_index)
                        if self.__generate_single_node_config__(
                                agency_config, node_config, ip,
                                node_name, node_config.agency_name, node_index) is False:
                            return False
        utilities.print_badge("* generate_node_config success")
        return True

    def __copy_binary__(self, agency_name, ip):
        if self.config.env_config.docker_mode is True:
            utilities.log_info(
                "* No need to copy binary for enable docker mode")
            return True
        # copy the binary
        binary_path = os.path.join(
            self.config.env_config.binary_path, self.binary_name)
        dst_binary_path = os.path.join(
            self.__generate_ip_output_path__(agency_name, ip), self.binary_name)
        return BinaryGenerator.generate_binary(binary_path, dst_binary_path)

    def __generate_node_shell_scripts__(self, agency_name, ip, node_name):
        if self.config.env_config.docker_mode is True:
            utilities.log_info(
                "* No need to copy shell scripts for enable docker mode")
            return True
        return ShellScriptGenerator.generate_node_shell_scripts(
            self.__generate_node_path__(agency_name, ip, node_name), self.binary_name)

    def __generate_docker_config__(
            self, node_path,
            node_config: NodeConfig,
            exposed_port_list,
            node_index):
        if self.config.env_config.docker_mode is False:
            return True
        # copy the docker files
        command = f"cp {constant.ConfigInfo.docker_tpl_path}/*.sh {node_path}"
        (ret, output) = utilities.execute_command_and_getoutput(command)
        if ret is False:
            raise Exception(f"Copy docker tpl file  from "
                            f"{constant.ConfigInfo.docker_tpl_path} to {node_path} "
                            f"failed, reason: {output}")
        props = AgencyConfig.generate_cpp_component_docker_properties(
            constant.ConfigInfo.wedpr_node_service_dir,
            self.config.env_config.zone,
            node_config.service_type, self.config.env_config,
            exposed_port_list, node_index)
        # substitute
        for file in constant.ConfigInfo.docker_file_list:
            utilities.substitute_configurations(
                props, os.path.join(node_path, file))
        return True

    def __generate_single_node_config__(
            self, agency_config: AgencyConfig,
            node_config, ip, node_name, agency_name, node_index):
        utilities.print_badge("* generate node config for %s, ip: %s, agency: %s" %
                              (node_name, ip, agency_name))
        ret = self.__copy_binary__(agency_name, ip)
        if ret is False:
            return ret
        # generate the node config
        node_path = self.__generate_node_path__(
            agency_name, ip, node_name)

        private_key_path = self.__generate_node_conf_path__(
            agency_name, ip, node_name)
        if self.__generate_single_node_inner_config__(agency_config,
                                                      constant.ConfigInfo.node_config_tpl_path,
                                                      node_path,
                                                      private_key_path, node_config, ip,
                                                      node_index) is False:
            utilities.log_error("* generate node config, ip: %s failed" %
                                (ip))
            return False
        # generate the node cert(for rpc)
        ret = CertGenerator.generate_node_cert(self.config.rpc_sm_ssl, self.__generate_ca_cert_path__(
        ), self.__generate_node_conf_path__(agency_name, ip, node_name))
        if ret is False:
            utilities.log_error("* generate node config, ip: %s failed for generate rpc cert failed" %
                                (ip))
            return False
        ret = self.__generate_node_shell_scripts__(agency_name, ip, node_name)
        if ret is False:
            return
        # generate the docker config
        rpc_listen_port = node_config.rpc_config.listen_port + node_index
        transport_listen_port = node_config.grpc_listen_port + node_index
        exposed_port_list = f" -p {rpc_listen_port}:{rpc_listen_port} " \
                            f"-p {transport_listen_port}:{transport_listen_port}"
        self.__generate_docker_config__(
            node_path, node_config, exposed_port_list, node_index)
        utilities.print_badge("* generate node config %s, ip: %s.%s success" %
                              (node_name, agency_name, ip))
        return True

    def __generate_single_node_inner_config__(self, agency_config: AgencyConfig,
                                              tpl_config_path, node_path, private_key_path,
                                              node_config, ip, node_index):
        config_content = utilities.load_config(tpl_config_path)
        utilities.log_debug(
            "__generate_single_node_inner_config__, load config.ini from %s" % tpl_config_path)
        # generate the private key
        (ret, node_id) = CertGenerator.generate_private_key(private_key_path)
        if ret is False:
            return False
        # load the common config
        self.__generate_common_config__(
            config_content, node_config)
        # load the rpc config
        self.__generate_rpc_config__(
            config_content, node_config.rpc_config,
            agency_config.psi_api_token,  node_index)
        # load the transport config
        self.__generate_transport_config__(config_content,
                                           node_config, node_id, ip, node_index)
        # load the storage config
        self.__generate_storage_config__(
            config_content, node_config.storage_config)
        # load the hdfs_storage_config
        self.__generate_hdfs_storage_config__(node_path, constant.ConfigInfo.krb5_config_tpl_path,
                                              config_content, node_config.hdfs_storage_config)
        # load the ra2018psi config
        self.__generate_ra2018psi_config__(
            config_content, node_config.ra2018psi_config)
        # store the config
        ini_config_output_path = os.path.join(
            node_path, constant.ConfigInfo.config_ini_file)
        ret = utilities.store_config(
            config_content, "ini", ini_config_output_path, "config.ini")
        if ret is False:
            return False
        return True

    def __generate_ca_cert_path__(self):
        return os.path.join(self.output_dir, self.service_type, "ca")

    def __generate_node_path__(self, agency_name, ip, node_name):
        return os.path.join(self.output_dir, agency_name, ip, self.service_type,
                            node_name)

    def __generate_node_conf_path__(self, agency_name, ip, node_name):
        node_path = self.__generate_node_path__(agency_name, ip, node_name)
        return os.path.join(node_path, "conf")

    def __generate_ip_output_path__(self, agency_name, ip):
        return os.path.join(self.output_dir, agency_name, ip, self.service_type)

    def __generate_common_config__(self, config_content, node_config):
        """
        generate the common config
        """
        # the agency config
        config_content["agency"]["id"] = node_config.agency_name
        #  disable ra2018 or not
        config_content["agency"]["disable_ra2018"] = utilities.convert_bool_to_str(
            node_config.disable_ra2018)
        # the crypto config
        config_content["crypto"]["sm_crypto"] = utilities.convert_bool_to_str(
            self.config.sm_crypto)

    def __generate_rpc_config__(self, config_content, rpc_config, psi_token: str, node_index):
        """
        generate the rpc config
        """
        section_name = "rpc"
        # listen_ip
        config_content[section_name]["listen_ip"] = rpc_config.listen_ip
        # listen_port
        rpc_listen_port = rpc_config.listen_port + node_index
        config_content[section_name]["listen_port"] = str(rpc_listen_port)
        # sm_ssl
        config_content[section_name]["sm_ssl"] = utilities.convert_bool_to_str(
            self.config.rpc_sm_ssl)
        # disable_ssl
        config_content[section_name]["disable_ssl"] = utilities.convert_bool_to_str(
            self.config.rpc_disable_ssl)
        config_content[section_name]["token"] = psi_token

    def __generate_storage_config__(self, config_content, storage_config):
        """
        generate the storage config
        """
        if storage_config is None:
            utilities.log_error("Must set the mysql-storage-config!")
            sys.exit(-1)

        section_name = "storage"
        config_content[section_name]["host"] = storage_config.host
        config_content[section_name]["port"] = str(storage_config.port)
        config_content[section_name]["user"] = storage_config.user
        config_content[section_name]["password"] = storage_config.password
        config_content[section_name]["database"] = storage_config.database

    def __generate_hdfs_storage_config__(self, node_path, krb5_tpl_file_path, config_content, hdfs_storage_config):
        if hdfs_storage_config is None:
            return
        section_name = "hdfs_storage"
        config_content[section_name]["user"] = hdfs_storage_config.user
        config_content[section_name]["name_node"] = hdfs_storage_config.name_node
        config_content[section_name]["name_node_port"] = str(
            hdfs_storage_config.name_node_port)
        config_content[section_name]["token"] = hdfs_storage_config.token
        config_content[section_name]["enable_krb5_auth"] = hdfs_storage_config.enable_krb5_auth_str
        config_content[section_name]["auth_principal"] = hdfs_storage_config.auth_principal
        config_content[section_name]["auth_password"] = hdfs_storage_config.auth_password
        config_content[section_name]["ccache_path"] = hdfs_storage_config.ccache_path
        config_content[section_name]["krb5_conf_path"] = hdfs_storage_config.krb5_conf_path
        # copy krb5.conf to krb5_conf_path specified path
        dst_path = os.path.join(node_path, hdfs_storage_config.krb5_conf_path)
        if hdfs_storage_config.krb5_conf_path.startswith("/"):
            dst_path = hdfs_storage_config.krb5_conf_path
        command = "cp %s %s" % (krb5_tpl_file_path, dst_path)
        (ret, output) = utilities.execute_command_and_getoutput(command)
        if ret is False:
            utilities.log_error("copy krb5 configuration from %s to %s failed, error: %s") % (
                krb5_tpl_file_path, dst_path, output)
            return False
        return True

    def __generate_transport_config__(self, config_content,
                                      node_config, node_id,
                                      deploy_ip, node_index):
        """_summary_

        Args:
            ; the endpoint information
            listen_ip = 0.0.0.0
            listen_port = 18000
            host_ip = 
            ; the threadPoolSize
            thread_count = 4
            ; the gatewayService endpoint information
            gateway_target =  
            ; the components
            components =
            nodeid=
        """
        section = "transport"
        config_content[section]["listen_ip"] = node_config.grpc_listen_ip
        config_content[section]["listen_port"] = str(
            node_config.grpc_listen_port + node_index)
        config_content[section]["host_ip"] = deploy_ip
        config_content[section]["gateway_target"] = node_config.gateway_config.gateway_targets
        config_content[section]["components"] = node_config.components
        config_content[section]["nodeid"] = node_id

    def __generate_ra2018psi_config__(self, config_content, ra2018psi_config):
        """
        generate the ra2018psi config
        """
        section_name = "ra2018psi"
        config_content[section_name]["database"] = ra2018psi_config.database
        config_content[section_name]["cuckoofilter_capacity"] = str(
            ra2018psi_config.cuckoofilter_capacity)
        config_content[section_name]["cuckoofilter_tagBits"] = str(
            ra2018psi_config.cuckoofilter_tagBits)
        config_content[section_name]["cuckoofilter_buckets_num"] = str(
            ra2018psi_config.cuckoofilter_buckets_num)
        config_content[section_name]["cuckoofilter_max_kick_out_count"] = str(
            ra2018psi_config.cuckoofilter_max_kick_out_count)
        config_content[section_name]["trash_bucket_size"] = str(
            ra2018psi_config.trash_bucket_size)
        config_content[section_name]["cuckoofilter_cache_size"] = str(
            ra2018psi_config.cuckoofilter_cache_size)
        config_content[section_name]["psi_cache_size"] = str(
            ra2018psi_config.psi_cache_size)
        config_content[section_name]["data_batch_size"] = str(
            ra2018psi_config.data_batch_size)
        config_content[section_name]["use_hdfs"] = utilities.convert_bool_to_str(
            ra2018psi_config.use_hdfs)
