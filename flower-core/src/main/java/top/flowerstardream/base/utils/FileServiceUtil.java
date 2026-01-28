package top.flowerstardream.base.utils;

import org.springframework.web.multipart.MultipartFile;
import top.flowerstardream.base.exception.BizException;

import java.util.UUID;

import static top.flowerstardream.base.exception.ExceptionEnum.THE_CONTENT_OF_THE_FILE_IS_EMPTY;


/**
 * @Author: 花海
 * @Date: 2025/11/04/23:16
 * @Description: 文件工具类
 */
public final class FileServiceUtil {
    public static String getFileName(MultipartFile file) {
        // 参数校验
        if (file.isEmpty()) {
            throw THE_CONTENT_OF_THE_FILE_IS_EMPTY.toException();
        }

        // 生成唯一文件名（包含原始扩展名）
        String originalName = file.getOriginalFilename();
        String fileExt = null;
        if (originalName != null) {
            fileExt = originalName.substring(originalName.lastIndexOf("."));
        }

        return UUID.randomUUID() + fileExt;
    }
}
