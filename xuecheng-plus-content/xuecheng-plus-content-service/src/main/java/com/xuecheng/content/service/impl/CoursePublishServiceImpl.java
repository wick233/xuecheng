package com.xuecheng.content.service.impl;

import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.CourseTeacherService;
import com.xuecheng.content.service.TeachPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description 课程发布接口实现
 * @Author Twithu
 * @Date 2024/4/21 11:42
 * @Version: 1.0
 */
@Service
public class CoursePublishServiceImpl implements CoursePublishService {
    @Autowired
    CourseBaseInfoService courseBaseInfoService;
    @Autowired
    TeachPlanService teachPlanService;
    @Autowired
    CourseTeacherService courseTeacherService;

    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        //查询课程基本信息、营销信息
        CourseBaseInfoDto courseBaseInfoDto = courseBaseInfoService.getCourseBaseById(courseId);
        //查询课程计划信息
        List<TeachPlanDto> teachPlanTree = teachPlanService.findTeachPlanTree(courseId);
        //查询师资信息
        List<CourseTeacher> courseTeachers = courseTeacherService.getCourseTeacherList(courseId);

        coursePreviewDto.setCourseBase(courseBaseInfoDto);
        coursePreviewDto.setTeachPlans(teachPlanTree);
        coursePreviewDto.setCourseTeachers(courseTeachers);
        return coursePreviewDto;
    }
}
