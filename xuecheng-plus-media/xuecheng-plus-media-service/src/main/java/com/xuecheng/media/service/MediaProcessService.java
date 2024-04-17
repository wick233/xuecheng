package com.xuecheng.media.service;

import com.xuecheng.media.model.po.MediaProcess;
import org.apache.ibatis.annotations.Param;

import javax.print.attribute.standard.Media;
import java.util.List;

public interface MediaProcessService {
    List<MediaProcess> selectListByShardIndex(int shardTotal, int shardIndex, int count);

    List<MediaProcess> getMediaProcessList(int shardIndex,int shardTotal,int count);
    public boolean startTask(long id);

    /**
     * @description 保存任务结果
     * @param taskId 任务id
     * @param status 任务状态
     * @param fileId 文件id
     * @param url    文件url
     * @param errorMsg
     */
    void saveProcessFinishStatus(Long taskId,String status,String fileId,String url,String errorMsg);
}
