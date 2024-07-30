package com.lelexuetang.media.service;


import com.lelexuetang.base.model.PageParams;
import com.lelexuetang.base.model.PageResult;
import com.lelexuetang.base.model.RestResponse;
import com.lelexuetang.media.model.dto.QueryMediaParamsDto;
import com.lelexuetang.media.model.dto.UploadFileParamsDto;
import com.lelexuetang.media.model.dto.UploadFileResultDto;
import com.lelexuetang.media.model.po.MediaFiles;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.File;
import java.util.List;

/**
 * @description 媒资文件管理业务类
 * @author Mr.M
 * @date 2022/9/10 8:55
 * @version 1.0
 */
public interface MediaFileService {

 /**
  * @description 媒资文件查询方法
  * @param pageParams 分页参数
  * @param queryMediaParamsDto 查询条件
  * @return com.xuecheng.base.model.PageResult<com.xuecheng.media.model.po.MediaFiles>
  * @author Mr.M
  * @date 2022/9/10 8:57
 */
 public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);

 /**
  * 上传文件
  * @param companyId
  * @param uploadFileParamsDto
  * @param localFilePath
  * @return
  */
 public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath);

 public boolean addMediaFilesToMinIO(String localPath, String memmType, String bucketName, String objectName);

 /**
  * 添加媒资文件信息到数据库
  * @param companyId
  * @param fileName
  * @param uploadFileParamsDto
  * @param files
  * @param objectName
  * @return
  */
 MediaFiles addMediaFilestoDb(Long companyId, String fileName, UploadFileParamsDto uploadFileParamsDto, String files, String objectName);

 /**
  * 上传分块文件
  * @param fileMd5
  * @param chunk
  * @param localTempFilePath
  * @return
  */
 RestResponse uploadChunk(String fileMd5, int chunk, String localTempFilePath);

 /**
  * 检查分块文件
  * @param fileMd5
  * @param chunk
  * @return
  */
 RestResponse<Boolean> checkChunk(String fileMd5, int chunk);


 /**
  * 检查文件
  * @param fileMd5
  * @return
  */
 RestResponse<Boolean> checkFile(String fileMd5);

 /** @param fileMd5  文件md5
 * @param chunkTotal 分块总和
 * @param uploadFileParamsDto 文件信息
 * @return com.xuecheng.base.model.RestResponse
 * @author Mr.M
 * @date 2022/9/13 15:56
 */
 RestResponse mergechunks(Long companyId,String fileMd5,int chunkTotal,UploadFileParamsDto uploadFileParamsDto);


 /**
  * 下载文件
  * @param bucket
  * @param mergeObjectName
  * @return
  */
 File downloadFileFromMinIO(String bucket, String mergeObjectName);
}
