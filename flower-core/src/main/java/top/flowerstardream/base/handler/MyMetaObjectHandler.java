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

    private final String createTimeFieldName = "createTime";
    private final String updateTimeFieldName = "updateTime";
    private final String createPersonFieldName = "createPersonId";
    private final String updatePersonFieldName = "updatePersonId";

    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("【id生成器：插入字段自动填充】traceId:{}, 插入填充：{}", getTraceId(), metaObject);
        this.strictInsertFill(metaObject, createTimeFieldName, LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, updateTimeFieldName, LocalDateTime.class, LocalDateTime.now());
        Long createPersonId = getOperatorId();
        if (createPersonId == null) {
            createPersonId = 1L;
        }
        if (metaObject.getValue(createPersonFieldName) == null) {
            this.strictInsertFill(metaObject, createPersonFieldName, Long.class, createPersonId);
        }
        if (metaObject.getValue(updatePersonFieldName) == null) {
            this.strictInsertFill(metaObject, updatePersonFieldName, Long.class, createPersonId);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("【更新字段自动填充】traceId:{}, 更新填充：{}", getTraceId(), metaObject);
        Long updatePersonId = getOperatorId();
        if (updatePersonId == null) {
            updatePersonId = 1L;
        }
        this.strictUpdateFill(metaObject, updateTimeFieldName, LocalDateTime.class, LocalDateTime.now());
        if (metaObject.getValue(updatePersonFieldName) == null) {
            this.strictUpdateFill(metaObject, updatePersonFieldName, Long.class, updatePersonId);
        }
    }
}