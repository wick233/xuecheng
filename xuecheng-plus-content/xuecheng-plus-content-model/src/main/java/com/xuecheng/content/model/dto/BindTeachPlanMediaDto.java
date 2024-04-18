package com.xuecheng.content.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Descriptions:
 * @Author: Twithu
 * @Date: 2024/4/18 上午 09:40
 * @Version: 1.0
 */
@Data
@ApiModel(value="BindTeachPlanMediaDto", description="教学计划-媒资绑定提交数据")
public class BindTeachPlanMediaDto {

    @ApiModelProperty(value = "媒资文件id", required = true)
    private String mediaId;

    @ApiModelProperty(value = "媒资文件名称", required = true)
    private String fileName;

    @ApiModelProperty(value = "课程计划标识", required = true)
    private Long teachplanId;


}
