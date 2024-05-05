package com.xuecheng.learning.service;

import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;

/**
 * @Description
 * @Author Twithu
 * @Date 2024/4/29 22:35
 * @Version: 1.0
 */
public interface MyCourseTablesService {
    public XcChooseCourseDto addChooseCourse(String userId, Long courseId);
    XcCourseTablesDto getLearningStatus(String userId, Long courseId);

    boolean saveChooseCourseStatus(String chooseCourseId);
}
