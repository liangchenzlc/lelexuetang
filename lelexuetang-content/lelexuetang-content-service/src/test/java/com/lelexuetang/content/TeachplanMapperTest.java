package com.lelexuetang.content;

import com.lelexuetang.content.mapper.TeachplanMapper;
import com.lelexuetang.content.model.dto.TeachplanDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootTest
public class TeachplanMapperTest {
    @Autowired
    private TeachplanMapper teachplanMapper;

    @Test
    public void testTeachplanMapper() {
        List<TeachplanDto> teachplanDtos = teachplanMapper.selectTreeNotes(117L);
        Map<Long, TeachplanDto> mapTemp = teachplanDtos.stream()
                .collect(Collectors.toMap(key -> key.getId(), value -> value, (key1, key2) -> key2));
        List<TeachplanDto> teachplanDtoList = new ArrayList<>();
        teachplanDtos.stream().forEach(item -> {
            if(item.getParentid().equals(0L)) {
                teachplanDtoList.add(item);
            }
            TeachplanDto teachplanDto = mapTemp.get(item.getParentid());
            if(teachplanDto != null) {
                if(teachplanDto.getTeachPlanTreeNodes() == null) {
                    teachplanDto.setTeachPlanTreeNodes(new ArrayList<TeachplanDto>());
                }
                teachplanDto.getTeachPlanTreeNodes().add(item);
            }
        });
        System.out.println(teachplanDtoList);
    }
}
