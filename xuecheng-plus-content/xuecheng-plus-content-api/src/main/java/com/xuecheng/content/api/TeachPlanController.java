package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.SaveTeachPlanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.service.TeachPlanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

//import javax.websocket.server.PathParam;
import javax.websocket.server.PathParam;
import java.util.List;

/**
 * @Descriptions:
 * @Author: Twithu
 * @Date: 2024/3/14 下午 03:27
 * @Version: 1.0
 */
@Api(value = "课程计划编辑接口" ,tags = "课程计划编辑接口")
@RestController
@RequestMapping("/teachplan")
public class TeachPlanController {

    @Autowired
    TeachPlanService teachPlanService;

    @ApiOperation("查询课程计划树形结构")
    @GetMapping("{courseId}/tree-nodes")
    public List<TeachPlanDto> getTreeNodes(@PathVariable Long courseId){
        return teachPlanService.findTeachPlanTree(courseId);
    }

    @ApiOperation("课程计划创建或修改")
    @PostMapping()
    public void saveTeachPlan(@RequestBody SaveTeachPlanDto dto){
        teachPlanService.saveTeachPlan(dto);
    }

    @ApiOperation("删除课程计划")
    @DeleteMapping("/{teachPlanId}")
    public void deleteTeachPlan(@PathVariable Long teachPlanId){
        teachPlanService.deleteTeachPlan(teachPlanId);
    }

    @ApiOperation("上移课程计划")
    @PostMapping("moveup/{teachPlanId}")
    public void moveUpTeachPlan(@PathVariable Long teachPlanId){
        teachPlanService.moveUpTeachPlan(teachPlanId);
    }

    @ApiOperation("下移课程计划")
    @PostMapping("movedown/{teachPlanId}")
    public void moveDownTeachPlan(@PathVariable Long teachPlanId){
        teachPlanService.moveDownTeachPlan(teachPlanId);
    }

}
























