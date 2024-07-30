package com.lelexuetang.media.service.impl;

import com.lelexuetang.media.mapper.MediaFilesMapper;
import com.lelexuetang.media.mapper.MediaProcessHistoryMapper;
import com.lelexuetang.media.mapper.MediaProcessMapper;
import com.lelexuetang.media.model.po.MediaFiles;
import com.lelexuetang.media.model.po.MediaProcess;
import com.lelexuetang.media.model.po.MediaProcessHistory;
import com.lelexuetang.media.service.MediaProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class MediaProcessServiceImpl implements MediaProcessService {

    @Autowired
    MediaProcessMapper mediaProcessMapper;

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    MediaProcessHistoryMapper mediaProcessHistoryMapper;

    @Override
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count) {
        List<MediaProcess> mediaProcessList = mediaProcessMapper.selectListByShardIndex(shardIndex, shardTotal, count);
        return mediaProcessList == null ? Collections.emptyList() : mediaProcessList;
    }

    @Override
    public boolean startTask(long id) {
        int result = mediaProcessMapper.startTask(id);
        return result <= 0 ? false : true;
    }

    @Transactional
    @Override
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg) {
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
        if(mediaProcess == null) return;

        if(status.equals("3")) {
            mediaProcess.setStatus("3");
            mediaProcess.setFailCount(mediaProcess.getFailCount() + 1);
            mediaProcess.setFinishDate(LocalDateTime.now());
            mediaProcess.setErrormsg(errorMsg);
            mediaProcessMapper.updateById(mediaProcess);
            return;
        }

        // 处理任务成功
        // 1.更新媒资文件表
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
        mediaFiles.setUrl(url);
        mediaFilesMapper.updateById(mediaFiles);

        // 2.更新任务记录表
        mediaProcess.setStatus("2");
        mediaProcess.setFinishDate(LocalDateTime.now());
        mediaProcess.setUrl(url);
        mediaProcessMapper.updateById(mediaProcess);

        // 3.更新历史任务记录表
        MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
        BeanUtils.copyProperties(mediaProcess, mediaProcessHistory);
        mediaProcessHistoryMapper.insert(mediaProcessHistory);

        // 4.删除任务记录表
        mediaProcessMapper.deleteById(taskId);
    }
}
