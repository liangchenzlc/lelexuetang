package com.lelexuetang.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.lelexuetang.base.exception.LeLeXueTangException;
import com.lelexuetang.base.model.PageParams;
import com.lelexuetang.base.model.PageResult;
import com.lelexuetang.media.config.Bucket;
import com.lelexuetang.media.config.MinIoProperties;
import com.lelexuetang.media.mapper.MediaFilesMapper;
import com.lelexuetang.media.model.dto.QueryMediaParamsDto;
import com.lelexuetang.media.model.dto.UploadFileParamsDto;
import com.lelexuetang.media.model.dto.UploadFileResultDto;
import com.lelexuetang.media.model.po.MediaFiles;
import com.lelexuetang.media.service.MediaFileService;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;


/**
 * @description TODO
 * @author Mr.M
 * @date 2022/9/10 8:58
 * @version 1.0
 */
@Slf4j
 @Service
public class MediaFileServiceImpl implements MediaFileService {

 @Autowired
 MediaFileService mediaFileProxy;

  @Autowired
  MediaFilesMapper mediaFilesMapper;

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
  boolean result = upload(localFilePath, objectName, bucket.getFiles(), memmType);
  if(!result) {
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
 public MediaFiles addMediaFilestoDb(Long companyId,String fileMd5,UploadFileParamsDto uploadFileParamsDto,String bucket,String objectName) {
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
    log.error("保存文件信息到数据库失败,{}",mediaFiles.toString());
    LeLeXueTangException.cast("保存文件信息失败");
   }
   log.debug("保存文件信息到数据库成功,{}",mediaFiles.toString());

  }
  return mediaFiles;
 }

 // 上传文件到minIO
 private boolean upload(String localFilePath, String fileName, String bucket, String memType) {
  try {
   UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
           .filename(localFilePath)
           .bucket(bucket)
           .object(fileName)
           .contentType(memType)
           .build();
   minioClient.uploadObject(uploadObjectArgs);
   return true;
  }catch (Exception e) {
    e.printStackTrace();
    log.error("上传文件失败, bucket: {}, fileName: {}, 错误信息: {}", bucket, fileName, e.getMessage());
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
  if(extension == null) {
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
