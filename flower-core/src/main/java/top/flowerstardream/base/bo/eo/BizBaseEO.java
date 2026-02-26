package top.flowerstardream.base.bo.eo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Author: 花海
 * @Date: 2026/02/26/15:56
 * @Description: 业务基础实体类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
public class BizBaseEO extends AuditBaseEO{
    @Serial
    private static final long serialVersionUID = 1L;
    @TableLogic(value = "0", delval = "1")
    protected Boolean deleted;
    @TableField(value = "version", fill = FieldFill.INSERT_UPDATE)
    protected Integer version;
}
