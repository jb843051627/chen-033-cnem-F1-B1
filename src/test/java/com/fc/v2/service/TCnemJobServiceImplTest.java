package com.fc.v2.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.fc.v2.mapper.auto.TCnemFeeBaseMapper;
import com.fc.v2.mapper.auto.TCnemFeeRowMapper;
import com.fc.v2.mapper.auto.TCnemInspRuleMapper;
import com.fc.v2.mapper.auto.TCnemJobLedgerMapper;
import com.fc.v2.mapper.auto.TCnemJobMapper;
import com.fc.v2.mapper.auto.TCnemJobRowMapper;
import com.fc.v2.mapper.auto.TSysOperLogMapper;
import com.fc.v2.model.auto.TCnemFeeBase;
import com.fc.v2.model.auto.TCnemInspRule;
import com.fc.v2.model.auto.TCnemJob;
import com.fc.v2.model.auto.TCnemJobLedger;
import com.fc.v2.model.auto.TCnemJobRow;
import com.fc.v2.service.impl.TCnemInspRuleServiceImpl;
import com.fc.v2.service.impl.TCnemJobServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

/**
 * 检验结论作业口径单测（无 Spring 容器 / 无库：mapper 全 Mockito，事务直跑回调）
 *
 * @author fuce
 * @date 2026-10-07
 */
class TCnemJobServiceImplTest {

    private TCnemJobMapper jobMapper;
    private TCnemJobRowMapper rowMapper;
    private TCnemJobLedgerMapper ledgerMapper;
    private TCnemFeeBaseMapper feeBaseMapper;
    private TCnemFeeRowMapper feeRowMapper;
    private TSysOperLogMapper operLogMapper;
    private ITCnemInspRuleService ruleService;
    private TCnemJobServiceImpl svc;

    private static final int ST_PENDING = 1;
    private static final int ST_EFFECTED = 3;
    private static final int ST_PARTIAL = 4;
    private static final int ST_ROLLING = 5;
    private static final int ST_ROLLED = 6;

    private static final int ROW_TODO = 0;
    private static final int ROW_DONE = 1;
    private static final int ROW_FAIL = 2;
    private static final int ROW_BACK = 3;

    /** REQUIRES_NEW 的替身：直接在当前线程跑回调，不碰真实事务资源 */
    private static PlatformTransactionManager passthroughTx() {
        return new PlatformTransactionManager() {
            @Override
            public TransactionStatus getTransaction(TransactionDefinition definition) {
                return mock(TransactionStatus.class);
            }

            @Override
            public void commit(TransactionStatus status) {
            }

            @Override
            public void rollback(TransactionStatus status) {
            }
        };
    }

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        jobMapper = mock(TCnemJobMapper.class);
        rowMapper = mock(TCnemJobRowMapper.class);
        ledgerMapper = mock(TCnemJobLedgerMapper.class);
        feeBaseMapper = mock(TCnemFeeBaseMapper.class);
        feeRowMapper = mock(TCnemFeeRowMapper.class);
        operLogMapper = mock(TSysOperLogMapper.class);
        ruleService = mock(ITCnemInspRuleService.class);
        svc = new TCnemJobServiceImpl(jobMapper, rowMapper, ledgerMapper, feeBaseMapper,
                feeRowMapper, operLogMapper, ruleService, passthroughTx());

        // 行级条件更新默认占位成功；个别用例覆写
        when(rowMapper.update(any(), any(Wrapper.class))).thenReturn(1);
    }

    private TCnemJob header(int status) {
        TCnemJob h = new TCnemJob();
        h.setId(100L);
        h.setJobNo("J1");
        h.setRowNo(0);
        h.setStatus(Integer.valueOf(status));
        h.setDelFlag(0);
        return h;
    }

    private TCnemJobRow row(int rowNo, int status, String inspNo, String version, Integer qty, String amount) {
        TCnemJobRow r = new TCnemJobRow();
        r.setId((long) rowNo);
        r.setJobNo("J1");
        r.setRowNo(rowNo);
        r.setStatus(status);
        r.setInspNo(inspNo);
        r.setVersion(version);
        r.setJobQty(qty);
        r.setAmount(amount == null ? null : new BigDecimal(amount));
        r.setDelFlag(0);
        r.setCompensated(0);
        return r;
    }

    private TCnemJob payload(String inspNo, String version, Integer qty, String amount) {
        TCnemJob p = new TCnemJob();
        p.setInspNo(inspNo);
        p.setVersion(version);
        p.setJobQty(qty);
        p.setAmount(amount == null ? null : new BigDecimal(amount));
        return p;
    }

    private void stubFeeBase() {
        TCnemFeeBase base = new TCnemFeeBase();
        base.setId(1L);
        base.setGradeCode("GB01");
        base.setStatus(0);
        when(feeBaseMapper.selectOne(any(Wrapper.class))).thenReturn(base);
    }

    private void stubRule(String code, int level) {
        TCnemInspRule rule = new TCnemInspRule();
        rule.setId(7L);
        rule.setRuleCode(code);
        rule.setStatus(0);
        when(ruleService.listAvailable(any(Date.class))).thenReturn(new ArrayList<>(Arrays.asList(rule)));
        when(ruleService.evaluateById(any(), any(), any(Date.class))).thenReturn(level);
    }

    // ---- 提交 ----------------------------------------------------------

    @Test
    void submit_idempotent_sameJobNoDoesNotCreateAnotherHeader() {
        when(jobMapper.selectOne(any(Wrapper.class))).thenReturn(header(ST_PENDING));

        String no = svc.submitJob("J1", Arrays.asList(payload("I1", "IR01", 10, "100")));

        assertEquals("J1", no);
        verify(jobMapper, never()).insert(any(TCnemJob.class));
        verify(rowMapper, never()).insert(any(TCnemJobRow.class));
    }

    @Test
    void submit_systemAssignsJobNoAndSequentialRowNumbers() {
        when(jobMapper.selectOne(any(Wrapper.class))).thenReturn(null);

        String no = svc.submitJob(null, Arrays.asList(
                payload("I1", null, null, "100"),
                payload("I2", null, null, "200")));

        assertNotNull(no);
        assertTrue(no.startsWith("J"));
        ArgumentCaptor<TCnemJobRow> cap = ArgumentCaptor.forClass(TCnemJobRow.class);
        verify(rowMapper, times(2)).insert(cap.capture());
        List<TCnemJobRow> saved = cap.getAllValues();
        assertEquals(Integer.valueOf(1), saved.get(0).getRowNo());
        assertEquals(Integer.valueOf(2), saved.get(1).getRowNo());
        assertEquals(no, saved.get(0).getJobNo());
        assertEquals(Integer.valueOf(ROW_TODO), saved.get(0).getStatus());
    }

    // ---- 生效：终态守卫 / 并发 -----------------------------------------

    @Test
    void effect_alreadyEffected_isIgnored() {
        when(jobMapper.selectOne(any(Wrapper.class))).thenReturn(header(ST_EFFECTED));

        int done = svc.effectJob("J1");

        assertEquals(0, done);
        verify(jobMapper, never()).update(any(), any(Wrapper.class));
        verify(rowMapper, never()).selectList(any(Wrapper.class));
    }

    @Test
    void effect_concurrentSecondClaim_losesAndDoesNothing() {
        when(jobMapper.selectOne(any(Wrapper.class))).thenReturn(header(ST_PENDING));
        when(jobMapper.update(any(), any(Wrapper.class))).thenReturn(0); // CAS 占位失败

        assertEquals(0, svc.effectJob("J1"));
        verify(rowMapper, never()).selectList(any(Wrapper.class));
    }

    // ---- 生效：判据出状况不连坐 + 四处落定 ------------------------------

    @Test
    @SuppressWarnings("unchecked")
    void effect_ruleMissingRowFails_othersSettle_fourWrites() {
        when(jobMapper.selectOne(any(Wrapper.class))).thenReturn(header(ST_PENDING));
        when(jobMapper.update(any(), any(Wrapper.class))).thenReturn(1);
        stubRule("IR01", 1);
        stubFeeBase();

        // 行1：老行没写判据版本（缺项口径放行，等级留空）
        // 行2：指了一条册上没有/撤下的判据 → 这台没做成
        // 行3：金额缺 → 这台没做成
        List<TCnemJobRow> rows = Arrays.asList(
                row(1, ROW_TODO, "I1", null, null, "100"),
                row(2, ROW_TODO, "I2", "GONE", 10, "200"),
                row(3, ROW_TODO, "I3", "IR01", 10, null));
        // GONE 在可用判据里匹不到
        when(ruleService.listAvailable(any(Date.class))).thenAnswer(inv -> {
            TCnemInspRule rule = new TCnemInspRule();
            rule.setId(7L);
            rule.setRuleCode("IR01");
            rule.setStatus(0);
            return new ArrayList<>(Arrays.asList(rule));
        });
        when(rowMapper.selectList(any(Wrapper.class))).thenReturn(new ArrayList<>(rows));

        int done = svc.effectJob("J1");

        assertEquals(1, done);                                   // 只有行1做成
        verify(ledgerMapper, times(1)).insert(any(TCnemJobLedger.class));
        verify(feeRowMapper, times(1)).insert(any());
        verify(operLogMapper, times(1)).insert(any());
        // 行1 做成占位 1 次 + 行2、行3 各写一条"没做成+原因" 2 次
        verify(rowMapper, times(3)).update(any(), any(Wrapper.class));
        // 做成那台四处之一的台账：动作=生效，等级留空（老行缺项）
        ArgumentCaptor<TCnemJobLedger> cap = ArgumentCaptor.forClass(TCnemJobLedger.class);
        verify(ledgerMapper).insert(cap.capture());
        assertEquals(Integer.valueOf(1), cap.getValue().getAction());
        assertEquals(null, cap.getValue().getResultLevel());
        assertEquals("I1", cap.getValue().getInspNo());
    }

    @Test
    @SuppressWarnings("unchecked")
    void effect_fourthWriteMissing_rowNotCounted() {
        when(jobMapper.selectOne(any(Wrapper.class))).thenReturn(header(ST_PENDING));
        when(jobMapper.update(any(), any(Wrapper.class))).thenReturn(1);
        stubRule("IR01", 2);
        // 收费档档案不可用 → 四处缺一处
        when(feeBaseMapper.selectOne(any(Wrapper.class))).thenReturn(null);
        List<TCnemJobRow> rows = new ArrayList<>(Arrays.asList(
                row(1, ROW_TODO, "I1", "IR01", 10, "100")));
        when(rowMapper.selectList(any(Wrapper.class))).thenReturn(rows);

        int done = svc.effectJob("J1");

        assertEquals(0, done);                                  // 没齐 → 这台不算做成
        // 行先占位做成、四处缺一回滚后，又在册上补了一条"没做成"的原因
        verify(rowMapper, times(2)).update(any(), any(Wrapper.class));
    }

    // ---- 收回：冲账只写一道 --------------------------------------------

    @Test
    @SuppressWarnings("unchecked")
    void rollback_appendsOneReversal_andMarksReturned() {
        when(jobMapper.selectOne(any(Wrapper.class))).thenReturn(header(ST_EFFECTED));
        when(jobMapper.update(any(), any(Wrapper.class))).thenReturn(1);
        when(feeRowMapper.update(any(), any(Wrapper.class))).thenReturn(1);
        List<TCnemJobRow> rows = new ArrayList<>(Arrays.asList(
                row(1, ROW_DONE, "I1", null, null, "100")));
        when(rowMapper.selectList(any(Wrapper.class))).thenReturn(rows);

        int back = svc.rollbackJob("J1");

        assertEquals(1, back);
        ArgumentCaptor<TCnemJobLedger> cap = ArgumentCaptor.forClass(TCnemJobLedger.class);
        verify(ledgerMapper, times(1)).insert(cap.capture());
        assertEquals(Integer.valueOf(2), cap.getValue().getAction()); // 冲账，不是删生效那条
        verify(feeRowMapper, times(1)).update(any(), any(Wrapper.class)); // 已结算→已退回
        verify(operLogMapper, times(1)).insert(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void rollback_alreadyCompensatedRow_noSecondReversal() {
        when(jobMapper.selectOne(any(Wrapper.class))).thenReturn(header(ST_EFFECTED));
        when(jobMapper.update(any(), any(Wrapper.class))).thenReturn(1);
        // 行1 冲账占位成功；行2 已收过（CAS=0，模拟 compensated 已置 1）
        when(rowMapper.update(any(), any(Wrapper.class))).thenReturn(1, 0);
        List<TCnemJobRow> rows = new ArrayList<>(Arrays.asList(
                row(1, ROW_DONE, "I1", null, null, "100"),
                row(2, ROW_DONE, "I2", null, null, "200")));
        when(rowMapper.selectList(any(Wrapper.class))).thenReturn(rows);

        int back = svc.rollbackJob("J1");

        assertEquals(1, back);                                 // 只收回一台
        verify(ledgerMapper, times(1)).insert(any(TCnemJobLedger.class)); // 冲账只一道
        verify(operLogMapper, times(1)).insert(any());
    }

    @Test
    void rollback_rolledJob_isIgnored() {
        when(jobMapper.selectOne(any(Wrapper.class))).thenReturn(header(ST_ROLLED));

        assertEquals(0, svc.rollbackJob("J1"));
        verify(ledgerMapper, never()).insert(any(TCnemJobLedger.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void rollback_halfReceived_canReenterFromRolling() {
        // 上轮一台没冲成停在回滚中；本轮那台已是 ROW_BACK（跳过），全部收净 → 封口已回滚
        when(jobMapper.selectOne(any(Wrapper.class))).thenReturn(header(ST_ROLLING));
        when(jobMapper.update(any(), any(Wrapper.class))).thenReturn(1);
        List<TCnemJobRow> rows = new ArrayList<>(Arrays.asList(
                row(1, ROW_BACK, "I1", null, null, "100")));
        when(rowMapper.selectList(any(Wrapper.class))).thenReturn(rows);

        int back = svc.rollbackJob("J1");

        assertEquals(0, back);                                 // 本轮没有新收回的台
        verify(ledgerMapper, never()).insert(any(TCnemJobLedger.class));
        verify(jobMapper, times(2)).update(any(), any(Wrapper.class)); // 进入+封口
    }

    // ---- 判据口径 ------------------------------------------------------

    @Test
    void rule_withdrawnOrExpiredOrMissing_notRecognized() {
        TCnemInspRuleMapper mapper = mock(TCnemInspRuleMapper.class);
        TCnemInspRuleServiceImpl rules = new TCnemInspRuleServiceImpl();
        org.springframework.test.util.ReflectionTestUtils.setField(rules, "cnemInspRuleMapper", mapper);

        Date now = new Date();
        TCnemInspRule withdrawn = new TCnemInspRule();
        withdrawn.setId(1L);
        withdrawn.setRuleCode("IR01");
        withdrawn.setStatus(1);                                // 撤下
        TCnemInspRule expired = new TCnemInspRule();
        expired.setId(2L);
        expired.setRuleCode("IR02");
        expired.setStatus(0);
        expired.setEffStart(new Date(now.getTime() - 86400000L * 4000L));
        expired.setEffEnd(new Date(now.getTime() - 86400000L)); // 已过期(不含)
        when(mapper.selectList(any(Wrapper.class)))
                .thenReturn(new ArrayList<>(Arrays.asList(withdrawn, expired)));

        assertEquals(0, rules.listAvailable(now).size());
        assertEquals(0, rules.evaluate("IR01", new BigDecimal("10"), now));
        assertEquals(0, rules.evaluate("IR02", new BigDecimal("10"), now));
    }

    @Test
    void rule_priorityWins_thenCodeDesc() {
        TCnemInspRuleMapper mapper = mock(TCnemInspRuleMapper.class);
        TCnemInspRuleServiceImpl rules = new TCnemInspRuleServiceImpl();
        org.springframework.test.util.ReflectionTestUtils.setField(rules, "cnemInspRuleMapper", mapper);

        Date now = new Date();
        TCnemInspRule low = new TCnemInspRule();
        low.setId(1L);
        low.setRuleCode("IR01");
        low.setPriority(5);
        low.setStatus(0);
        low.setTh1Max(new BigDecimal("2000"));
        low.setTh2Max(new BigDecimal("8000"));
        low.setTh3Max(new BigDecimal("20000"));
        TCnemInspRule high = new TCnemInspRule();
        high.setId(2L);
        high.setRuleCode("IR09");
        high.setPriority(10);
        high.setStatus(0);
        high.setTh1Max(new BigDecimal("100"));
        high.setTh2Max(new BigDecimal("8000"));
        high.setTh3Max(new BigDecimal("20000"));
        when(mapper.selectList(any(Wrapper.class)))
                .thenReturn(new ArrayList<>(Arrays.asList(low, high)));

        // 优先级高者判档：500 在 high 里落 2 档，在 low 里落 1 档；取 high
        assertEquals(2, rules.evaluateTop(new BigDecimal("500"), now));
        assertEquals(2, rules.listAvailable(now).size());
        assertEquals("IR09", rules.listAvailable(now).get(0).getRuleCode());
    }
}
