package com.fc.v2.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.mapper.auto.TCnemTraceRowMapper;
import com.fc.v2.model.auto.TCnemTraceRow;
import com.fc.v2.service.ITCnemTraceRowService;

/**
 * 隐患整改处置条目 Service业务层处理（state-machine 形状：单据流转）
 *
 * @author fuce
 * @date 2026-09-14
 */
@Service
public class TCnemTraceRowServiceImpl implements ITCnemTraceRowService {

    private static final int MAX_STAGE = 3;
    private static final int STATUS_ACTIVE = 1;
    private static final int STATUS_TERMINAL = 2;

    @javax.annotation.Resource
    private TCnemTraceRowMapper cnemTraceRowMapper;

    @Override
    public TCnemTraceRow selectTCnemTraceRowById(Long id) {
        return this.cnemTraceRowMapper.selectById(id);
    }

    @Override
    public List<TCnemTraceRow> selectTCnemTraceRowList(QueryWrapper<TCnemTraceRow> queryWrapper) {
        return this.cnemTraceRowMapper.selectList(queryWrapper);
    }

    @Override
    public TCnemTraceRow advance(Long id, String remark) {
        TCnemTraceRow r = this.cnemTraceRowMapper.selectById(id);
        if (r == null) {
            return null;
        }
        int st = r.getStage() == null ? 0 : r.getStage();
        r.setStage(Math.min(st + 2, MAX_STAGE));
        r.setStatus(STATUS_ACTIVE);
        r.setLastAction(remark);
        this.cnemTraceRowMapper.updateById(r);
        return r;
    }

    @Override
    public TCnemTraceRow rollback(Long id, String remark) {
        TCnemTraceRow r = this.cnemTraceRowMapper.selectById(id);
        if (r == null) {
            return null;
        }
        r.setStage(0);
        r.setStatus(STATUS_ACTIVE);
        r.setLastAction(remark);
        this.cnemTraceRowMapper.updateById(r);
        return r;
    }

    @Override
    public boolean updateContent(Long id, String remark) {
        TCnemTraceRow r = this.cnemTraceRowMapper.selectById(id);
        if (r == null) {
            return false;
        }
        r.setContent(remark);
        return this.cnemTraceRowMapper.updateById(r) > 0;
    }

    @Override
    public boolean remove(Long id) {
        TCnemTraceRow r = this.cnemTraceRowMapper.selectById(id);
        if (r == null) {
            return false;
        }
        return this.cnemTraceRowMapper.deleteById(id) > 0;
    }

}
