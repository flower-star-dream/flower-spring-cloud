package top.flowerstardream.base.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Data
@Component
@ConfigurationProperties(prefix = "minio")  // 文件上传 配置前缀file.oss
public class MinioProperties implements Serializable {
    private String accessKey; // 账号
    private String secretKey; // 密钥
    private String bucket; // 存储桶名称
    private String endpoint; // 域名
}
