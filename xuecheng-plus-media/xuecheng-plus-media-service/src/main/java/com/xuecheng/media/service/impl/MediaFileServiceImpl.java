package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/9/10 8:58
 */
@Service
@Slf4j
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    MinioClient minioClient;

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Value("${minio.bucket.files}")
    private String bucketFiles;

    private String getDefaultFolderPath() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String folder = sdf.format(new Date()).replace("-", "/") + "/";
        return folder;
    }

    private String getFileMd5(File file) {
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            String fileMd5 = DigestUtils.md5Hex(fileInputStream);
            return fileMd5;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getMimeType(String extension) {
        if (extension == null) extension = "";
        //根据拓展名取出mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if (extensionMatch != null) mimeType = extensionMatch.getMimeType();
        return mimeType;
    }

    public boolean addMediaFilesToMinIO(String localFilePath, String mimeType, String bucket, String objectName) {

        try {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket(bucket)//桶名
                    .filename(localFilePath)//本地文件名
                    .object(objectName)//文件系统对象名
                    .contentType(mimeType)//设置媒体文件类型
                    .build();

            minioClient.uploadObject(uploadObjectArgs);
            log.debug("上传文件到minio成功,bucket:{},objectName:{}", bucket, objectName);
            System.out.println("上传成功");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            e.printStackTrace();
            log.error("上传文件到minio出错,bucket:{},objectName:{},错误原因:{}", bucket, objectName, e.getMessage(), e);
            XueChengPlusException.cast("上传文件到文件系统失败");
            return false;
        }

    }

    @Transactional
    public MediaFiles addMediaFilesToDatabase(Long companyId, String fileMd5, UploadFileParamsDto uploadFileParamsDto, String bucket, String objectName) {
        //从数据库查询数据
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles == null) {
            mediaFiles = new MediaFiles();
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            mediaFiles.setId(fileMd5);
            mediaFiles.setFileId(fileMd5);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setUrl("/" + bucket + "/" + objectName);
            mediaFiles.setBucket(bucket);
            mediaFiles.setFilePath(objectName);
            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setAuditStatus("002003");
            mediaFiles.setStatus("1");
            //保存文件到数据库
            if (mediaFilesMapper.insert(mediaFiles) < 0) {
                log.error("保存文件信息到数据库失败,{}", mediaFiles.toString());
                XueChengPlusException.cast("保存文件信息失败");
            }
            log.debug("保存文件信息到数据库成功,{}",mediaFiles.toString());
        }
        return mediaFiles;
    }

    @Transactional
    @Override
    public UploadFileResultDto upload(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath) {
        File file = new File(localFilePath);
        if (!file.exists()){
            XueChengPlusException.cast("文件不存在");
        }

        String filename = uploadFileParamsDto.getFilename();
        String extension = filename.substring(filename.lastIndexOf("."));
        String mimeType = getMimeType(extension);
        String fileMd5 = getFileMd5(file);
        String defaultFolderPath = getDefaultFolderPath();
        String objectName = defaultFolderPath + fileMd5 +extension;

        //文件上传到minio
        addMediaFilesToMinIO(localFilePath, mimeType, bucketFiles, objectName);
        uploadFileParamsDto.setFileSize(file.length());

        //文件信息存入数据库
        MediaFiles mediaFiles = addMediaFilesToDatabase(companyId, fileMd5, uploadFileParamsDto, bucketFiles, objectName);
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles,uploadFileResultDto);
        return uploadFileResultDto;
    }

    @Override
    public PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;

    }



}












