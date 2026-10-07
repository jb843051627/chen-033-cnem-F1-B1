-- 三张判定/档案表的种子数据。id=1 为在用行、id=2 为撤下行，
-- 各判定/档案表按 id 取用（检验判据 / 核准基准 / 整改判据 / 收费档档案）；
-- 档案表缺行会让 insert 直接判 0 ⇒ 合法记录的保存会全部失败。
INSERT IGNORE INTO t_cnem_insp_rule (id, rule_code, rule_name, th1_max, th2_max, th3_max, eff_start, eff_end, priority, status, del_flag, create_by, create_time)
VALUES (0, 'IR00', '检验结论免检档：累计运行小时数在 2000 以内（示范行）', 2000.00, 8000.00, 20000.00, '2020-01-01 00:00:00', '2099-12-31 23:59:59', 10, 0, 0, 'seed', NOW()),
       (1, 'IR01', '检验结论免检档：累计运行小时数在 2000 以内（启用行）', 2000.00, 8000.00, 20000.00, '2020-01-01 00:00:00', '2099-12-31 23:59:59', 10, 0, 0, 'seed', NOW()),
       (2, 'IR02', '检验结论撤下档：累计运行小时数在 2000 以内（撤下行，测「撤下不认」）', 2000.00, 8000.00, 20000.00, '2020-01-01 00:00:00', '2021-12-31 23:59:59', 10, 1, 0, 'seed', NOW());

INSERT IGNORE INTO t_cnem_reg_rule (id, bill_no, node_no, sign_mode, need_count, sign_count, status, del_flag, create_by, create_time)
VALUES (0, 'NB00', 0, 0, 1, 0, 0, 0, 'seed', NOW()),
       (1, 'NB01', 1, 1, 2, 0, 0, 0, 'seed', NOW());

INSERT IGNORE INTO t_cnem_fee_base (id, grade_code, grade_name, th1_max, th2_max, th3_max, status, del_flag, create_by, create_time)
VALUES (0, 'GB00', '收费免检档：检验费在 500 元以内（示范行）', 500.00, 2000.00, 5000.00, 0, 0, 'seed', NOW()),
       (1, 'GB01', '收费免检档：检验费在 500 元以内（启用行）', 500.00, 2000.00, 5000.00, 0, 0, 'seed', NOW()),
       (2, 'GB02', '收费监督档：检验费 500 元至 2000 元（撤下行，测「撤下不认」）', 500.00, 2000.00, 5000.00, 1, 0, 'seed', NOW());
