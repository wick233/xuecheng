package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.CourseTeacher;
import lombok.Data;

import java.util.List;

/**
 * @Description 课程预览模型类
 * @Author Twithu
 * @Date 2024/4/21 11:15
 * @Version: 1.0
 */
@Data
public class CoursePreviewDto {

    //课程基本信息 营销信息
    private CourseBaseInfoDto courseBase;

    //课程计划信息
    List<TeachPlanDto> teachPlans;

    //课程师资信息
    List<CourseTeacher> courseTeachers;
}
