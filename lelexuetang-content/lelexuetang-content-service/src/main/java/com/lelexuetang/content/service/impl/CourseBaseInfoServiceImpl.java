package com.lelexuetang.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lelexuetang.base.exception.LeLeXueTangException;
import com.lelexuetang.base.model.PageParams;
import com.lelexuetang.base.model.PageResult;
import com.lelexuetang.content.mapper.*;
import com.lelexuetang.content.model.dto.AddCourseDto;
import com.lelexuetang.content.model.dto.CourseBaseInfoDto;
import com.lelexuetang.content.model.dto.EditCourseDto;
import com.lelexuetang.content.model.dto.QueryCourseParamsDto;
import com.lelexuetang.content.model.po.*;
import com.lelexuetang.content.service.CourseBaseInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.CharSetUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {

    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Autowired
    CourseMarketMapper courseMarketMapper;

    @Autowired
    CourseTeacherMapper courseTeacherMapper;

    @Autowired
    TeachplanMapper teachplanMapper;

    @Autowired
    TeachplanMediaMapper teachplanMediaMapper;

    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {
        // 查询条件拼接
        QueryWrapper<CourseBase> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .like(StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()),
                        CourseBase::getName, queryCourseParamsDto.getCourseName())
                .eq(StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()),
                        CourseBase::getAuditStatus, queryCourseParamsDto.getAuditStatus())
                .eq(StringUtils.isNotEmpty(queryCourseParamsDto.getPublishStatus()),
                        CourseBase::getStatus, queryCourseParamsDto.getPublishStatus());

        // 分页条件
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());

        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);

        List<CourseBase> records = pageResult.getRecords();
        long count = pageResult.getTotal();
        PageResult<CourseBase> result = new PageResult<>(records, count, pageParams.getPageNo(), pageParams.getPageSize());

        return result;
    }

    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto dto) {
        // 参数的合法性校验

        CourseBase courseBase = new CourseBase();
        BeanUtils.copyProperties(dto, courseBase);
        courseBase.setCompanyId(companyId);
        courseBase.setCreateDate(LocalDateTime.now());
        //设置审核状态
        courseBase.setAuditStatus("202002");
        //设置发布状态
        courseBase.setStatus("203001");
        // 插入课程信息
        int insert = courseBaseMapper.insert(courseBase);
        if(insert <= 0 ) {
            LeLeXueTangException.cast("新增课程基本信息失败");
        }
        // 向课程营销表保存课程营销信息
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(dto, courseMarket);
        courseMarket.setId(courseBase.getId());
        int i = saveCourseMarket(courseMarket);
        if (i <= 0) {
            LeLeXueTangException.cast("保存课程营销信息失败");
        }
        // 查询课程基本信息及营销信息并返回
        //查询课程基本信息及营销信息并返回
        return getCourseBaseInfo(courseBase.getId());
    }


    /**
     * 查询课程基本信息及营销信息
    */
    public CourseBaseInfoDto getCourseBaseInfo(Long id) {
        CourseBase courseBase = courseBaseMapper.selectById(id);
        if (courseBase == null) return null;
        CourseMarket courseMarket = courseMarketMapper.selectById(id);
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
        if (courseMarket != null) {
            BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);
        }

        // 查询课程大分类
        CourseCategory courseCategoryMt= courseCategoryMapper.selectById(courseBase.getMt());
        courseBaseInfoDto.setMtName(courseCategoryMt.getName());
        // 查询课程小分类
        CourseCategory courseCategorySt = courseCategoryMapper.selectById(courseBase.getSt());
        courseBaseInfoDto.setStName(courseCategorySt.getName());

        return courseBaseInfoDto;
    }

    @Transactional
    @Override
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto editCourseDto) {
        Long id = editCourseDto.getId();
        CourseBase courseBase = courseBaseMapper.selectById(id);
        if (courseBase == null) {
            LeLeXueTangException.cast("课程不存在");
        }
        // 参数合法性校验
        // 根据具体的业务逻辑校验
        if(!companyId.equals(courseBase.getCompanyId())) {
            LeLeXueTangException.cast("只能修改本机构的课程");
        }
        // 封装对象
        BeanUtils.copyProperties(editCourseDto, courseBase);
        courseBase.setId(editCourseDto.getId());
        courseBase.setChangeDate(LocalDateTime.now());
        // sql执行
        int insert = courseBaseMapper.updateById(courseBase);
        if(insert <= 0) {
            LeLeXueTangException.cast("修改课程基本信息失败");
        }
        // 更新课程营销信息
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(editCourseDto, courseMarket);
        courseMarket.setId(editCourseDto.getId());
        int i = saveCourseMarket(courseMarket);
        if(i <= 0) {
            LeLeXueTangException.cast("修改课程营销信息失败");
        }
        return getCourseBaseInfo(courseBase.getId());
    }

    @Transactional
    @Override
    public void deleteCourseBase(Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(courseBase.getAuditStatus().equals("202002")) {
            // 删除课程基础信息
            courseBaseMapper.deleteById(courseId);
            // 删除课程营销信息
            courseMarketMapper.deleteById(courseId);
            // 删除课程计划信息
            teachplanMapper.delete(new QueryWrapper<Teachplan>().eq("course_id", courseId));
            // 删除课程教师信息
            courseTeacherMapper.delete(new QueryWrapper<CourseTeacher>().eq("course_id", courseId));
        }else {
            LeLeXueTangException.cast("只有未提交的课程才能删除");
        }
    }

    /**
     * 保存课程营销信息, 数据库有数据则更新，没有则插入
     */
    private int saveCourseMarket(CourseMarket courseMarket) {
        // 参数合法性校验
        String charge = courseMarket.getCharge();
        if(charge.equals("201001")) { // 收费
            if(courseMarket.getPrice() == null || courseMarket.getPrice().floatValue() <= 0) {
                LeLeXueTangException.cast("课程价格不能为空且必须大于0");
            }
        }

        CourseMarket courseMarketOld = courseMarketMapper.selectById(courseMarket.getId());
        if(courseMarketOld == null) {
            // 插入
            return courseMarketMapper.insert(courseMarket);
        } else {
            // 更新
            BeanUtils.copyProperties(courseMarket, courseMarketOld);
            courseMarketOld.setId(courseMarket.getId());
            return courseMarketMapper.updateById(courseMarketOld);
        }
    }
}
