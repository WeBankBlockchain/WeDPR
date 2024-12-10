#!/usr/bin/python
# -*- coding: UTF-8 -*-
import os
from abc import ABC, abstractmethod
from wedpr_builder.config.wedpr_deploy_config import WeDPRDeployConfig
from wedpr_builder.config.wedpr_deploy_config import AgencyConfig
from wedpr_builder.config.wedpr_deploy_config import ServiceConfig
from wedpr_builder.common import utilities
from wedpr_builder.common import constant
from wedpr_builder.generator.docker_generator import DockerGenerator


class WedprServiceGenerator:
    def __init__(self,
                 config: WeDPRDeployConfig,
                 deploy_path: str):
        self.config = config
        self.deploy_path = os.path.join(
            deploy_path, self.config.env_config.deploy_dir)

    @abstractmethod
    def get_properties(
            self, deploy_ip: str,
            agency_config: AgencyConfig,
            node_index: int) -> {}:
        pass

    @abstractmethod
    def get_service_config(self, agency_config: AgencyConfig) -> ServiceConfig:
        pass

    def generate_config(self):
        for agency in self.config.agency_list.keys():
            agency_config = self.config.agency_list.get(agency)
            service_config = self.get_service_config(agency_config)
            self.__generate_service_config__(agency_config, service_config)

    @abstractmethod
    def generate_nginx_config(self, node_path: str, server_config: ServiceConfig):
        pass

    def __generate_service_config__(
            self, agency_config: AgencyConfig,
            service_config: ServiceConfig):
        utilities.print_badge(f"* generate {service_config.service_type} config, "
                              f"agency: {agency_config.agency_name}, deploy_dir: "
                              f"{self.config.env_config.deploy_dir}, "
                              f"service_config: {service_config}")
        node_path_list = []
        for ip_str in service_config.deploy_ip_list:
            ip_array = ip_str.split(":")
            ip = ip_array[0]
            count = 1
            if len(ip_array) > 1:
                count = int(ip_array[1])
            for i in range(count):
                node_path = self.__generate_single_site_config(
                    agency_config=agency_config,
                    service_config=service_config,
                    agency_name=service_config.agency,
                    deploy_ip=ip, node_index=i)
                node_path_list.append(node_path)
        for node_path in node_path_list:
            self.generate_nginx_config(node_path, service_config)
        utilities.print_badge(f"* generate {service_config.service_type} config success, "
                              f"agency: {agency_config.agency_name}, deploy_dir: "
                              f"{self.config.env_config.deploy_dir}, "
                              f"service_type: {service_config.service_type}")

    def __generate_single_site_config(
            self, agency_config:  AgencyConfig,
            service_config: ServiceConfig,
            agency_name: str,
            deploy_ip: str,
            node_index: int):
        node_name = f"{service_config.service_type}-node{node_index}"
        node_path = self.__get_deploy_path__(
            agency_name, deploy_ip, node_name, service_config.service_type)
        utilities.print_badge(f"* generate {service_config.service_type} config, "
                              f"deploy_ip: {deploy_ip}, "
                              f"node_index: {node_index}, "
                              f"node_path: {node_path}")
        if os.path.exists(node_path) is True:
            raise Exception(f"The path {node_path} already exists!"
                            f" Please make sure and remove it firstly!")

        # generate the deploy_path
        utilities.mkdir(node_path)
        # copy configuration into the dest path
        command = f"cp -r {service_config.tpl_config_file_path} {node_path}"
        if service_config.service_type == constant.ServiceInfo.wedpr_model_service:
            command = f"cp {service_config.tpl_config_file_path}/* {node_path}"
        (ret, output) = utilities.execute_command_and_getoutput(command)
        if ret is False:
            raise Exception(f"copy configuration failed, reason: {output}")
        # copy the blockchain cert
        if service_config.service_type == constant.ServiceInfo.wedpr_site_service:
            self.__copy_blockchain_cert__(
                node_path, agency_config.blockchain_config.blockchain_cert_path)
        # copy the shell-scripts(start.sh and stop.sh)
        dist_path = self.config.env_config.get_dist_path_by_service_type(
            service_config.service_type)
        self.__generate_shell_scripts__(dist_path, node_path)
        self.__copy_binary__(dist_path, node_path)
        # substitute the configuration
        config_properties = self.get_properties(
            deploy_ip, agency_config, node_index)
        # the docker mode case
        self.__generate_docker_scripts__(
            node_path, config_properties, service_config)
        for config_file in service_config.config_file_list:
            config_path = os.path.join(node_path, "conf", config_file)
            if service_config.service_type == constant.ServiceInfo.wedpr_model_service:
                config_path = os.path.join(node_path, config_file)
            utilities.substitute_configurations(config_properties, config_path)
        return node_path

    def __generate_docker_scripts__(
            self, node_path: str,
            config_props: {},
            service_config: ServiceConfig):
        # Note: jupyter only support docker mode
        if self.config.env_config.docker_mode is False \
                and service_config.service_type != \
                constant.ServiceInfo.wedpr_jupyter_worker_service:
            return
        self.__generate_docker_scripts_impl__(node_path, config_props)

    def __generate_docker_scripts_impl__(self, node_path, config_props: dict):
        utilities.log_info(
            f"* generate docker scripts, node_path: {node_path}")
        docker_generater = DockerGenerator(node_path,
                                           constant.ConfigInfo.docker_tpl_path)
        docker_generater.generate_config(config_props)
        utilities.log_info(
            f"* generate docker scripts success, node_path: {node_path}")

    @abstractmethod
    def __generate_shell_scripts__(self, dist_path, dst_path):
        pass

    def __generate_shell_scripts_impl__(self, shell_path, dst_path):
        # no need to copy the shell script in docker mode
        if self.config.env_config.docker_mode is True:
            utilities.log_info(
                "* no need to generate the shell script in docker-mode!")
            return
        if os.path.exists(shell_path) is False:
            raise Exception(
                f"The specified shell path {shell_path} not exists!"
                f" Please replace with a valid path")
        command = f"cp {shell_path}/*.sh {dst_path}"
        (ret, output) = utilities.execute_command_and_getoutput(command)
        if ret is False:
            raise Exception(f"* generate shell script failed, s"
                            f"hell_path: {shell_path}, "
                            f"dst_path: {dst_path}, reason: {output}")

    @abstractmethod
    def __copy_binary__(self, dist_path, dst_path):
        pass

    def __copy_java_binary__(self, dist_path, dst_path):
        if self.config.env_config.docker_mode is True:
            utilities.log_info(
                "* no need to copy the dist/lib, dist/apps for docker-mode!")
            return
        if os.path.exists(dist_path) is False:
            raise Exception(f"The specified dst_path {dst_path} not exists! "
                            f"Please check and replace with a valid path!")
        lib_path = os.path.join(dist_path, "lib")
        if os.path.exists(lib_path) is False:
            raise Exception(f"The lib path: {lib_path} not exists! "
                            f"Please check and replace with a valid path!")

        apps_path = os.path.join(dist_path, "apps")
        if os.path.exists(apps_path) is False:
            raise Exception(f"The apps path: {apps_path} not exists! "
                            f"Please check and replace with a valid path!")
        command = f"cp -r {lib_path} {dst_path} && cp -r {apps_path} {dst_path}"
        (ret, output) = utilities.execute_command_and_getoutput(command)
        if ret is False:
            raise Exception(f"* copy lib and apps failed, "
                            f"dist_path: {dist_path}, "
                            f"dst_path: {dst_path}, reason: {output}")

    def __copy_blockchain_cert__(self, node_path, blockchain_cert_path):
        if blockchain_cert_path is None or \
                len(blockchain_cert_path) == 0 or \
                os.path.exists(blockchain_cert_path) is False:
            raise Exception(f"Please check the existence of block chain cert, "
                            f"path: {blockchain_cert_path}")
        dst_path = os.path.join(node_path, "conf")
        command = f"cp {blockchain_cert_path}/* {dst_path}"
        (ret, output) = utilities.execute_command_and_getoutput(command)
        if ret is False:
            raise Exception(f"copy blockchain cert failed, "
                            f"from: {blockchain_cert_path}, "
                            f"to: {dst_path}, reason: {output}")

    def __get_deploy_path__(self, agency: str, ip: str,
                            node_name: str, service_type: str):
        return os.path.join(self.deploy_path, agency,
                            ip, service_type, node_name)


class WedprSiteServiceGenerator(WedprServiceGenerator):
    def __init__(self,
                 config: WeDPRDeployConfig,
                 deploy_path: str):
        super().__init__(config, deploy_path)

    def __generate_shell_scripts__(self, dist_path, dst_path):
        utilities.log_info(
            f"* generate shell script, dist_path: {dist_path}, dst_path: {dst_path}")
        self.__generate_shell_scripts_impl__(dist_path, dst_path)
        utilities.log_info(
            f"* generate shell script success, dist_path: {dist_path}, dst_path: {dst_path}")

    def __copy_binary__(self, dist_path, dst_path):
        self.__copy_java_binary__(dist_path, dst_path)

    def generate_nginx_config(self, node_path: str, server_config: ServiceConfig):
        utilities.log_info(f"* generate nginx for {node_path}")
        # copy the nginx config
        command = f"cp {constant.ConfigInfo.nginx_tpl_path}/* {node_path}/conf"
        (ret, output) = utilities.execute_command_and_getoutput(command)
        if ret is False:
            raise Exception(f"Generate nginx config failed when execute command: {command}, "
                            f"error: {output}")
        props = server_config.to_nginx_properties()
        for config_file in constant.ConfigInfo.nginx_config_file_list:
            config_path = os.path.join(node_path, "conf", config_file)
            utilities.substitute_configurations(props, config_path)
        utilities.log_info(f"* generate nginx for {node_path} success")
        return

    def get_properties(
            self, deploy_ip: str,
            agency_config: AgencyConfig,
            node_index: int) -> {}:
        return agency_config.get_wedpr_site_properties(deploy_ip, node_index)

    def get_service_config(self, agency_config: AgencyConfig) -> ServiceConfig:
        return agency_config.site_config


class WedprModelServiceGenerator(WedprServiceGenerator):
    def __init__(self, config: WeDPRDeployConfig, deploy_path: str):
        super().__init__(config, deploy_path)

    def __copy_binary__(self, dist_path, dst_path):
        if self.config.env_config.docker_mode is True:
            utilities.log_info(
                "* no need to copy the wedpr-model source code for docker-mode!")
            return
        # check dist_path existence
        if os.path.exists(dist_path) is False:
            raise Exception(f"The specified dst_path {dst_path} not exists! "
                            f"Please check and replace with a valid path!")
        # check ppc-common
        src_common_path = os.path.join(
            dist_path, constant.ConfigInfo.wedpr_model_common_dir)
        if os.path.exists(src_common_path) is False:
            raise Exception(f"The specified ppc_common_path {src_common_path} not exists! "
                            f"Please check and replace with a valid path!")
        # check ppc-model
        src_model_path = os.path.join(
            dist_path, constant.ConfigInfo.wedpr_model_source_dir)
        if os.path.exists(src_model_path) is False:
            raise Exception(f"The specified ppc_model_path {src_model_path} not exists! "
                            f"Please check and replace with a valid path!")
        # copy ppc-common and ppc-model
        command = f"cp -r {src_common_path} {dst_path} && cp -r {src_model_path} {dst_path}"
        (ret, output) = utilities.execute_command_and_getoutput(command)
        if ret is False:
            raise Exception(f"* copy source files for wedpr-model, "
                            f"dist_path: {dist_path}, "
                            f"dst_path: {dst_path}, reason: {output}")

    def __generate_shell_scripts__(self, dist_path, dst_path):
        self.__generate_shell_scripts_impl__(
            os.path.join(dist_path, constant.ConfigInfo.wedpr_model_source_dir, "tools"), dst_path)

    def get_properties(
            self, deploy_ip: str,
            agency_config: AgencyConfig,
            node_index: int) -> {}:
        return agency_config.get_wedpr_model_properties(deploy_ip, node_index)

    def get_service_config(self, agency_config: AgencyConfig) -> ServiceConfig:
        return agency_config.model_service_config

    def generate_nginx_config(self,  node_path: str, server_config: ServiceConfig):
        return


class WedprPirServiceGenerator(WedprServiceGenerator):
    def __init__(self,
                 config: WeDPRDeployConfig,
                 deploy_path: str):
        super().__init__(config, deploy_path)

    def __copy_binary__(self, dist_path, dst_path):
        self.__copy_java_binary__(dist_path, dst_path)

    def __generate_shell_scripts__(self, dist_path, dst_path):
        self.__generate_shell_scripts_impl__(dist_path, dst_path)

    def get_properties(
            self, deploy_ip: str,
            agency_config: AgencyConfig,
            node_index: int) -> {}:
        return agency_config.get_pir_properties(deploy_ip, node_index)

    def get_service_config(self, agency_config: AgencyConfig) -> ServiceConfig:
        return agency_config.pir_config

    def generate_nginx_config(self,  node_path: str, server_config: ServiceConfig):
        return


# use docker
class WedprJupyterWorkerServiceGenerator(WedprServiceGenerator):
    def __init__(self,
                 config: WeDPRDeployConfig,
                 deploy_path: str):
        super().__init__(config, deploy_path)

    def __generate_shell_scripts__(self, dist_path, dst_path):
        return

    def __copy_binary__(self, dist_path, dst_path):
        return

    def get_properties(
            self, deploy_ip: str,
            agency_config: AgencyConfig,
            node_index: int) -> {}:
        return agency_config.get_jupyter_worker_properties(deploy_ip, node_index)

    def get_service_config(self, agency_config: AgencyConfig) -> ServiceConfig:
        return agency_config.jupyter_worker_config

    def generate_nginx_config(self,  node_path: str, server_config: ServiceConfig):
        return
