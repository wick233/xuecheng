package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.QueryCourseParamDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.service.CourseBaseInfoService;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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
        //合法性校验
        if (StringUtils.isBlank(dto.getName())) {
            throw new RuntimeException("课程名称为空");
        }

        if (StringUtils.isBlank(dto.getMt())) {
            throw new RuntimeException("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getSt())) {
            throw new RuntimeException("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getGrade())) {
            throw new RuntimeException("课程等级为空");
        }

        if (StringUtils.isBlank(dto.getTeachmode())) {
            throw new RuntimeException("教育模式为空");
        }

        if (StringUtils.isBlank(dto.getUsers())) {
            throw new RuntimeException("适应人群为空");
        }

        if (StringUtils.isBlank(dto.getCharge())) {
            throw new RuntimeException("收费规则为空");
        }

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
        Long courseId = courseMarket.getId();
        BeanUtils.copyProperties(dto, courseMarket);
        courseBaseNew.setId(courseId);
        int i = saveCourseMarket(courseMarket);
        if (i < 0) {
            throw new RuntimeException("保存课程营销信息失败");
        }
        //查询课程基本信息及营销信息并返回
        return getCourseBaseInfo(courseId);
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
                throw new RuntimeException("收费价格不能为空且须大于0");
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























