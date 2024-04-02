package com.xuecheng.meida;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

/**
 * @Description
 * @Author Twithu
 * @Date 2024/4/2 23:42
 * @Version: 1.0
 */
public class BigFileTest {

    //测试分块
    @Test
    void testChunk() throws IOException {
        //找到源文件
        File sourceFile = new File("D:\\Test\\video.mp4");
        //分块文件存储路径
        String chunkFilePath = "D:\\Test\\chunk\\";
        //分块大小 数量
        int chunkSize = 1024 * 1024 *1;
        int chunkNum = (int) Math.ceil(sourceFile.length() * 1.0 / chunkSize);

        //使用流从源文件读数据，向分块文件写数据
        byte[] bytes = new byte[1024];
        RandomAccessFile raf_r = new RandomAccessFile(sourceFile, "r");
        for (int i = 0; i < chunkNum; i++) {
            File chunkFile = new File(chunkFilePath + i);
            //分块文件写入流
            RandomAccessFile raf_rw = new RandomAccessFile(chunkFile, "rw");
            int len = -1;
            while ((len = raf_r.read(bytes)) != -1){
                raf_rw.write(bytes,0,len);
                if (chunkFile.length() >= chunkSize) break;
            }
            raf_rw.close();
        }
        raf_r.close();
    }

    //测试合并
    @Test
    void testMerge() throws IOException {
        File mergeFile = new File("D:\\Test\\mergeVideo.mp4");
        File sourceFile = new File("D:\\Test\\video.mp4");
        File chunkFolder = new File("D:\\Test\\chunk\\");
        
        //取出所有分块文件
        File[] files = chunkFolder.listFiles();
        List<File> list = Arrays.asList(files);
        Collections.sort(list, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return Integer.parseInt(o1.getName()) - Integer.parseInt(o2.getName());
            }
        });

        //读文件
        byte[] bytes = new byte[1024];
        RandomAccessFile rw = new RandomAccessFile(mergeFile, "rw");

        for(File file : list){
            //读分块的流
            RandomAccessFile r = new RandomAccessFile(file, "r");
            int len = -1;
            while ((len = r.read(bytes)) != -1){
                rw.write(bytes,0,len);
            }
            r.close();
        }
        rw.close();
        
        //对比Md5
        String mergeMd5 = DigestUtils.md5Hex(Files.newInputStream(mergeFile.toPath()));
        String sourceMd5 = DigestUtils.md5Hex(Files.newInputStream(sourceFile.toPath()));
        if (mergeMd5.equals(sourceMd5)){
            System.out.println("成功");
        }

    }
    
    
    
    
    
    
    
    
    
}
