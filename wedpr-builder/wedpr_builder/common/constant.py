#!/usr/bin/python
# -*- coding: UTF-8 -*-
#!/usr/bin/python
# -*- coding: UTF-8 -*-
import os
import logging

logging.basicConfig(format='%(message)s',
                    level=logging.INFO)


class ServiceInfo:
    ssl_file_list = ["ca.crt", "ssl.key", "ssl.crt"]
    sm_ssl_file_list = ["sm_ca.crt", "sm_ssl.key",
                        "sm_ssl.crt", "sm_enssl.key", "sm_enssl.crt"]
    cert_generation_script_path = "wedpr_builder/scripts/gen_cert.sh"
    node_service_type = "wedpr-node"
    gateway_service_type = "wedpr-gateway"
    wedpr_site_service = "wedpr-site"
    wedpr_pir_service = "wedpr-pir"
    wedpr_jupyter_worker_service = "wedpr-jupyter-worker"
    supported_service_type = [node_service_type, gateway_service_type,
                              wedpr_site_service, wedpr_pir_service,
                              wedpr_jupyter_worker_service]


def get_abs_path(file_path, tpl_abs_path="wedpr_builder/tpl/"):
    pwd_path = os.getcwd()
    return os.path.join(pwd_path, tpl_abs_path, file_path)


class ConfigInfo:
    config_ini_file = "config.ini"
    tpl_abs_path = "wedpr_builder/tpl/"
    ppc_gateway_binary_name = "ppc-gateway-service"
    ppc_node_binary_name = "ppc-pro-node"
    hdfs_key_tab_file_name = "hdfs-wedpr.keytab"

    node_config_tpl_path = get_abs_path("config.ini.node")
    gateway_config_tpl_path = get_abs_path("config.ini.gateway")
    krb5_config_tpl_path = get_abs_path("krb5.conf")

    start_tpl_path = get_abs_path("start.sh")
    stop_tpl_path = get_abs_path("stop.sh")

    start_all_tpl_path = get_abs_path("start_all.sh")
    stop_all_tpl_path = get_abs_path("stop_all.sh")

    # the site config path
    wedpr_site_config_path = get_abs_path("site/conf")
    site_config_list = ["application-wedpr.properties",
                        "config.toml", "wedpr.properties"]

    wedpr_pir_config_path = get_abs_path("pir/conf")
    pir_config_list = ["application-wedpr.properties", "wedpr.properties"]

    wedpr_jupyter_worker_config_path = get_abs_path("worker/conf")
    jupyter_config_list = ["application-wedpr.properties", "wedpr.properties"]


class CommandInfo:
    generate_config = "genconfig"
    extend_config = "extend"
    supported_command_list = [generate_config, extend_config]


class ConfigProperities:
    WEDPR_ZONE = "WEDPR_ZONE"
    WEDPR_AGENCY = "WEDPR_AGENCY"
    # the user configuration
    USER_JWT_SK = "USER_JWT_SK"
    USER_JWT_SESSION = "USER_JWT_SESSION_KEY"
    # the mysql configuration
    MYSQL_URL = "MYSQL_URL"
    MYSQL_USER = "MYSQL_USER"
    MYSQL_PASSWORD = "MYSQL_PASSWORD"
    # the blockchain configuration
    BLOCKCHAIN_GROUP_ID = "BLOCKCHAIN_GROUP_ID"
    BLOCKCHAIN_PEERS = "BLOCKCHAIN_PEERS"
    WEDPR_RECORDER_CONTRACT_ADDRESS = "WEDPR_RECORDER_CONTRACT_ADDRESS"
    WEDPR_SEQUENCER_CONTRACT_ADDRESS = "WEDPR_SEQUENCER_CONTRACT_ADDRESS"
    # the psi configuration
    PSI_API_TOKEN = "PSI_API_TOKEN"
    # the transport configuration
    WEDPR_NODE_ID = "WEDPR_NODE_ID"
    GATEWAY_TARGET = "GATEWAY_TARGET"
    WEDPR_TRANSPORT_HOST_IP = "WEDPR_TRANSPORT_HOST_IP"
    WEDPR_TRANSPORT_LISTEN_PORT = "WEDPR_TRANSPORT_LISTEN_PORT"
    # the service configuration
    WEDPR_SERVER_LISTEN_PORT = "WEDPR_SERVER_LISTEN_PORT"
    # the hdfs configuration
    HDFS_USER = "HDFS_USER"
    HDFS_HOME = "HDFS_HOME"
    HDFS_ENTRYPOINT = "HDFS_ENTRYPOINT"
    # the hdfs auth configuration
    HDFS_ENABLE_AUTH = "HDFS_ENABLE_AUTH"
    HDFS_AUTH_PRINCIPAL = "HDFS_AUTH_PRINCIPAL"
