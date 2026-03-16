package top.flowerstardream.base.service.Impl;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import top.flowerstardream.base.properties.MinioProperties;
import top.flowerstardream.base.service.FileStorageService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static top.flowerstardream.base.exception.ExceptionEnum.*;

/**
 * @author 花海
 * @date 2025/11/01 21:37
 * @description MinIO文件存储服务
 */
@Service
@Slf4j
@EnableConfigurationProperties(MinioProperties.class)
@ConditionalOnClass({MinioClient.class, FileStorageService.class})
@ConditionalOnProperty(prefix = "minio", name = {"endpoint", "access-key", "secret-key"})
public class MinIOFileStorageService implements FileStorageService {

    @Resource
    private MinioClient minioClient;

    @Resource
    private MinioProperties minioProperties;

    private final static String separator = "/";

    /**
     * @param dirPath
     * @param filename  yyyy/mm/dd/file.jpg
     * @return
     */
    public String builderFilePath(String dirPath,String filename) {
        StringBuilder stringBuilder = new StringBuilder(50);
        if(StringUtils.hasText(dirPath)){
            stringBuilder.append(dirPath).append(separator);
        }
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
//        String todayStr = sdf.format(new Date());
//        stringBuilder.append(todayStr).append(separator);
        stringBuilder.append(filename);
        return stringBuilder.toString();
    }

    /**
     * 根据文件扩展名获取内容类型
     * @param filename 文件名
     * @return 对应的内容类型字符串
     */
    private String getContentType(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "application/octet-stream";
        }

        String lowerFileName = filename.toLowerCase();
        if (lowerFileName.endsWith(".jpg") || lowerFileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerFileName.endsWith(".png")) {
            return "image/png";
        } else if (lowerFileName.endsWith(".gif")) {
            return "image/gif";
        } else {
            return "application/octet-stream";
        }
    }


    /**
     *  上传图片文件
     * @param prefix  文件前缀
     * @param filename  文件名
     * @param inputStream 文件流
     * @return  文件全路径
     */
    @Override
    public String uploadImgFile(String prefix, String filename,InputStream inputStream) {
        String filePath = builderFilePath(prefix, filename);
        try {
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .object(filePath)
                    .contentType(getContentType(filename))
                    .bucket(minioProperties.getBucket()).stream(inputStream,inputStream.available(),-1)
                    .build();
            minioClient.putObject(putObjectArgs);
            StringBuilder urlPath = new StringBuilder(minioProperties.getEndpoint());
            urlPath.append(separator).append(minioProperties.getBucket());
            urlPath.append(separator);
            urlPath.append(filePath);
            return urlPath.toString();
        }catch (Exception ex){
            log.error("Minio上传文件错误.",ex);
            throw FAILED_FILE_UPLOAD.toException();
        }
    }

    /**
     * 删除文件
     * @param pathUrl  文件全路径
     */
    @Override
    public void delete(String pathUrl) {
        String key = pathUrl.replace(minioProperties.getEndpoint()+"/","");
        int index = key.indexOf(separator);
        String bucket = key.substring(0,index);
        String filePath = key.substring(index+1);
        // 删除Objects
        RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder().bucket(bucket).object(filePath).build();
        try {
            minioClient.removeObject(removeObjectArgs);
        } catch (Exception e) {
            log.error("Minio删除文件错误.  pathUrl:{}",pathUrl);
            log.error("Minio异常:",e);
        }
    }


    /**
     * 下载文件
     * @param pathUrl  文件全路径
     * @return  文件流
     *
     */
    @Override
    public byte[] downLoadFile(String pathUrl)  {
        String key = pathUrl.replace(minioProperties.getEndpoint()+"/","");
        int index = key.indexOf(separator);
        String filePath = key.substring(index+1);
        InputStream inputStream = null;
        try {
            inputStream = minioClient.getObject(GetObjectArgs.builder().bucket(minioProperties.getBucket()).object(filePath).build());
        } catch (Exception e) {
            log.error("Minio 下载文件错误.  pathUrl:{}",pathUrl);
            log.error("Minio异常:",e);
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buff = new byte[100];
        int rc = 0;
        while (true) {
            try {
                if (inputStream != null && !((rc = inputStream.read(buff, 0, 100)) > 0)) {
                    break;
                }
            } catch (IOException e) {
                log.error("Minio异常:",e);
            }
            byteArrayOutputStream.write(buff, 0, rc);
        }
        return byteArrayOutputStream.toByteArray();
    }
}
