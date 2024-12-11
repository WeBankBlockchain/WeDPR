-- 管理端初始化
insert into wedpr_user (username, password, status) values('admin', '{bcrypt}$2a$10$XuiuKLg23kxtC/ldvYN0/evt0Y3aoBC9iV29srhIBMMDORzCQiYA.', 0);
insert into wedpr_user_role(username, role_id) values ('admin', '10');
insert into wedpr_role_permission (role_id, role_name, permission_id) values ('10', 'admin_user', '1');

insert into `wedpr_config_table`(`config_key`, `config_value`) values("wedpr_algorithm_templates", '{"version":"1.0","templates":[{"name":"PSI","title":"数据对齐","detail":"","version":"1.0"},{"name":"XGB_TRAINING","title":"SecureLGBM训练","detail":"","version":"1.0"},{"name":"XGB_PREDICTING","title":"SecureLGBM预测","detail":"","version":"1.0"}]}');
