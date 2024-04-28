package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;

/**
 * @Description 统一认证接口
 * @Author Twithu
 * @Date 2024/4/28 11:31
 * @Version: 1.0
 */
public interface AuthService {
    XcUserExt execute(AuthParamsDto authParamsDto);
}
