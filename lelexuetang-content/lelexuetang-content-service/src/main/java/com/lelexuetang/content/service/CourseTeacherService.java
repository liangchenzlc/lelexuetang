package com.lelexuetang.content.service;

import com.lelexuetang.content.model.po.CourseTeacher;

public interface CourseTeacherService {
    //查询课程教师
    CourseTeacher selectCourseTeacher(Long courseId);

    // 添加课程教师
    CourseTeacher addCourseTeacher(Long companyId, CourseTeacher courseTeacher);

    //删除课程教师
    void deleteCourseTeacher(Long companyId, Long courseId, Long teacherId);
}
