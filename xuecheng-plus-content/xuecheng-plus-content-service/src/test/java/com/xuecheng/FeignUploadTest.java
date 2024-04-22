package com.xuecheng;

import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * @Description
 * @Author Twithu
 * @Date 2024/4/22 23:36
 * @Version: 1.0
 */
@SpringBootTest
public class FeignUploadTest {

    @Autowired
    MediaServiceClient mediaServiceClient;

    @Test
    void test(){

        File file = new File("D:\\Test\\120.html");
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);

        mediaServiceClient.upload(multipartFile,"course/test.html");
    }
}
