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

    /**
     * 删除课程计划
     * @param id
     */
    void deleteTeachplan(Long id);

    /**
     * 移动课程计划 向下移动
     * @param id
     */
    void moveDown(Long id);

    /**
     * 移动课程计划 向上移动
     * @param id
     */
    void moveUp(Long id);
}