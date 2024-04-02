package com.xuecheng.meida;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.*;
import io.minio.errors.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.io.*;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @Description 测试minIO的sdk
 * @Author Twithu
 * @Date 2024/3/20 22:16
 * @Version: 1.0
 */
public class MinIOTest {

    MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://192.168.101.65:9000")
                    .credentials("minioadmin", "minioadmin")
                    .build();

    @Test
    public void testUpload() throws Exception {

        //根据扩展名取出mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(".jpg");
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;//通用mimeType，字节流
        if (extensionMatch != null){
            mimeType = extensionMatch.getMimeType();
        }

        UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                .bucket("testbucket")//桶名
                .filename("D:\\Download\\two.jpg")//本地文件名
                .object("test/01/two.jpg")//文件系统对象名
                .contentType(mimeType)//设置媒体文件类型
                .build();

        minioClient.uploadObject(uploadObjectArgs);
    }

    @Test
    public void testDelete() throws Exception {

        RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder().bucket("testbucket").object("two.jpg").build();

        minioClient.removeObject(removeObjectArgs);
    }

    //查询文件
    @Test
    void getFile() throws Exception {
        GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket("testbucket").object("test/01/two.jpg").build();
        FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
        //指定输出流
        FileOutputStream outputStream = new FileOutputStream(new File("D:\\Download\\two2.jpg"));
        IOUtils.copy(inputStream,outputStream);

        //对文件进行md5校验
        String target_md5 = DigestUtils.md5Hex(Files.newInputStream(new File("D:\\Download\\two2.jpg").toPath()));
        String source_md5 = DigestUtils.md5Hex(Files.newInputStream(new File("D:\\Download\\two.jpg").toPath()));
        if (target_md5.equals(source_md5)){
            System.out.println("下载成功");
        }
    }

}
