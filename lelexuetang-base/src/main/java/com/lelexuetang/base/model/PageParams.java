package com.lelexuetang.base.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PageParams {
    @ApiModelProperty("当前页码")
    private Long pageNo = 1L;
    @ApiModelProperty("每页记录数")
    private Long pageSize = 30L;
}
