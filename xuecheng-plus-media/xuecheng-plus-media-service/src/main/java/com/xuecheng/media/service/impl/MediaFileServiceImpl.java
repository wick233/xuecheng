package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import io.minio.*;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @Autowired
    MediaFileService proxyService;

    @Autowired
    MediaProcessMapper mediaProcessMapper;

    @Value("${minio.bucket.files}")
    private String bucketFiles;

    @Value("${minio.bucket.videofiles}")
    private String bucketVideo;

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
            log.debug("保存文件信息到数据库成功,{}", mediaFiles.toString());

            //记录待处理任务
            addWaitingTask(mediaFiles);
        }
        return mediaFiles;
    }

    private void addWaitingTask(MediaFiles mediaFiles){
        String filename = mediaFiles.getFilename();
        String extension = filename.substring(filename.lastIndexOf("."));
        String mimeType = getMimeType(extension);
        if("video/x-msvideo".equals(mimeType)){//如果是avi视频则写入待处理任务
            MediaProcess mediaProcess = new MediaProcess();
            BeanUtils.copyProperties(mediaFiles,mediaProcess);
            //状态是未处理
            mediaProcess.setStatus("1");
            mediaProcess.setCreateDate(LocalDateTime.now());
            mediaProcess.setFailCount(0);
            mediaProcess.setUrl(null);

            mediaProcessMapper.insert(mediaProcess);
        }
    }

    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        //先查数据库
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles != null) {
            String bucket = mediaFiles.getBucket();
            String filePath = mediaFiles.getFilePath();
            //如果数据库存在，再查询minio
            GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket(bucket).object(filePath).build();

            try {
                GetObjectResponse object = minioClient.getObject(getObjectArgs);
                if (object != null) return RestResponse.success(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return RestResponse.success(false);
    }

    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        //获取分块文件目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        //路径
        String chunkFilePath = chunkFileFolderPath + chunkIndex;

        try {
            //查询minio
            InputStream fileInputStream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketVideo)
                    .object(chunkFilePath).build());
            if (fileInputStream != null) return RestResponse.success(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return RestResponse.success(false);
    }

    @Override
    public RestResponse<Boolean> uploadChunk(String fileMd5, int chunk, String localChunkFilePath) {

        String chunkFilePath = getChunkFileFolderPath(fileMd5) + chunk;
        String mimeType = getMimeType(null);
        boolean b = addMediaFilesToMinIO(localChunkFilePath, mimeType, bucketVideo, chunkFilePath);
        if (!b) return RestResponse.validfail(false,"上传分块失败");
        else return RestResponse.success(true);
    }

    @Override
    public RestResponse<Boolean> mergeChunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        //获取分块文件路径list，用于合并
        List<ComposeSource> sourceList = new ArrayList<>();
        for (int i = 0; i < chunkTotal; i++) {
            ComposeSource source = ComposeSource.builder().bucket(bucketVideo).object(chunkFileFolderPath + i).build();
            sourceList.add(source);
        }
        //合并
        String filename = uploadFileParamsDto.getFilename();
        String extendName = filename.substring(filename.lastIndexOf("."));
        String mergeFilePath = getFilePathByMd5(fileMd5,extendName);
        try{
            ObjectWriteResponse response = minioClient.composeObject(ComposeObjectArgs.builder()
                    .bucket(bucketVideo)
                    .object(mergeFilePath)
                    .sources(sourceList)
                    .build());
            log.debug("合并文件成功:{}",mergeFilePath);
        }catch (Exception e){
            log.debug("合并文件失败，fileMd5:{},异常:{}",fileMd5,e.getMessage());
        }

        //校验Md5
        //先从minio下载
        File minioFile = downloadFileFromMinio(bucketVideo,mergeFilePath);

        if (minioFile == null){
            log.debug("下载合并后文件失败:mergeFilePath:{}",mergeFilePath);
            return RestResponse.validfail(false,"下载合并后文件失败");
        }

        //比较下载合并后的文件的MD5值和传入的MD5值
        try(FileInputStream fileInputStream = new FileInputStream(minioFile)){
            String md5Hex = DigestUtils.md5Hex(fileInputStream);
            if (!fileMd5.equals(md5Hex)) return RestResponse.validfail(false,"文件合并校验失败，最终上传失败。");
            //文件大小
            uploadFileParamsDto.setFileSize(minioFile.length());
        } catch (Exception e){
            log.debug("校验文件失败,fileMd5:{},异常:{}",fileMd5,e.getMessage(),e);
            return RestResponse.validfail(false, "文件合并校验失败，最终上传失败。");
        }finally {
            if(minioFile!=null){
                minioFile.delete();
            }
        }

        //合并无误后，文件入库
        proxyService.addMediaFilesToDatabase(companyId,fileMd5,uploadFileParamsDto,bucketVideo,mergeFilePath);
        //清除分块文件
        try {
            for (int i = 0; i< chunkTotal;i++) {
                minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketVideo).object(chunkFileFolderPath+i).build());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RestResponse.success(true);
    }

    @Override
    public MediaFiles getFileById(String mediaId){
        return mediaFilesMapper.selectById(mediaId);
    }

    @Override
    public File downloadFileFromMinio(String bucket, String objectName) {
        File minioFile = null;
        FileOutputStream outputStream = null;
        try {
            InputStream inputStream = minioClient.getObject(GetObjectArgs.builder().bucket(bucket).object(objectName).build());
            minioFile = File.createTempFile("minio",".merge");
            outputStream = new FileOutputStream(minioFile);
            IOUtils.copy(inputStream,outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(outputStream!=null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return minioFile;
    }

    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + "chunk" + "/";
    }

    private String getFilePathByMd5(String fileMd5,String fileExt){
        return   fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" +fileMd5 +fileExt;
    }


    @Override
    public UploadFileResultDto upload(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath) {
        File file = new File(localFilePath);
        if (!file.exists()) {
            XueChengPlusException.cast("文件不存在");
        }

        String filename = uploadFileParamsDto.getFilename();
        String extension = filename.substring(filename.lastIndexOf("."));
        String mimeType = getMimeType(extension);
        String fileMd5 = getFileMd5(file);
        String defaultFolderPath = getDefaultFolderPath();
        String objectName = defaultFolderPath + fileMd5 + extension;

        //文件上传到minio
        addMediaFilesToMinIO(localFilePath, mimeType, bucketFiles, objectName);
        uploadFileParamsDto.setFileSize(file.length());

        //文件信息存入数据库
        MediaFiles mediaFiles = proxyService.addMediaFilesToDatabase(companyId, fileMd5, uploadFileParamsDto, bucketFiles, objectName);
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
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












