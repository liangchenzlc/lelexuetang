package com.lelexuetang.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lelexuetang.base.exception.CommonError;
import com.lelexuetang.base.exception.LeLeXueTangException;
import com.lelexuetang.content.mapper.TeachplanMapper;
import com.lelexuetang.content.model.dto.SaveTeachplanDto;
import com.lelexuetang.content.model.dto.TeachplanDto;
import com.lelexuetang.content.model.po.Teachplan;
import com.lelexuetang.content.service.TeachplanService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TeachplanServiceImpl implements TeachplanService {
    @Autowired
    private TeachplanMapper teachplanMapper;


    /**
     * 查找指定课程ID的课程教学计划树
     *
     * @param courseId 课程ID，用于查找对应的教学计划
     * @return 返回教学计划树结构列表，其中包含了教学计划的层次结构
     */
    @Override
    public List<TeachplanDto> findTeachplanTree(Long courseId) {
        // 从数据库中根据课程ID查询所有的教学计划，并转换为教学计划DTO列表
        List<TeachplanDto> teachplanDtos = teachplanMapper.selectTreeNotes(courseId);

        // 使用stream将教学计划DTO列表转换为Map，以便根据ID快速查找对应的DTO对象
        Map<Long, TeachplanDto> mapTemp = teachplanDtos.stream()
                .collect(Collectors.toMap(key -> key.getId(), value -> value, (key1, key2) -> key2));

        // 创建一个列表，用于存放根节点教学计划
        List<TeachplanDto> teachplanDtoList = new ArrayList<>();

        // 遍历所有教学计划，构建教学计划的树状结构
        teachplanDtos.stream().forEach(item -> {
            // 如果是根节点，则添加到结果列表中
            if(item.getParentid().equals(0L)) {
                teachplanDtoList.add(item);
            }
            // 获取当前教学计划的父教学计划
            TeachplanDto teachplanDto = mapTemp.get(item.getParentid());
            if(teachplanDto != null) {
                // 如果父教学计划的子节点列表为空，则初始化为新列表
                if(teachplanDto.getTeachPlanTreeNodes() == null) {
                    teachplanDto.setTeachPlanTreeNodes(new ArrayList<TeachplanDto>());
                }
                // 将当前教学计划添加到其父教学计划的子节点列表中
                teachplanDto.getTeachPlanTreeNodes().add(item);
            }
        });

        return teachplanDtoList;
    }

    private int getOrderBy(Long courseId, Long parentId) {
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId, courseId)
                .eq(Teachplan::getParentid, parentId);
        return teachplanMapper.selectCount(queryWrapper) + 1;
    }

    @Transactional
    @Override
    public void saveTeachplan(SaveTeachplanDto teachplanDto) {
        // 检查输入参数是否为空
        if (teachplanDto == null || teachplanDto.getCourseId() == null || teachplanDto.getParentid() == null) {
            LeLeXueTangException.cast(CommonError.OBJECT_NOT_FOUND);
        }
        Long id = teachplanDto.getId();
        if(id == null) {
            // 添加
            Teachplan teachplan = new Teachplan();
            try {
                // 复制属性值，考虑异常处理
                BeanUtils.copyProperties(teachplanDto, teachplan);
            } catch (Exception e) {
                e.printStackTrace();
                LeLeXueTangException.cast("属性复制失败");
            }
            teachplan.setCreateDate(LocalDateTime.now());
            // 保证参数不为空，使用Optional避免直接null
            int orderBy = Optional.ofNullable(getOrderBy(teachplanDto.getCourseId(), teachplanDto.getParentid()))
                    .orElseThrow(() -> new LeLeXueTangException("获取排序值时发生错误"));
            teachplan.setOrderby(orderBy);
            teachplanMapper.insert(teachplan);
        } else {
            // 修改
            Teachplan teachplan = Optional.ofNullable(teachplanMapper.selectById(id))
                    .orElseThrow(() -> new LeLeXueTangException("教学计划不存在"));

            BeanUtils.copyProperties(teachplanDto, teachplan);
            teachplan.setChangeDate(LocalDateTime.now());

            teachplanMapper.updateById(teachplan);
        }
    }
}
