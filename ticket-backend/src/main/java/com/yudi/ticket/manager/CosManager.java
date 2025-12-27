package com.yudi.ticket.manager;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectRequest;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * COS 对象存储操作
 */
@Component
public class CosManager {

    @Value("${cos.client.bucketName}")
    private String bucket;

    @Value("${cos.client.region}")
    private String region;

    @Resource
    private COSClient cosClient;

    /**
     * 上传对象
     *
     * @param key  唯一键
     * @param file 文件
     */
    public void putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, key, file);
        cosClient.putObject(putObjectRequest);
    }

    /**
     * 获取文件访问链接
     *
     * @param key 唯一键
     * @return url
     */
    public String getFileUrl(String key) {
        return String.format("https://%s.cos.%s.myqcloud.com/%s", bucket, region, key);
    }
}