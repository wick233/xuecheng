package com.xuecheng.orders.service;

import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @Description
 * @Author Twithu
 * @Date 2024/5/1 21:06
 * @Version: 1.0
 */
public interface OrderService {

    PayRecordDto createOrder(String userId,AddOrderDto addOrderDto);
}
