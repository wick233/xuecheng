package com.xuecheng.content.api;

import com.xuecheng.base.exception.ValidationGroups;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.utils.StringUtil;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@Api(value = "课程信息管理接口",tags = "课程信息管理接口")
public class CourseBaseInfoController {

    @Autowired
    private CourseBaseInfoService courseBaseInfoService;

    /**
     * @descriptions 查询全部课程分页
     * @param pageParams
     * @param queryCourseParamDto
     * @return
     */
    @ApiOperation("课程列表分页查询")
    @PostMapping("/course/list")
    @PreAuthorize("hasAuthority('xc_teachmanager_course_list')")
    public PageResult<CourseBase> list(PageParams pageParams,@RequestBody(required = false) QueryCourseParamDto queryCourseParamDto){
        SecurityUtil.XcUser user = SecurityUtil.getUser();

        Long companyId = null;
        if (!StringUtil.isEmpty(user.getCompanyId())){
            companyId = Long.parseLong(user.getCompanyId());
        }

        return courseBaseInfoService.queryCourseBaseList(companyId,pageParams, queryCourseParamDto);
    }

    @ApiOperation("新增课程基础")
    @PostMapping("/course")
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated({ValidationGroups.Insert.class}) AddCourseDto addCourseDto){
        Long companyId = 1232141425L;
        return courseBaseInfoService.createCourseBase(companyId,addCourseDto);
    }

    @ApiOperation("根据id查询课程")
    @GetMapping("/course/{courseId}")
    public CourseBaseInfoDto getCourseBaseById(@PathVariable Long courseId){
        ////取出当前用户身份
        //Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        //System.out.println(principal);
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        System.out.println(user.getUsername());
        return courseBaseInfoService.getCourseBaseById(courseId);
    }

    @ApiOperation("修改")
    @PutMapping("/course")
    public CourseBaseInfoDto updateCourseBase(@RequestBody EditCourseDto editCourseDto){
        Long companyId = 1232141425L;
        return courseBaseInfoService.updateCourseBase(companyId,editCourseDto);
    }

    @ApiOperation("删除课程")
    @DeleteMapping("/course/{courseId}")
    public void deleteCourse(@PathVariable Long courseId){
        Long companyId = 1232141425L;

        courseBaseInfoService.deleteCourse(companyId,courseId);
    }

}
