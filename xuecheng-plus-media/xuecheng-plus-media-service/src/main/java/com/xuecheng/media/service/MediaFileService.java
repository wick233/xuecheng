package com.xuecheng.media.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

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

 UploadFileResultDto upload(Long companyId, UploadFileParamsDto uploadFileParamsDto,String localFilePath);

 MediaFiles addMediaFilesToDatabase(Long companyId, String fileMd5, UploadFileParamsDto uploadFileParamsDto, String bucket, String objectName);

 /**
  * UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
  *                 .bucket("testbucket")//桶名
  *                 .filename("D:\\Download\\two.jpg")//本地文件名
  *                 .object("test/01/two.jpg")//文件系统对象名
  *                 .contentType(mimeType)//设置媒体文件类型
  *                 .build();
  */
}
