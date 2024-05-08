package com.lelexuetang.media;

import io.minio.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.nio.channels.FileChannel;

@SpringBootTest
public class MinIoClientTest {

    @Autowired
    MinioClient minioClient;

    @Test
    public void testMinIoUpload() {
        try {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket("testbucket")
                    .object("test.mp4")
                    .filename("D:\\凉尘\\Videos\\哇哈哈广告视频\\1.mp4")
                    .build();
            minioClient.uploadObject(uploadObjectArgs);
            System.out.println("上传成功");
        }catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    @Test
    public void testMinIoDownload() {
        try {
            minioClient.downloadObject(
                    DownloadObjectArgs.builder()
                            .bucket("testbucket")
                            .object("test.mp4")
                            .filename("D:\\凉尘\\Videos\\哇哈哈广告视频\\2.mp4")
                            .build());
            System.out.println("下载成功");
        }catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    @Test
    public void testMinIoDelete() {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket("testbucket")
                            .object("test.mp4")
                            .build());
            System.out.println("删除成功");
        }catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    @Test
    public void getMinIoObject() {
        try {
            FilterInputStream object = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket("testbucket")
                            .object("test.mp4")
                            .build());
            FileOutputStream outputStream = new FileOutputStream("D:\\凉尘\\Videos\\哇哈哈广告视频\\3.mp4");
            IOUtils.copy(object, outputStream);
            FileInputStream inputStream1 = new FileInputStream("D:\\凉尘\\Videos\\哇哈哈广告视频\\3.mp4");
            FileInputStream inputStream = new FileInputStream("D:\\凉尘\\Videos\\哇哈哈广告视频\\1.mp4");
            String soure_md5 = DigestUtils.md5Hex(inputStream);
            String target_md5 = DigestUtils.md5Hex(inputStream1);
            if (soure_md5.equals(target_md5)) {
                System.out.println("md5校验成功");
                System.out.println("下载成功");
            }
        }catch (Exception e) {
            e.fillInStackTrace();
        }
    }
}
