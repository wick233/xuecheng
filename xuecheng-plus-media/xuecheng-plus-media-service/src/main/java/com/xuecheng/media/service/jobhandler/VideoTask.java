package com.xuecheng.media.service.jobhandler;

import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import com.xuecheng.media.service.MediaProcessService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 * 视频任务处理
 */
@Slf4j
@Component
public class VideoTask {
    @Autowired
    MediaProcessService mediaProcessService;
    @Autowired
    MediaFileService mediaFileService;

    @Value("${videoprocess.ffmpegpath}")
    private String ffmpeg_path;

    @XxlJob("videoJobHandler")
    public void shardingJobHandler() throws Exception {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex(); //执行器序号
        int shardTotal = XxlJobHelper.getShardTotal(); //执行器总数

        //cpu核心数
        int processors = Runtime.getRuntime().availableProcessors();
        //查询待处理的任务
        List<MediaProcess> mediaProcessList = mediaProcessService.selectListByShardIndex(shardTotal, shardIndex, processors);
        int size = mediaProcessList.size();
        log.debug("去到的视频处理任务数:" + size);
        if (size <= 0) return;
        //创建线程池
        ExecutorService executorService = Executors.newFixedThreadPool(size);
        CountDownLatch countDownLatch = new CountDownLatch(size);
        mediaProcessList.forEach(mediaProcess -> {
            //将任务加入线程池
            executorService.execute(()->{
                try {
                    //开启任务
                    Long taskId = mediaProcess.getId();
                    String fileId = mediaProcess.getFileId();//文件id就是md5
                    boolean b = mediaProcessService.startTask(taskId);
                    if (!b){
                        log.debug("抢占任务失败，任务id:{}",taskId);
                        return;
                    }
                    //下载minio视频到本地
                    String bucket = mediaProcess.getBucket();
                    String objectName = mediaProcess.getFilePath();
                    File file = mediaFileService.downloadFileFromMinio(bucket, objectName);
                    if (file == null){
                        log.debug("下载视频出错，任务id:{},bucket:{},objectName:{}",taskId,bucket,objectName);
                        mediaProcessService.saveProcessFinishStatus(taskId,"3",fileId,null,"下载视频到本地失败");
                        return;
                    }

                    String videoPath = file.getAbsolutePath();

                    //创建一个临时文件，作为转换后的文件
                    File mp4File = null;
                    try{
                        mp4File = File.createTempFile("minio",".mp4");
                    }catch (IOException e){
                        log.debug("创建临时文件异常,{}",e.getMessage());
                        mediaProcessService.saveProcessFinishStatus(taskId,"3",fileId,null,"创建临时文件异常");
                        return;
                    }
                    Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpeg_path, videoPath, mp4File.getName(), mp4File.getAbsolutePath());
                    //视频转码
                    String result = videoUtil.generateMp4();
                    if (!"success".equals(result)){
                        log.debug("视频转码失败，原因:{},bucket:{},objectName:{}",result,bucket,objectName);
                        mediaProcessService.saveProcessFinishStatus(taskId,"3",fileId,null,result);
                        return;
                    }
                    //上传到minio
                    String mp4ObjectName = getFilePath(fileId,".mp4");
                    boolean b1 = mediaFileService.addMediaFilesToMinIO(mp4File.getAbsolutePath(), "video/mp4", bucket, mp4ObjectName);
                    if (!b1){
                        log.debug("上传文件到minio失败,taskId：{}",taskId);
                        mediaProcessService.saveProcessFinishStatus(taskId,"3",fileId,null,"上传文件到minio失败");
                        return;
                    }
                    //保存处理结果
                    String url = getFilePathByMd5(fileId,".mp4");
                    mediaProcessService.saveProcessFinishStatus(taskId,"2",fileId,url,"保存成功");
                } finally {
                    countDownLatch.countDown();
                }


            });
        });
        //阻塞，指定最大的等待时间
        countDownLatch.await(30,TimeUnit.MINUTES);
        //

    }


    private String getFilePathByMd5(String fileMd5,String fileExt){
        return   fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" +fileMd5 +fileExt;
    }

    private String getFilePath(String fileMd5,String fileExt){
        return   fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" +fileMd5 +fileExt;
    }


}
