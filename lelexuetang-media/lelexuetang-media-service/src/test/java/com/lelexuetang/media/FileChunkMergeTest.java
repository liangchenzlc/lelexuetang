package com.lelexuetang.media;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FileChunkMergeTest {

    @Test
    public void testChunk() throws Exception {
     String soureFilePath = "D:\\凉尘\\Videos\\7月19日\\7月19日.mp4";
     String chunkFilePath = "D:\\凉尘\\Videos\\7月19日\\chunk";
     File sourceFile = new File(soureFilePath);
     File chunkFile = new File(chunkFilePath);
     if(!chunkFile.exists()) {
         chunkFile.mkdirs();
     }
     int chunkSize = 1024 * 1024 * 5;
     int chunkNum = (int) Math.ceil(sourceFile.length() * 1.0 / chunkSize);

     RandomAccessFile ref_r = new RandomAccessFile(sourceFile,"r");
     Long startTime = System.currentTimeMillis();
     for(int i = 0; i < chunkNum; i++) {
         RandomAccessFile ref_w = new RandomAccessFile(new File(chunkFilePath + "\\" + i),"rw");
         int len = -1;
         byte[] buf = new byte[1024 * 2];
         while((len = ref_r.read(buf)) != -1) {
             ref_w.write(buf,0,len);
             if(ref_w.length() >= chunkSize) {
                 break;
             }
         }
         ref_w.close();
     }
     Long endTime = System.currentTimeMillis();
     ref_r.close();
     System.out.println("分块完成");
     System.out.println("分块耗时：" + (endTime - startTime) + "ms");

    }
    @Test
    public void testMerge() throws Exception {
        String soureFilePath = "D:\\凉尘\\Videos\\7月19日\\7月19日.mp4";
        String chunkFilePath = "D:\\凉尘\\Videos\\7月19日\\chunk";
        String mergeFilePath = "D:\\凉尘\\Videos\\7月19日\\111.mp4";

        File sourceFile = new File(soureFilePath);
        RandomAccessFile ref_w = new RandomAccessFile(new File(mergeFilePath),"rw");
        File chunkFile = new File(chunkFilePath);
        File[] files = chunkFile.listFiles();
        List<File> fileList = Arrays.asList(files);

        Collections.sort(fileList, (o1, o2) -> Integer.parseInt(o1.getName()) - Integer.parseInt(o2.getName()));
        Long startTime = System.currentTimeMillis();
        for (File file : fileList) {
            RandomAccessFile ref_r = new RandomAccessFile(file,"r");
            int len = -1;
            byte[] buf = new byte[1024 * 2];
            while((len = ref_r.read(buf)) != -1) {
                ref_w.write(buf,0,len);
            }
            ref_r.close();
        }
        Long endTime = System.currentTimeMillis();
        ref_w.close();
        FileInputStream mergeInputStream = new FileInputStream(mergeFilePath);
        FileInputStream sourceInputStream = new FileInputStream(sourceFile);
        String mergeMd5 = DigestUtils.md5Hex(mergeInputStream);
        String sourceMd5 = DigestUtils.md5Hex(sourceInputStream);
        if (mergeMd5.equals(sourceMd5)) {
            System.out.println("合并成功");
            System.out.println("合并耗时：" + (endTime - startTime) + "ms");
        }
    }
}
