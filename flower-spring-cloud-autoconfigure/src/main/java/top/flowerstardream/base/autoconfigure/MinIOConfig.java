package top.flowerstardream.base.autoconfigure;

import io.minio.MinioClient;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.flowerstardream.base.properties.MinioProperties;
import top.flowerstardream.base.service.FileStorageService;


/**
 * @author 花海
 * @date 2025/11/01 21:37
 * @description MinIO配置类
 */
@Data
@Configuration
@EnableConfigurationProperties({MinioProperties.class})
//当引入FileStorageService接口时
@ConditionalOnClass({FileStorageService.class, MinioClient.class})
@ConditionalOnProperty(prefix = "minio", name = {"endpoint", "access-key", "secret-key"})
public class MinIOConfig {

    @Resource
    private MinioProperties minioProperties;

    @Bean
    public MinioClient buildMinioClient() {
        return MinioClient
                .builder()
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .endpoint(minioProperties.getEndpoint())
                .build();
    }
}