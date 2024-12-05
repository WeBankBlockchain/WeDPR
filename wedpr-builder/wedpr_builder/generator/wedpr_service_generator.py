#!/usr/bin/python
# -*- coding: UTF-8 -*-
import os
from abc import ABC, abstractmethod
from wedpr_builder.config.wedpr_deploy_config import WeDPRDeployConfig
from wedpr_builder.config.wedpr_deploy_config import AgencyConfig
from wedpr_builder.config.wedpr_deploy_config import ServiceConfig
from wedpr_builder.common import utilities


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

    def __generate_service_config__(
            self, agency_config: AgencyConfig,
            service_config: ServiceConfig):
        utilities.print_badge(f"* generate wedpr-site config, deploy_dir: "
                              f"{self.config.env_config.deploy_dir}, "
                              f"service_config: {service_config}")
        for ip_str in service_config.deploy_ip_list:
            ip_array = ip_str.split(":")
            ip = ip_array[0]
            count = 1
            if len(ip_array) > 1:
                count = int(ip_array[1])
            for i in range(count):
                self.__generate_single_site_config(
                    agency_config=agency_config,
                    service_config=service_config,
                    agency_name=service_config.agency,
                    deploy_ip=ip, node_index=i)
        utilities.print_badge(f"* generate wedpr-site config success, deploy_dir: "
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
        utilities.print_badge(f"* generate wedpr-site config, "
                              f"deploy_ip: {deploy_ip}, "
                              f"node_index: {node_index}, "
                              f"node_path: {node_path}")
        # generate the deploy_path
        utilities.mkdir(node_path)
        # copy configuration into the dest path
        command = f"cp -r {service_config.tpl_config_file_path} {node_path}"
        (ret, output) = utilities.execute_command_and_getoutput(command)
        if ret is False:
            utilities.log_error(f"copy configuration failed, reason: {output}")
            return False
        # substitute the configuration
        config_properties = self.get_properties(
            deploy_ip, agency_config, node_index)
        for config_file in service_config.config_file_list:
            config_path = os.path.join(node_path, "conf", config_file)
            utilities.substitute_configurations(config_properties, config_path)

    def __get_deploy_path__(self, agency: str, ip: str,
                            node_name: str, service_type: str):
        return os.path.join(self.deploy_path, agency,
                            ip, service_type, node_name)


class WedprSiteServiceGenerator(WedprServiceGenerator):
    def __init__(self,
                 config: WeDPRDeployConfig,
                 deploy_path: str):
        super().__init__(config, deploy_path)

    def get_properties(
            self, deploy_ip: str,
            agency_config: AgencyConfig,
            node_index: int) -> {}:
        return agency_config.get_wedpr_site_properties(deploy_ip, node_index)

    def get_service_config(self, agency_config: AgencyConfig) -> ServiceConfig:
        return agency_config.site_config


class WedprPirServiceGenerator(WedprServiceGenerator):
    def __init__(self,
                 config: WeDPRDeployConfig,
                 deploy_path: str):
        super().__init__(config, deploy_path)

    def get_properties(
            self, deploy_ip: str,
            agency_config: AgencyConfig,
            node_index: int) -> {}:
        return agency_config.get_pir_properties(deploy_ip, node_index)

    def get_service_config(self, agency_config: AgencyConfig) -> ServiceConfig:
        return agency_config.pir_config


class WedprJupyterWorkerServiceGenerator(WedprServiceGenerator):
    def __init__(self,
                 config: WeDPRDeployConfig,
                 deploy_path: str):
        super().__init__(config, deploy_path)

    def get_properties(
            self, deploy_ip: str,
            agency_config: AgencyConfig,
            node_index: int) -> {}:
        return agency_config.get_jupyter_worker_properties(deploy_ip, node_index)

    def get_service_config(self, agency_config: AgencyConfig) -> ServiceConfig:
        return agency_config.jupyter_worker_config
