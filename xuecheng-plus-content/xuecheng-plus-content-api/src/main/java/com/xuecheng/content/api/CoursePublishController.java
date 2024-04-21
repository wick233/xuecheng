package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.service.CoursePublishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

/**
 * @Description
 * @Author Twithu
 * @Date 2024/4/21 10:33
 * @Version: 1.0
 */
@Controller
public class CoursePublishController {

    @Autowired
    CoursePublishService coursePublishService;

    @GetMapping("/coursepreview/{courseId}")
    public ModelAndView preview(@PathVariable("courseId") Long courseId) {
        ModelAndView modelAndView = new ModelAndView();
        //查询课程的数据作为数据模型
        CoursePreviewDto coursePreviewDto = coursePublishService.getCoursePreviewInfo(courseId);

        //设置模型数据
        modelAndView.addObject("model",coursePreviewDto);
        //设置模板名称
        modelAndView.setViewName("course_template");
        return modelAndView;
    }
}

