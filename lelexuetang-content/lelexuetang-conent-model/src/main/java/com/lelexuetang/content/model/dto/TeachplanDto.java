package com.lelexuetang.content.model.dto;

import com.lelexuetang.content.model.po.Teachplan;
import com.lelexuetang.content.model.po.TeachplanMedia;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class TeachplanDto extends Teachplan {
    // 子节点
    private List<TeachplanDto> teachPlanTreeNodes;
    // 媒资信息
    private TeachplanMedia teachplanMedia;
}
