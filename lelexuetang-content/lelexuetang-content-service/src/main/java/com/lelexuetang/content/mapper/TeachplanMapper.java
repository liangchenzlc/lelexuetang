package com.lelexuetang.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lelexuetang.content.model.dto.TeachplanDto;
import com.lelexuetang.content.model.po.Teachplan;

import java.util.List;


/**
 * <p>
 * 课程计划 Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface TeachplanMapper extends BaseMapper<Teachplan> {
    public List<TeachplanDto> selectTreeNotes(Long courseId);
}
