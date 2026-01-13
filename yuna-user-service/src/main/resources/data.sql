
-- ----------------------------
-- Records of permission
-- ----------------------------
-- 菜单三段标识
INSERT INTO `permission` VALUES (22, 15, 3, 'menu:sys:*', '系统管理菜单总权限', '2026-01-11 15:00:00');
INSERT INTO `permission` VALUES (23, 22, 1, 'menu:sys:user:list', '账号管理菜单', '2026-01-11 15:00:00');
INSERT INTO `permission` VALUES (24, 22, 1, 'menu:sys:role:list', '角色管理菜单', '2026-01-11 15:00:00');
INSERT INTO `permission` VALUES (25, 22, 1, 'menu:sys:permission:list', '权限管理菜单', '2026-01-11 15:00:00');
INSERT INTO `permission` VALUES (26, 22, 1, 'menu:sys:ban:list', '封禁管理菜单', '2026-01-11 15:00:00');
-- 用户相关（list 为菜单，其余为按钮）
INSERT INTO `permission` VALUES (19, 15, 3, 'sys:user:*', '用户管理总权限', '2026-01-11 15:00:00');
INSERT INTO `permission` VALUES (1, 19, 1, 'sys:user:list', '用户列表', '2026-01-11 15:00:00');
INSERT INTO `permission` VALUES (2, 19, 2, 'sys:user:add', '添加用户', '2026-01-11 15:00:00');
INSERT INTO `permission` VALUES (3, 19, 2, 'sys:user:edit', '编辑用户', '2026-01-11 15:00:00');
INSERT INTO `permission` VALUES (4, 19, 2, 'sys:user:delete', '删除用户', '2026-01-11 15:00:00');
INSERT INTO `permission` VALUES (5, 19, 2, 'sys:user:assign', '分配角色', '2026-01-11 15:00:00');
INSERT INTO `permission` VALUES (27, 19, 2, 'sys:user:ban', '封禁用户', '2026-01-11 15:00:00');
INSERT INTO `permission` VALUES (28, 19, 2, 'sys:user:unban', '解封用户', '2026-01-11 15:00:00');
INSERT INTO `permission` VALUES (29, 19, 2, 'sys:ban:list', '封禁列表', '2026-01-11 15:00:00');
-- 角色相关
INSERT INTO `permission` VALUES (20, 15, 3, 'sys:role:*', '角色管理总权限', '2026-01-11 15:00:00');
INSERT INTO `permission` VALUES (6, 20, 1, 'sys:role:list', '角色列表', '2026-01-11 15:00:00');
INSERT INTO `permission` VALUES (7, 20, 2, 'sys:role:add', '添加角色', '2026-01-11 15:00:00');
INSERT INTO `permission` VALUES (8, 20, 2, 'sys:role:edit', '编辑角色', '2026-01-11 15:00:00');
INSERT INTO `permission` VALUES (9, 20, 2, 'sys:role:delete', '删除角色', '2026-01-11 15:00:00');
INSERT INTO `permission` VALUES (10, 20, 2, 'sys:role:assign', '分配权限', '2026-01-11 15:00:00');
-- 权限相关
INSERT INTO `permission` VALUES (21, 15, 3, 'sys:permission:*', '权限管理总权限', '2026-01-11 15:00:00');
INSERT INTO `permission` VALUES (11, 21, 1, 'sys:permission:list', '权限列表', '2026-01-11 15:00:00');
INSERT INTO `permission` VALUES (12, 21, 2, 'sys:permission:add', '添加权限', '2026-01-11 15:00:00');
INSERT INTO `permission` VALUES (13, 21, 2, 'sys:permission:edit', '编辑权限', '2026-01-11 15:00:00');
INSERT INTO `permission` VALUES (14, 21, 2, 'sys:permission:delete', '删除权限', '2026-01-11 15:00:00');
-- 超管通配（特殊类型，用于显示为“特殊”）
INSERT INTO `permission` VALUES (15, 0, 3, '*:*:*', '全部权限', '2026-01-11 15:00:00');

-- ----------------------------
-- Records of role
-- ----------------------------
INSERT INTO `role` VALUES (1, 'super_admin', '超级管理员', 9999, '拥有所有权限', '2026-01-11 15:00:00');
INSERT INTO `role` VALUES (2, 'admin', '普通管理员', 100, '具备系统管理权限', '2026-01-11 15:00:00');
INSERT INTO `role` VALUES (3, 'user', '普通用户', 10, '普通用户基础权限', '2026-01-11 15:00:00');

-- ----------------------------
-- Records of role_permission
-- ----------------------------
INSERT INTO `role_permission` (role_id, permission_id) VALUES (1, 15);
-- 可按需为普通管理员赋权示例（此处仅赋予权限管理菜单）
INSERT INTO `role_permission` (role_id, permission_id) VALUES (2, 22);
-- 普通用户默认不赋系统管理权限

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` (id, uuid, username, password, email, status, create_time) VALUES (1, 'a1b2c3d4-e5f6-7890-1234-567890abcdef', 'admin', '$2a$10$E.q3.1yB/2yH.C5qj3j3j.iG/5tX.9Yv/6uX.Z5b.8Yv/3uX.Z5b.8', 'admin@example.com', 1, '2026-01-11 15:00:00');

-- ----------------------------
-- Records of user_info
-- ----------------------------
INSERT INTO `user_info` (user_id, nickname, avatar_id, gender, birthday, biography, experience, update_time) VALUES (1, 'Yuna#Admin', NULL, 0, NULL, 'I am the administrator.', 999, '2026-01-11 15:00:00');

-- ----------------------------
-- Records of user_role
-- ----------------------------
INSERT INTO `user_role` (user_id, role_id) VALUES (1, 1);
