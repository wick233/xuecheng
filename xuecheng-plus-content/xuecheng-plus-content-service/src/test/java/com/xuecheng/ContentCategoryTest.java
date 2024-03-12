package com.xuecheng;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class ContentCategoryTest {

    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    @Autowired
    private CourseCategoryService courseCategoryService;

    @Test
    void testCourseCategoryService(){
        List<CourseCategoryTreeDto>  list = courseCategoryService.queryTreeNodes("1");
        System.out.println(list);
    }

    @Test
    void testCourseCategoryMapper(){
        List<CourseCategoryTreeDto> list = courseCategoryMapper.selectTreeNodes("1");
        list.forEach(System.out::println);
    }

}
