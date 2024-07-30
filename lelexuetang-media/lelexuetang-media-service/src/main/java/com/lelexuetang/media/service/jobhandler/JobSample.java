package com.lelexuetang.media.service.jobhandler;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@Slf4j
public class JobSample {
    @XxlJob("demoSample")
    public void run()throws Exception {
        System.out.println("开始测试任务~");
    }

    // 分片广播
    @XxlJob("splitDemo")
    public void split()throws Exception {
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        long currented = System.currentTimeMillis();
        System.out.println(currented +"  shardIndex:"+shardIndex+",shardTotal:"+shardTotal);
    }
}
