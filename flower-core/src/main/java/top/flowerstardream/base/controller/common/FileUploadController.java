package top.flowerstardream.base.controller.common;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import top.flowerstardream.base.properties.OtherProperties;
import top.flowerstardream.base.result.Result;
import top.flowerstardream.base.service.FileStorageService;
import top.flowerstardream.base.utils.FileServiceUtil;

import java.io.IOException;
import java.io.InputStream;

/**
 * @Author: 花海
 * @Date: 2025/11/04/23:05
 * @Description: 文件上传接口
 */
@RestController
@RequestMapping("/api/base/v1/common/file")
@EnableConfigurationProperties(OtherProperties.class)
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "minio", name = {"endpoint", "access-key", "secret-key"})
public class FileUploadController {

    @Resource
    private OtherProperties otherProperties;

    @Resource
    private FileStorageService fileStorageService;

    @PostMapping("/upload")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        String filename = FileServiceUtil.getFileName(file);
        InputStream inputStream = file.getInputStream();
        return Result.successResult(fileStorageService.uploadImgFile(otherProperties.getPrefix(), filename, inputStream));
    }

}
