package com.fc.v2.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.mapper.auto.TCnemInspRuleMapper;
import com.fc.v2.model.auto.TCnemInspRule;
import com.fc.v2.service.ITCnemInspRuleService;

/**
 * 检验结论判定规则 Service业务层处理（rule-eval 形状）
 *
 * 口径（册上说法）：判据撤下(status=1)、过期(eff_end 不含已过)、册上没有，都不认；
 * 几条同时适用时，优先级(priority)降序、ruleCode 降序取头一条。
 *
 * @author fuce
 * @date 2026-09-14
 */
@Service
public class TCnemInspRuleServiceImpl implements ITCnemInspRuleService {

    /** 优先级降序；并列按 ruleCode 降序（确定性） */
    private static final Comparator<TCnemInspRule> PRIORITY_THEN_CODE_DESC = new Comparator<TCnemInspRule>() {
        @Override
        public int compare(TCnemInspRule a, TCnemInspRule b) {
            int pa = a.getPriority() == null ? 0 : a.getPriority();
            int pb = b.getPriority() == null ? 0 : b.getPriority();
            if (pa != pb) {
                return pb - pa;
            }
            String ca = a.getRuleCode() == null ? "" : a.getRuleCode();
            String cb = b.getRuleCode() == null ? "" : b.getRuleCode();
            return cb.compareTo(ca);
        }
    };

    @javax.annotation.Resource
    private TCnemInspRuleMapper cnemInspRuleMapper;

    @Override
    public TCnemInspRule selectTCnemInspRuleById(Long id) {
        return this.cnemInspRuleMapper.selectById(id);
    }

    @Override
    public List<TCnemInspRule> listAvailable(Date at) {
        if (at == null) {
            return new ArrayList<TCnemInspRule>();
        }
        // 时段过滤放 Java 侧：JDBC serverTimezone 与本机不对称，Date 直接作绑定参数会整体错档
        List<TCnemInspRule> all = this.cnemInspRuleMapper.selectList(
                new QueryWrapper<TCnemInspRule>().eq("del_flag", 0));
        List<TCnemInspRule> avail = new ArrayList<TCnemInspRule>();
        for (TCnemInspRule r : all) {
            if (inForce(r, at)) {
                avail.add(r);
            }
        }
        Collections.sort(avail, PRIORITY_THEN_CODE_DESC);
        return avail;
    }

    @Override
    public int evaluate(String ruleCode, BigDecimal input, Date at) {
        if (ruleCode == null || ruleCode.trim().isEmpty() || input == null || at == null) {
            return 0;
        }
        if (input.compareTo(BigDecimal.ZERO) < 0) {
            return 0;
        }
        for (TCnemInspRule r : listAvailable(at)) {
            if (ruleCode.trim().equals(r.getRuleCode())) {
                return levelOf(r, input);
            }
        }
        // 撤下/过期/册上没有 → 0（调用方按「判据这边出了状况」记）
        return 0;
    }

    @Override
    public int evaluateTop(BigDecimal input, Date at) {
        if (input == null || at == null || input.compareTo(BigDecimal.ZERO) < 0) {
            return 0;
        }
        List<TCnemInspRule> avail = listAvailable(at);
        if (avail.isEmpty()) {
            return 0;
        }
        // 几条同时适用：listAvailable 已按优先级降序、ruleCode 降序排定，取头一条
        return levelOf(avail.get(0), input);
    }

    @Override
    public int evaluateById(Long ruleId, BigDecimal input, Date at) {
        if (ruleId == null || input == null || at == null) {
            return 0;
        }
        if (input.compareTo(BigDecimal.ZERO) < 0) {
            return 0;
        }
        TCnemInspRule r = this.cnemInspRuleMapper.selectById(ruleId);
        if (!inForce(r, at)) {
            return 0;
        }
        return levelOf(r, input);
    }

    @Override
    public boolean usable(Long id, Date at) {
        if (id == null || at == null) {
            return false;
        }
        TCnemInspRule r = this.cnemInspRuleMapper.selectById(id);
        return inForce(r, at);
    }

    @Override
    public int countAvailable(Date at) {
        if (at == null) {
            return 0;
        }
        return listAvailable(at).size();
    }

    /** 在册在用：未删、未撤下、在 [eff_start, eff_end) 窗口内；边界空端不限 */
    private static boolean inForce(TCnemInspRule r, Date at) {
        if (r == null) {
            return false;
        }
        if (r.getDelFlag() != null && r.getDelFlag().intValue() != 0) {
            return false;
        }
        if (r.getStatus() != null && r.getStatus().intValue() == 1) {
            return false;
        }
        if (r.getEffStart() != null && at.before(r.getEffStart())) {
            return false;
        }
        if (r.getEffEnd() != null && !at.before(r.getEffEnd())) {
            return false;
        }
        return true;
    }

    /** 分档：等于上限取高一档 */
    private int levelOf(TCnemInspRule rule, BigDecimal input) {
        if (rule.getTh1Max() != null && input.compareTo(rule.getTh1Max()) <= 0) {
            return 1;
        }
        if (rule.getTh2Max() != null && input.compareTo(rule.getTh2Max()) <= 0) {
            return 2;
        }
        if (rule.getTh3Max() != null && input.compareTo(rule.getTh3Max()) <= 0) {
            return 3;
        }
        return 4;
    }
}
