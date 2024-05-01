package com.lelexuetang.content.model.dto;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

// 查询课程条件模型类
@ApiModel(value = "查询课程条件模型类", description = "课程查询条件")
@Data
@ToString
public class QueryCourseParamsDto {
    // 审核状态
    @ApiModelProperty("审核状态")
    private String auditStatus;
    // 课程名称
    @ApiModelProperty("课程名称")
    private String courseName;
    // 发布状态
    @ApiModelProperty("发布状态")
    private String publishStatus;
}
