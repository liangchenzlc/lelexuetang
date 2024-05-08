package com.lelexuetang.content.api;

import com.lelexuetang.content.model.po.CourseTeacher;
import com.lelexuetang.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Api(tags = "课程教师管理接口")
@RestController
public class CourseTeacherController {
    @Autowired
    CourseTeacherService courseTeacherService;

    @GetMapping("/courseTeacher/list/{courseId}")
    @ApiOperation("查询课程教师")
    public CourseTeacher selectCourseTeacher(@PathVariable Long courseId) {
        return courseTeacherService.selectCourseTeacher(courseId);
    }

    @PostMapping("/courseTeacher")
    @ApiOperation("添加或修改课程教师")
    public CourseTeacher addCourseTeacher(@RequestBody @Validated CourseTeacher courseTeacher) {
        Long companyId = 1232141425L;
        return courseTeacherService.addCourseTeacher(companyId, courseTeacher);
    }

    @DeleteMapping("/ourseTeacher/course/{courseId}/{teacherId}")
    @ApiOperation("删除课程教师")
    public void deleteCourseTeacher(@PathVariable Long courseId, @PathVariable Long teacherId) {
        Long companyId = 1232141425L;
        courseTeacherService.deleteCourseTeacher(companyId, courseId, teacherId);
    }
}
