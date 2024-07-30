package com.lelexuetang.media;

import io.minio.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Test
    public void testMinIoUpload2() throws Exception {
        String chunkFilePath = "D:\\凉尘\\Videos\\7月19日\\chunk";
        File chunkFile = new File(chunkFilePath);
        File[] files = chunkFile.listFiles();
        for (File file : files) {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket("testbucket")
                    .object("chunk/" + file.getName())
                    .filename(file.getAbsolutePath())
                    .build();
            minioClient.uploadObject(uploadObjectArgs);
        }
        System.out.println("上传成功");
    }

    @Test
    public void testMinIoMerge() throws Exception {

        List<ComposeSource> sourceList = new ArrayList<>();
        for(int i = 0; i <= 69; i++ ) {
            ComposeSource testbucket = ComposeSource.builder().bucket("testbucket").object("chunk/" + i).build();
            sourceList.add(testbucket);
        }
        ComposeObjectArgs composeObjectArgs =  ComposeObjectArgs.builder()
                .bucket("testbucket")
                .object("test.mp4")
                .sources(sourceList)
                .build();
        minioClient.composeObject(composeObjectArgs);
        System.out.println("合并成功");
    }

}
