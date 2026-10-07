package com.fc.v2.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.common.base.BaseController;
import com.fc.v2.common.domain.AjaxResult;
import com.fc.v2.common.domain.ResultTable;
import com.fc.v2.common.log.Log;
import com.fc.v2.model.auto.TCnemTraceRow;
import com.fc.v2.service.ITCnemTraceRowService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

/**
 * 隐患整改处置条目 Controller（state-machine 形状：流转入口）
 *
 * @author fuce
 * @date 2026-09-14
 */
@Api(value = "隐患整改处置条目")
@Controller
@RequestMapping("/cnemTraceRow")
public class CnemTraceRowController extends BaseController {

    private final String prefix = "admin/cnemTraceRow";

    @Autowired
    private ITCnemTraceRowService cnemTraceRowService;

    @ApiOperation(value = "流转台账跳转", notes = "流转台账跳转")
    @GetMapping("/view")
    @RequiresPermissions("cnemTraceRow:view")
    public String view(ModelMap model) {
        return prefix + "/list";
    }

    @Log(title = "隐患整改处置条目流转台账", action = "list")
    @ApiOperation(value = "流转台账", notes = "流转台账")
    @GetMapping("/list")
    @RequiresPermissions("cnemTraceRow:list")
    @ResponseBody
    public ResultTable list(TCnemTraceRow record) {
        QueryWrapper<TCnemTraceRow> queryWrapper = new QueryWrapper<TCnemTraceRow>();
        startPage();
        com.github.pagehelper.PageInfo<TCnemTraceRow> page =
                new com.github.pagehelper.PageInfo<TCnemTraceRow>(cnemTraceRowService.selectTCnemTraceRowList(queryWrapper));
        return pageTable(page.getList(), page.getTotal());
    }

    @Log(title = "隐患整改处置条目推进", action = "advance")
    @ApiOperation(value = "推进一档", notes = "推进一档")
    @PostMapping("/advance")
    @RequiresPermissions("cnemTraceRow:advance")
    @ResponseBody
    public AjaxResult advance(Long id, String remark) {
        return toAjax(cnemTraceRowService.advance(id, remark) != null ? 1 : 0);
    }

    @Log(title = "隐患整改处置条目回退", action = "rollback")
    @ApiOperation(value = "回退一档", notes = "回退一档")
    @PostMapping("/rollback")
    @RequiresPermissions("cnemTraceRow:rollback")
    @ResponseBody
    public AjaxResult rollback(Long id, String remark) {
        return toAjax(cnemTraceRowService.rollback(id, remark) != null ? 1 : 0);
    }
}
