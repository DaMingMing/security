package com.keymao.security.distributed.uaa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;

/**
 * 令牌配置类
 */
@Configuration
public class TokenConfig {
    /**
     * 令牌存储策略
     * @return
     */
    @Bean
    public TokenStore tokenStore() {
        //内存方式，生成普通令牌
        return new InMemoryTokenStore();
    }
}