package com.xuecheng.content.service;


import com.xuecheng.content.model.dto.CoursePreviewDto;

import java.io.File;

/**
 * @description 课程预览、发布接口
 * @version 1.0
 */

public interface CoursePublishService {

    CoursePreviewDto getCoursePreviewInfo(Long courseId);


    void commitAudit(Long companyId, Long courseId);

    void publish(Long companyId, Long courseId);

    File generateCourseHtml(Long courseId);

    void  uploadCourseHtml(Long courseId, File file);

}
