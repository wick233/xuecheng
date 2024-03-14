package com.xuecheng.content.service;

import com.xuecheng.content.model.po.CourseTeacher;

import java.util.List;

/**
 * @Description
 * @Author Twithu
 * @Date 2024/3/15 2:17
 * @Version: 1.0
 */
public interface CourseTeacherService {
    List<CourseTeacher> getCourseTeacherList(Integer courseId);
}
