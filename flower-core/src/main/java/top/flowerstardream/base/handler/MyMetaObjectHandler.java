package top.flowerstardream.base.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static top.flowerstardream.base.utils.GetInfoUtil.*;

/**
 * @Author: 花海
 * @Date: 2025/10/31/19:00
 * @Description: 字段自动填充处理
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MyMetaObjectHandler implements MetaObjectHandler {

    // 创建时间字段名
    private final String createTimeFieldName = "createTime";
    // 更新时间字段名
    private final String updateTimeFieldName = "updateTime";
    // 创建人 ID 字段名
    private final String createPersonFieldName = "createPersonId";
    // 更新人 ID 字段名
    private final String updatePersonFieldName = "updatePersonId";
    // 版本号字段名
    private final String versionFieldName = "version";

    /**
     * 插入时自动填充字段
     * @param metaObject MyBatis 元对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("【id 生成器：插入字段自动填充】traceId:{}, 插入填充：{}", getTraceId(), metaObject);
        // 如果创建时间未设置，则填充当前时间
        if (metaObject.getValue(createTimeFieldName) == null) {
            this.strictInsertFill(metaObject, createTimeFieldName, LocalDateTime.class, LocalDateTime.now());
        }
        // 如果更新时间未设置，则填充当前时间
        if (metaObject.getValue(updateTimeFieldName) == null) {
            this.strictInsertFill(metaObject, updateTimeFieldName, LocalDateTime.class, LocalDateTime.now());
        }
        // 如果版本号未设置，则初始化为 1
        if (metaObject.getValue(versionFieldName) == null) {
            this.strictInsertFill(metaObject, versionFieldName, Integer.class, 1);
        }
        // 获取当前操作人 ID，若为空则默认为 1
        Long createPersonId = getOperatorId();
        if (createPersonId == null) {
            createPersonId = 1L;
        }
        // 如果创建人 ID 未设置，则填充操作人 ID
        if (metaObject.getValue(createPersonFieldName) == null) {
            this.strictInsertFill(metaObject, createPersonFieldName, Long.class, createPersonId);
        }
        // 如果更新人 ID 未设置，则填充操作人 ID
        if (metaObject.getValue(updatePersonFieldName) == null) {
            this.strictInsertFill(metaObject, updatePersonFieldName, Long.class, createPersonId);
        }
    }

    /**
     * 更新时自动填充字段
     * @param metaObject MyBatis 元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("【更新字段自动填充】traceId:{}, 更新填充：{}", getTraceId(), metaObject);
        // 获取当前操作人 ID，若为空则默认为 1
        Long updatePersonId = getOperatorId();
        if (updatePersonId == null) {
            updatePersonId = 1L;
        }
        // 如果更新时间未设置，则填充当前时间
        if (metaObject.getValue(updateTimeFieldName) == null) {
            this.strictUpdateFill(metaObject, updateTimeFieldName, LocalDateTime.class, LocalDateTime.now());
        }
        // 如果更新人 ID 未设置，则填充操作人 ID
        if (metaObject.getValue(updatePersonFieldName) == null) {
            this.strictUpdateFill(metaObject, updatePersonFieldName, Long.class, updatePersonId);
        }
    }
}