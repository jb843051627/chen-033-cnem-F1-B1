package com.fc.v2.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.fc.v2.model.auto.TCnemFeeRow;

import java.util.List;

/**
 * 检验费结算条目 Service接口
 *
 * @author fuce
 * @date 2026-09-12
 */
public interface ITCnemFeeRowService {

    /** 按主键查询 */
    TCnemFeeRow selectTCnemFeeRowById(Long id);

    /** 按条件查询列表（分页由调用方统一处理） */
    List<TCnemFeeRow> selectTCnemFeeRowList(Wrapper<TCnemFeeRow> queryWrapper);

    /** 新增 */
    int insertTCnemFeeRow(TCnemFeeRow record);

    /** 修改 */
    int updateTCnemFeeRow(TCnemFeeRow record);

    /** 批量删除 */
    int deleteTCnemFeeRowByIds(String ids);

    /** 按主键删除 */
    int deleteTCnemFeeRowById(Long id);
}
