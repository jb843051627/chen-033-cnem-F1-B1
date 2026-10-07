package com.fc.v2.service;

import com.fc.v2.model.auto.TCnemRegRule;

/**
 * 使用登记核准基准 Service接口（approval-chain 形状：多阶段签批，无增删改查入口）
 *
 * @author fuce
 * @date 2026-09-14
 */
public interface ITCnemRegRuleService {

    /** 按主键回查单据 */
    TCnemRegRule selectTCnemRegRuleById(Long id);

    /** 签批一票：返回更新后的单据；被拒返回 null */
    TCnemRegRule approve(Long id, String approver, String comment);

    /** 否决：返回更新后的单据；被拒返回 null */
    TCnemRegRule reject(Long id, String approver, String comment);

    /** 退回上一环节：返回更新后的单据；被拒返回 null */
    TCnemRegRule rollback(Long id, String comment);
}
