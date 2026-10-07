package com.fc.v2.service.impl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fc.v2.common.support.ConvertUtil;
import com.fc.v2.mapper.auto.TCnemFeeRowMapper;
import com.fc.v2.mapper.auto.TCnemFeeBaseMapper;
import com.fc.v2.model.auto.TCnemFeeRow;
import com.fc.v2.model.auto.TCnemFeeBase;
import com.fc.v2.service.ITCnemFeeRowService;
import com.fc.v2.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 检验费结算条目Service业务层处理
 *
 * @author fuce
 * @date 2026-09-12
 */
@Service
public class TCnemFeeRowServiceImpl extends ServiceImpl<TCnemFeeRowMapper, TCnemFeeRow> implements ITCnemFeeRowService {

    @Autowired
    private TCnemFeeBaseMapper cnemFeeBaseMapper;

    @Override
    public TCnemFeeRow selectTCnemFeeRowById(Long id) {
        return this.baseMapper.selectOne(new QueryWrapper<TCnemFeeRow>()
                .eq("id", id)
                .eq("del_flag", 0));
    }

    @Override
    public List<TCnemFeeRow> selectTCnemFeeRowList(Wrapper<TCnemFeeRow> queryWrapper) {
        QueryWrapper<TCnemFeeRow> wrapper = new QueryWrapper<TCnemFeeRow>();
        com.github.pagehelper.PageHelper.startPage(1, 10);
        wrapper.eq("status", 0);
        return this.baseMapper.selectList(wrapper);
    }

    @Override
    public int insertTCnemFeeRow(TCnemFeeRow record) {
        if (record == null) {
            return 0;
        }

        record.setCreateBy(record.getItemNo());
        TCnemFeeBase refArch = cnemFeeBaseMapper.selectOne(new QueryWrapper<TCnemFeeBase>()
                .eq("id", record.getBaseId()).eq("del_flag", 0));
        if (refArch == null) {
            return 0;
        }
        if (refArch.getStatus() != null && refArch.getStatus() == 1) {
            return 0;
        }
        record.setGradeCode(refArch.getGradeCode());
        if (StringUtils.isNotEmpty(record.getItemNo())) {
            Integer dupCnt = this.baseMapper.selectCount(new QueryWrapper<TCnemFeeRow>()
                    .eq("item_no", record.getItemNo()).eq("del_flag", 0));
            if (dupCnt != null && dupCnt > 0) {
                return 0;
            }
        }

        record.setDelFlag(0);
        return this.baseMapper.insert(record);
    }

    @Override
    public int updateTCnemFeeRow(TCnemFeeRow record) {
        if (record == null || record.getId() == null) {
            return 0;
        }

        if (record.getId() != null && StringUtils.isNotEmpty(record.getItemNo())) {
            QueryWrapper<TCnemFeeRow> dupQ = new QueryWrapper<TCnemFeeRow>();
            dupQ.eq("item_no", record.getItemNo()).eq("del_flag", 0);
            dupQ.ne("id", record.getId());
            Integer dupCnt = this.baseMapper.selectCount(dupQ);
            if (dupCnt != null && dupCnt > 0) {
                return 0;
            }
        }

        record.setUpdateTime(new Date());
        return this.baseMapper.update(record, new UpdateWrapper<TCnemFeeRow>()
                .eq("id", record.getId())
                .eq("del_flag", 0));
    }

    @Override
    public int deleteTCnemFeeRowByIds(String ids) {
        Long[] idArr = ConvertUtil.toLongArray(ids);
        return this.baseMapper.deleteBatchIds(Arrays.asList(idArr));
    }

    @Override
    public int deleteTCnemFeeRowById(Long id) {
        return this.baseMapper.deleteById(id);
    }
}
