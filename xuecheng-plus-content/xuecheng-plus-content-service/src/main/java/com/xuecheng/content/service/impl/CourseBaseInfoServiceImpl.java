package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.*;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamDto;
import com.xuecheng.content.model.po.*;
import com.xuecheng.content.service.CourseBaseInfoService;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 课程信息管理业务接口实现类
 **/
@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {

    @Autowired
    private CourseBaseMapper courseBaseMapper;
    @Autowired
    private CourseMarketMapper courseMarketMapper;
    @Autowired
    private CourseCategoryMapper courseCategoryMapper;
    @Autowired
    private TeachPlanMapper teachPlanMapper;
    @Autowired
    private CourseTeacherMapper courseTeacherMapper;

    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamDto queryCourseParamDto) {
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(queryCourseParamDto.getCourseName()), CourseBase::getName, queryCourseParamDto.getCourseName());
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamDto.getAuditStatus()), CourseBase::getAuditStatus, queryCourseParamDto.getAuditStatus());
        //TODO 课程发布状态查询
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamDto.getPublishStatus()), CourseBase::getStatus, queryCourseParamDto.getPublishStatus());
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);
        List<CourseBase> records = pageResult.getRecords();
        long total = pageResult.getTotal();
        return new PageResult<>(records, total, pageParams.getPageNo(), pageParams.getPageSize());
    }

    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto dto) {

        CourseBase courseBaseNew = new CourseBase();
        BeanUtils.copyProperties(dto, courseBaseNew);
        //设置审核状态
        courseBaseNew.setAuditStatus("202002");
        //设置发布状态
        courseBaseNew.setStatus("203001");
        //机构id
        courseBaseNew.setCompanyId(companyId);
        //添加时间
        courseBaseNew.setCreateDate(LocalDateTime.now());

        int insert = courseBaseMapper.insert(courseBaseNew);

        if (insert <= 0) {
            throw new RuntimeException("新增课程失败");
        }

        //向课程营销表保存课程营销信息
        CourseMarket courseMarket = new CourseMarket();
        Long courseId = courseBaseNew.getId();
        BeanUtils.copyProperties(dto, courseMarket);
        courseMarket.setId(courseId);
        int i = saveCourseMarket(courseMarket);
        if (i < 0) {
            throw new RuntimeException("保存课程营销信息失败");
        }
        //查询课程基本信息及营销信息并返回
        return getCourseBaseInfo(courseId);
    }

    @Override
    public CourseBaseInfoDto getCourseBaseById(Long courseId) {
        return getCourseBaseInfo(courseId);
    }

    @Override
    @Transactional
    public CourseBaseInfoDto updateCourseBase(Long companyId,EditCourseDto dto) {
        long courseId = dto.getId();
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null) {
            XueChengPlusException.cast("课程不存在");
        }

        //校验本机构只能修改本机构的课程
        if (!companyId.equals(courseBase.getCompanyId())){
            XueChengPlusException.cast("校验本机构只能修改本机构的课程");
        }

        //封装基本信息并更新到数据库
        BeanUtils.copyProperties(dto,courseBase);
        courseBase.setChangeDate(LocalDateTime.now());
        courseBaseMapper.updateById(courseBase);

        //封装营销信息并更新到数据库
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(dto,courseMarket);
        saveCourseMarket(courseMarket);

        return getCourseBaseById(courseId);
    }

    @Override
    public void deleteCourse(Long companyId, Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (!Objects.equals(companyId, courseBase.getCompanyId())) XueChengPlusException.cast("权限不足！");
        else{
            if (!"202002".equals(courseBase.getAuditStatus())) {
                XueChengPlusException.cast("课程信息已提交，删除失败！");
            }
            courseBaseMapper.deleteById(courseId);
            courseMarketMapper.deleteById(courseId);
            teachPlanMapper.delete(new LambdaQueryWrapper<TeachPlan>().eq(TeachPlan::getCourseId,courseId));
            courseTeacherMapper.delete(new LambdaQueryWrapper<CourseTeacher>().eq(CourseTeacher::getCourseId,courseId));
        }
    }

    private int saveCourseMarket(CourseMarket courseMarket) {
        //收费规则
        String charge = courseMarket.getCharge();
        if (StringUtils.isBlank(charge)) {
            throw new RuntimeException("收费规则没有选择");
        }

        //收费价格规则
        if (charge.equals("201001")) {
            if (courseMarket.getPrice() == null || courseMarket.getPrice() <= 0) {
                //throw new RuntimeException("收费价格不能为空且须大于0");
                XueChengPlusException.cast("收费价格不能为空且须大于0");
            }
        }

        CourseMarket courseMarketObj = courseMarketMapper.selectById(courseMarket.getId());
        if (courseMarketObj == null) {
            return courseMarketMapper.insert(courseMarket);
        } else {
            BeanUtils.copyProperties(courseMarket, courseMarketObj);
            courseMarketObj.setId(courseMarket.getId());
            return courseMarketMapper.updateById(courseMarketObj);
        }
    }

    //根据id查询课程，包括基本信息和营销信息
    public CourseBaseInfoDto getCourseBaseInfo(long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null) return null;
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        CourseBaseInfoDto dto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase, dto);
        if (courseMarket != null) {
            BeanUtils.copyProperties(courseMarket, dto);
        }

        CourseCategory courseCategoryBySt = courseCategoryMapper.selectById(courseBase.getSt());
        dto.setStName(courseCategoryBySt.getName());
        CourseCategory courseCategoryByMt = courseCategoryMapper.selectById(courseBase.getMt());
        dto.setMtName(courseCategoryByMt.getName());

        return dto;
    }


}























