package com.xuecheng.content.feignclient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Description
 * @Author Twithu
 * @Date 2024/4/24 20:24
 * @Version: 1.0
 */

@Slf4j
@Component
public class SearchServiceClientFallbackFactory implements FallbackFactory<SearchServiceClient> {
    @Override
    public SearchServiceClient create(Throwable throwable) {
        return new SearchServiceClient() {
            @Override
            public Boolean add(CourseIndex courseIndex) {
                log.error("调用搜索发生熔断走降级方法,索引信息:{},熔断异常:{}", courseIndex,throwable.getMessage());
                //走降级返回false
                return false;
            }
        };
    }
}
