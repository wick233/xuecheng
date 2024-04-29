package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.mapper.XcUserRoleMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.model.po.XcUserRole;
import com.xuecheng.ucenter.service.AuthService;
import com.xuecheng.ucenter.service.WxAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * @Description 微信扫码认证
 * @Author Twithu
 * @Date 2024/4/28 11:34
 * @Version: 1.0
 */
@Slf4j
@Service("wx_authService")
public class WxAuthServiceImpl implements AuthService, WxAuthService {

    @Autowired
    XcUserMapper xcUserMapper;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    XcUserRoleMapper xcUserRoleMapper;
    @Autowired
    WxAuthServiceImpl proxy;


    @Value("${weixin.appid}")
    String appid;
    @Value("${weixin.secret}")
    String secret;


    @Override
    public XcUser wxAuth(String code) {
        //申请令牌
        Map<String, String> map = getAccess_token(code);
        if (map == null){
            return null;
        }

        //携带令牌查询用户信息
        String access_token = map.get("access_token");
        String openid = map.get("openid");
        Map<String, String> userInfo = getUserInfo(access_token, openid);

        //保存用户到数据库
        XcUser xcUser = proxy.addWxUser(userInfo);
        return xcUser;
    }

    //申请令牌
    private Map<String,String> getAccess_token(String code){
        String wxUrl_template = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
        //请求微信地址
        String wxUrl = String.format(wxUrl_template,appid,secret, code);
        log.info("调用微信接口申请access_token, url:{}",wxUrl);

        ResponseEntity<String> exchange = restTemplate.exchange(wxUrl, HttpMethod.POST, null, String.class);
        String result = exchange.getBody();
        Map<String,String> map = JSON.parseObject(result, Map.class);
        return map;
    }

    //携带令牌查询用户信息
    private Map<String,String> getUserInfo(String access_token,String openid) {
        String url_template = "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";//%s是占位符
        String url = String.format(url_template, access_token, openid);

        ResponseEntity<String> exchange = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
        //获取响应结果
        String result = new String(exchange.getBody().getBytes(StandardCharsets.ISO_8859_1),StandardCharsets.UTF_8) ;
        Map<String,String> map = JSON.parseObject(result, Map.class);
        return map;
    }

    //保存用户到数据库
    @Transactional
    public XcUser addWxUser(Map userInfo_map){
        String unionid = userInfo_map.get("unionid").toString();
        //根据unionId查询用户信息
        XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getWxUnionid, unionid));
        if (xcUser!= null){//存在则直接返回，无需新增
            return  xcUser;
        }
        xcUser = new XcUser();

        String userId = UUID.randomUUID().toString();
        xcUser = new XcUser();
        xcUser.setId(userId);
        xcUser.setWxUnionid(unionid);
        //记录从微信得到的昵称
        xcUser.setNickname(userInfo_map.get("nickname").toString());
        xcUser.setUserpic(userInfo_map.get("headimgurl").toString());
        xcUser.setName(userInfo_map.get("nickname").toString());
        xcUser.setUsername(unionid);
        xcUser.setPassword(unionid);
        xcUser.setUtype("101001");//学生类型
        xcUser.setStatus("1");//用户状态
        xcUser.setCreateTime(LocalDateTime.now());
        xcUserMapper.insert(xcUser);
        XcUserRole xcUserRole = new XcUserRole();
        xcUserRole.setId(UUID.randomUUID().toString());
        xcUserRole.setUserId(userId);
        xcUserRole.setRoleId("17");//学生角色
        xcUserRoleMapper.insert(xcUserRole);
        return xcUser;

    }


    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, authParamsDto.getUsername()));
        if (xcUser==null){
            throw new RuntimeException("用户不存在，登陆失败");
        }
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(xcUser,xcUserExt);
        return xcUserExt;
    }

}
