package com.xuecheng.orders;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.xuecheng.orders.config.AlipayConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/10/2 10:32
 */
@SpringBootTest
public class AliPayTest {

    @Value("${pay.alipay.APP_ID}")
    String APP_ID;
    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    String APP_PRIVATE_KEY;
    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    String ALIPAY_PUBLIC_KEY;


    @Test
    public void queryPayResult() throws AlipayApiException {
        System.out.println(AlipayConfig.URL);
        System.out.println(APP_ID);
        System.out.println(APP_PRIVATE_KEY);
        System.out.println(ALIPAY_PUBLIC_KEY);
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.URL, APP_ID, APP_PRIVATE_KEY, "json", AlipayConfig.CHARSET, ALIPAY_PUBLIC_KEY, AlipayConfig.SIGNTYPE); //获得初始化的AlipayClient
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", "202403200101777102");
        //bizContent.put("trade_no", "2014112611001004680073956707");
        request.setBizContent(bizContent.toString());
        AlipayTradeQueryResponse response = alipayClient.execute(request);
        if (response.isSuccess()) {
            System.out.println("调用成功");
            String resultJson = response.getBody();
            //转map
            Map resultMap = JSON.parseObject(resultJson, Map.class);
            Map alipay_trade_query_response = (Map) resultMap.get("alipay_trade_query_response");
            //支付结果
            String trade_status = (String) alipay_trade_query_response.get("trade_status");
            System.out.println(trade_status);
        } else {
            System.out.println("调用失败");
        }
    }



}
