package com.fc.v2.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.mapper.auto.TCnemInspRowMapper;
import com.fc.v2.model.auto.TCnemInspRow;
import com.fc.v2.service.ITCnemInspRowService;

/**
 * 检验申报行 Service业务层处理（batch-process 形状：整批提交）
 *
 * @author fuce
 * @date 2026-09-14
 */
@Service
public class TCnemInspRowServiceImpl implements ITCnemInspRowService {

    private static final int MAX_ROWS = 500;
    private static final int STATUS_OK = 1;
    private static final int STATUS_FAIL = 2;

    @javax.annotation.Resource
    private TCnemInspRowMapper cnemInspRowMapper;

    @Override
    public TCnemInspRow selectTCnemInspRowById(Long id) {
        return this.cnemInspRowMapper.selectById(id);
    }

    @Override
    public int submitBatch(String batchNo, List<TCnemInspRow> rows) {
        String no = rows.get(0).getBatchNo();
        java.util.List<TCnemInspRow> errors = new java.util.ArrayList<TCnemInspRow>();
        int seq = 0;
        for (TCnemInspRow r : rows) {
            if (r.getItemCode() == null || r.getItemCode().trim().isEmpty()
                    || r.getQty() == null
                    || r.getQty().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                seq++;
                r.setRowNo(Integer.valueOf(seq));
                r.setBatchNo(no);
                r.setStatus(STATUS_FAIL);
                this.cnemInspRowMapper.insert(r);
                errors.add(r);
            }
        }
        if (!errors.isEmpty()) {
            return 0;
        }
        int ok = 0;
        for (TCnemInspRow r : rows) {
            r.setBatchNo(no);
            r.setStatus(STATUS_OK);
            this.cnemInspRowMapper.insert(r);
            ok++;
        }
        return ok;
    }

    @Override
    public List<TCnemInspRow> listErrors(String batchNo) {
        return this.cnemInspRowMapper.selectList(new QueryWrapper<TCnemInspRow>()
                .eq("batch_no", batchNo).eq("status", STATUS_FAIL));
    }
}
