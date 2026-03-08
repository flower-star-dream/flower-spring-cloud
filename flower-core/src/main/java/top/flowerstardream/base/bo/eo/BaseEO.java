package top.flowerstardream.base.bo.eo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 花海
 * @Date: 2025/10/15/11:03
 * @Description 基础实体类
 */
@Data
@SuperBuilder
@NoArgsConstructor
public class BaseEO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
//    @TableId(value = "id", type = IdType.INPUT)
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @TableField(value = "id", fill = FieldFill.INSERT)
    protected Long id;
}