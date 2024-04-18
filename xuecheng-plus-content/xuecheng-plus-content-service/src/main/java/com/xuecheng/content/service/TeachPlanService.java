package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.BindTeachPlanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachPlanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.TeachPlanMedia;

import java.util.List;

public interface TeachPlanService {
    List<TeachPlanDto> findTeachPlanTree(Long courseId);

    void saveTeachPlan(SaveTeachPlanDto dto);

    void deleteTeachPlan(Long teachPlanId);

    void moveUpTeachPlan(Long teachPlanId);

    void moveDownTeachPlan(Long teachPlanId);

    void associateMedia(BindTeachPlanMediaDto bindTeachPlanMediaDto);

    void disassociateMedia(String teachPlanId, String mediaId);
}
