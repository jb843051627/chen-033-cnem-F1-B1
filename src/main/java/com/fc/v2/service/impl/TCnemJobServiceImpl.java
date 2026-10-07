package com.fc.v2.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fc.v2.mapper.auto.TCnemFeeBaseMapper;
import com.fc.v2.mapper.auto.TCnemFeeRowMapper;
import com.fc.v2.mapper.auto.TCnemJobLedgerMapper;
import com.fc.v2.mapper.auto.TCnemJobMapper;
import com.fc.v2.mapper.auto.TCnemJobRowMapper;
import com.fc.v2.mapper.auto.TSysOperLogMapper;
import com.fc.v2.model.auto.TCnemFeeBase;
import com.fc.v2.model.auto.TCnemFeeRow;
import com.fc.v2.model.auto.TCnemInspRule;
import com.fc.v2.model.auto.TCnemJob;
import com.fc.v2.model.auto.TCnemJobLedger;
import com.fc.v2.model.auto.TCnemJobRow;
import com.fc.v2.model.auto.TSysOperLog;
import com.fc.v2.service.ITCnemInspRuleService;
import com.fc.v2.service.ITCnemJobService;
import com.fc.v2.shiro.util.ShiroUtils;
import com.fc.v2.util.SnowflakeIdWorker;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 检验结论作业 Service实现（job-lifecycle 形状：批量作业提交/生效/回滚，跨模块协调）
 *
 * 业务口径（册下规矩）：
 * 1. 作业号系统在册上排出；同一作业号在册只容一行占住，重复递交不另立新行（幂等）。
 *    明细行次系统由上往下排，人手里没有填号/挪行的格子；行缺项不在提交时炸批，留到生效裁。
 * 2. 三段：待审 → 生效（做）→ 回滚（收），不许跳。已做完/已收完的再喊做不理；回滚中不许往前做。
 *    同刻压上的两笔，靠作业主表状态 CAS + 行条件更新 + 台账唯一键裁决，后到者不得覆掉先到者。
 * 3. 每台做成立刻四处同落（同一业务时刻、同一独立事务，少一处这台不算做成）：
 *    ①检验结论行 t_cnem_job_row ②作业台账 t_cnem_job_ledger ③收费流水 t_cnem_fee_row
 *    ④操作留痕 t_sys_oper_log（谁动的、什么时候动的）。
 * 4. 哪台没做成不推翻整批：该行留册，写明没做成、为什么，其余照做。
 * 5. 每台指着一条检验判据（行上 version 格放判据 ruleCode）。判据撤下/过期/册上没有，
 *    这台做不成，写明判据这边出了状况；几条同时适用按优先级降序、ruleCode 降序取一条。
 *    存量老行 version/等级/检验编号缺格，按缺项口径接着走，不因缺项整批回退；金额缺则该行判没做成。
 * 6. 收回不是删结论行，是补一道冲账：台账追加 action=2、收费流水置「已退回」、结论行置已冲回；
 *    同一台收过只补一道（compensated 幂等键 + 台账 uk_row_action），不许写第二道。
 *
 * @author fuce
 * @date 2026-10-03
 */
@Service
public class TCnemJobServiceImpl implements ITCnemJobService {

    private final TCnemJobMapper cnemJobMapper;
    private final TCnemJobRowMapper jobRowMapper;
    private final TCnemJobLedgerMapper ledgerMapper;
    private final TCnemFeeBaseMapper feeBaseMapper;
    private final TCnemFeeRowMapper feeRowMapper;
    private final TSysOperLogMapper operLogMapper;
    private final ITCnemInspRuleService ruleService;
    private final TransactionTemplate txNew;

    private static final SnowflakeIdWorker ID_WORKER = new SnowflakeIdWorker(1, 1);

    public TCnemJobServiceImpl(TCnemJobMapper cnemJobMapper,
                               TCnemJobRowMapper jobRowMapper,
                               TCnemJobLedgerMapper ledgerMapper,
                               TCnemFeeBaseMapper feeBaseMapper,
                               TCnemFeeRowMapper feeRowMapper,
                               TSysOperLogMapper operLogMapper,
                               ITCnemInspRuleService ruleService,
                               PlatformTransactionManager transactionManager) {
        this.cnemJobMapper = cnemJobMapper;
        this.jobRowMapper = jobRowMapper;
        this.ledgerMapper = ledgerMapper;
        this.feeBaseMapper = feeBaseMapper;
        this.feeRowMapper = feeRowMapper;
        this.operLogMapper = operLogMapper;
        this.ruleService = ruleService;
        // 每行独立事务：一处写落失败，只回这一台，不连坐整批
        this.txNew = new TransactionTemplate(transactionManager);
        this.txNew.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    // 作业状态机（0草稿 1待审 2生效中 3已生效 4部分生效 5回滚中 6已回滚）
    private static final int ST_DRAFT = 0;
    private static final int ST_PENDING = 1;
    private static final int ST_EFFECTING = 2;
    private static final int ST_EFFECTED = 3;
    private static final int ST_PARTIAL = 4;
    private static final int ST_ROLLING = 5;
    private static final int ST_ROLLED = 6;

    // 行状态 0待做 1已做成 2没做成 3已冲回
    private static final int ROW_TODO = 0;
    private static final int ROW_DONE = 1;
    private static final int ROW_FAIL = 2;
    private static final int ROW_BACK = 3;

    // 台账动作 1生效落账 2冲回冲账
    private static final int ACT_EFFECT = 1;
    private static final int ACT_BACK = 2;

    // 收费流水状态 0未结算 1已结算 2已退回
    private static final int FEE_SETTLED = 1;
    private static final int FEE_RETURNED = 2;

    @Override
    public TCnemJob selectTCnemJobById(Long id) {
        if (id == null) {
            return null;
        }
        return this.cnemJobMapper.selectById(id);
    }

    @Override
    public TCnemJob selectByJobNo(String jobNo) {
        if (jobNo == null) {
            return null;
        }
        return this.cnemJobMapper.selectOne(new QueryWrapper<TCnemJob>()
                .eq("job_no", jobNo).eq("row_no", 0).eq("del_flag", 0).last("limit 1"));
    }

    @Override
    public String submitJob(String jobNo, List<TCnemJob> rows) {
        if (rows == null || rows.isEmpty()) {
            return jobNo;
        }
        // 号系统在册上排，人手里没有填号的格子
        String no = (jobNo == null || jobNo.trim().isEmpty()) ? nextJobNo() : jobNo.trim();

        // 同一作业号在册只容一行占住：再递一回也不另立新行（整批幂等，原样返回）
        TCnemJob existing = selectByJobNo(no);
        if (existing != null) {
            return no;
        }

        TCnemJob header = new TCnemJob();
        header.setJobNo(no);
        header.setStatus(Integer.valueOf(ST_PENDING));
        header.setRowNo(Integer.valueOf(0));
        header.setTotalRows(Integer.valueOf(rows.size()));
        header.setDoneRows(Integer.valueOf(0));
        header.setFailRows(Integer.valueOf(0));
        header.setDelFlag(Integer.valueOf(0));
        try {
            this.cnemJobMapper.insert(header);
        } catch (DuplicateKeyException e) {
            // 同刻并发递交：uk_job_no 裁决，后到者不另立
            return no;
        }

        // 行次系统从上往下排，报上去不许自己挪；缺项不炸批，留到生效时逐台裁
        int idx = 0;
        for (TCnemJob payload : rows) {
            idx++;
            TCnemJobRow r = new TCnemJobRow();
            r.setJobNo(no);
            r.setRowNo(Integer.valueOf(idx));
            r.setInspNo(payload.getInspNo());
            r.setAmount(payload.getAmount());
            r.setJobQty(payload.getJobQty());
            r.setVersion(payload.getVersion());
            r.setRemark(payload.getRemark());
            r.setStatus(Integer.valueOf(ROW_TODO));
            r.setVersionStatus(Integer.valueOf(0));
            r.setWritebackFlag(Integer.valueOf(0));
            r.setCompensateFlag(Integer.valueOf(0));
            r.setCompensated(Integer.valueOf(0));
            r.setCompensateCnt(Integer.valueOf(0));
            r.setDelFlag(Integer.valueOf(0));
            this.jobRowMapper.insert(r);
        }
        return no;
    }

    @Override
    public int effectJob(String jobNo) {
        TCnemJob header = selectByJobNo(jobNo);
        if (header == null) {
            return 0;
        }
        // 只有 待审 / 部分生效 能往前做；已生效、回滚中、已回滚再喊一声都不理
        int cur = header.getStatus() == null ? ST_DRAFT : header.getStatus().intValue();
        if (cur != ST_PENDING && cur != ST_PARTIAL) {
            return 0;
        }
        // 同刻两笔：CAS 占位生效中，只有一笔进得来，后到者 affected=0 直接退出
        boolean claimed = this.cnemJobMapper.update(null, new UpdateWrapper<TCnemJob>()
                .eq("job_no", jobNo).eq("row_no", 0).eq("del_flag", 0)
                .in("status", Integer.valueOf(ST_PENDING), Integer.valueOf(ST_PARTIAL))
                .set("status", Integer.valueOf(ST_EFFECTING))
                .setSql("eff_start = ifnull(eff_start, now())")) > 0;
        if (!claimed) {
            return 0;
        }

        Date now = new Date();
        String oper = operator();
        List<TCnemJobRow> items = listRows(jobNo);

        int done = 0;
        int fail = 0;
        for (TCnemJobRow r : items) {
            int st = r.getStatus() == null ? ROW_TODO : r.getStatus().intValue();
            // 已做成的不重做；已冲回的不做；只裁待做与上轮没做成的
            if (st == ROW_DONE || st == ROW_BACK) {
                done++;
                continue;
            }
            String reason = judge(r, now);
            if (reason != null) {
                markFail(r, reason);
                fail++;
            } else if (settleOne(r, now, oper)) {
                done++;
            } else {
                // 独立事务内四处没齐 → 这台没做成，台账记原因，其余台照做
                fail++;
            }
        }

        // 落定几台、没成几台回写作业主表；全成才是已生效，否则部分生效（允许再递一轮补做）
        int finalStatus = (fail == 0 && done == items.size()) ? ST_EFFECTED : ST_PARTIAL;
        this.cnemJobMapper.update(null, new UpdateWrapper<TCnemJob>()
                .eq("job_no", jobNo).eq("row_no", 0).eq("del_flag", 0)
                .eq("status", Integer.valueOf(ST_EFFECTING))
                .set("status", Integer.valueOf(finalStatus))
                .set("done_rows", Integer.valueOf(done))
                .set("fail_rows", Integer.valueOf(fail)));
        return done;
    }

    @Override
    public int rollbackJob(String jobNo) {
        TCnemJob header = selectByJobNo(jobNo);
        if (header == null) {
            return 0;
        }
        int cur = header.getStatus() == null ? ST_DRAFT : header.getStatus().intValue();
        // 做成过（已生效/部分生效）才轮得到收；收到一半停在回滚中，允许再递一轮接着收；
        // 待审/生效中/已回滚不理。回滚中只许收、不许往前做（effect 侧已挡）。
        if (cur != ST_EFFECTED && cur != ST_PARTIAL && cur != ST_ROLLING) {
            return 0;
        }
        boolean claimed = this.cnemJobMapper.update(null, new UpdateWrapper<TCnemJob>()
                .eq("job_no", jobNo).eq("row_no", 0).eq("del_flag", 0)
                .in("status", Integer.valueOf(ST_EFFECTED), Integer.valueOf(ST_PARTIAL),
                        Integer.valueOf(ST_ROLLING))
                .set("status", Integer.valueOf(ST_ROLLING))) > 0;
        if (!claimed) {
            return 0;
        }

        Date now = new Date();
        String oper = operator();
        List<TCnemJobRow> items = listRows(jobNo);

        int back = 0;
        int stillDone = 0;
        for (TCnemJobRow r : items) {
            int st = r.getStatus() == null ? ROW_TODO : r.getStatus().intValue();
            if (st != ROW_DONE) {
                continue;
            }
            if (compensateOne(r, now, oper)) {
                back++;
            } else {
                // 这台冲账没冲成 → 仍算做成未收回，作业留在回滚中，可再递一轮接着收
                stillDone++;
            }
        }

        // 做成的台全部收回才封口为已回滚；一台没收完就停在回滚中（此时不许再往前做）
        int finalStatus = stillDone == 0 ? ST_ROLLED : ST_ROLLING;
        this.cnemJobMapper.update(null, new UpdateWrapper<TCnemJob>()
                .eq("job_no", jobNo).eq("row_no", 0).eq("del_flag", 0)
                .eq("status", Integer.valueOf(ST_ROLLING))
                .set("status", Integer.valueOf(finalStatus))
                .set("eff_end", now));
        return back;
    }

    // ---------------------------------------------------------------------
    // 内部口径
    // ---------------------------------------------------------------------

    private List<TCnemJobRow> listRows(String jobNo) {
        return this.jobRowMapper.selectList(new QueryWrapper<TCnemJobRow>()
                .eq("job_no", jobNo).eq("del_flag", 0).orderByAsc("row_no"));
    }

    /** 系统发作业号：J + 雪花号 */
    private static String nextJobNo() {
        return "J" + ID_WORKER.nextId();
    }

    /**
     * 做之前先看判据。返回 null=可以做成；非空=没做成的原因。
     * 口径：金额缺 → 行不完整；version(判据编号) 空 → 老行缺项放行（等级留空，不回退）；
     * 写了判据但撤下/过期/册上没有 → 判据这边出了状况。
     */
    private String judge(TCnemJobRow r, Date at) {
        if (r.getAmount() == null) {
            return "行数据不完整:金额缺";
        }
        String code = r.getVersion();
        if (code == null || code.trim().isEmpty()) {
            // 存量老行：判据版本没写，照缺项口径接着走，等级留空
            return null;
        }
        if (r.getJobQty() == null) {
            return "行数据不完整:数量缺";
        }
        TCnemInspRule rule = pickRule(code.trim(), at);
        if (rule == null) {
            return "判据这边出了状况:已撤下/已过期/册上没有(" + code + ")";
        }
        BigDecimal input = new BigDecimal(r.getJobQty().intValue());
        int level = this.ruleService.evaluateById(rule.getId(), input, at);
        if (level == 0) {
            return "判据这边出了状况:判档不成立(" + code + ")";
        }
        r.setRuleId(rule.getId());
        r.setResultLevel(Integer.valueOf(level));
        return null;
    }

    /** 同一刻几条判据都适用：优先级降序、ruleCode 降序，取头一条；册上只留在用且在生效窗口内的 */
    private TCnemInspRule pickRule(String ruleCode, Date at) {
        for (TCnemInspRule rule : this.ruleService.listAvailable(at)) {
            if (ruleCode.equals(rule.getRuleCode())) {
                return rule;
            }
        }
        return null;
    }

    /** 没做成：行留在册上，写明没做成、为什么；不推翻整批（独立语句即落） */
    private void markFail(TCnemJobRow r, String reason) {
        this.jobRowMapper.update(null, new UpdateWrapper<TCnemJobRow>()
                .eq("id", r.getId()).eq("del_flag", 0)
                .in("status", Integer.valueOf(ROW_TODO), Integer.valueOf(ROW_FAIL))
                .set("status", Integer.valueOf(ROW_FAIL))
                .set("fail_reason", reason));
        r.setStatus(Integer.valueOf(ROW_FAIL));
        r.setFailReason(reason);
    }

    /**
     * 一台做成：四处同一时刻落定，同在一个 REQUIRES_NEW 事务里，少一处整体回滚 → 这台不算做成。
     * ①结论行 CAS 待做/没做成→已做成 ②台账落生效一条 ③收费流水落一笔 ④操作留痕一笔。
     */
    private boolean settleOne(final TCnemJobRow r, final Date now, final String oper) {
        try {
            this.txNew.execute(status -> {
                // 行条件占位：同刻两笔只有一笔能把待做/没做成翻成已做成，后到者覆不掉
                int claimed = this.jobRowMapper.update(null, new UpdateWrapper<TCnemJobRow>()
                        .eq("id", r.getId()).eq("del_flag", 0)
                        .in("status", Integer.valueOf(ROW_TODO), Integer.valueOf(ROW_FAIL))
                        .set("status", Integer.valueOf(ROW_DONE))
                        .set("writeback_flag", Integer.valueOf(1))
                        .set("version_status", Integer.valueOf(1))
                        .set("rule_id", r.getRuleId())
                        .set("result_level", r.getResultLevel())
                        .set("fail_reason", null));
                if (claimed == 0) {
                    throw new IllegalStateException("行已被先到一笔定住: rowNo=" + r.getRowNo());
                }

                // ②台账落一条（uk_row_action：同刻重入直接判重复，事务回滚）
                TCnemJobLedger ledger = newLedger(r, ACT_EFFECT, now, oper, null);
                this.ledgerMapper.insert(ledger);

                // ③收费流水落一笔（落定后回收入账时改状态，不删）
                TCnemFeeRow fee = newFeeRow(r, now);
                this.feeRowMapper.insert(fee);
                ledger.setFeeRowId(fee.getId());
                this.ledgerMapper.updateById(ledger);

                // ④谁动的、什么时候动的
                writeOperLog(oper, now, "effect", r);
                return Integer.valueOf(1);
            });
            r.setStatus(Integer.valueOf(ROW_DONE));
            r.setWritebackFlag(Integer.valueOf(1));
            return true;
        } catch (DuplicateKeyException e) {
            // 同刻并发：台账唯一键拦住第二道，后到者不覆
            return false;
        } catch (RuntimeException e) {
            markFail(r, "四处没齐,这台不算做成:" + rootMsg(e));
            return false;
        }
    }

    /**
     * 一台收回：补一道冲账流水，把落过的地方一处一处冲回来，不删结论行。
     * 同一台收过只补一道：行 CAS(已做成且未补偿) + 台账 uk_row_action 双保险。
     */
    private boolean compensateOne(final TCnemJobRow r, final Date now, final String oper) {
        try {
            this.txNew.execute(status -> {
                int claimed = this.jobRowMapper.update(null, new UpdateWrapper<TCnemJobRow>()
                        .eq("id", r.getId()).eq("del_flag", 0)
                        .eq("status", Integer.valueOf(ROW_DONE))
                        .and(w -> w.eq("compensated", 0).or().isNull("compensated"))
                        .set("status", Integer.valueOf(ROW_BACK))
                        .set("compensated", Integer.valueOf(1))
                        .set("compensate_flag", Integer.valueOf(1))
                        .set("compensate_cnt", Integer.valueOf(1)));
                if (claimed == 0) {
                    throw new IllegalStateException("该行已收过,冲账不写第二道: rowNo=" + r.getRowNo());
                }

                // 冲账台账一道（不是删生效那条）
                this.ledgerMapper.insert(newLedger(r, ACT_BACK, now, oper, null));

                // 收费流水冲回：已结算 → 已退回（同一笔改状态，不另立不删除）
                this.feeRowMapper.update(null, new UpdateWrapper<TCnemFeeRow>()
                        .eq("item_no", feeItemNo(r)).eq("del_flag", 0)
                        .eq("status", Integer.valueOf(FEE_SETTLED))
                        .set("status", Integer.valueOf(FEE_RETURNED)));

                writeOperLog(oper, now, "rollback", r);
                return Integer.valueOf(1);
            });
            r.setStatus(Integer.valueOf(ROW_BACK));
            return true;
        } catch (DuplicateKeyException e) {
            return false;
        } catch (RuntimeException e) {
            return false;
        }
    }

    private TCnemJobLedger newLedger(TCnemJobRow r, int action, Date now, String oper, Long feeRowId) {
        TCnemJobLedger l = new TCnemJobLedger();
        l.setJobNo(r.getJobNo());
        l.setRowNo(r.getRowNo());
        l.setAction(Integer.valueOf(action));
        l.setInspNo(r.getInspNo());
        l.setResultLevel(r.getResultLevel());
        l.setAmount(r.getAmount());
        l.setFeeRowId(feeRowId);
        l.setOperBy(oper);
        l.setOperAt(now);
        l.setDelFlag(Integer.valueOf(0));
        return l;
    }

    private TCnemFeeRow newFeeRow(TCnemJobRow r, Date now) {
        // 收费档取在册启用行；没有收费档则四处缺一处，事务回滚、这台判没做成
        TCnemFeeBase base = this.feeBaseMapper.selectOne(new QueryWrapper<TCnemFeeBase>()
                .eq("del_flag", 0).eq("status", 0).orderByAsc("id").last("limit 1"));
        if (base == null) {
            throw new IllegalStateException("收费档档案不可用");
        }
        TCnemFeeRow fee = new TCnemFeeRow();
        fee.setItemNo(feeItemNo(r));
        fee.setDueAt(now);
        fee.setAmount(r.getAmount());
        fee.setBaseId(base.getId());
        fee.setGradeCode(base.getGradeCode());
        fee.setStatus(Integer.valueOf(FEE_SETTLED));
        fee.setDelFlag(Integer.valueOf(0));
        return fee;
    }

    /** 收费流水条目编号：作业号-行次，天然对回同一台，重入可幂等查到 */
    private static String feeItemNo(TCnemJobRow r) {
        return r.getJobNo() + "-" + r.getRowNo();
    }

    private void writeOperLog(String oper, Date now, String action, TCnemJobRow r) {
        TSysOperLog log = new TSysOperLog();
        log.setTitle("检验结论作业");
        log.setMethod("TCnemJobServiceImpl." + action + "Job");
        log.setOperName(oper);
        log.setOperParam("jobNo=" + r.getJobNo() + ",rowNo=" + r.getRowNo()
                + ",inspNo=" + r.getInspNo() + ",level=" + r.getResultLevel());
        log.setOperTime(now);
        this.operLogMapper.insert(log);
    }

    /** 定时/无会话场景取操作人：取不到落 system，绝不因没有登录上下文把账写炸 */
    private static String operator() {
        try {
            return ShiroUtils.getLoginName();
        } catch (RuntimeException e) {
            return "system";
        }
    }

    private static String rootMsg(RuntimeException e) {
        Throwable c = e;
        while (c.getCause() != null && c.getCause() != c) {
            c = c.getCause();
        }
        String m = c.getMessage();
        return m == null ? c.getClass().getSimpleName() : m;
    }
}
