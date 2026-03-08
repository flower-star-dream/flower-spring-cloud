package top.flowerstardream.base.bo.dto;

import com.baomidou.mybatisplus.annotation.IEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import top.flowerstardream.base.bo.eo.BaseEO;
import top.flowerstardream.base.state.BaseStatus;
import top.flowerstardream.base.state.IBaseState;
import top.flowerstardream.base.state.StatusAble;

/**
 * @Author: 花海
 * @Date: 2025/12/16/23:15
 * @Description: 状态传输对象
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "基础状态更改传输对象")
public class BaseStatusDTO<T extends BaseEO & StatusAble<E>, E extends IEnum<?> & IBaseState<?>> {
    @Schema(description = "ID")
    @NotNull(message = "ID不能为空")
    private Long id;

    @Schema(description = "状态")
    @NotNull(message = "状态不能为空")
    private E status;

    @Schema(description = "业务对象")
    @NotNull(message = "业务对象不能为空")
    private T data;
}
