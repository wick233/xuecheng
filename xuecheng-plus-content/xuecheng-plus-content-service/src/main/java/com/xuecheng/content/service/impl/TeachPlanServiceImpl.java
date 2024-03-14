package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.TeachPlanMapper;
import com.xuecheng.content.mapper.TeachPlanMediaMapper;
import com.xuecheng.content.model.dto.SaveTeachPlanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.TeachPlan;
import com.xuecheng.content.model.po.TeachPlanMedia;
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
    @Autowired
    TeachPlanMediaMapper mediaMapper;

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
            Integer orderby = teachPlanMapper.generateOrderby(parentId, courseId);
            teachPlan.setOrderby(orderby);

            teachPlanMapper.insert(teachPlan);
        } else {
            //修改
            TeachPlan teachPlan = teachPlanMapper.selectById(id);
            BeanUtils.copyProperties(dto, teachPlan);
            teachPlanMapper.updateById(teachPlan);
        }
    }

    @Override
    public void deleteTeachPlan(Long teachPlanId) {
        TeachPlan teachPlan = teachPlanMapper.selectById(teachPlanId);
        Integer grade = teachPlan.getGrade();
        //大章节删除
        if (grade == 1) {
            LambdaQueryWrapper<TeachPlan> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TeachPlan::getParentid,teachPlan.getId());
            if (teachPlanMapper.selectCount(queryWrapper) > 0) {
                XueChengPlusException.cast("删除失败！该章节下还存有内容！");
            }else teachPlanMapper.deleteById(teachPlan);
        } else if(grade == 2){
            //小章节删除
            mediaMapper.delete(new LambdaQueryWrapper<TeachPlanMedia>().eq(TeachPlanMedia::getTeachplanId,teachPlanId));
            teachPlanMapper.deleteById(teachPlan);
        }else XueChengPlusException.cast("系统数据异常");
    }

    @Override
    public void moveUpTeachPlan(Long teachPlanId) {
        TeachPlan teachPlan = teachPlanMapper.selectById(teachPlanId);
        Integer orderby = teachPlan.getOrderby();
        LambdaQueryWrapper<TeachPlan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TeachPlan::getCourseId,teachPlan.getCourseId())
                .eq(TeachPlan::getParentid,teachPlan.getParentid())
                .lt(TeachPlan::getOrderby,orderby)
                .orderByDesc(TeachPlan::getOrderby)
                .last("limit 1");
        TeachPlan moveObj = teachPlanMapper.selectOne(queryWrapper);
        if (moveObj == null) XueChengPlusException.cast("已在最顶端");
        teachPlan.setOrderby(moveObj.getOrderby());
        moveObj.setOrderby(orderby);
        teachPlanMapper.updateById(teachPlan);
        teachPlanMapper.updateById(moveObj);
    }

    @Override
    public void moveDownTeachPlan(Long teachPlanId) {
        TeachPlan teachPlan = teachPlanMapper.selectById(teachPlanId);
        Integer orderby = teachPlan.getOrderby();
        LambdaQueryWrapper<TeachPlan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TeachPlan::getCourseId,teachPlan.getCourseId())
                .eq(TeachPlan::getParentid,teachPlan.getParentid())
                .gt(TeachPlan::getOrderby,orderby)
                .orderByAsc(TeachPlan::getOrderby)
                .last("limit 1");
        TeachPlan moveObj = teachPlanMapper.selectOne(queryWrapper);
        if (moveObj == null) XueChengPlusException.cast("已在最底端");
        teachPlan.setOrderby(moveObj.getOrderby());
        moveObj.setOrderby(orderby);
        teachPlanMapper.updateById(teachPlan);
        teachPlanMapper.updateById(moveObj);
    }

}














