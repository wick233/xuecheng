package com.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.mapper.CoursePublishPreMapper;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.*;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.CourseTeacherService;
import com.xuecheng.content.service.TeachPlanService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

/**
 * @Description 课程发布接口实现
 * @Author Twithu
 * @Date 2024/4/21 11:42
 * @Version: 1.0
 */
@Service
@Slf4j
public class CoursePublishServiceImpl implements CoursePublishService {
    @Autowired
    CourseBaseInfoService courseBaseInfoService;
    @Autowired
    TeachPlanService teachPlanService;
    @Autowired
    CourseTeacherService courseTeacherService;
    @Autowired
    CourseBaseMapper courseBaseMapper;
    @Autowired
    CourseMarketMapper courseMarketMapper;
    @Autowired
    CoursePublishPreMapper coursePublishPreMapper;
    @Autowired
    CoursePublishMapper coursePublishMapper;
    @Autowired
    MqMessageService mqMessageService;
    @Autowired
    MediaServiceClient mediaServiceClient;

    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        //查询课程基本信息、营销信息
        CourseBaseInfoDto courseBaseInfoDto = courseBaseInfoService.getCourseBaseById(courseId);
        //查询课程计划信息
        List<TeachPlanDto> teachPlanTree = teachPlanService.findTeachPlanTree(courseId);
        //查询师资信息
        List<CourseTeacher> courseTeachers = courseTeacherService.getCourseTeacherList(courseId);

        coursePreviewDto.setCourseBase(courseBaseInfoDto);
        coursePreviewDto.setTeachplans(teachPlanTree);
        coursePreviewDto.setCourseTeachers(courseTeachers);
        return coursePreviewDto;
    }

    @Transactional
    @Override
    public void commitAudit(Long companyId, Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        String auditStatus = courseBase.getAuditStatus();
        //当前审核状态为已提交，则不允许再次提交
        if ("202003".equals(auditStatus)){
            XueChengPlusException.cast("当前为等待审核状态，审核完成可以再次提交。");
        }//本机构只允许提交本机构的课程
        if (!courseBase.getCompanyId().equals(companyId)){
            XueChengPlusException.cast("不允许提交其它机构的课程。");
        }//课程图片是否填写
        if(StringUtils.isEmpty(courseBase.getPic())){
            XueChengPlusException.cast("提交失败，请上传课程图片");
        }

        //添加课程预发布记录
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        //课程基本信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseById(courseId);
        BeanUtils.copyProperties(courseBaseInfo,coursePublishPre);
        //课程营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        String courseMarketJson = JSON.toJSONString(courseMarket);
        coursePublishPre.setMarket(courseMarketJson);

        //查询课程计划
        List<TeachPlanDto> teachPlanTree = teachPlanService.findTeachPlanTree(courseId);
        if (teachPlanTree.size() <= 0) {
            XueChengPlusException.cast("提交失败，还没有添加课程计划");
        }
        String teachPlanJson = JSON.toJSONString(teachPlanTree);
        coursePublishPre.setTeachplan(teachPlanJson);

        //设置预发布记录状态为已提交
        coursePublishPre.setStatus("202003");
        coursePublishPre.setCompanyId(companyId);
        coursePublishPre.setCreateDate(LocalDateTime.now());

        //新增或更新课程预发布数据
        CoursePublishPre coursePreUpdate = coursePublishPreMapper.selectById(courseId);
        if (coursePreUpdate == null){//不存在则新增，存在则修改
            coursePublishPreMapper.insert(coursePublishPre);
        }else{
            coursePublishPreMapper.updateById(coursePublishPre);
        }

        //更新课程基本表的审核状态
        courseBase.setAuditStatus("202003");
        courseBaseMapper.updateById(courseBase);


    }

    @Transactional
    @Override
    public void publish(Long companyId, Long courseId) {
        //查询课程预发布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre == null){
            XueChengPlusException.cast("请先提交课程审核，审核通过才可以发布");
        }
        //本机构只允许提交本机构的课程
        if(!coursePublishPre.getCompanyId().equals(companyId)){
            XueChengPlusException.cast("不允许提交其它机构的课程。");
        }
        //课程审核状态
        String status = coursePublishPre.getStatus();
        if (!"202004".equals(status)){
            XueChengPlusException.cast("操作失败，课程审核通过方可发布。");
        }

        //保存或更新课程发布信息
        CoursePublish coursePublish = new CoursePublish();
        BeanUtils.copyProperties(coursePublishPre,coursePublish);
        CoursePublish coursePublishUpdate = coursePublishMapper.selectById(courseId);
        if (coursePublishUpdate == null){//不存在则新增，存在则更新
            coursePublishMapper.insert(coursePublish);
        }else{
            coursePublishMapper.updateById(coursePublish);
        }

        //更新课程基本表的发布状态
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setStatus("203002");
        courseBaseMapper.updateById(courseBase);

        //保存消息表
        saveCoursePublishMessage(courseId);

        //删除课程预发布表对应记录
        coursePublishPreMapper.deleteById(courseId);


    }

    @Override
    public File generateCourseHtml(Long courseId) {
        File htmlFile = null;
        try {
            Configuration configuration = new Configuration(Configuration.getVersion());
            //拿classpath路径
            String classPath = this.getClass().getResource("/").getPath();
            //指定模板目录
            configuration.setDirectoryForTemplateLoading(new File(classPath + "/templates/"));
            //指定编码
            configuration.setDefaultEncoding("utf-8");
            Template template = configuration.getTemplate("course_template.ftl");
            //准备数据
            CoursePreviewDto coursePreviewDto = this.getCoursePreviewInfo(courseId);
            HashMap<String, Object> map = new HashMap<>();
            map.put("model", coursePreviewDto);

            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);

            //输入流
            InputStream inputStream = IOUtils.toInputStream(html, "utf-8");

            htmlFile = File.createTempFile("coursePublish",".html");
            //输出文件
            FileOutputStream outputStream = new FileOutputStream(htmlFile);
            //使用流将html写入文件
            IOUtils.copy(inputStream, outputStream);
        }catch (Exception e){
            log.error("课程静态化异常:{}",e.toString());
            XueChengPlusException.cast("课程静态化异常");
        }
        return htmlFile;
    }

    @Override
    public void uploadCourseHtml(Long courseId, File file) {
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);

        String upload = mediaServiceClient.upload(multipartFile,"course/"+courseId+".html");
        if (upload == null){
            System.out.println("走了降级逻辑");
            XueChengPlusException.cast("上传静态文件异常");
        }
    }

    @Override
    public CoursePublish getCoursePublish(Long courseId) {
        return coursePublishMapper.selectById(courseId);
    }

    private void saveCoursePublishMessage(Long courseId){
        MqMessage mqMessage = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);
        if (mqMessage == null){
            XueChengPlusException.cast(CommonError.UNKNOWN_ERROR);
        }
    }

}


























