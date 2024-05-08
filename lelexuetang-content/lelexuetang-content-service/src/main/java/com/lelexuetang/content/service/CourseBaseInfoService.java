package com.lelexuetang.content.service;

import com.lelexuetang.base.model.PageParams;
import com.lelexuetang.base.model.PageResult;
import com.lelexuetang.content.model.dto.AddCourseDto;
import com.lelexuetang.content.model.dto.CourseBaseInfoDto;
import com.lelexuetang.content.model.dto.EditCourseDto;
import com.lelexuetang.content.model.dto.QueryCourseParamsDto;
import com.lelexuetang.content.model.po.CourseBase;

/**
 * 课程信息管理服务接口
 */
public interface CourseBaseInfoService {
    /**
     * 课程分页查询
     * @param pageParams 分页参数
     * @param queryCourseParamsDto 查询条件
     * @return 课程信息
     */
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);

    /**
     * 新增课程
     * @param companyId 机构id
     * @param addCourseDto 课程信息
     * @return 课程详细信息
     */
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto);

    /**
     * 根据课程id查询课程信息
     * @param courseId 课程id
     * @return 课程详细信息
     */
    public CourseBaseInfoDto getCourseBaseInfo(Long courseId);

    /**
     * 修改课程信息
     * @param companyId 机构id
     * @param editCourseDto 编辑课程信息
     * @return 课程详细信息
     */
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto editCourseDto);

    /**
     * 删除课程信息
     * @param courseId
     */
    void deleteCourseBase(Long courseId);
}
