package com.xuecheng.content.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Description
 * @Author Twithu
 * @Date 2024/3/15 2:11
 * @Version: 1.0
 */
@Api(value = "课程师资管理", tags = "课程师资管理")
@RestController
@RequestMapping("/courseTeacher")
public class CourseTeacherController {

    @Autowired
    CourseTeacherService courseTeacherService;
    @Autowired
    CourseTeacherMapper mapper;
    @Autowired
    CourseBaseMapper courseBaseMapper;

    @ApiOperation("查询课程所有老师")
    @GetMapping("/list/{courseId}")
    public List<CourseTeacher> getCourseTeacherList(@PathVariable Integer courseId) {
        return mapper.selectList(new LambdaQueryWrapper<CourseTeacher>().eq(CourseTeacher::getCourseId, courseId));
    }

    @ApiOperation("修改/新增课程老师信息")
    @PostMapping()
    public CourseTeacher updateCourseTeacher(@RequestBody CourseTeacher courseTeacher) {
        long companyId = 1232141425L;
        CourseBase courseBase = courseBaseMapper.selectById(courseTeacher.getCourseId());
        if (companyId == courseBase.getCompanyId()) {
            if (courseTeacher.getId() == null) mapper.insert(courseTeacher);
            else mapper.updateById(courseTeacher);
        } else XueChengPlusException.cast("权限不足");
        return courseTeacher;
    }

    @ApiOperation("删除课程老师信息")
    @DeleteMapping("/course/{courseId}/{teacherId}")
    public void deleteCourseTeacher(@PathVariable("courseId") Long courseId, @PathVariable("teacherId") Long teacherId) {
        long companyId = 1232141425L;
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (companyId == courseBase.getCompanyId()) {
            mapper.delete(new LambdaQueryWrapper<CourseTeacher>().eq(CourseTeacher::getId, teacherId).eq(CourseTeacher::getCourseId, courseId));
        } else XueChengPlusException.cast("权限不足");
    }

}
