package com.lelexuetang.system;

import com.lelexuetang.SystemApplicationTest;
import com.lelexuetang.system.model.po.Dictionary;
import com.lelexuetang.system.service.DictionaryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;


@SpringBootTest(classes = SystemApplicationTest.class)
public class SystemMapperTest {
    @Autowired
    private DictionaryService dictionaryService;

    @Test
    public void testDictionaryMapper() {
        List<Dictionary> dictionaries = dictionaryService.queryAll();
        dictionaries.forEach(System.out::println);
    }
}
