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
 * 检验结论作业对象 t_cnem_job
 *
 * @author fuce
 * @date 2026-09-12
 */
@TableName("t_cnem_job")
@ApiModel(value = "TCnemJob", description = "检验结论作业")
public class TCnemJob implements Serializable {
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

    /** 作业名称 */
    @TableField("job_name")
    @ApiModelProperty(value = "作业名称")
    private String jobName;

    /** 作业状态(0草稿1待审2生效中3已生效4部分生效5回滚中6已回滚) */
    @TableField("status")
    @ApiModelProperty(value = "作业状态(0草稿1待审2生效中3已生效4部分生效5回滚中6已回滚)")
    private Integer status;

    /** 提交总行数 */
    @TableField("total_rows")
    @ApiModelProperty(value = "提交总行数")
    private Integer totalRows;

    /** 生效成功行数 */
    @TableField("done_rows")
    @ApiModelProperty(value = "生效成功行数")
    private Integer doneRows;

    /** 生效失败行数 */
    @TableField("fail_rows")
    @ApiModelProperty(value = "生效失败行数")
    private Integer failRows;

    /** 作业生效起始时刻 */
    @TableField("eff_start")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "作业生效起始时刻")
    private Date effStart;

    /** 作业生效截止时刻(不含) */
    @TableField("eff_end")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "作业生效截止时刻(不含)")
    private Date effEnd;

    /** 判据版本 */
    @TableField("version")
    @ApiModelProperty(value = "判据版本")
    private String version;

    /** 行次 */
    @TableField("row_no")
    @ApiModelProperty(value = "行次")
    private Integer rowNo;

    /** 检验编号 */
    @TableField("insp_no")
    @ApiModelProperty(value = "检验编号")
    private String inspNo;

    /** 金额 */
    @TableField("amount")
    @ApiModelProperty(value = "金额")
    private BigDecimal amount;

    /** 数量 */
    @TableField("job_qty")
    @ApiModelProperty(value = "数量")
    private Integer jobQty;

    /** 版本状态(1生效中 0未生效) */
    @TableField("version_status")
    @ApiModelProperty(value = "版本状态(1生效中 0未生效)")
    private Integer versionStatus;

    /** 是否已写回(0否1是) */
    @TableField("writeback_flag")
    @ApiModelProperty(value = "是否已写回(0否1是)")
    private Integer writebackFlag;

    /** 补偿流水标记(0否1是) */
    @TableField("compensate_flag")
    @ApiModelProperty(value = "补偿流水标记(0否1是)")
    private Integer compensateFlag;

    /** 是否已补偿(0否1是) */
    @TableField("compensated")
    @ApiModelProperty(value = "是否已补偿(0否1是)")
    private Integer compensated;

    /** 补偿累计次数 */
    @TableField("compensate_cnt")
    @ApiModelProperty(value = "补偿累计次数")
    private Integer compensateCnt;

    /** 失败原因 */
    @TableField("fail_reason")
    @ApiModelProperty(value = "失败原因")
    private String failReason;

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

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(Integer totalRows) {
        this.totalRows = totalRows;
    }

    public Integer getDoneRows() {
        return doneRows;
    }

    public void setDoneRows(Integer doneRows) {
        this.doneRows = doneRows;
    }

    public Integer getFailRows() {
        return failRows;
    }

    public void setFailRows(Integer failRows) {
        this.failRows = failRows;
    }

    public Date getEffStart() {
        return effStart;
    }

    public void setEffStart(Date effStart) {
        this.effStart = effStart;
    }

    public Date getEffEnd() {
        return effEnd;
    }

    public void setEffEnd(Date effEnd) {
        this.effEnd = effEnd;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Integer getRowNo() {
        return rowNo;
    }

    public void setRowNo(Integer rowNo) {
        this.rowNo = rowNo;
    }

    public String getInspNo() {
        return inspNo;
    }

    public void setInspNo(String inspNo) {
        this.inspNo = inspNo;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getJobQty() {
        return jobQty;
    }

    public void setJobQty(Integer jobQty) {
        this.jobQty = jobQty;
    }

    public Integer getVersionStatus() {
        return versionStatus;
    }

    public void setVersionStatus(Integer versionStatus) {
        this.versionStatus = versionStatus;
    }

    public Integer getWritebackFlag() {
        return writebackFlag;
    }

    public void setWritebackFlag(Integer writebackFlag) {
        this.writebackFlag = writebackFlag;
    }

    public Integer getCompensateFlag() {
        return compensateFlag;
    }

    public void setCompensateFlag(Integer compensateFlag) {
        this.compensateFlag = compensateFlag;
    }

    public Integer getCompensated() {
        return compensated;
    }

    public void setCompensated(Integer compensated) {
        this.compensated = compensated;
    }

    public Integer getCompensateCnt() {
        return compensateCnt;
    }

    public void setCompensateCnt(Integer compensateCnt) {
        this.compensateCnt = compensateCnt;
    }

    public String getFailReason() {
        return failReason;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
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
