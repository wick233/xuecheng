package com.xuecheng.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description 微信扫码认证
 * @Author Twithu
 * @Date 2024/4/28 11:34
 * @Version: 1.0
 */
@Service("wx_authService")
public class WxAuthServiceImpl implements AuthService {

    @Autowired
    XcUserMapper xcUserMapper;

    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        //XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, authParamsDto.getUsername()));
        XcUserExt xcUserExt = new XcUserExt();
        //BeanUtils.copyProperties(xcUser,xcUserExt);
        xcUserExt.setUsername("t1");
        xcUserExt.setPassword("111111");
        xcUserExt.setName("周Sir");
        return xcUserExt;
    }
}
