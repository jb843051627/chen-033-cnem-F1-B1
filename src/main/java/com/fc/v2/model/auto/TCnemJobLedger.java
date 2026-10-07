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
 * 检验作业台账对象 t_cnem_job_ledger（生效/冲账流水，只追加不删改）
 *
 * @author fuce
 * @date 2026-10-07
 */
@TableName("t_cnem_job_ledger")
@ApiModel(value = "TCnemJobLedger", description = "检验作业台账")
public class TCnemJobLedger implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键")
    private Long id;

    /** 作业编号 */
    @TableField("job_no")
    @ApiModelProperty(value = "作业编号")
    private String jobNo;

    /** 行次 */
    @TableField("row_no")
    @ApiModelProperty(value = "行次")
    private Integer rowNo;

    /** 动作 1生效落账 2冲回冲账 */
    @TableField("action")
    @ApiModelProperty(value = "动作 1生效落账 2冲回冲账")
    private Integer action;

    /** 检验编号 */
    @TableField("insp_no")
    @ApiModelProperty(value = "检验编号")
    private String inspNo;

    /** 检验结论等级 */
    @TableField("result_level")
    @ApiModelProperty(value = "检验结论等级")
    private Integer resultLevel;

    /** 金额 */
    @TableField("amount")
    @ApiModelProperty(value = "金额")
    private BigDecimal amount;

    /** 对应收费流水条目主键 */
    @TableField("fee_row_id")
    @ApiModelProperty(value = "对应收费流水条目主键")
    private Long feeRowId;

    /** 动的人 */
    @TableField("oper_by")
    @ApiModelProperty(value = "动的人")
    private String operBy;

    /** 动的时刻 */
    @TableField("oper_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "动的时刻")
    private Date operAt;

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

    public String getJobNo() {
        return jobNo;
    }

    public void setJobNo(String jobNo) {
        this.jobNo = jobNo;
    }

    public Integer getRowNo() {
        return rowNo;
    }

    public void setRowNo(Integer rowNo) {
        this.rowNo = rowNo;
    }

    public Integer getAction() {
        return action;
    }

    public void setAction(Integer action) {
        this.action = action;
    }

    public String getInspNo() {
        return inspNo;
    }

    public void setInspNo(String inspNo) {
        this.inspNo = inspNo;
    }

    public Integer getResultLevel() {
        return resultLevel;
    }

    public void setResultLevel(Integer resultLevel) {
        this.resultLevel = resultLevel;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Long getFeeRowId() {
        return feeRowId;
    }

    public void setFeeRowId(Long feeRowId) {
        this.feeRowId = feeRowId;
    }

    public String getOperBy() {
        return operBy;
    }

    public void setOperBy(String operBy) {
        this.operBy = operBy;
    }

    public Date getOperAt() {
        return operAt;
    }

    public void setOperAt(Date operAt) {
        this.operAt = operAt;
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
