package com.webank.wedpr.components.admin.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import lombok.Data;

/**
 * @author caryliao
 * @since 2024-09-04
 */
@TableName("wedpr_job_table")
@ApiModel(value = "WedprJobTable对象", description = "")
@Data
public class WedprJobTable implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "任务ID")
    @TableId("id")
    private String id;

    @ApiModelProperty(value = "任务名称")
    private String name;

    @ApiModelProperty(value = "任务所属项目Id")
    private String projectId;

    @ApiModelProperty(value = "任务发起人")
    private String owner;

    @ApiModelProperty(value = "任务发起机构")
    private String ownerAgency;

    @ApiModelProperty(value = "任务类型")
    private String jobType;

    @ApiModelProperty(value = "任务相关机构信息(json)")
    private String parties;

    @ApiModelProperty(value = "任务参数(json)")
    private String param;

    @ApiModelProperty(value = "任务状态")
    private String status;

    @ApiModelProperty(value = "任务执行结果(json)")
    private String jobResult;

    @ApiModelProperty(value = "上报状态")
    private Integer reportStatus;

    @ApiModelProperty(value = "任务创建时间")
    private String createTime;

    @ApiModelProperty(value = "任务更新时间")
    private String lastUpdateTime;

    @TableField(exist = false)
    private Integer count;
}
