package com.lelexuetang.media.service.jobhandler;

import com.lelexuetang.base.utils.Mp4VideoUtil;
import com.lelexuetang.media.model.po.MediaProcess;
import com.lelexuetang.media.service.MediaFileService;
import com.lelexuetang.media.service.MediaProcessService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author : coderedma
 * @Version: 1.0
 * @Desc :
 * @date : 2024/6/16 14:24
 */
@Component
@Slf4j
public class VideoTask {

    @Autowired
    private MediaProcessService mediaProcessService;

    @Autowired
    private MediaFileService mediaFileService;

    @Value("${ffmpeg_apth}")
    private String ffmpegPath;

    @XxlJob("videoJobHandler")
    public void videoJobHandler() throws Exception {
        log.debug("开始执行视频处理任务");
        // 获得分片
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.debug("shardIndex:{},shardTotal:{}", shardIndex, shardTotal);
        List<MediaProcess> mediaProcessList = null;
        int size = 0;
        try{
            // 去除cpu核心数作为一次处理数据的个数
            int processors = Runtime.getRuntime().availableProcessors();
            log.debug("cpu核心数:{}", processors);
            mediaProcessList = mediaProcessService.getMediaProcessList(shardIndex, shardTotal, processors);
            size = mediaProcessList.size();

            log.debug("本次处理数据个数:{}", size);
            if (size <= 0) {
                return;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }

        ExecutorService executorService = Executors.newFixedThreadPool(size);
        CountDownLatch countDownLatch = new CountDownLatch(size);

        mediaProcessList.forEach(mediaProcess -> {
            executorService.execute(() -> {
                // 任务Id
                Long taskId = mediaProcess.getId();
                // 获取任务,乐观锁
                boolean b = mediaProcessService.startTask(taskId);
                if (!b) return; // 获取任务失败

                log.debug("开始执行任务:{}", taskId);

                String bucket = mediaProcess.getBucket();
                String filePath = mediaProcess.getFilePath();
                String fileId = mediaProcess.getFileId();
                File downFile = mediaFileService.downLoadFile(bucket, filePath);

                if (downFile == null) {
                    log.debug("下载待处理文件失败,originalFile:{}", mediaProcess.getBucket().concat(mediaProcess.getFilePath()));
                    mediaProcessService.saveProcessFinishStatus(mediaProcess.getId(), "3", fileId, null, "下载待处理文件失败");
                    return;
                }

                File mp4File = null;
                try {
                    mp4File = File.createTempFile("mp4", ".mp4");
                }catch (Exception e) {
                    log.error("创建mp4临时文件失败");
                    mediaProcessService.saveProcessFinishStatus(mediaProcess.getId(), "3", fileId, null, "创建mp4临时文件失败");
                    return;
                }
                String mp4folderPath = mp4File.getAbsolutePath();
                String mp4Name = fileId.concat(".mp4");
                String videoPath = downFile.getAbsolutePath();

                String result = null;
                try {
                    Mp4VideoUtil mp4VideoUtil = new Mp4VideoUtil(
                            ffmpegPath,
                            videoPath,
                            mp4Name,
                            mp4folderPath
                    );

                    result = mp4VideoUtil.generateMp4();
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("处理视频文件:{},出错:{}", mediaProcess.getFilePath(), e.getMessage());
                }

                if (result.equals("success")) {
                    //记录错误信息d
                    log.error("处理视频失败,视频地址:{},错误信息:{}", bucket + filePath, result);
                    mediaProcessService.saveProcessFinishStatus(mediaProcess.getId(), "3", fileId, null, result);
                    return;
                }
                // 将mp4上传到minio
                String objectName = getFilePath(fileId, ".mp4");
                String url = "/" + bucket + "/" + objectName;

                try{
                    // todo 更新minio
                    // mediaFileService.addMediaFilesToMinIO(mp4File.getAbsolutePath(), "video/mp4", bucket, objectName);
                    //将url存储至数据，并更新状态为成功，并将待处理视频记录删除存入历史
                    mediaProcessService.saveProcessFinishStatus(mediaProcess.getId(), "2", fileId, url, null);
                }
                catch (Exception e) {
                    log.error("上传视频失败或入库失败,视频地址:{},错误信息:{}", bucket + objectName, e.getMessage());
                    //最终还是失败了
                    mediaProcessService.saveProcessFinishStatus(mediaProcess.getId(), "3", fileId, null, "处理后视频上传或入库失败");

                }finally {
                    countDownLatch.countDown();
                }

            });
        });
        //等待,给一个充裕的超时时间,防止无限等待，到达超时时间还没有处理完成则结束任务
        countDownLatch.await(30, TimeUnit.MINUTES);
    }

    private String getFilePath(String fileMd5,String fileExt){
        return   fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" +fileMd5 +fileExt;
    }

}
