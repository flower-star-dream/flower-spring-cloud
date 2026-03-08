package top.flowerstardream.base.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import top.flowerstardream.base.annotation.AutoConfigProperties;

import java.io.Serializable;

/**
 * @Author: 花海
 * @Date: 2025/11/04/17:01
 * @Description: Minio 配置
 */
@Data
@Component
@AutoConfigProperties(prefix = "minio")  // 文件上传 配置前缀file.oss
@ConfigurationProperties(prefix = "minio")
public class MinioProperties implements Serializable {
    // 账号
    private String accessKey;
    // 密码
    private String secretKey;
    // 存储桶
    private String bucket;
    // 切入点
    private String endpoint;
}
