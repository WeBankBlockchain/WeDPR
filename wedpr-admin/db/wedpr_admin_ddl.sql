-- 创建机构表
CREATE TABLE IF NOT EXISTS wedpr_agency (
    agency_no VARCHAR(64) NOT NULL comment "机构编号",
    agency_name VARCHAR(64) NOT NULL comment "机构名",
    `desc` text NOT NULL comment "机构描述",
    agency_contact VARCHAR(64) NOT NULL comment "机构联系人",
    contact_phone VARCHAR(64) NOT NULL comment "联系电话",
    gateway_endpoint VARCHAR(64) NOT NULL comment "网关地址",
    agency_status TINYINT DEFAULT 0 NOT NULL comment "机构状态(0:启用，1:禁用)",
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by VARCHAR(20) NOT NULL DEFAULT '',
    update_by VARCHAR(20) NOT NULL DEFAULT '',
    PRIMARY KEY (agency_no),
    INDEX idx_agency_name (agency_name)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;


-- 创建机构用户表
CREATE TABLE IF NOT EXISTS wedpr_agency_user (
    agency_no VARCHAR(64) NOT NULL comment "机构编号",
    user_count INT DEFAULT 0 comment "机构用户数",
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by VARCHAR(20) NOT NULL DEFAULT '',
    update_by VARCHAR(20) NOT NULL DEFAULT '',
    PRIMARY KEY (agency_no)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ROW_FORMAT=DYNAMIC;

-- 创建机构证书表
CREATE TABLE IF NOT EXISTS wedpr_cert (
    cert_id VARCHAR(64) NOT NULL comment "证书id",
    agency_no VARCHAR(64) NOT NULL comment "机构编号",
    agency_name VARCHAR(64) NOT NULL comment "机构名",
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

CREATE TABLE if not exists `wedpr_dataset` (
    `dataset_id` VARCHAR(64) NOT NULL COMMENT '数据集id',
    `dataset_title` VARCHAR(1024) NOT NULL COMMENT '数据集标题',
    `dataset_label` VARCHAR(1024) NOT NULL COMMENT '数据集标签',
    `dataset_desc` TEXT NOT NULL COMMENT '数据集描述',
    `dataset_fields` TEXT COMMENT '数据源字段以及预览信息',
    `dataset_version_hash` VARCHAR(64) DEFAULT '' COMMENT '数据集hash',
    `dataset_data_size` bigint DEFAULT 0 COMMENT '数据集大小',
    `dataset_record_count` bigint DEFAULT 0 COMMENT '数据集记录数目',
    `dataset_column_count` int DEFAULT 0 COMMENT '数据集列数目',
    `dataset_storage_type` VARCHAR(255) DEFAULT '' COMMENT '数据集存储类型',
    `dataset_storage_path` VARCHAR(1024) DEFAULT '' COMMENT '数据集存储路径',
    `owner_agency_name` VARCHAR(255) NOT NULL COMMENT '数据集所属机构名称',
    `owner_user_name` VARCHAR(1024) NOT NULL COMMENT '数据集所属用户名',
    `data_source_type` VARCHAR(255) NOT NULL COMMENT '数据源类型 : CSV、DB、XLSX、FPS、HDFS、HIVE',
    `data_source_meta` TEXT NOT NULL COMMENT '数据源参数信息，JSON字符串',
    `visibility` int(8) NOT NULL COMMENT '数据集可见性, 0: 私有，1: 公开可见',
    `visibility_details` TEXT NOT NULL COMMENT '数据源可见范围描述, visibility 为1时有效',
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

create table if not exists `wedpr_project_table`(
    `id` varchar(64) not null comment "项目ID",
    `name` varchar(1024) not null comment "项目名称",
    `desc` varchar(1024) not null comment "项目描述",
    `owner` varchar(255) not null comment "项目属主",
    `owner_agency` varchar(255) not null comment "项目所属机构",
    `project_type` varchar(255) not null comment "项目类型(Export/Wizard)",
    `label` varchar(1024) comment "项目标签",
    `create_time` DATETIME DEFAULT  CURRENT_TIMESTAMP comment "项目创建时间",
    `last_update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment "项目更新时间",
    primary key (`id`),
    unique index name_index(`name`(128)),
    index owner_index(`owner`(128), `owner_agency`(128)),
    index project_type_index(`project_type`(128)),
    index label_index(`label`(128))
)ENGINE=InnoDB default charset=utf8mb4 default collate=utf8mb4_bin ROW_FORMAT=DYNAMIC;

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
    `create_time` DATETIME DEFAULT  CURRENT_TIMESTAMP comment "任务创建时间",
    `last_update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment "任务更新时间",
    primary key(`id`),
    index name_index(`name`(128)),
    index owner_index(`owner`(128)),
    index owner_agency_index(`owner_agency`(128)),
    index project_index(`project_name`(128)),
    index status_index(`status`(128))
)ENGINE=InnoDB default charset=utf8mb4 default collate=utf8mb4_bin ROW_FORMAT=DYNAMIC;

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
