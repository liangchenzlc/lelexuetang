package com.lelexuetang.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lelexuetang.base.model.PageParams;
import com.lelexuetang.base.model.PageResult;
import com.lelexuetang.content.mapper.CourseBaseMapper;
import com.lelexuetang.content.mapper.CourseCategoryMapper;
import com.lelexuetang.content.mapper.CourseMarketMapper;
import com.lelexuetang.content.model.dto.AddCourseDto;
import com.lelexuetang.content.model.dto.CourseBaseInfoDto;
import com.lelexuetang.content.model.dto.QueryCourseParamsDto;
import com.lelexuetang.content.model.po.CourseBase;
import com.lelexuetang.content.model.po.CourseCategory;
import com.lelexuetang.content.model.po.CourseMarket;
import com.lelexuetang.content.service.CourseBaseInfoService;
import lombok.extern.slf4j.Slf4j;
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
        if (StringUtils.isBlank(dto.getName())) {
            throw new RuntimeException("课程名称为空");
        }

        if (StringUtils.isBlank(dto.getMt())) {
            throw new RuntimeException("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getSt())) {
            throw new RuntimeException("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getGrade())) {
            throw new RuntimeException("课程等级为空");
        }

        if (StringUtils.isBlank(dto.getTeachmode())) {
            throw new RuntimeException("教育模式为空");
        }

        if (StringUtils.isBlank(dto.getUsers())) {
            throw new RuntimeException("适应人群为空");
        }

        if (StringUtils.isBlank(dto.getCharge())) {
            throw new RuntimeException("收费规则为空");
        }

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
            throw  new RuntimeException("新增课程基本信息失败");
        }
        // 向课程营销表保存课程营销信息
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(dto, courseMarket);
        courseMarket.setId(courseBase.getId());
        int i = saveCourseMarket(courseMarket);
        if (i <= 0) {
            throw new RuntimeException("保存课程营销信息失败");
        }
        // 查询课程基本信息及营销信息并返回
        //查询课程基本信息及营销信息并返回
        return getCourseBaseInfo(courseBase.getId());
    }

    /**
     * 查询课程基本信息及营销信息
    */
    private CourseBaseInfoDto getCourseBaseInfo(Long id) {
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

    /**
     * 保存课程营销信息, 数据库有数据则更新，没有则插入
     */
    private int saveCourseMarket(CourseMarket courseMarket) {
        // 参数合法性校验
        String charge = courseMarket.getCharge();
        if(StringUtils.isBlank(charge)) {
            throw new RuntimeException("收费规则不能为空");
        }
        if(charge.equals("201001")) { // 收费
            if(courseMarket.getPrice() == null || courseMarket.getPrice().floatValue() <= 0) {
                throw new RuntimeException("课程价格不能为空且必须大于0");
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
