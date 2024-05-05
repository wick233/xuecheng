package com.xuecheng.orders.service;

import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.dto.PayStatusDto;
import com.xuecheng.orders.model.po.XcPayRecord;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @Description
 * @Author Twithu
 * @Date 2024/5/1 21:06
 * @Version: 1.0
 */
public interface OrderService {

    PayRecordDto createOrder(String userId,AddOrderDto addOrderDto);

    public XcPayRecord getPayRecordByPayno(String payNo);

    public PayRecordDto queryPayResult(String payNo);

    public void saveAliPayStatus(PayStatusDto payStatusDto);

    /**
     * 发送通知结果
     * @param message
     */
    public void notifyPayResult(MqMessage message);

}
