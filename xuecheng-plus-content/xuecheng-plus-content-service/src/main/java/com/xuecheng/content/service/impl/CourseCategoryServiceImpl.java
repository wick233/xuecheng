package com.xuecheng.content.service.impl;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CourseCategoryServiceImpl implements CourseCategoryService {
    @Autowired
    public CourseCategoryMapper courseCategoryMapper;

    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {
        List<CourseCategoryTreeDto> list = courseCategoryMapper.selectTreeNodes(id);
        Map<String, CourseCategoryTreeDto> mapTemp = list.stream().filter(item -> !id.equals(item.getId()))
                .collect(Collectors.toMap(key -> key.getId(), v -> v, (key1, key2) -> key2));

        List<CourseCategoryTreeDto> resultList = new ArrayList<>();

        list.stream().filter(i -> !id.equals(i.getId()))
                .forEach(item -> {
                    //放父节点
                    if (item.getParentid().equals(id)) resultList.add(item);
                    //找当前节点的父节点
                    CourseCategoryTreeDto parent = mapTemp.get(item.getParentid());
                    if (parent != null) {//表示当前子节点有效
                        if (parent.getChildrenTreeNodes() == null) {//第一次添加为空，先声明
                            parent.setChildrenTreeNodes(new ArrayList<CourseCategoryTreeDto>());
                        }
                        //添加子节点
                        parent.getChildrenTreeNodes().add(item);
                    }
                });

        return resultList;
    }
}
