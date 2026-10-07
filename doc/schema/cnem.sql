-- cnem 特种设备检验检测与使用登记监管 -- schema (chen-033)
-- 列名与基线实体契约（@TableName/@TableField）逐列对齐，改列必须同步实体。
-- 库：chen_033

CREATE TABLE IF NOT EXISTS t_cnem_fee_base (
  id bigint NOT NULL COMMENT '主键',
  grade_code varchar(32) DEFAULT NULL COMMENT '收费档编号',
  grade_name varchar(64) DEFAULT NULL COMMENT '收费档名称',
  th1_max decimal(14,2) DEFAULT NULL COMMENT '免检档上限(元)',
  th2_max decimal(14,2) DEFAULT NULL COMMENT '一般档上限(元)',
  th3_max decimal(14,2) DEFAULT NULL COMMENT '监督档上限(元)',
  status int DEFAULT NULL COMMENT '收费档状态 0启用 1撤下',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='检验费收费档档案';

CREATE TABLE IF NOT EXISTS t_cnem_fee_row (
  id bigint NOT NULL COMMENT '主键',
  item_no varchar(64) DEFAULT NULL COMMENT '条目编号',
  due_at datetime DEFAULT NULL COMMENT '到期时刻',
  amount decimal(14,2) DEFAULT NULL COMMENT '应收检验费(元)',
  base_id bigint DEFAULT NULL COMMENT '所依据的收费档档案主键',
  grade_code varchar(32) DEFAULT NULL COMMENT '收费档编号(冗余自档案)',
  settle_type varchar(128) DEFAULT NULL COMMENT '结算方式',
  unit_name varchar(128) DEFAULT NULL COMMENT '使用单位',
  status int DEFAULT NULL COMMENT '条目状态 0未结算 1已结算 2已退回',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='检验费结算条目';

CREATE TABLE IF NOT EXISTS t_cnem_insp_remind_row (
  id bigint NOT NULL COMMENT '主键',
  item_no varchar(64) DEFAULT NULL COMMENT '条目编号',
  due_at datetime DEFAULT NULL COMMENT '到期时刻',
  amount decimal(14,2) DEFAULT NULL COMMENT '预估检验费(元)',
  notify_type varchar(128) DEFAULT NULL COMMENT '提醒方式',
  unit_name varchar(128) DEFAULT NULL COMMENT '使用单位',
  status int DEFAULT NULL COMMENT '条目状态 0待发 1已发 2已处理',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='检验到期提醒条目';

CREATE TABLE IF NOT EXISTS t_cnem_insp_row (
  id bigint NOT NULL COMMENT '主键',
  batch_no varchar(64) DEFAULT NULL COMMENT '申报批次码',
  row_no int DEFAULT NULL COMMENT '原始行次',
  item_code varchar(64) DEFAULT NULL COMMENT '设备编号',
  qty decimal(14,2) DEFAULT NULL COMMENT '本轮累计运行小时数',
  device_kind varchar(128) DEFAULT NULL COMMENT '设备类别',
  unit_name varchar(128) DEFAULT NULL COMMENT '使用单位',
  status int DEFAULT NULL COMMENT '行状态 0待受理 1已受理 2已退回',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='检验申报行';

CREATE TABLE IF NOT EXISTS t_cnem_insp_rule (
  id bigint NOT NULL COMMENT '主键',
  rule_code varchar(32) DEFAULT NULL COMMENT '规则编号',
  rule_name varchar(64) DEFAULT NULL COMMENT '规则名称',
  th1_max decimal(14,2) DEFAULT NULL COMMENT '免检档上限(运行小时)',
  th2_max decimal(14,2) DEFAULT NULL COMMENT '一般检验档上限(运行小时)',
  th3_max decimal(14,2) DEFAULT NULL COMMENT '监督检验档上限(运行小时)',
  eff_start datetime DEFAULT NULL COMMENT '生效起始时刻',
  eff_end datetime DEFAULT NULL COMMENT '生效截止时刻(不含)',
  priority int DEFAULT NULL COMMENT '优先级(数值越大越优先)',
  status int DEFAULT NULL COMMENT '规则状态 0启用 1撤下',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='检验结论判定规则';

CREATE TABLE IF NOT EXISTS t_cnem_job (
  id bigint NOT NULL COMMENT '主键',
  job_no varchar(32) DEFAULT NULL COMMENT '作业编号',
  job_name varchar(64) DEFAULT NULL COMMENT '作业名称',
  status int DEFAULT NULL COMMENT '作业状态(0草稿1待审2生效中3已生效4部分生效5回滚中6已回滚)',
  total_rows int DEFAULT NULL COMMENT '提交总行数',
  done_rows int DEFAULT NULL COMMENT '生效成功行数',
  fail_rows int DEFAULT NULL COMMENT '生效失败行数',
  eff_start datetime DEFAULT NULL COMMENT '作业生效起始时刻',
  eff_end datetime DEFAULT NULL COMMENT '作业生效截止时刻(不含)',
  version varchar(32) DEFAULT NULL COMMENT '判据版本',
  row_no int DEFAULT NULL COMMENT '行次',
  insp_no varchar(64) DEFAULT NULL COMMENT '检验编号',
  amount decimal(14,2) DEFAULT NULL COMMENT '金额',
  job_qty int DEFAULT NULL COMMENT '数量',
  version_status int DEFAULT NULL COMMENT '版本状态(1生效中 0未生效)',
  writeback_flag int DEFAULT NULL COMMENT '是否已写回(0否1是)',
  compensate_flag int DEFAULT NULL COMMENT '补偿流水标记(0否1是)',
  compensated int DEFAULT NULL COMMENT '是否已补偿(0否1是)',
  compensate_cnt int DEFAULT NULL COMMENT '补偿累计次数',
  fail_reason varchar(255) DEFAULT NULL COMMENT '失败原因',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_job_no (job_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='检验结论作业';

CREATE TABLE IF NOT EXISTS t_cnem_job_row (
  id bigint NOT NULL COMMENT '主键',
  job_no varchar(32) DEFAULT NULL COMMENT '作业编号',
  row_no int DEFAULT NULL COMMENT '行次(系统由上往下排,不可挪)',
  insp_no varchar(64) DEFAULT NULL COMMENT '检验编号',
  amount decimal(14,2) DEFAULT NULL COMMENT '金额',
  job_qty int DEFAULT NULL COMMENT '数量',
  version varchar(32) DEFAULT NULL COMMENT '判据版本(老行可空,按缺项口径接着走)',
  rule_id bigint DEFAULT NULL COMMENT '本行所指检验判据主键',
  result_level int DEFAULT NULL COMMENT '检验结论等级 1..4(老行可空,按缺项口径接着走)',
  version_status int DEFAULT NULL COMMENT '版本状态(1生效中 0未生效)',
  writeback_flag int DEFAULT NULL COMMENT '是否已写回(0否1是)',
  compensate_flag int DEFAULT NULL COMMENT '补偿流水标记(0否1是)',
  compensated int DEFAULT NULL COMMENT '是否已补偿(0否1是,冲账流水幂等键)',
  compensate_cnt int DEFAULT NULL COMMENT '补偿累计次数(冲账至多一道,恒 0/1)',
  fail_reason varchar(255) DEFAULT NULL COMMENT '失败原因(没做成写明,不推翻整批)',
  status int DEFAULT NULL COMMENT '行状态 0待做 1已做成 2没做成 3已冲回',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_job_row (job_no, row_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='检验作业行';

-- 作业台账:生效/冲回逐行追加,只补不删;同刻同动作幂等键 uk_row_action 防重复落账
CREATE TABLE IF NOT EXISTS t_cnem_job_ledger (
  id bigint NOT NULL COMMENT '主键',
  job_no varchar(32) DEFAULT NULL COMMENT '作业编号',
  row_no int DEFAULT NULL COMMENT '行次',
  action int DEFAULT NULL COMMENT '动作 1生效落账 2冲回冲账',
  insp_no varchar(64) DEFAULT NULL COMMENT '检验编号',
  result_level int DEFAULT NULL COMMENT '检验结论等级',
  amount decimal(14,2) DEFAULT NULL COMMENT '金额(冲账为负向记录的正向绝对值)',
  fee_row_id bigint DEFAULT NULL COMMENT '对应收费流水条目主键',
  oper_by varchar(64) DEFAULT NULL COMMENT '动的人',
  oper_at datetime DEFAULT NULL COMMENT '动的时刻',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_row_action (job_no, row_no, action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='检验作业台账(生效/冲账流水)';

CREATE TABLE IF NOT EXISTS t_cnem_reg_rule (
  id bigint NOT NULL COMMENT '主键',
  bill_no varchar(64) DEFAULT NULL COMMENT '登记单代号',
  node_no int DEFAULT NULL COMMENT '当前关口 0..2',
  sign_mode int DEFAULT NULL COMMENT '落签模式 0或签 1齐签',
  need_count int DEFAULT NULL COMMENT '本关口应落人数',
  sign_count int DEFAULT NULL COMMENT '本关口已落人数',
  status int DEFAULT NULL COMMENT '基准状态 0在办 1已核准 2已封住',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='使用登记核准基准';

CREATE TABLE IF NOT EXISTS t_cnem_trace_row (
  id bigint NOT NULL COMMENT '主键',
  item_no varchar(64) DEFAULT NULL COMMENT '条目编号',
  biz_no varchar(64) DEFAULT NULL COMMENT '整改单号',
  stage int DEFAULT NULL COMMENT '当前处置步次',
  content varchar(255) DEFAULT NULL COMMENT '处置内容',
  last_action varchar(32) DEFAULT NULL COMMENT '最近一次动作',
  amount decimal(14,2) DEFAULT NULL COMMENT '整改费用(元)',
  settle_type varchar(128) DEFAULT NULL COMMENT '处置方式',
  unit_name varchar(128) DEFAULT NULL COMMENT '使用单位',
  status int DEFAULT NULL COMMENT '条目状态 0在办 1已定下 2已封住',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='隐患整改处置条目';

-- 初始档案数据（id=1 启用 / id=2 停用）
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

