package com.xuecheng.content.service;


import com.xuecheng.content.model.dto.CoursePreviewDto;

/**
 * @description 课程预览、发布接口
 * @version 1.0
 */

public interface CoursePublishService {

    public CoursePreviewDto getCoursePreviewInfo(Long courseId);


    void commitAudit(Long companyId, Long courseId);

    void publish(Long companyId, Long courseId);
}
