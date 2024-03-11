package com.xuecheng.content;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.content.model.dto.QueryCourseParamDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.mapper.CourseBaseMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ContentTest {

    @Autowired()
    private CourseBaseMapper courseBaseMapper;

    @Test
    void testCourseBaseMapper(){
        CourseBase courseBase = courseBaseMapper.selectById(74L);
        Assertions.assertNotNull(courseBase);

        LambdaQueryWrapper<CourseBase> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        QueryCourseParamDto queryCourseParamDto = new QueryCourseParamDto();
        queryCourseParamDto.setCourseName("java");
        queryCourseParamDto.setAuditStatus("202004");
        queryCourseParamDto.setPublishStatus("203001");
    }

}
