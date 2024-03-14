package com.xuecheng.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.TeachPlan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 课程计划 Mapper 接口
 * </p>
 *
 * @author itcast
 */
@Mapper
public interface TeachPlanMapper extends BaseMapper<TeachPlan> {

    public List<TeachPlanDto> selectTreeNodes(long courseId);

    @Select("select max(orderby)+1 from teachplan where parentid = #{parentId} and course_id = #{courseId}")
    Integer generateOrderby(@Param("parentId") Long parentId, @Param("courseId") Long courseId);
}
