-- the wedpr agency table
create table if not exists `wedpr_agency_table`(
    `name` varchar(128) not null comment "机构英文名",
    `cnName` varchar(1024) not null comment "机构中文名",
    `desc` text not null comment "机构描述",
    `meta` longtext not null comment "机构元信息",
    `create_time` datetime DEFAULT  CURRENT_TIMESTAMP comment "创建时间",
    `last_update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment "更新时间",
    primary key(`name`),
    index cnName_index(`cnName`(128))
)ENGINE=InnoDB default charset=utf8mb4 default collate=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- the wedpr config table
create table if not exists `wedpr_config_table`(
    `config_key` varchar(128) not null comment "配置项主键",
    `config_value` longtext not null comment "配置项的值",
    `report_status` tinyint default 0 comment "上报状态",
    `create_time` datetime DEFAULT  CURRENT_TIMESTAMP comment "配置创建时间",
    `last_update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment "配置更新时间",
     primary key(`config_key`),
     index report_status_index(`report_status`)
)ENGINE=InnoDB default charset=utf8mb4 default collate=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- the sync table(record the sync status of all resources)
create table if not exists `wedpr_sync_status_table`(
    `resource_id` varchar(64) not null comment "资源ID",
    `resource_type` varchar(255) not null comment "资源类型",
    `trigger` varchar(255) default '' comment "触发者",
    `agency` varchar(128) NOT NULL comment "发起机构",
    `resource_action` varchar(1024) not null comment "资源类型",
    `index` varchar(255) comment "资源索引",
    `block_number` varchar(255) comment "资源所在块高",
    `transaction_hash` varchar(255) comment "链上交易Hash",
    `status` varchar(1024) DEFAULT '' comment "状态",
    `status_msg` text comment "状态说明",
    `create_time` datetime DEFAULT  CURRENT_TIMESTAMP comment "创建时间",
    `last_update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment "更新时间",
    primary key(`resource_id`),
    index trigger_index(`trigger`),
    index agency_index(`agency`)
)ENGINE=InnoDB default charset=utf8mb4 default collate=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- the authorization related table
-- the authorization table
create table if not exists `wedpr_authorization_table`(
    `id` varchar(64) not null comment "申请单ID",
    `applicant` varchar(255) not null comment "申请人姓名",
    `applicant_agency` varchar(255) not null comment "申请人所属机构",
    `apply_type` varchar(255) not null comment "申请类型,如数据集权限申请等",
    `apply_title` varchar(1024) not null comment "申请标题",
    `apply_desc` text default null comment "申请单详情",
    `auth_chain` text default null comment "审批链信息",
    `apply_content` longtext not null comment "申请信息",
    `apply_template` varchar(128) not null comment "申请表单模板名",
    `status` varchar(128) not null comment "审批状态",
    `current_auth_node` varchar(255) comment "当前审批节点",
    `current_auth_node_agency` varchar(255) comment "当前审批节点所属机构",
    `result` longtext comment "包含所有审批链的审批结果",
    `execute_result` text comment "审批单执行结果",
    `create_time` DATETIME DEFAULT  CURRENT_TIMESTAMP comment "审批单创建时间",
    `last_update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment "审批单更新时间",
    primary key (`id`),
    index applicant_index(`applicant`, `applicant_agency`),
    index status_index(`status`),
    index auth_node_index(`current_auth_node`(128), `current_auth_node_agency`(128)),
    index apply_title_index(`apply_title`(128))
)ENGINE=InnoDB default charset=utf8mb4 default collate=utf8mb4_bin ROW_FORMAT=DYNAMIC;

create table if not exists `wedpr_follower_table`(
    `id` varchar(64) not null comment "id",
    `user_name` varchar(255) not null comment "关注人",
    `agency` varchar(255) not null comment "关注人所在机构",
    `resource_id` varchar(64) not null comment "关注的审批单",
    `type` varchar(255) not null comment "关注类型(如审批单, 任务)",
    `create_time` DATETIME DEFAULT  CURRENT_TIMESTAMP comment "创建时间",
    `last_update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment "更新时间",
    primary key(`id`),
    index resource_id_index(`resource_id`),
    index user_index(`user_name`(128), `agency`(128)),
    index follower_type_index(`type`)
)ENGINE=InnoDB default charset=utf8mb4 default collate=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- the authorization template table
create table if not exists `wedpr_authorization_template_table`(
    `id` varchar(64) not null comment "审批模板的ID",
    `template_name` varchar(128) not null comment "审批模板名称",
    `template_desc` text comment "审批模板描述",
    `template_setting` longtext not null comment "审批模板设置(json字符串)",
    `create_user` varchar(255) not null comment "审批模板创建人",
    `create_time` DATETIME DEFAULT  CURRENT_TIMESTAMP comment "审批模板创建时间",
    `last_update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment "审批模板更新时间",
    primary key (`id`),
    unique index template_name_index(`template_name`),
    index create_user_index(`create_user`(128))
)ENGINE=InnoDB default charset=utf8mb4 default collate=utf8mb4_bin ROW_FORMAT=DYNAMIC;


-- the project related table
-- the project table
create table if not exists `wedpr_project_table`(
    `id` varchar(64) not null comment "项目ID",
    `name` varchar(1024) not null comment "项目名称",
    `desc` varchar(1024) not null comment "项目描述",
    `owner` varchar(255) not null comment "项目属主",
    `owner_agency` varchar(255) not null comment "项目所属机构",
    `project_type` varchar(255) not null comment "项目类型(Export/Wizard)",
    `label` varchar(1024) comment "项目标签",
    `report_status` tinyint default 0 comment "上报状态",
    `create_time` DATETIME DEFAULT  CURRENT_TIMESTAMP comment "项目创建时间",
    `last_update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment "项目更新时间",
    primary key (`id`),
    index name_index(`name`(128)),
    index owner_index(`owner`(128), `owner_agency`(128)),
    index project_type_index(`project_type`(128)),
    index label_index(`label`(128)),
    index report_status_index(`report_status`)
)ENGINE=InnoDB default charset=utf8mb4 default collate=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- the job table
create table if not exists `wedpr_job_table`(
    `id` varchar(64) not null comment "任务ID",
    `name` varchar(1024) comment "任务名称",
    `project_name` varchar(1024) comment "任务所属项目",
    `owner` varchar(255) not null comment "任务发起人",
    `owner_agency` varchar(255) not null comment "任务发起机构",
    `job_type` varchar(255) not null comment "任务类型",
    `parties` text comment "任务相关机构信息(json)",
    `param` longtext comment "任务参数(json)",
    `status` varchar(255) comment "任务状态",
    `job_result` longtext comment "任务执行结果(json)",
    `report_status` tinyint default 0 comment "上报状态",
    `create_time` DATETIME DEFAULT  CURRENT_TIMESTAMP comment "任务创建时间",
    `last_update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment "任务更新时间",
    primary key(`id`),
    index name_index(`name`(128)),
    index owner_index(`owner`(128)),
    index owner_agency_index(`owner_agency`(128)),
    index project_index(`project_name`(128)),
    index status_index(`status`(128)),
    index report_status_index(`report_status`)
)ENGINE=InnoDB default charset=utf8mb4 default collate=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- job dataset relationship table
create table if not exists `wedpr_job_dataset_relation`(
    `job_id` varchar(64) not null comment "任务ID",
    `dataset_id` varchar(64) not null comment "数据集ID",
    `report_status` tinyint default 0 comment "上报状态",
    `create_time` DATETIME DEFAULT  CURRENT_TIMESTAMP comment "任务创建时间",
    index job_id_index(`job_id`),
    index dataset_id_index(`dataset_id`),
    index report_status_index(`report_status`)
)ENGINE=InnoDB default charset=utf8mb4 default collate=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- the algorithm_setting template
create table if not exists `wedpr_setting_template`(
    `id` varchar(64) not null comment "配置模板ID",
    `name` varchar(255) not null comment "配置模板名称",
    `type` varchar(255) not null comment "配置分类，如XGB, LR等",
    `owner` varchar(255) not null comment "配置模板属主, *表示所有人可用",
    `agency` varchar(255) not null comment "所属机构",
    `setting` longtext not null comment "配置模板具体内容(json字符串)",
    `create_time` DATETIME DEFAULT  CURRENT_TIMESTAMP comment "算法模板创建时间",
    `last_update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment "算法模板更新时间",
    primary key (`id`),
    unique index name_index(`name`(128)),
    index type_index(`type`(128)),
    index agency_index(`agency`(128))
)ENGINE=InnoDB default charset=utf8mb4 default collate=utf8mb4_bin ROW_FORMAT=DYNAMIC;

CREATE TABLE if not exists `wedpr_dataset` (
    `dataset_id` VARCHAR(64) NOT NULL COMMENT '数据集id',
    `dataset_title` VARCHAR(1024) NOT NULL COMMENT '数据集标题',
    `dataset_label` VARCHAR(1024) NOT NULL COMMENT '数据集标签',
    `dataset_desc` TEXT NOT NULL COMMENT '数据集描述',
    `dataset_fields` TEXT COMMENT '数据源字段以及预览信息',
    `dataset_version_hash` VARCHAR(64) DEFAULT '' COMMENT '数据集hash',
    `dataset_size` bigint DEFAULT 0 COMMENT '数据集大小',
    `dataset_record_count` bigint DEFAULT 0 COMMENT '数据集记录数目',
    `dataset_column_count` int DEFAULT 0 COMMENT '数据集列数目',
    `dataset_storage_type` VARCHAR(255) DEFAULT '' COMMENT '数据集存储类型',
    `dataset_storage_path` VARCHAR(1024) DEFAULT '' COMMENT '数据集存储路径',
    `owner_agency_name` VARCHAR(255) NOT NULL COMMENT '数据集所属机构名称',
    `owner_user_name` VARCHAR(1024) NOT NULL COMMENT '数据集所属用户名',
    `data_source_type` VARCHAR(255) NOT NULL COMMENT '数据源类型 : CSV、DB、XLSX、HDFS、HIVE',
    `data_source_meta` TEXT NOT NULL COMMENT '数据源参数信息，JSON字符串',
    `visibility` int(8) NOT NULL COMMENT '数据集可见性, 0: 私有，1: 公开可见',
    `visibility_details` TEXT NOT NULL COMMENT '数据源可见范围描述, visibility 为1时有效',
    `approval_chain` TEXT NOT NULL COMMENT '审批链信息',
    `status` tinyint(4) NOT NULL COMMENT '数据集状态, 0: 有效，其他无效',
    `status_desc` VARCHAR(1024) NOT NULL COMMENT '数据集状态描述',
    `create_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`dataset_id`),
    INDEX dataset_title_index (`dataset_title`(128)),
    INDEX owner_agency_name_index (`owner_agency_name`),
    INDEX owner_user_name_index (`owner_user_name`(128)),
    INDEX create_at_index (`create_at`),
    INDEX update_at_index (`update_at`)
)ENGINE='InnoDB' DEFAULT CHARSET='utf8mb4' COLLATE='utf8mb4_bin' ROW_FORMAT=DYNAMIC COMMENT '数据集记录表';

CREATE TABLE if not exists `wedpr_dataset_permission` (
    `id` VARCHAR(64) NOT NULL COMMENT '数据集权限id',
    `dataset_id` VARCHAR(64) NOT NULL COMMENT '数据集id',
    `permission_type` int(8) NOT NULL COMMENT '权限类型',
    `permission_scope` VARCHAR(255) NOT NULL COMMENT '权限范围',
    `permission_subject_id` TEXT NOT NULL COMMENT '数据集授权对象的id',
    `expired_at` DATE NOT NULL DEFAULT '9999-12-31',
    `create_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX dataset_id_index (`dataset_id`),
    INDEX permission_type_index (`permission_type`),
    INDEX permission_scope_index (`permission_scope`),
    INDEX expired_at_index (`expired_at`)
)ENGINE='InnoDB' DEFAULT CHARSET='utf8mb4' COLLATE='utf8mb4_bin' ROW_FORMAT=DYNAMIC COMMENT '数据集权限表';

-- 创建用户组表
CREATE TABLE IF NOT EXISTS wedpr_group (
    group_id VARCHAR(64) NOT NULL,
    group_name VARCHAR(64) NOT NULL,
    admin_name VARCHAR(64) NOT NULL DEFAULT '',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by VARCHAR(20) NOT NULL DEFAULT '',
    update_by VARCHAR(20) NOT NULL DEFAULT '',
    status TINYINT DEFAULT 0,
    PRIMARY KEY (group_id),
    INDEX idx_group_name (group_name),
    INDEX idx_admin_name (admin_name)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- 创建用户组详情表
CREATE TABLE IF NOT EXISTS wedpr_group_detail (
    group_id VARCHAR(64) NOT NULL,
    username VARCHAR(128) NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by VARCHAR(20) NOT NULL DEFAULT '',
    update_by VARCHAR(20) NOT NULL DEFAULT '',
    status TINYINT DEFAULT 0,
    PRIMARY KEY (group_id, username),
    INDEX idx_username (username)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- 创建用户表
CREATE TABLE IF NOT EXISTS wedpr_user (
    username VARCHAR(128) NOT NULL,
    email VARCHAR(128),
    password VARCHAR(256),
    phone VARCHAR(64),
    try_count int(10),
    allowed_timestamp BIGINT(20),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by VARCHAR(20) NOT NULL DEFAULT '',
    update_by VARCHAR(20) NOT NULL DEFAULT '',
    status TINYINT DEFAULT 0,
    PRIMARY KEY (username),
    INDEX idx_email (email),
    INDEX idx_phone (phone)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- 创建用户角色表
CREATE TABLE IF NOT EXISTS wedpr_user_role (
    username VARCHAR(128) NOT NULL,
    role_id VARCHAR(64) NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by VARCHAR(20) NOT NULL DEFAULT '',
    update_by VARCHAR(20) NOT NULL DEFAULT '',
    PRIMARY KEY (username, role_id),
    INDEX idx_role_id (role_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- 创建角色权限表
CREATE TABLE IF NOT EXISTS wedpr_role_permission (
    role_id VARCHAR(64) NOT NULL,
    role_name VARCHAR(64) NOT NULL,
    permission_id VARCHAR(64) NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by VARCHAR(20) NOT NULL DEFAULT '',
    update_by VARCHAR(20) NOT NULL DEFAULT '',
    PRIMARY KEY (role_id, permission_id),
    INDEX idx_role_name (role_name ),
    INDEX idx_permission_id (permission_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- 创建权限表
CREATE TABLE IF NOT EXISTS wedpr_permission (
    permission_id VARCHAR(64) NOT NULL,
    permission_name VARCHAR(128) NOT NULL,
    permission_content  TEXT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by VARCHAR(20) NOT NULL DEFAULT '',
    update_by VARCHAR(20) NOT NULL DEFAULT '',
    PRIMARY KEY (permission_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- the jupyter table
create table if not exists `wedpr_jupyter_table`(
    `id` varchar(64) not null comment "Jupyter资源的ID",
    `owner` varchar(255) not null comment "Jupyter属主",
    `agency` varchar(255) not null comment "Jupyter所属机构",
    `access_entrypoint` varchar(1024) comment "Jupyter访问入口",
    `setting` longtext comment "Jupyter配置",
    `status` varchar(1024) comment "Jupyter状态",
    `create_time` DATETIME DEFAULT  CURRENT_TIMESTAMP comment "创建时间",
    `last_update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment "更新时间",
    PRIMARY KEY (id),
    index owner_index(`owner`(128)),
    index status_index(`status`(128)),
    index access_entrypoint_index(`access_entrypoint`(128))
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- 创建机构表
CREATE TABLE IF NOT EXISTS wedpr_agency (
    agency_id VARCHAR(64) NOT NULL comment "机构编号",
    agency_name VARCHAR(64) NOT NULL comment "机构名",
    agency_desc text NOT NULL comment "机构描述",
    agency_contact VARCHAR(64) NOT NULL comment "机构联系人",
    contact_phone VARCHAR(64) NOT NULL comment "联系电话",
    gateway_endpoint VARCHAR(64) NOT NULL comment "网关地址",
    agency_status TINYINT DEFAULT 0 NOT NULL comment "机构状态(0:启用，1:禁用)",
    user_count INT DEFAULT 0 comment "机构用户数",
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by VARCHAR(20) NOT NULL DEFAULT '',
    update_by VARCHAR(20) NOT NULL DEFAULT '',
    PRIMARY KEY (agency_id),
    INDEX idx_agency_name (agency_name)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- 创建机构证书表
CREATE TABLE IF NOT EXISTS wedpr_cert (
    cert_id VARCHAR(64) NOT NULL comment "证书id",
    agency_id VARCHAR(64) NOT NULL comment "机构编号",
    agency_name VARCHAR(64) NOT NULL comment "机构名",
    csr_file_name VARCHAR(64) NOT NULL comment "机构证书请求文件名",
    csr_file_text text NOT NULL comment "机构证书请求文件内容",
    cert_file_text text NOT NULL comment "机构证书文件内容",
    expire_time DATETIME NOT NULL comment "过期时间",
    cert_status TINYINT DEFAULT 0 NOT NULL comment "证书状态(0：无证书，1：有效，2：过期，3：禁用)",
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by VARCHAR(20) NOT NULL DEFAULT '',
    update_by VARCHAR(20) NOT NULL DEFAULT '',
    PRIMARY KEY (cert_id),
    INDEX idx_agency_name (agency_name),
    INDEX idx_cert_status (cert_status)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;
-- the api credential table
create table if not exists `wedpr_api_credential_table`(
    `id` varchar(64) not null comment "id",
    `access_key_id` varchar(255) not null comment "访问ID(用于标志用户)",
    `access_key_secret` VARCHAR(1024) not null comment "访问密钥",
    `owner` varchar(255) not null comment "属主",
    `status` varchar(1024) not null comment "状态",
    `desc` text comment "描述信息",
    `create_time` DATETIME DEFAULT  CURRENT_TIMESTAMP comment "创建时间",
    `last_update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment "更新时间",
    primary key (`id`),
    index access_key_id_index(`access_key_id`(128)),
    index owner_index(`owner`(128)),
    index status_index(`status`(128))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- the service publish related tables
create table if not exists wedpr_published_service(
    `service_id` varchar(64) not null comment "发布的服务ID",
    `service_name` varchar(1024) not null comment "服务名称",
    `service_desc` text default null comment "服务描述",
    `service_type` varchar(128) not null comment "发布的服务类型, 如pir/lr/xgb",
    `service_config` longtext comment "服务配置",
    `owner` varchar(255) not null comment "属主",
    `agency` varchar(255) not null comment "所属机构",
    `status` varchar(1024) not null comment "服务状态",
    `status_msg` text comment "服务状态说明",
    `sync_status` tinyint default 0 comment "同步状态",
    `create_time` DATETIME DEFAULT  CURRENT_TIMESTAMP comment "创建时间",
    `last_update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment "更新时间",
     primary key (`service_id`),
     index service_name_index(`service_name`(128)),
     index service_type_index(`service_type`(128)),
     index status_index(`status`(128)),
     index owner_index(`owner`(128)),
     index agency_index(`agency`(128))
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

create table if not exists wedpr_service_auth_table(
    `id` varchar(64) not null comment "ID",
    `service_id` varchar(64) not null comment "服务ID",
    `access_key_id` varchar(255) not null comment "访问ID(用于标志用户)",
    `accessible_user` varchar(255) not null comment "授权访问的用户",
    `accessible_agency` varchar(255) not null comment "授权机构",
    `expire_time` DATETIME not null comment "过期时间",
    `apply_time` DATETIME DEFAULT  CURRENT_TIMESTAMP comment "申请时间",
    `last_update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment "更新时间",
    primary key (`id`),
    index service_id_index(`service_id`(64)),
    index access_key_id_index(`access_key_id`(128)),
    index accessible_user_index(`accessible_user`(128)),
    index accessible_agency_index(`accessible_agency`(128))
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

create table if not exists wedpr_service_invoke_table(
    `invoke_id` varchar(64) not null comment "调用ID",
    `service_id` varchar(64) not null comment "调用的服务ID",
    `service_type` varchar(128) not null comment "发布的服务类型, 如pir/lr/xgb",
    `invoke_user` varchar(1024) not null comment "调用者",
    `invoke_agency` varchar(1024) not null comment "调用方所属机构",
    `invoke_status` varchar(64) comment "调用的服务状态",
    `invoke_time` DATETIME DEFAULT  CURRENT_TIMESTAMP comment "调用时间",
    `last_update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment "更新时间",
    primary key (`invoke_id`),
    index service_id_index(`service_id`),
    index service_type_index(`service_type`),
    index invoke_status_index(`invoke_status`),
    index invoker_index(`invoke_agency`(64), `invoke_user`(64))
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;
