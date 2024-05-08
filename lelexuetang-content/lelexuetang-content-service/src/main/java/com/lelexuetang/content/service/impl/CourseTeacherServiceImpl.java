package com.lelexuetang.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lelexuetang.base.exception.LeLeXueTangException;
import com.lelexuetang.content.mapper.CourseBaseMapper;
import com.lelexuetang.content.mapper.CourseTeacherMapper;
import com.lelexuetang.content.model.po.CourseBase;
import com.lelexuetang.content.model.po.CourseTeacher;
import com.lelexuetang.content.service.CourseTeacherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
public class CourseTeacherServiceImpl implements CourseTeacherService {
    @Autowired
    private CourseTeacherMapper courseTeacherMapper;

    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Override
    public CourseTeacher selectCourseTeacher(Long courseId) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId, courseId);
        return courseTeacherMapper.selectOne(queryWrapper);
    }

    @Transactional
    @Override
    public CourseTeacher addCourseTeacher(Long companyId, CourseTeacher courseTeacher) {
        // 参数合法性校验
        // 判断该机构是否有该课程
        CourseBase courseBase = courseBaseMapper.selectById(courseTeacher.getCourseId());
        if(courseBase == null) LeLeXueTangException.cast("没有该课程信息");
        if(!companyId.equals(courseBase.getCompanyId())){
            LeLeXueTangException.cast("本机构不能增加或修改其他机构的教师");
        }

        if(courseTeacher.getId() == null) { // 新增
            courseTeacher.setCreateDate(LocalDateTime.now());
            int insert = courseTeacherMapper.insert(courseTeacher);
            if(insert <= 0) {
                LeLeXueTangException.cast("添加课程教师失败");
            }
        }else { // 修改
            int i = courseTeacherMapper.updateById(courseTeacher);
            if (i <= 0){
                LeLeXueTangException.cast("修改课程教师失败");
            }
        }

        return selectCourseTeacher(courseTeacher.getCourseId());
    }

    @Override
    public void deleteCourseTeacher(Long companyId, Long courseId, Long teacherId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null) LeLeXueTangException.cast("没有该课程信息");
        if (!companyId.equals(courseBase.getCompanyId())){
            LeLeXueTangException.cast("本机构不能删除其他机构的教师");
        }
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId, courseId).eq(CourseTeacher::getId, teacherId);
        courseTeacherMapper.delete(queryWrapper);
    }
}
