package top.flowerstardream.base.bo.eo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;

/**
 * @Author: 花海
 * @Date: 2026/02/26/16:21
 * @Description: 带乐观锁实体类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
public class OptimisticLockBaseEO extends AuditBaseEO{
    @Serial
    private static final long serialVersionUID = 1L;
    @TableField(value = "version", fill = FieldFill.INSERT_UPDATE)
    protected Integer version;
}
