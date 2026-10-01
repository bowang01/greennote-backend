INSERT INTO gn_dept (id, name, parent_id, sort_no, status)
SELECT 'a1000000-0000-4000-8000-000000000001', 'Headquarters', NULL, 0, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM gn_dept WHERE name = 'Headquarters');

INSERT INTO gn_role (id, name, code, status)
SELECT 'a2000000-0000-4000-8000-000000000001', 'Super Admin', 'super_admin', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM gn_role WHERE code = 'super_admin');

INSERT INTO gn_admin_user (id, dept_id, username, password, nickname, status)
SELECT 'a4000000-0000-4000-8000-000000000001', d.id, 'admin', '$2a$10$jXEjVvEB0bHOwysSYhIK9unVOItbPrvEhQilA5oapdd/2Ewz4yJHG', 'Administrator', 0
FROM gn_dept d
WHERE d.name = 'Headquarters'
  AND NOT EXISTS (SELECT 1 FROM gn_admin_user WHERE username = 'admin');

INSERT INTO gn_admin_user_role (admin_user_id, role_id)
SELECT u.id, r.id
FROM gn_admin_user u
JOIN gn_role r ON r.code = 'super_admin'
WHERE u.username = 'admin'
  AND NOT EXISTS (
      SELECT 1 FROM gn_admin_user_role ur WHERE ur.admin_user_id = u.id AND ur.role_id = r.id
  );

INSERT INTO gn_menu (id, parent_id, name, path, sort_no, status)
SELECT 'a3000000-0000-4000-8000-000000000001', NULL, 'Home', '/', 1, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM gn_menu WHERE path = '/');

INSERT INTO gn_menu (id, parent_id, name, path, sort_no, status)
SELECT 'a3000000-0000-4000-8000-000000000002', NULL, 'Members', '/members', 2, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM gn_menu WHERE path = '/members');

INSERT INTO gn_menu (id, parent_id, name, path, sort_no, status)
SELECT 'a3000000-0000-4000-8000-000000000003', NULL, 'Site', '/site', 3, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM gn_menu WHERE path = '/site');

INSERT INTO gn_menu (id, parent_id, name, path, sort_no, status)
SELECT 'a3000000-0000-4000-8000-000000000004', NULL, 'Departments', '/departments', 4, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM gn_menu WHERE path = '/departments');

INSERT INTO gn_menu (id, parent_id, name, path, sort_no, status)
SELECT 'a3000000-0000-4000-8000-000000000005', NULL, 'Dictionaries', '/dictionaries', 5, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM gn_menu WHERE path = '/dictionaries');

INSERT INTO gn_menu (id, parent_id, name, path, sort_no, status)
SELECT 'a3000000-0000-4000-8000-000000000006', NULL, 'Files', '/files', 6, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM gn_menu WHERE path = '/files');

INSERT INTO gn_menu (id, parent_id, name, path, sort_no, status)
SELECT 'a3000000-0000-4000-8000-000000000007', NULL, 'Operation logs', '/logs', 7, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM gn_menu WHERE path = '/logs');

INSERT INTO gn_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM gn_role r
JOIN gn_menu m ON 1 = 1
WHERE r.code = 'super_admin'
  AND NOT EXISTS (
      SELECT 1 FROM gn_role_menu rm WHERE rm.role_id = r.id AND rm.menu_id = m.id
  );

INSERT INTO gn_dict_type (id, name, dict_type, status)
SELECT 'a7000000-0000-4000-8000-000000000001', 'User status', 'user_status', 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM gn_dict_type WHERE dict_type = 'user_status');

INSERT INTO gn_dict_data (id, dict_type, label, dict_value, sort_no, status)
SELECT 'a8000000-0000-4000-8000-000000000001', 'user_status', 'Active', '0', 1, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM gn_dict_data WHERE dict_type = 'user_status' AND dict_value = '0');

INSERT INTO gn_dict_data (id, dict_type, label, dict_value, sort_no, status)
SELECT 'a8000000-0000-4000-8000-000000000002', 'user_status', 'Disabled', '1', 2, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM gn_dict_data WHERE dict_type = 'user_status' AND dict_value = '1');

INSERT INTO gn_config (id, config_key, config_value)
SELECT 'a9000000-0000-4000-8000-000000000001', 'site.name', 'GreenNote' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM gn_config WHERE config_key = 'site.name');

INSERT INTO gn_config (id, config_key, config_value)
SELECT 'a9000000-0000-4000-8000-000000000002', 'site.logo', '' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM gn_config WHERE config_key = 'site.logo');

INSERT INTO gn_config (id, config_key, config_value)
SELECT 'a9000000-0000-4000-8000-000000000003', 'site.theme', '#1b6b45' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM gn_config WHERE config_key = 'site.theme');

INSERT INTO gn_menu (id, parent_id, name, path, sort_no, status)
SELECT 'a3000000-0000-4000-8000-000000000008', NULL, 'Channels', '/channels', 8, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM gn_menu WHERE path = '/channels');

INSERT INTO gn_menu (id, parent_id, name, path, sort_no, status)
SELECT 'a3000000-0000-4000-8000-000000000009', NULL, 'Topics', '/topics', 9, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM gn_menu WHERE path = '/topics');

INSERT INTO gn_menu (id, parent_id, name, path, sort_no, status)
SELECT 'a3000000-0000-4000-8000-00000000000a', NULL, 'Notes', '/notes', 10, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM gn_menu WHERE path = '/notes');

INSERT INTO gn_role_menu (role_id, menu_id)
SELECT r.id, m.id
FROM gn_role r
JOIN gn_menu m ON m.path IN ('/channels', '/topics', '/notes')
WHERE r.code = 'super_admin'
  AND NOT EXISTS (
      SELECT 1 FROM gn_role_menu rm WHERE rm.role_id = r.id AND rm.menu_id = m.id
  );

INSERT INTO gn_channel (id, name, sort_no, status)
SELECT 'a5000000-0000-4000-8000-000000000001', 'Fashion', 1, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM gn_channel WHERE name = 'Fashion');
INSERT INTO gn_channel (id, name, sort_no, status)
SELECT 'a5000000-0000-4000-8000-000000000002', 'Food', 2, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM gn_channel WHERE name = 'Food');
INSERT INTO gn_channel (id, name, sort_no, status)
SELECT 'a5000000-0000-4000-8000-000000000003', 'Travel', 3, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM gn_channel WHERE name = 'Travel');
INSERT INTO gn_channel (id, name, sort_no, status)
SELECT 'a5000000-0000-4000-8000-000000000004', 'Beauty', 4, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM gn_channel WHERE name = 'Beauty');
INSERT INTO gn_channel (id, name, sort_no, status)
SELECT 'a5000000-0000-4000-8000-000000000005', 'Home', 5, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM gn_channel WHERE name = 'Home');
INSERT INTO gn_channel (id, name, sort_no, status)
SELECT 'a5000000-0000-4000-8000-000000000006', 'Fitness', 6, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM gn_channel WHERE name = 'Fitness');

INSERT INTO gn_topic (id, name, intro, sort_no, status)
SELECT 'a6000000-0000-4000-8000-000000000001', 'Outfit of the Day', 'One outfit a day', 1, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM gn_topic WHERE name = 'Outfit of the Day');
INSERT INTO gn_topic (id, name, intro, sort_no, status)
SELECT 'a6000000-0000-4000-8000-000000000002', 'Home Cooking', 'Dishes you can cook at home', 2, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM gn_topic WHERE name = 'Home Cooking');
INSERT INTO gn_topic (id, name, intro, sort_no, status)
SELECT 'a6000000-0000-4000-8000-000000000003', 'Weekend Trip', 'Short getaways', 3, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM gn_topic WHERE name = 'Weekend Trip');
INSERT INTO gn_topic (id, name, intro, sort_no, status)
SELECT 'a6000000-0000-4000-8000-000000000004', 'Budget Finds', 'Good and affordable', 4, 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM gn_topic WHERE name = 'Budget Finds');
