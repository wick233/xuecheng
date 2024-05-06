package com.xuecheng.learning.service;

import com.xuecheng.base.model.PageResult;
import com.xuecheng.learning.model.dto.MyCourseTableParams;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcCourseTables;

/**
 * @Description
 * @Author Twithu
 * @Date 2024/4/29 22:35
 * @Version: 1.0
 */
public interface MyCourseTablesService {
    XcChooseCourseDto addChooseCourse(String userId, Long courseId);

    XcCourseTablesDto getLearningStatus(String userId, Long courseId);

    boolean saveChooseCourseStatus(String chooseCourseId);

    PageResult<XcCourseTables> mycourestabls(MyCourseTableParams params);
}
