package com.xuecheng.content.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Descriptions:
 * @Author: Twithu
 * @Date: 2024/3/14 上午 11:27
 * @Version: 1.0
 */
@Data
@ApiModel(value = "EditCourseDto",description = "修改课程基本信息")
public class EditCourseDto extends AddCourseDto{

    @ApiModelProperty(value = "课程id",required = true)
    private long id;
}
