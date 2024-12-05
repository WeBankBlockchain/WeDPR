#!/usr/bin/python
# -*- coding: UTF-8 -*-

from wedpr_builder.common import utilities
from wedpr_builder.common import constant
from wedpr_builder.config.wedpr_user_config import UserJWTConfig


class ComponentSwitch:
    def __init__(self, node_must_exists: bool = False,
                 gateway_must_exists: bool = False,
                 site_must_exists: bool = False,
                 pir_must_exists: bool = False,
                 jupyter_must_exists: bool = False):
        self.node_must_exists = node_must_exists
        self.gateway_must_exists = gateway_must_exists
        self.site_must_exists = site_must_exists
        self.pir_must_exists = pir_must_exists
        self.jupyter_must_exists = jupyter_must_exists


class PeerInfo:
    def __init__(self, agency: str, endpoints: []):
        self.agency = agency
        self.endpoints = endpoints

    def __repr__(self):
        return f"**PeerInfo: agency: {self.agency}, " \
               f"endpoints: {self.endpoints}**\n"


class EnvConfig:
    def __init__(self, config, section_name: str):
        self.config = config
        self.section_name = section_name
        self.binary_path = utilities.get_value(
            self.config, self.section_name, "binary_path", None, True)
        self.deploy_dir = utilities.get_value(
            self.config, self.section_name, "deploy_dir", None, True)
        # zone
        self.zone = utilities.get_value(
            self.config, self.section_name, "zone", None, True)

    def __repr__(self):
        return f"**EnvConfig: binary_path: {self.binary_path}, " \
               f"deploy_dir: {self.deploy_dir}, zone: {self.zone}**\n"

    def to_properties(self) -> {}:
        props = {}
        props.update({constant.ConfigProperities.WEDPR_ZONE: self.zone})
        return props


class BlockchainConfig:
    def __init__(self, config, section_name: str):
        self.config = config
        self.blockchain_group = utilities.get_value(
            self.config, section_name, "blockchain_group", None, True)
        self.blockchain_peers = utilities.get_value(
            self.config, section_name, "blockchain_peers", None, True)
        self.blockchain_cert_path = utilities.get_value(
            self.config, section_name, "blockchain_cert_path", None, True)
        self.recorder_factory_contract_address = utilities.get_value(
            self.config, section_name, "recorder_factory_contract_address", None, True)
        self.sequencer_contract_address = utilities.get_value(
            self.config, section_name, "sequencer_contract_address", None, True)

    def __repr__(self):
        return f"**BlockchainConfig: blockchain_group: {self.blockchain_group}, " \
               f"blockchain_peers: [{','.join(self.blockchain_peers)}], " \
               f"blockchain_cert_path: {self.blockchain_cert_path}**\n"

    def to_properties(self) -> {}:
        properties = {}
        # the group id
        properties.update({
            constant.ConfigProperities.BLOCKCHAIN_GROUP_ID:
            self.blockchain_group})
        # the recorder factory contract address
        properties.update({
            constant.ConfigProperities.WEDPR_RECORDER_CONTRACT_ADDRESS:
            self.recorder_factory_contract_address})
        # the sequencer contract address
        properties.update({
            constant.ConfigProperities.WEDPR_SEQUENCER_CONTRACT_ADDRESS:
            self.sequencer_contract_address})
        # the blockchain peers
        blockchain_peers_info = ','.join(map(str, self.blockchain_peers))
        properties.update({constant.ConfigProperities.BLOCKCHAIN_PEERS:
                           f"[{blockchain_peers_info}]"})
        return properties


class GatewayConfig:
    """
    the gateway config
    """

    def __init__(self, agency_name: str, holding_msg_minutes: int,
                 config, config_section: str, must_exist: bool):
        self.config = config
        self.config_section = config_section
        self.holding_msg_minutes = holding_msg_minutes
        self.agency_name = agency_name

        # the deploy_ip
        self.deploy_ip = utilities.get_item_value(
            self.config, "deploy_ip", None, must_exist, config_section)
        # the listen_ip
        self.listen_ip = utilities.get_item_value(
            self.config, "listen_ip", "0.0.0.0", False, config_section)
        # the listen_port
        self.listen_port = utilities.get_item_value(
            self.config, "listen_port", None, must_exist, config_section)
        # the thread count
        self.thread_count = utilities.get_item_value(
            self.config, "thread_count", 4, False, config_section)
        # the peers
        self.peers = []
        peers = utilities.get_item_value(
            self.config, "peers", None, must_exist, config_section)
        for peer in peers:
            agency = utilities.get_item_value(
                peer, "agency", None, must_exist, "[[peers]]")
            endpoints = utilities.get_item_value(
                peer, "endpoints", None, must_exist, "[[peers]]")
            self.peers.append(PeerInfo(agency, endpoints))

        # the grpc_listen_ip
        self.grpc_listen_ip = utilities.get_item_value(
            self.config, "grpc_listen_ip", "0.0.0.0", False, config_section)
        # the grpc_listen_port
        self.grpc_listen_port = utilities.get_item_value(
            self.config, "grpc_listen_port", None, must_exist, config_section)
        # get the gateway target
        gateway_grpc_targets_array = []
        for ip_str in self.deploy_ip:
            ip_array = ip_str.split(":")
            ip = ip_array[0]
            count = int(ip_array[1])
            for i in range(count):
                port = self.grpc_listen_port + i
                gateway_grpc_targets_array.append(f"{ip}:{port}")
        gateway_targets_str = ','.join(map(str, gateway_grpc_targets_array))
        self.gateway_targets = f"ipv4:{gateway_targets_str}"
        utilities.log_info(
            f"* load gateway configuration, gateway targets: {self.gateway_targets}")

    def __repr__(self):
        return f"**GatewayConfig: deploy_ip: {self.deploy_ip}, " \
               f"listen_ip: {self.listen_ip}, listen_port: {self.listen_port}, " \
               f"grpc_listen_ip: {self.grpc_listen_ip}, " \
               f"grpc_listen_port: {self.grpc_listen_port}\n**"


class RpcConfig:
    """
    the rpc config
    """

    def __init__(self, config, config_section: str, must_exist: bool):
        self.config = config
        self.config_section = config_section
        self.listen_ip = utilities.get_item_value(
            self.config, "listen_ip", "0.0.0.0", False, config_section)
        self.listen_port = utilities.get_item_value(
            self.config, "listen_port", None, must_exist, config_section)
        self.thread_count = utilities.get_item_value(
            self.config, "thread_count", 4, False, config_section)

    def __repr__(self):
        return f"**RpcConfig: listen_ip: {self.listen_ip}," \
               f"listen_port: {self.listen_port} \n**"


class StorageConfig:
    """
    the sql storage config
    """

    def __init__(self, config, config_section: str, must_exist: bool):
        self.config = config
        self.config_section = config_section
        # the mysql configuration
        self.host = utilities.get_item_value(
            self.config, "host", None, must_exist, config_section)
        self.port = utilities.get_item_value(
            self.config, "port", None, must_exist, config_section)
        self.user = utilities.get_item_value(
            self.config, "user", None, must_exist, config_section)
        self.password = utilities.get_item_value(
            self.config, "password", None, must_exist, config_section)
        self.database = utilities.get_item_value(
            self.config, "database", None, must_exist, config_section)

    def __repr__(self):
        return f"**StorageConfig: host: {self.host}, port: {self.port}, " \
               f"user: {self.user}, database: {self.database}\n**"

    def to_properties(self) -> {}:
        """
        convert to dict properties
        :return: the converted properties
        """
        properties = {}
        mysql_url = f"jdbc:mysql://{self.host}:{self.port}/{self.database}?characterEncoding=UTF-8&allowMultiQueries=true"
        properties.update({constant.ConfigProperities.MYSQL_URL: mysql_url})
        properties.update({constant.ConfigProperities.MYSQL_USER: self.user})
        properties.update(
            {constant.ConfigProperities.MYSQL_PASSWORD: self.password})
        return properties


class ServiceConfig:
    def __init__(self, config, env_config: EnvConfig,
                 gateway_targets: str,
                 config_section: str,
                 must_exist: bool,
                 service_type: str,
                 tpl_config_file_path: str,
                 config_file_list: [],
                 agency: str):
        self.config = config
        self.env_config = env_config
        self.gateway_targets = gateway_targets
        self.service_type = service_type
        self.agency = agency
        self.tpl_config_file_path = tpl_config_file_path
        self.config_file_list = config_file_list
        self.deploy_ip_list = utilities.get_item_value(
            self.config, "deploy_ip", [], must_exist, config_section)
        self.server_start_port = int(utilities.get_item_value(
            self.config, "server_start_port",
            0, must_exist, config_section))

    def __repr__(self):
        return f"**ServiceConfig: deploy_ip: {self.deploy_ip_list}, " \
               f"agency: {self.agency}" \
               f"server_start_port: {self.server_start_port}," \
               f"service_type: {self.service_type} \n**"

    def to_properties(self, deploy_ip, node_index: int) -> {}:
        props = {}
        start_port = self.server_start_port + 2 * node_index
        # nodeid
        node_id = f"{self.service_type}_{self.env_config.zone}_node{node_index}"
        props.update({constant.ConfigProperities.WEDPR_NODE_ID: node_id})
        # gateway target
        props.update(
            {constant.ConfigProperities.GATEWAY_TARGET: self.gateway_targets})
        # host_ip
        if deploy_ip is None or len(deploy_ip) == 0:
            raise Exception(
                f"Invalid ServiceConfig, must define the deploy ip")
        host_ip = deploy_ip.split(":")[0]
        props.update(
            {constant.ConfigProperities.WEDPR_TRANSPORT_HOST_IP: host_ip})
        # the server listen port
        props.update(
            {constant.ConfigProperities.WEDPR_SERVER_LISTEN_PORT: start_port})
        # transport listen_port
        props.update(
            {constant.ConfigProperities.WEDPR_TRANSPORT_LISTEN_PORT: start_port + 1})
        return props


class HDFSStorageConfig:
    """
    the hdfs storage config
    """

    def __init__(self, config, config_section: str, must_exist: bool):
        self.config = config
        self.config_section = config_section
        # the hdfs configuration
        self.user = utilities.get_item_value(
            self.config, "user", None, must_exist, config_section)
        self.home = utilities.get_item_value(
            self.config, "home", None, must_exist, config_section)
        self.name_node = utilities.get_item_value(
            self.config, "name_node", None, must_exist, config_section)
        self.name_node_port = utilities.get_item_value(
            self.config, "name_node_port", None, must_exist, config_section)
        self.token = utilities.get_item_value(
            self.config, "token", "", False, config_section)
        # enable auth or not
        enable_krb5_auth = utilities.get_item_value(
            self.config, "enable_krb5_auth", "",
            False, config_section)
        self.enable_krb5_auth_str = utilities.convert_bool_to_str(
            enable_krb5_auth)
        # auth principal
        self.auth_principal = utilities.get_item_value(
            self.config, "auth_principal",
            "", enable_krb5_auth, config_section)
        # auth password
        self.auth_password = utilities.get_item_value(
            self.config, "auth_password",
            "", enable_krb5_auth, config_section)
        # cacche path
        self.ccache_path = utilities.get_item_value(
            self.config, "ccache_path",
            "", enable_krb5_auth, config_section)
        # the krb5.conf
        self.krb5_conf_path = utilities.get_item_value(
            self.config, "krb5_conf_path",
            "conf/krb5.conf", enable_krb5_auth, config_section)
        self.krb5_keytab_path = utilities.get_item_value(
            self.config, "krb5_keytab_path", None, enable_krb5_auth, config_section)

    def __repr__(self):
        return f"**HDFSStorageConfig: user: {self.user}, " \
               f"home: {self.home}, name_node: {self.name_node}, " \
               f"name_node_port: {self.name_node_port}, " \
               f"enable_krb5_auth: {self.enable_krb5_auth_str}, " \
               f"auth_principal: {self.auth_principal}, " \
               f"ccache_path: {self.ccache_path}, " \
               f"krb5_conf_path: {self.krb5_conf_path}**\n"

    def to_properties(self) -> {}:
        props = {}
        props.update({constant.ConfigProperities.HDFS_USER: self.user})
        props.update({constant.ConfigProperities.HDFS_HOME: self.home})
        hdfs_url = f"hdfs://{self.name_node}:{self.name_node_port}"
        props.update({constant.ConfigProperities.HDFS_ENTRYPOINT: hdfs_url})
        props.update(
            {constant.ConfigProperities.HDFS_ENABLE_AUTH: self.enable_krb5_auth_str})
        props.update(
            {constant.ConfigProperities.HDFS_AUTH_PRINCIPAL: self.auth_principal})
        return props


class RA2018PSIConfig:
    """
    the ra2018-psi config
    """

    def __init__(self, config, config_section: str, must_exist: bool):
        self.config = config
        self.config_section = config_section
        self.database = utilities.get_item_value(
            self.config, "database", None, must_exist, config_section)
        self.cuckoofilter_capacity = utilities.get_item_value(
            self.config, "cuckoofilter_capacity", 1, False, config_section)
        self.cuckoofilter_tagBits = utilities.get_item_value(
            self.config, "cuckoofilter_tagBits", 32, False, config_section)
        self.cuckoofilter_buckets_num = utilities.get_item_value(
            self.config, "cuckoofilter_buckets_num", 4, False, config_section)
        self.cuckoofilter_max_kick_out_count = utilities.get_item_value(
            self.config, "cuckoofilter_max_kick_out_count", 20, False, config_section)
        self.trash_bucket_size = utilities.get_item_value(
            self.config, "trash_bucket_size", 10000, False, config_section)
        self.cuckoofilter_cache_size = utilities.get_item_value(
            self.config, "cuckoofilter_cache_size", 256, False, config_section)
        self.psi_cache_size = utilities.get_item_value(
            self.config, "psi_cache_size", 1024, False, config_section)
        self.data_batch_size = utilities.get_item_value(
            self.config, "data_batch_size", -1, False, config_section)
        self.use_hdfs = utilities.get_item_value(
            self.config, "use_hdfs", False, False, config_section)

    def __repr__(self):
        return f"**RA2018PSIConfig: database: {self.database}, " \
               f"use_hdfs: {self.use_hdfs}, " \
               f"data_batch_size: {self.data_batch_size} **\n"


class NodeGatewayConfig:
    """
    the gateway config for the node
    """

    def __init__(self, agency_name: str,
                 gateway_targets: str):
        self.agency_name = agency_name
        self.gateway_targets = gateway_targets

    def __repr__(self):
        return f"** NodeGatewayConfig: agency: {self.agency_name}, " \
               f"gateway_targets: {self.gateway_targets}**\n"


class NodeConfig:
    """
    the ppc-node config
    """

    def __init__(self, agency_name: str, holding_msg_minutes: int, gateway_targets,
                 hdfs_storage_config: HDFSStorageConfig, config, must_exist: bool):
        self.config = config
        self.section_name = "[[agency.node]]."
        self.holding_msg_minutes = holding_msg_minutes
        # the hdfs config
        self.hdfs_storage_config = hdfs_storage_config
        # set the agency_name
        self.agency_name = agency_name
        # disable ra2018 or not, default enable the ra2018
        self.disable_ra2018 = utilities.get_item_value(
            self.config, "disable_ra2018", False, False, self.section_name)
        # the components
        self.components = utilities.get_item_value(
            self.config, "components", None, False, self.section_name)
        # the deploy_ip
        self.deploy_ip = utilities.get_item_value(
            self.config, "deploy_ip", None, must_exist, self.section_name)
        # the node_name
        self.node_name = utilities.get_item_value(
            self.config, "node_name", None, must_exist, self.section_name)
        # the grpc_listen_ip
        self.grpc_listen_ip = utilities.get_item_value(
            self.config, "grpc_listen_ip", "0.0.0.0", False, self.section_name)
        # the grpc_listen_port
        self.grpc_listen_port = utilities.get_item_value(
            self.config, "grpc_listen_port", None, must_exist, self.section_name)
        utilities.log_debug("load the node config success")

        # parse the rpc config
        utilities.log_debug("load the rpc config")
        rpc_config_section_name = "[[agency.node.rpc]]"
        rpc_config_object = utilities.get_item_value(
            self.config, "rpc", None, must_exist, rpc_config_section_name)
        self.rpc_config = None
        if rpc_config_object is not None:
            self.rpc_config = RpcConfig(
                rpc_config_object, rpc_config_section_name, must_exist)
        utilities.log_debug("load the rpc config success")

        # parse the ra2018-psi config
        utilities.log_debug("load the ra2018psi config")
        ra2018psi_config_section = "[[agency.node.ra2018psi]]"
        ra2018psi_config_object = utilities.get_item_value(
            self.config, "ra2018psi", None, must_exist, ra2018psi_config_section)
        self.ra2018psi_config = None
        if ra2018psi_config_object is not None:
            self.ra2018psi_config = RA2018PSIConfig(
                ra2018psi_config_object, ra2018psi_config_section, must_exist)
        utilities.log_debug("load the ra2018psi config success")
        # parse the storage config
        utilities.log_debug("load the sql storage config")
        storage_config_section = "[[agency.node.storage]]"
        storage_config_object = utilities.get_item_value(
            self.config, "storage", None, must_exist, storage_config_section)
        self.storage_config = None
        if storage_config_object is not None:
            self.storage_config = StorageConfig(
                storage_config_object, storage_config_section, must_exist)
        utilities.log_debug("load the sql storage success")
        # parse the gateway-inforamtion
        utilities.log_debug("load the gateway config")
        self.gateway_config = NodeGatewayConfig(
            agency_name=self.agency_name,
            gateway_targets=gateway_targets)
        utilities.log_debug("load the gateway success")

    def __repr__(self):
        return f"**NodeConfig: agency: {self.agency_name}, " \
               f"disable_ra2018: {self.disable_ra2018}, " \
               f"node_name: {self.node_name}, grpc_listen_ip: {self.grpc_listen_ip}"


class AgencyConfig:
    """
    the agency config
    """

    def __init__(self, config, env_config: EnvConfig,
                 blockchain_config: BlockchainConfig,
                 component_switch: ComponentSwitch):
        self.config = config
        self.env_config = env_config
        self.blockchain_config = blockchain_config
        self.component_switch = component_switch
        self.section_name = "[[agency]]"
        # the agency-name
        self.agency_name = utilities.get_item_value(
            self.config, "name", None, True, self.section_name)
        #  the holding_msg_minutes
        self.holding_msg_minutes = utilities.get_item_value(
            self.config, "holding_msg_minutes", 30, False, self.section_name)
        # the psi api token
        self.psi_api_token = utilities.get_item_value(
            self.config, "psi_api_token", "wedpr_psi_api_token", False, self.section_name)
        # parse the gateway config
        utilities.log_debug("load the gateway config")
        gateway_config_section_name = "[agency.gateway]"
        gateway_config_object = utilities.get_item_value(
            self.config, "gateway", None,
            self.component_switch.gateway_must_exists,
            gateway_config_section_name)
        self.gateway_config = None
        if gateway_config_object is not None:
            self.gateway_config = GatewayConfig(
                self.agency_name, self.holding_msg_minutes, gateway_config_object,
                gateway_config_section_name, self.component_switch.gateway_must_exists)
        utilities.log_debug("load the gateway config success")

        # parse the hdfs config
        self.hdfs_storage_config = self.__load_hdfs_config__()

        # load the sql storage config
        self.sql_storage_config = self.__load_sql_storage_config__()

        # load the site config
        self.site_config = self.__load_service_config__(
            "[agency.site]", "site", self.component_switch.site_must_exists,
            constant.ServiceInfo.wedpr_site_service,
            constant.ConfigInfo.wedpr_site_config_path,
            constant.ConfigInfo.site_config_list)

        # load the pir config
        self.pir_config = self.__load_service_config__(
            "[agency.pir]", "pir", self.component_switch.pir_must_exists,
            constant.ServiceInfo.wedpr_pir_service,
            constant.ConfigInfo.wedpr_pir_config_path,
            constant.ConfigInfo.pir_config_list)

        # load the jupyter_worker config
        self.jupyter_worker_config = self.__load_service_config__(
            "[agency.jupyter_worker]", "jupyter_worker",
            self.component_switch.jupyter_must_exists,
            constant.ServiceInfo.wedpr_jupyter_worker_service,
            constant.ConfigInfo.wedpr_jupyter_worker_config_path,
            constant.ConfigInfo.jupyter_config_list)

        # load the jwt user config
        self.user_jwt_config = UserJWTConfig()

        # parse the node config
        utilities.log_debug("load the node config")
        node_config_section_name = "[[agency.node]]"
        # Note: the node is not required to exist
        node_config_list = utilities.get_item_value(
            self.config, "node", None, False, node_config_section_name)
        self.node_list = {}
        # the case without node
        if node_config_list is None:
            return
        # TODO: check the node-name
        for node_object in node_config_list:
            node_config = NodeConfig(
                agency_name=self.agency_name,
                holding_msg_minutes=self.holding_msg_minutes,
                hdfs_storage_config=self.hdfs_storage_config,
                config=node_object,
                must_exist=self.component_switch.node_must_exists,
                gateway_targets=self.gateway_config.gateway_targets)
            self.node_list[node_config.node_name] = node_config
            utilities.log_debug(
                "load node config for %s success" % node_config.node_name)
        utilities.log_debug("load the node config success")

    def __load_hdfs_config__(self):
        # parse the hdfs storage config
        utilities.log_debug("load the hdfs storage config")
        storage_config_section = "[agency.hdfs]"
        hdfs_storage_config_object = utilities.get_item_value(
            self.config, "hdfs", None, True, storage_config_section)
        utilities.log_debug("load the hdfs storage success")
        return HDFSStorageConfig(hdfs_storage_config_object, storage_config_section, True)

    def __load_sql_storage_config__(self):
        utilities.log_debug("load the mysql storage config")
        section = "[agency.mysql]"
        sql_storage_config_dict = utilities.get_item_value(
            self.config, "mysql", None, True, section)
        utilities.log_debug("load the sql storage config success")
        return StorageConfig(sql_storage_config_dict, section, True)

    def __load_service_config__(self, config_section,
                                sub_config_key,
                                service_must_exists,
                                service_type,
                                tpl_config_file_path,
                                config_file_list):
        utilities.log_debug(f"load service config for {config_section}")
        config_dict = utilities.get_item_value(
            self.config, sub_config_key, None, service_must_exists, config_section)
        utilities.log_debug(
            f"load service config for {config_section} success")
        return ServiceConfig(config=config_dict,
                             env_config=self.env_config,
                             gateway_targets=self.gateway_config.gateway_targets,
                             must_exist=service_must_exists,
                             config_section=config_section,
                             service_type=service_type,
                             tpl_config_file_path=tpl_config_file_path,
                             config_file_list=config_file_list,
                             agency=self.agency_name)

    def to_properties(self) -> {}:
        props = {}
        props.update(
            {constant.ConfigProperities.WEDPR_AGENCY: self.agency_name})
        props.update(
            {constant.ConfigProperities.PSI_API_TOKEN: self.psi_api_token})
        return props

    def get_wedpr_site_properties(self, deploy_ip: str, node_index: int) -> {}:
        """
        get the site config properties
        :param node_index: the node index of the same ip
        :return: the properties
        """
        props = self.to_properties()
        # the zone config
        props.update(self.env_config.to_properties())
        # the user config
        props.update(self.user_jwt_config.to_properties())
        # the sql config
        props.update(self.sql_storage_config.to_properties())
        # the blockchain config
        props.update(self.blockchain_config.to_properties())
        # the service config
        props.update(self.site_config.to_properties(deploy_ip, node_index))
        # the hdfs config
        props.update(self.hdfs_storage_config.to_properties())
        return props

    def get_jupyter_worker_properties(self, deploy_ip: str, node_index: int) -> {}:
        """
        get the jupyter worker properties according to the config
        :param node_index: the node index of the same ip
        :return: the properties
        """
        props = self.to_properties()
        # the zone config
        props.update(self.env_config.to_properties())
        # the service config
        props.update(self.jupyter_worker_config.to_properties(
            deploy_ip, node_index))
        return props

    def get_pir_properties(self, deploy_ip: str, node_index: int):
        """
        get the pir worker properties according to the config
        :param node_index: the node index of the same ip
        :return: the properties
        """
        props = self.to_properties()
        # the zone config
        props.update(self.env_config.to_properties())
        # the sql config
        props.update(self.sql_storage_config.to_properties())
        # the service config
        props.update(self.pir_config.to_properties(deploy_ip, node_index))
        # the hdfs config
        props.update(self.hdfs_storage_config.to_properties())
        return props

    def __repr__(self):
        return f"agency: {self.agency_name}, gateway_config: {self.gateway_config}, " \
               f"node_config: {self.node_list}, hdfs_config: {self.hdfs_storage_config}"


class WeDPRDeployConfig:
    """
    load all config from config.toml
    """

    def __init__(self, config, component_switch: ComponentSwitch):
        self.config = config
        # load the crypto config
        utilities.log_debug("load the crypto config")
        crypto_section = "crypto"
        self.component_switch = component_switch
        self.gateway_disable_ssl = utilities.get_value(
            self.config, crypto_section, "gateway_disable_ssl", False, False)
        self.gateway_sm_ssl = utilities.get_value(
            self.config, crypto_section, "gateway_sm_ssl", False, False)
        # the rpc disable ssl or not
        self.rpc_disable_ssl = utilities.get_value(
            self.config, crypto_section, "rpc_disable_ssl", False, False)
        # the rpc use sm-ssl or not
        self.rpc_sm_ssl = utilities.get_value(
            self.config, crypto_section, "rpc_sm_ssl", False, False)
        self.sm_crypto = utilities.get_value(
            self.config, crypto_section, "sm_crypto", False, False)
        utilities.log_debug("load the crypto config success")
        self.env_config = EnvConfig(self.config, "env")
        # the blockchain config
        self.blockchain_config = BlockchainConfig(self.config, "blockchain")
        # load the agency config
        # TODO: check duplicated case
        utilities.log_debug("load the agency config")
        self.agency_list = {}
        agency_list_object = utilities.get_item_value(
            self.config, "agency", None, False, "[[agency]]")
        for agency_object in agency_list_object:
            agency_config = AgencyConfig(
                config=agency_object,
                env_config=self.env_config,
                blockchain_config=self.blockchain_config,
                component_switch=self.component_switch)
            self.agency_list[agency_config.agency_name] = agency_config
            utilities.log_debug(
                "load the agency config for %s success" % agency_config.agency_name)
        utilities.log_debug("load the agency config success")
