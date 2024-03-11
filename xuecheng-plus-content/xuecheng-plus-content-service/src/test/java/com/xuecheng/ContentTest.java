package com.xuecheng;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.QueryCourseParamDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.service.CourseBaseInfoService;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class ContentTest {

    @Autowired
    private CourseBaseMapper courseBaseMapper;
    @Autowired
    private CourseBaseInfoService courseBaseInfoService;

    @Test
    void testCourseBaseService(){
        QueryCourseParamDto dto = new QueryCourseParamDto("202004", "java", "203001");
        PageParams pageParams = new PageParams(1L, 3L);

        PageResult<CourseBase> result = courseBaseInfoService.queryCourseBaseList(pageParams, dto);
        System.out.println(result);
    }

    @Test
    void testCourseBaseMapper(){
        CourseBase courseBase = courseBaseMapper.selectById(74L);
        //Assertions.assertNotNull(courseBase);

        LambdaQueryWrapper<CourseBase> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        QueryCourseParamDto queryCourseParamDto = new QueryCourseParamDto();
        queryCourseParamDto.setCourseName("java");
        queryCourseParamDto.setAuditStatus("202004");

        lambdaQueryWrapper.like(StringUtils.isNotBlank(queryCourseParamDto.getCourseName()),CourseBase::getName,queryCourseParamDto.getCourseName());
        lambdaQueryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamDto.getAuditStatus()),CourseBase::getAuditStatus,queryCourseParamDto.getAuditStatus());
        Page<CourseBase> page = new Page<>(1, 20);
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, lambdaQueryWrapper);
        List<CourseBase> records = pageResult.getRecords();
        long total = pageResult.getTotal();
        PageResult<CourseBase> result = new PageResult<>(records, total, 1, 20);
        System.out.println(result);
    }

}
