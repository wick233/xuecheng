package com.xuecheng.media.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

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
 PageResult<MediaFiles> queryMediaFiles(Long companyId,PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);

 UploadFileResultDto upload(Long companyId, UploadFileParamsDto uploadFileParamsDto,String localFilePath,String objectName);

 MediaFiles addMediaFilesToDatabase(Long companyId, String fileMd5, UploadFileParamsDto uploadFileParamsDto, String bucket, String objectName);

 public RestResponse<Boolean> checkFile(String fileMd5);

 public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex);

 public RestResponse<Boolean> uploadChunk(String fileMd5,int chunk,String localChunkFilePath);

 File downloadFileFromMinio(String bucket, String objectName);

 boolean addMediaFilesToMinIO(String localFilePath, String mimeType, String bucket, String objectName);

 public RestResponse<Boolean> mergeChunks(Long companyId,String fileMd5,int chunkTotal,UploadFileParamsDto uploadFileParamsDto);

    MediaFiles getFileById(String mediaId);
}
