package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.content.mapper.TeachPlanMapper;
import com.xuecheng.content.model.dto.SaveTeachPlanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.TeachPlan;
import com.xuecheng.content.service.TeachPlanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Descriptions:
 * @Author: Twithu
 * @Date: 2024/3/14 下午 06:51
 * @Version: 1.0
 */
@Service
public class TeachPlanServiceImpl implements TeachPlanService {

    @Autowired
    TeachPlanMapper teachPlanMapper;

    @Override
    public List<TeachPlanDto> findTeachPlanTree(Long courseId) {
        return teachPlanMapper.selectTreeNodes(courseId);
    }

    @Override
    public void saveTeachPlan(SaveTeachPlanDto dto) {
        Long id = dto.getId();
        if (id == null) {
            //新增
            TeachPlan teachPlan = new TeachPlan();
            BeanUtils.copyProperties(dto, teachPlan);
            //确定排序字段,找到同级节点个数，排序为+1
            Long parentId = dto.getParentid();
            Long courseId = dto.getCourseId();
            Integer count = getTeachPlanCount(parentId, courseId);
            teachPlan.setOrderby(count);

            teachPlanMapper.insert(teachPlan);
        } else {
            //修改
            TeachPlan teachPlan = teachPlanMapper.selectById(id);
            BeanUtils.copyProperties(dto, teachPlan);
            teachPlanMapper.updateById(teachPlan);
        }
    }

    private Integer getTeachPlanCount(Long parentId, Long courseId) {

        LambdaQueryWrapper<TeachPlan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TeachPlan::getParentid, parentId).eq(TeachPlan::getCourseId, courseId);
        return teachPlanMapper.selectCount(queryWrapper) + 1;
    }
}














