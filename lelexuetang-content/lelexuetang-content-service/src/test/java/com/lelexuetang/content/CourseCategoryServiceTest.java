package com.lelexuetang.content;

import com.lelexuetang.content.model.dto.CourseCategoryTreeDto;
import com.lelexuetang.content.service.CourseCategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class CourseCategoryServiceTest {

    @Autowired
    public CourseCategoryService courseCategoryService;
    @Test
    public void testCourseCategoryService() {
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryService.queryTreeNodes("1");
        System.out.println(courseCategoryTreeDtos);
    }

}
