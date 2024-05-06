package com.lelexuetang.content.service.impl;

import com.lelexuetang.content.mapper.CourseCategoryMapper;
import com.lelexuetang.content.model.dto.CourseCategoryTreeDto;
import com.lelexuetang.content.service.CourseCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CourseCategoryServiceImpl implements CourseCategoryService {

    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    /**
     * 根据指定的ID，构建课程分类树形结构列表。
     *
     * @param id 主分类的ID，用于构建该分类及其子分类的树形结构。
     * @return 返回一个包含指定分类及其子分类的树形结构列表。
     */
    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {
        // 从数据库中根据ID查询所有的分类节点，并转换为树形结构DTO列表
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.selectTreeNodes(id);

        // 过滤掉当前分类节点，以避免出现循环引用，并将剩余节点按ID映射到Map中
        Map<String, CourseCategoryTreeDto> mapTemp = courseCategoryTreeDtos.stream().filter(item -> !item.getId().equals(id))
                .collect(Collectors.toMap(key -> key.getId(), value -> value, (key1, key2) -> key2));

        // 初始化用于存储最终树形结构的列表
        List<CourseCategoryTreeDto> courseCategoryList = new ArrayList<>();
        // 遍历所有节点，将父ID为指定ID的节点添加到最终列表，并为它们的父节点添加子节点
        courseCategoryTreeDtos.stream()
                .forEach(item -> {
                    if(item.getParentid().equals(id)) {
                        courseCategoryList.add(item);
                    }
                    CourseCategoryTreeDto courseCategoryTreeDto = mapTemp.get(item.getParentid());
                    if(courseCategoryTreeDto != null) {
                        // 如果父节点的子节点列表为空，则初始化为空列表，然后添加当前节点
                        if(courseCategoryTreeDto.getChildrenTreeNodes() == null) {
                            courseCategoryTreeDto.setChildrenTreeNodes(new ArrayList<CourseCategoryTreeDto>());
                        }
                        courseCategoryTreeDto.getChildrenTreeNodes().add(item);
                    }
                });

        return courseCategoryList;
    }

}
