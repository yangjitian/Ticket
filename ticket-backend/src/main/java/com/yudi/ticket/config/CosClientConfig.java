package com.yudi.ticket.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 腾讯云 COS 客户端配置
 */
@Configuration
public class CosClientConfig {

    @Value("${cos.client.secretId}")
    private String secretId;

    @Value("${cos.client.secretKey}")
    private String secretKey;

    @Value("${cos.client.region}")
    private String region;

    @Bean
    public COSClient cosClient() {
        // 1. 初始化身份信息（secretId, secretKey）
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        // 2. 设置 bucket 的区域
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        // 3. 生成 cos 客户端
        return new COSClient(cred, clientConfig);
    }
}