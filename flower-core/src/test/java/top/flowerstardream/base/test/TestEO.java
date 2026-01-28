package top.flowerstardream.base.test;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import top.flowerstardream.base.bo.eo.BaseEO;
import top.flowerstardream.base.state.StatusAble;

import java.io.Serializable;

/**
 * @Author: 花海
 * @Date: 2025/12/17/00:50
 * @Description: 测试实体类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@TableName("test")
public class TestEO extends BaseEO implements Serializable, StatusAble {

    @TableField("status")
    private Integer status;
}
