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
    wedpr_mpc_service = "wedpr-mpc-service"
    wedpr_model_service = "wedpr-model"
    wedpr_jupyter_worker_service = "wedpr-jupyter-worker"
    supported_service_type = [node_service_type, gateway_service_type,
                              wedpr_site_service, wedpr_pir_service,
                              wedpr_jupyter_worker_service,
                              wedpr_model_service,
                              wedpr_mpc_service]


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

    # the exec_all_tpl_paths
    exec_all_tpl_path = get_abs_path("exec_all_bash.sh")
    create_all_dockers_shell = "create_all_dockers.sh"
    start_all_dockers_shell = "start_all_dockers.sh"
    stop_all_dockers_shell = "stop_all_dockers.sh"

    start_docker_shell = "start_docker.sh"
    create_docker_shell = "create_docker.sh"
    stop_docker_shell = "stop_docker.sh"

    # the site config path
    wedpr_site_config_path = get_abs_path("site/conf")
    site_config_list = ["application-wedpr.properties",
                        "config.toml", "wedpr.properties"]

    wedpr_pir_config_path = get_abs_path("pir/conf")
    pir_config_list = ["application-wedpr.properties", "wedpr.properties"]

    wedpr_model_config_path = get_abs_path("model/conf")
    wedpr_model_config_list = ["application.yml"]
    wedpr_model_common_dir = "ppc_common"
    wedpr_model_source_dir = "ppc_model"

    wedpr_jupyter_worker_config_path = get_abs_path("worker/conf")
    jupyter_config_list = ["application-wedpr.properties", "wedpr.properties"]
    # the docker config
    docker_tpl_path = get_abs_path("docker/")
    default_docker_work_dir = "/data/home/wedpr/"
    wedpr_worker_docker_dir = "wedpr-worker"
    wedpr_pir_docker_dir = "wedpr-pir"
    wedpr_site_docker_dir = "wedpr-site"

    wedpr_gateway_service_dir = "wedpr-gateway-service"
    wedpr_node_service_dir = "wedpr-pro-node-service"
    wedpr_mpc_service_dir = "wedpr-mpc-service"
    docker_file_list = ["create_docker.sh",
                        "start_docker.sh", "stop_docker.sh"]
    # the nginx config
    nginx_tpl_path = get_abs_path("nginx/")
    nginx_config_file_list = ["nginx.conf"]

    # the init db file path
    pwd_path = os.getcwd()
    db_dir_name = "db"
    db_file_path = os.path.join(pwd_path, db_dir_name)
    dml_sql_file = "wedpr_dml.sql"
    # the site init conf
    init_tpl_path = get_abs_path("init/")
    init_file_path_list = ["site_init.sh"]

    @staticmethod
    def get_docker_path(file_path: str):
        return os.path.join(ConfigInfo.default_docker_work_dir, file_path)


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
    SQLALCHEMY_URL = "SQLALCHEMY_URL"
    MYSQL_USER = "MYSQL_USER"
    MYSQL_PASSWORD = "MYSQL_PASSWORD"
    # the mysql var used for init script
    MYSQL_HOST = "MYSQL_HOST"
    MYSQL_PORT = "MYSQL_PORT"
    MYSQL_DATABASE = "MYSQL_DATABASE"
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
    HDFS_WEBFS_ENTRYPOINT = "HDFS_WEBFS_ENTRYPOINT"
    # the hdfs auth configuration
    HDFS_ENABLE_AUTH = "HDFS_ENABLE_AUTH"
    HDFS_AUTH_PRINCIPAL = "HDFS_AUTH_PRINCIPAL"
    HDFS_AUTH_PASSWORD = "HDFS_AUTH_PASSWORD"
    HDFS_HOSTNAME_OVERRIDE = "HDFS_HOSTNAME_OVERRIDE"
    #### the docker related vars ###
    # specify the docker image desc, include: org/docker_name:version
    WEDPR_IMAGE_DESC = "WEDPR_IMAGE_DESC"
    # specify the conf path to mount
    WEDPR_CONFIG_DIR = "WEDPR_CONFIG_DIR"
    # specify the mounted docker conf path
    DOCKER_CONF_PATH = "DOCKER_CONF_PATH"
    BASE_SHELL_SCRIPT_NAME = "BASE_SHELL_SCRIPT_NAME"
    # specify the log path to mount
    WEDPR_LOG_DIR = "WEDPR_LOG_DIR"
    # specify the mounted docker log path
    DOCKER_LOG_PATH = "DOCKER_LOG_PATH"
    # specify the exposed docker port list
    WEDPR_DOCKER_EXPORSE_PORT_LIST = "WEDPR_DOCKER_EXPORSE_PORT_LIST"
    # the created docker name
    WEDPR_DOCKER_NAME = "WEDPR_DOCKER_NAME"
    EXTENDED_MOUNT_CONF = "EXTENDED_MOUNT_CONF"
    # the nginx configuration
    NGINX_BACKEND_SERVER_LIST = "NGINX_BACKEND_SERVER_LIST"
