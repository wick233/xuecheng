package com.xuecheng;

import com.xuecheng.content.mapper.TeachPlanMapper;
import com.xuecheng.content.model.dto.TeachPlanDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @Descriptions:
 * @Author: Twithu
 * @Date: 2024/3/14 下午 06:46
 * @Version: 1.0
 */
@SpringBootTest
public class TeachPlanTest {
    @Autowired
    TeachPlanMapper teachPlanMapper;

    @Test
    void testSelectTreeNodes(){
        List<TeachPlanDto> teachPlanDtos = teachPlanMapper.selectTreeNodes(117L);
        System.out.println(teachPlanDtos);
    }
}
