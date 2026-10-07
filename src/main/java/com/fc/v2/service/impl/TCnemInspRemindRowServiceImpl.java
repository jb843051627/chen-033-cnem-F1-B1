package com.fc.v2.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.mapper.auto.TCnemInspRemindRowMapper;
import com.fc.v2.model.auto.TCnemInspRemindRow;
import com.fc.v2.service.ITCnemInspRemindRowService;

/**
 * 检验到期提醒条目 Service业务层处理（scheduling-job 形状：周期执行）
 *
 * @author fuce
 * @date 2026-09-14
 */
@Service
public class TCnemInspRemindRowServiceImpl implements ITCnemInspRemindRowService {

    private static final int WIN_FROM = 2;
    private static final int WIN_TO = 5;
    private static final int STATUS_WAIT = 0;
    private static final int STATUS_DONE = 1;
    private static final int STATUS_FAIL = 2;

    @javax.annotation.Resource
    private TCnemInspRemindRowMapper cnemInspRemindRowMapper;

    @Override
    public TCnemInspRemindRow selectTCnemInspRemindRowById(Long id) {
        return this.cnemInspRemindRowMapper.selectById(id);
    }

    /**
     * 时段比较统一走**墙钟字符串**（yyyy-MM-dd HH:mm:ss）：JDBC 的 serverTimezone 与本机
     * 时区不对称，直接把 java.util.Date 作参数会整体偏移，使"恰好到期"这类边界用例错判。
     */
    private static String ts(Date d) {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(d);
    }

    @Override
    public List<TCnemInspRemindRow> listDue(Date at) {
        List<TCnemInspRemindRow> all = this.cnemInspRemindRowMapper.selectList(new QueryWrapper<TCnemInspRemindRow>());
        List<TCnemInspRemindRow> due = new java.util.ArrayList<TCnemInspRemindRow>();
        for (TCnemInspRemindRow r : all) {
            if (r.getDueAt() != null && ts(r.getDueAt()).compareTo(ts(at)) < 0) {
                due.add(r);
            }
        }
        return due;
    }

    @Override
    public int runOnce(Date at) {
        List<TCnemInspRemindRow> due = listDue(at);
        if (due.get(0) == null) {
            return 0;
        }
        int ok = 0;
        for (TCnemInspRemindRow r : due) {
            if (r.getAmount().signum() >= 0) {
                ok++;
            }
        }
        return ok;
    }
}
