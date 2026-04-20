package top.flowerstardream.base.bo.eo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * @Author: 花海
 * @Date: 2026/03/11/14:30
 * @Description: 时间审计基类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
public class TimeAuditBaseEO extends BaseEO {
    @Serial
    private static final long serialVersionUID = 1L;
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableField(value = "create_person_id", fill = FieldFill.INSERT)
    private Long createPersonId;
    @TableField(value = "update_person_id", fill = FieldFill.INSERT_UPDATE)
    private Long updatePersonId;
    @TableField(exist = false)
    private String createPerson;
    @TableField(exist = false)
    private String updatePerson;
}
