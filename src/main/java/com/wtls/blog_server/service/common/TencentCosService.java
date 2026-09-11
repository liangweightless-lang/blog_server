package com.wtls.blog_server.service.common;

import cn.hutool.core.util.StrUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
public class TencentCosService {

    @Value("${tencent.cos.enabled:false}")
    private boolean enabled;

    @Value("${tencent.cos.secret-id:}")
    private String secretId;

    @Value("${tencent.cos.secret-key:}")
    private String secretKey;

    @Value("${tencent.cos.region:ap-guangzhou}")
    private String regionName;

    @Value("${tencent.cos.bucket-name:}")
    private String bucketName;

    @Value("${tencent.cos.domain:}")
    private String customDomain;

    private COSClient cosClient;

    @PostConstruct
    public void init() {
        if (isCosEnabled()) {
            try {
                COSCredentials cred = new BasicCOSCredentials(secretId.trim(), secretKey.trim());
                ClientConfig clientConfig = new ClientConfig();
                clientConfig.setRegion(new Region(regionName.trim()));
                clientConfig.setHttpProtocol(HttpProtocol.https);
                this.cosClient = new COSClient(cred, clientConfig);
                log.info("腾讯云对象存储 (COS) 客户端初始化成功，Region: {}, Bucket: {}", regionName, bucketName);
            } catch (Exception e) {
                log.error("腾讯云对象存储 (COS) 初始化失败: {}", e.getMessage(), e);
            }
        }
    }

    @PreDestroy
    public void destroy() {
        if (cosClient != null) {
            cosClient.shutdown();
        }
    }

    /**
     * 判断是否已启用且配置完备
     */
    public boolean isCosEnabled() {
        return enabled && StrUtil.isNotBlank(secretId) && StrUtil.isNotBlank(secretKey) && StrUtil.isNotBlank(bucketName);
    }

    /**
     * 上传文件到腾讯云 COS
     * @param file 上传的文件对象
     * @param keyPath 存储桶中的路径 (例如: uploads/2026/09/11/abc.jpg)
     * @return 文件的公开访问绝对地址
     */
    public String uploadFile(MultipartFile file, String keyPath) throws IOException {
        if (cosClient == null) {
            init();
        }
        if (cosClient == null) {
            throw new IllegalStateException("腾讯云 COS 客户端未成功初始化，请检查配置");
        }

        // 去除 keyPath 开头的斜杠
        String cleanKey = keyPath.startsWith("/") ? keyPath.substring(1) : keyPath;

        try (InputStream inputStream = file.getInputStream()) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            if (file.getContentType() != null) {
                metadata.setContentType(file.getContentType());
            }

            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName.trim(), cleanKey, inputStream, metadata);
            cosClient.putObject(putObjectRequest);

            // 拼接可直接访问的 HTTPS URL
            if (StrUtil.isNotBlank(customDomain)) {
                String domain = customDomain.trim().replaceAll("/+$", "");
                return domain + "/" + cleanKey;
            } else {
                return String.format("https://%s.cos.%s.myqcloud.com/%s", bucketName.trim(), regionName.trim(), cleanKey);
            }
        }
    }
}
