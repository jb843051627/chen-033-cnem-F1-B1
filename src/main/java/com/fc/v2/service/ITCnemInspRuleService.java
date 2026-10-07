package com.fc.v2.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.fc.v2.model.auto.TCnemInspRule;

/**
 * 检验结论判定规则 Service接口（rule-eval 形状：规则求值，无增删改）
 *
 * @author fuce
 * @date 2026-09-14
 */
public interface ITCnemInspRuleService {

    /** 按主键查询规则 */
    TCnemInspRule selectTCnemInspRuleById(Long id);

    /**
     * 单规则求值：按 ruleCode 在 at 时刻生效的版本判档（1..4）。
     * 无生效版本 / 输入越界 / 参数缺失一律返回 0。
     */
    int evaluate(String ruleCode, BigDecimal input, Date at);

    /**
     * 按判据主键判档（作业行各指一条判据的入口）。
     * 判据册上没有 / 已撤下 / 已过期一律返回 0（调用方按「判据这边出了状况」记行失败）；
     * input 为 null（老行缺项）时不判档，调用方可按缺项口径放行并留空等级。
     */
    int evaluateById(Long ruleId, BigDecimal input, Date at);

    /** 多规则求值：at 时刻可用规则中优先级最高者的档位；无可用规则返回 0 */
    int evaluateTop(BigDecimal input, Date at);

    /** at 时刻可用规则（优先级降序、ruleCode 降序） */
    List<TCnemInspRule> listAvailable(Date at);

    /** 单条规则在 at 时刻是否可用（批量导入/报表等旁路复用，口径须与定位/列表一致） */
    boolean usable(Long id, Date at);

    /** at 时刻可用规则条数（列表页角标 / 看板） */
    int countAvailable(Date at);
}
