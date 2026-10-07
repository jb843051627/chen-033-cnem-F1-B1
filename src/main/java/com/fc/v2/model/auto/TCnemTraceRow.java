package com.fc.v2.model.auto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 隐患整改处置条目对象 t_cnem_trace_row
 *
 * @author fuce
 * @date 2026-09-12
 */
@TableName("t_cnem_trace_row")
@ApiModel(value = "TCnemTraceRow", description = "隐患整改处置条目")
public class TCnemTraceRow implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键")
    private Long id;

    /** 条目编号 */
    @TableField("item_no")
    @ApiModelProperty(value = "条目编号")
    private String itemNo;

    /** 整改单号 */
    @TableField("biz_no")
    @ApiModelProperty(value = "整改单号")
    private String bizNo;

    /** 当前处置步次 */
    @TableField("stage")
    @ApiModelProperty(value = "当前处置步次")
    private Integer stage;

    /** 处置内容 */
    @TableField("content")
    @ApiModelProperty(value = "处置内容")
    private String content;

    /** 最近一次动作 */
    @TableField("last_action")
    @ApiModelProperty(value = "最近一次动作")
    private String lastAction;

    /** 整改费用(元) */
    @TableField("amount")
    @ApiModelProperty(value = "整改费用(元)")
    private BigDecimal amount;

    /** 处置方式 */
    @TableField("settle_type")
    @ApiModelProperty(value = "处置方式")
    private String settleType;

    /** 使用单位 */
    @TableField("unit_name")
    @ApiModelProperty(value = "使用单位")
    private String unitName;

    /** 条目状态 0在办 1已定下 2已封住 */
    @TableField("status")
    @ApiModelProperty(value = "条目状态 0在办 1已定下 2已封住")
    private Integer status;

    /** 删除标记 0正常 1删除 */
    @TableField("del_flag")
    @ApiModelProperty(value = "删除标记 0正常 1删除")
    private Integer delFlag;

    /** 创建者 */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建者")
    private String createBy;

    /** 创建时间 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /** 更新者 */
    @TableField(value = "update_by", fill = FieldFill.UPDATE)
    @ApiModelProperty(value = "更新者")
    private String updateBy;

    /** 更新时间 */
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    /** 备注 */
    @TableField("remark")
    @ApiModelProperty(value = "备注")
    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getItemNo() {
        return itemNo;
    }

    public void setItemNo(String itemNo) {
        this.itemNo = itemNo;
    }

    public String getBizNo() {
        return bizNo;
    }

    public void setBizNo(String bizNo) {
        this.bizNo = bizNo;
    }

    public Integer getStage() {
        return stage;
    }

    public void setStage(Integer stage) {
        this.stage = stage;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLastAction() {
        return lastAction;
    }

    public void setLastAction(String lastAction) {
        this.lastAction = lastAction;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getSettleType() {
        return settleType;
    }

    public void setSettleType(String settleType) {
        this.settleType = settleType;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(Integer delFlag) {
        this.delFlag = delFlag;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
