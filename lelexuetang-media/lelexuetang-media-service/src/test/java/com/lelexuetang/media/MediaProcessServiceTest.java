package com.lelexuetang.media;

import com.lelexuetang.media.service.MediaProcessService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MediaProcessServiceTest {
    @Autowired
    MediaProcessService mediaProcessService;

    @Test
    public void test() {
        mediaProcessService.getMediaProcessList(1, 2, 2).stream()
                .forEach(mediaProcess -> {
                    System.out.println(mediaProcess);
        });
    }
}
