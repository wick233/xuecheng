package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.SaveTeachPlanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;

import java.util.List;

public interface TeachPlanService {
    List<TeachPlanDto> findTeachPlanTree(Long courseId);

    void saveTeachPlan(SaveTeachPlanDto dto);

    void deleteTeachPlan(Long teachPlanId);

    void moveUpTeachPlan(Long teachPlanId);

    void moveDownTeachPlan(Long teachPlanId);
}
