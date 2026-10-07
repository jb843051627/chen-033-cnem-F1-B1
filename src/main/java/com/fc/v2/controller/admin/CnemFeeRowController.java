package com.fc.v2.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.common.base.BaseController;
import com.fc.v2.common.domain.AjaxResult;
import com.fc.v2.common.domain.ResultTable;
import com.fc.v2.common.log.Log;
import com.fc.v2.model.auto.TCnemFeeRow;
import com.fc.v2.service.ITCnemFeeRowService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

/**
 * 检验费结算条目 Controller
 *
 * @author fuce
 * @date 2026-09-12
 */
@Api(value = "检验费结算条目")
@Controller
@RequestMapping("/CnemFeeRowController")
public class CnemFeeRowController extends BaseController {

    private final String prefix = "admin/cnemFeeRow";

    @Autowired
    private ITCnemFeeRowService cnemFeeRowService;

    @ApiOperation(value = "分页跳转", notes = "分页跳转")
    @GetMapping("/view")
    @RequiresPermissions("cnem:cnemFeeRow:view")
    public String view(ModelMap model) {
        return prefix + "/list";
    }

    @Log(title = "检验费结算条目集合查询", action = "list")
    @ApiOperation(value = "分页查询", notes = "分页查询")
    @GetMapping("/list")
    @RequiresPermissions("cnem:cnemFeeRow:list")
    @ResponseBody
    public ResultTable list(TCnemFeeRow record) {
        QueryWrapper<TCnemFeeRow> queryWrapper = new QueryWrapper<TCnemFeeRow>();
        startPage();
        com.github.pagehelper.PageInfo<TCnemFeeRow> page =
                new com.github.pagehelper.PageInfo<TCnemFeeRow>(cnemFeeRowService.selectTCnemFeeRowList(queryWrapper));
        return pageTable(page.getList(), page.getTotal());
    }

    @Log(title = "检验费结算条目新增", action = "add")
    @ApiOperation(value = "新增", notes = "新增")
    @PostMapping("/add")
    @RequiresPermissions("cnem:cnemFeeRow:add")
    @ResponseBody
    public AjaxResult add(TCnemFeeRow record) {
        return toAjax(cnemFeeRowService.insertTCnemFeeRow(record));
    }

    @Log(title = "检验费结算条目修改", action = "edit")
    @ApiOperation(value = "修改保存", notes = "修改保存")
    @PostMapping("/edit")
    @RequiresPermissions("cnem:cnemFeeRow:edit")
    @ResponseBody
    public AjaxResult editSave(TCnemFeeRow record) {
        return toAjax(cnemFeeRowService.updateTCnemFeeRow(record));
    }

    @Log(title = "检验费结算条目删除", action = "remove")
    @ApiOperation(value = "删除", notes = "删除")
    @DeleteMapping("/remove")
    @RequiresPermissions("cnem:cnemFeeRow:remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        return toAjax(cnemFeeRowService.deleteTCnemFeeRowByIds(ids));
    }
}
