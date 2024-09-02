-- 管理端初始化
insert into wedpr_user (username, password, status) values('admin', '{bcrypt}$2a$10$XuiuKLg23kxtC/ldvYN0/evt0Y3aoBC9iV29srhIBMMDORzCQiYA.', 0);
insert into wedpr_user_role(username, role_id) values ('admin', '10');
insert into wedpr_role_permission (role_id, role_name, permission_id) values ('10', 'admin_user', '1');