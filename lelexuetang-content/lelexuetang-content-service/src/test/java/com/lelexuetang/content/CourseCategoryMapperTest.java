package com.lelexuetang.content;

import com.lelexuetang.content.mapper.CourseCategoryMapper;
import com.lelexuetang.content.model.dto.CourseCategoryTreeDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class CourseCategoryMapperTest {
    @Autowired
    CourseCategoryMapper categoryMapper;

    @Test
    public void testCourseCategoryMapper() {
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = categoryMapper.selectTreeNodes("1");
        System.out.println(courseCategoryTreeDtos);
    }
}
