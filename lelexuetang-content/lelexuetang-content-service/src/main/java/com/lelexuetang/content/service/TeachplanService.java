package com.lelexuetang.content.service;

import com.lelexuetang.content.model.dto.SaveTeachplanDto;
import com.lelexuetang.content.model.dto.TeachplanDto;

import java.util.List;

/**
 * 课程计划接口
 */
public interface TeachplanService {
    public List<TeachplanDto> findTeachplanTree(Long courseId);

    /**
     * 新增或修改课程计划
     * @param teachplan
     */
    void saveTeachplan(SaveTeachplanDto teachplan);
}
