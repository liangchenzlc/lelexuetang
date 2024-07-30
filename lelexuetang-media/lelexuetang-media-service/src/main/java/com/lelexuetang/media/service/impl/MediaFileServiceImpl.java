package com.lelexuetang.media.service.impl;

import com.alibaba.nacos.common.utils.UuidUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.lelexuetang.base.exception.LeLeXueTangException;
import com.lelexuetang.base.model.PageParams;
import com.lelexuetang.base.model.PageResult;
import com.lelexuetang.base.model.RestResponse;
import com.lelexuetang.media.config.Bucket;
import com.lelexuetang.media.config.MinIoProperties;
import com.lelexuetang.media.mapper.MediaFilesMapper;
import com.lelexuetang.media.mapper.MediaProcessMapper;
import com.lelexuetang.media.model.dto.QueryMediaParamsDto;
import com.lelexuetang.media.model.dto.UploadFileParamsDto;
import com.lelexuetang.media.model.dto.UploadFileResultDto;
import com.lelexuetang.media.model.po.MediaFiles;
import com.lelexuetang.media.model.po.MediaProcess;
import com.lelexuetang.media.service.MediaFileService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/9/10 8:58
 */
@Slf4j
@Service
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    MediaFileService mediaFileProxy;

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    MediaProcessMapper mediaProcessMapper;

    @Autowired
    MinIoProperties minIoProperties;

    @Autowired
    MinioClient minioClient;


    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;

    }

    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath) {
        File file = new File(localFilePath);
        if (!file.exists()) {
            LeLeXueTangException.cast("文件不存在");
        }
        String oriFileName = uploadFileParamsDto.getFilename();
        // 获取扩展名
        String extension = getExtension(oriFileName);
        // 获取媒资类型
        String memmType = getMemmType(extension);
        // 获取文件路径，按年月日约定
        String filePath = getFilePath();
        // 获取文件名，按md5加密
        String fileName = getFileName(file);
        // 拼接文件路径
        String objectName = filePath + fileName + extension;
        // 上传文件到minIO
        Bucket bucket = minIoProperties.getBucket();
        boolean result = addMediaFilesToMinIO(localFilePath, objectName, bucket.getFiles(), memmType);
        if (!result) {
            LeLeXueTangException.cast("上传文件失败");
        }
        // 保存信息到数据库
        MediaFiles mediaFiles = mediaFileProxy.addMediaFilestoDb(companyId, fileName, uploadFileParamsDto, bucket.getFiles(), objectName);
        // 返回结果
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
        return uploadFileResultDto;
    }

    @Transactional
    public MediaFiles addMediaFilestoDb(Long companyId, String fileMd5, UploadFileParamsDto uploadFileParamsDto, String bucket, String objectName) {
        //从数据库查询文件
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles == null) {
            mediaFiles = new MediaFiles();
            //拷贝基本信息
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            mediaFiles.setId(fileMd5);
            mediaFiles.setFileId(fileMd5);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setUrl("/" + bucket + "/" + objectName);
            mediaFiles.setBucket(bucket);
            mediaFiles.setFilePath(objectName);
            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setAuditStatus("002003");
            mediaFiles.setStatus("1");
            //保存文件信息到文件表
            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert < 0) {
                log.error("保存文件信息到数据库失败,{}", mediaFiles.toString());
                LeLeXueTangException.cast("保存文件信息失败");
            }
            log.debug("保存文件信息到数据库成功,{}", mediaFiles.toString());
            addWaitingTask(mediaFiles);
        }
        return mediaFiles;
    }

    private void addWaitingTask(MediaFiles mediaFiles) {
        String filename = mediaFiles.getFilename();
        String extension = getExtension(filename);
        String memmType = getMemmType(extension);
        String memmType1 = getMemmType(".avi");
        if (memmType1.equals(memmType)) {
            MediaProcess mediaProcess = new MediaProcess();
            BeanUtils.copyProperties(mediaFiles, mediaProcess);
            mediaProcess.setCreateDate(LocalDateTime.now());
            mediaProcess.setStatus("1");
            mediaProcess.setFailCount(0);
            mediaProcess.setUrl(null);
            int insert = mediaProcessMapper.insert(mediaProcess);
            if (insert <= 0) {
                log.error("插入待处理任务表发生错误");
            }
            log.debug("插入待处理任务表成功");
        }
    }

    /**
     * 上传分块文件
     *
     * @param fileMd5
     * @param chunk
     * @param localTempFilePath
     * @return
     */
    @Override
    public RestResponse uploadChunk(String fileMd5, int chunk, String localTempFilePath) {
        String bucket = minIoProperties.getBucket().getVideofiles();
        String chunkFolderPath = getChunkFolderPath(fileMd5);
        String objectName = chunkFolderPath + chunk;
        String memmType = getMemmType(null);
        boolean b = addMediaFilesToMinIO(localTempFilePath, objectName, bucket, memmType);
        if (!b) {
            // 上传失败
            return RestResponse.error("上传失败", false);
        }
        return RestResponse.success(true);
    }

    /**
     * 块文件校验
     *
     * @param fileMd5
     * @param chunk
     * @return
     */
    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunk) {
        String bucket = minIoProperties.getBucket().getVideofiles();
        String chunkFolderPath = getChunkFolderPath(fileMd5);
        String objectName = chunkFolderPath + chunk;
        // 检车改块是否存在在minio中
        try {
            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build();
            GetObjectResponse objectResponse = minioClient.getObject(getObjectArgs);
            if (objectResponse != null) {
                return RestResponse.success(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RestResponse.success(false);
    }

    private String getChunkFolderPath(String fileMd5) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(fileMd5.substring(0, 1));
        stringBuilder.append("/");
        stringBuilder.append(fileMd5.substring(1, 2));
        stringBuilder.append("/");
        stringBuilder.append(fileMd5);
        stringBuilder.append("/chunk/");
        return stringBuilder.toString();
    }

    /**
     * 检查文件是否存在
     *
     * @param fileMd5
     * @return
     */

    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        // 检查数据库是否有记录
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles != null) {
            // 查看minio是否真的有文件
            try {
                GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                        .bucket(mediaFiles.getBucket())
                        .object(mediaFiles.getFilePath())
                        .build();
                GetObjectResponse objectResponse = minioClient.getObject(getObjectArgs);
                if (objectResponse != null) {
                    return RestResponse.success(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return RestResponse.success(false);
    }

    @Override
    public RestResponse mergechunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
        // 获取桶
        String bucket = minIoProperties.getBucket().getVideofiles();
        // 获取分块文件路径
        String chunkFolderPath = getChunkFolderPath(fileMd5);
        // 合并分块
        List<ComposeSource> sourceList = Stream.iterate(0, i -> i + 1).limit(chunkTotal)
                .map(i -> ComposeSource.builder().bucket(bucket).object(chunkFolderPath + i).build())
                .collect(Collectors.toList());

        String fileName = uploadFileParamsDto.getFilename();
        String extension = getExtension(fileName);
        String mergeObjectName = getMergeObjectName(fileMd5, extension);
        try {
            ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                    .bucket(bucket)
                    .object(mergeObjectName)
                    .sources(sourceList)
                    .build();
            minioClient.composeObject(composeObjectArgs);
        } catch (Exception e) {
            log.error("合并分块失败，bucket: {}, objectName: {}, 错误信息: {}", bucket, mergeObjectName, e.getMessage());
            return RestResponse.error("文件上传失败", -1);
        }
        // 校验文件是否一致
        File file = downloadFileFromMinIO(bucket, mergeObjectName);
        if (file == null) {
            return RestResponse.error("文件校验时下载失败", -1);
        }
        String mergeMd5 = null;
        try (FileInputStream inputStream = new FileInputStream(file)) {
            mergeMd5 = DigestUtils.md5Hex(inputStream);
            if (!fileMd5.equals(mergeMd5)) {
                return RestResponse.error("文件校验失败", -1);
            }
            // 文件一致, 获取文件大小
            uploadFileParamsDto.setFileSize(file.length());
        } catch (IOException e) {
            e.printStackTrace();
            log.error("检验文件md5时不一致, soure_md5: {}, merge_md5: {}", fileMd5, mergeMd5);
            return RestResponse.error("文件校验失败", -1);
        }
        // 插入数据库
        // 事务生效，要用代理对象
        MediaFiles mediaFiles = mediaFileProxy.addMediaFilestoDb(companyId, fileMd5, uploadFileParamsDto, bucket, mergeObjectName);
        // 删除分块文件
        deleteChunkFiles(bucket, chunkTotal, chunkFolderPath);
        return RestResponse.success(true);
    }

    private void deleteChunkFiles(String bucket, int chunkTotal, String chunkFolderPath) {

        List<DeleteObject> deleteObjects = Stream.iterate(0, i -> i + 1)
                .limit(chunkTotal)
                .map(i -> new DeleteObject(chunkFolderPath + i))
                .collect(Collectors.toList());

        RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder()
                .bucket(bucket)
                .objects(deleteObjects)
                .build();
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
        results.forEach(result -> {
            try {
                DeleteError deleteError = result.get();
            } catch (Exception e) {
                e.printStackTrace();
                log.error("删除分块文件失败, bucket: {}, objectName: {}, 错误信息: {}",
                        bucket, chunkFolderPath, e.getMessage());
            }
        });
    }

    // 下载文件
    @Override
    public File downloadFileFromMinIO(String bucket, String mergeObjectName) {
        try {
            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(mergeObjectName)
                    .build();
            InputStream objectResponse = minioClient.getObject(getObjectArgs);
            File tempFile = File.createTempFile("mergefile", ".temp");
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            IOUtils.copy(objectResponse, outputStream);
            log.debug("下载文件成功, bucket: {}, objectName: {}, downFilePath: {}", bucket, mergeObjectName, tempFile.getAbsolutePath());
            return tempFile;
        } catch (Exception e) {
            log.error("下载文件失败, bucket: {}, objectName: {}, 错误信息: {}",
                    bucket, mergeObjectName, e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // 获取合并后的文件名
    private String getMergeObjectName(String fileMd5, String extension) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(fileMd5.substring(0, 1));
        stringBuilder.append("/");
        stringBuilder.append(fileMd5.substring(1, 2));
        stringBuilder.append("/");
        stringBuilder.append(fileMd5);
        stringBuilder.append("/");
        stringBuilder.append(fileMd5 + extension);
        return stringBuilder.toString();
    }

    // 上传文件到minIO
    public boolean addMediaFilesToMinIO(String localFilePath, String objectName, String bucket, String memmType) {
        try {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .filename(localFilePath)
                    .bucket(bucket)
                    .object(objectName)
                    .contentType(memmType)
                    .build();
            minioClient.uploadObject(uploadObjectArgs);
            log.debug("上传文件成功, bucket: {}, objectName: {}", bucket, objectName);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("上传文件失败, bucket: {}, objectName: {}, 错误信息: {}", bucket, objectName, e.getMessage());
            return false;
        }
    }

    // 获取扩展名
    private String getExtension(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf("."));
        return extension;
    }

    // 获取文件名
    private String getFileName(File file) {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            return DigestUtils.md5Hex(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 获取文件路径
    private String getFilePath() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String filePath = format.format(new Date()).replace("-", "/") + "/";
        return filePath;
    }


    // 根据扩展名获取mimeType
    private String getMemmType(String extension) {
        if (extension == null) {
            extension = "";
        }

        String memmType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        //根据扩展名取出mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
        if (extensionMatch != null) {
            memmType = extensionMatch.getMimeType();
        }
        return memmType;
    }
}
