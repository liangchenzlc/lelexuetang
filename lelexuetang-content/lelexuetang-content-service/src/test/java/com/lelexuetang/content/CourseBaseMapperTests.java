package com.lelexuetang.content;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lelexuetang.base.model.PageParams;
import com.lelexuetang.base.model.PageResult;
import com.lelexuetang.content.mapper.CourseBaseMapper;
import com.lelexuetang.content.model.dto.QueryCourseParamsDto;
import com.lelexuetang.content.model.po.CourseBase;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

@SpringBootTest
public class CourseBaseMapperTests {
    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Test
    public void testCourseBaseMapper() {
        CourseBase courseBase = courseBaseMapper.selectById(1L);
        Assertions.assertNotNull(courseBase);

        // 单元测试 分页查询课程列表
        QueryCourseParamsDto queryCourseParamsDto = new QueryCourseParamsDto();
        queryCourseParamsDto.setCourseName("JAVA");
        queryCourseParamsDto.setAuditStatus("202004");
        QueryWrapper<CourseBase> queryWrapper = new QueryWrapper<>();

        queryWrapper.lambda().like(StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()),
                CourseBase::getName, queryCourseParamsDto.getCourseName())
                .eq(StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()),
                        CourseBase::getAuditStatus, queryCourseParamsDto.getAuditStatus())
                .eq(StringUtils.isNotEmpty(queryCourseParamsDto.getPublishStatus()),
                        CourseBase::getStatus, queryCourseParamsDto.getPublishStatus());

        // 分页条件
        PageParams pageParams = new PageParams();
        pageParams.setPageNo(1L);
        pageParams.setPageSize(2L);
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());

        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);
        Optional<PageResult<CourseBase>> optional = Optional.ofNullable(pageResult)
                .map(res -> {
                    List<CourseBase> records = res.getRecords();
                    long count = res.getTotal();
                    PageResult<CourseBase> result = new PageResult<>(records, count, pageParams.getPageNo(), pageParams.getPageSize());
                    return result;
                });
        optional.ifPresent(System.out::println);

    }
}
