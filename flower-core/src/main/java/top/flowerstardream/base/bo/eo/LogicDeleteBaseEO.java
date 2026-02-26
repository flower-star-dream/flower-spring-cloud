package top.flowerstardream.base.bo.eo;

import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;

/**
 * @Author: 花海
 * @Date: 2026/02/26/16:20
 * @Description: 带逻辑删除实体类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
public class LogicDeleteBaseEO extends AuditBaseEO{
    @Serial
    private static final long serialVersionUID = 1L;
    @TableLogic(value = "0", delval = "1")
    protected Boolean deleted;
}
