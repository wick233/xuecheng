package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.TeachPlan;
import com.xuecheng.content.model.po.TeachPlanMedia;

import java.util.List;

/**
 * @Descriptions:
 * @Author: Twithu
 * @Date: 2024/3/14 下午 03:18
 * @Version: 1.0
 */
public class TeachPlanDto extends TeachPlan {
    //媒介信息
    TeachPlanMedia teachPlanMedia;

    List<TeachPlanDto> teachPlanTreeNodes;
}
