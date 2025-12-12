package top.flowerstardream.base.ao.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Author: 花海
 * @Date: 2025/11/08/15:01
 * @Description: 基础分页查询请求
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BasePageQueryREQ implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "页码")
    protected int page;

    @Schema(description = "每页显示记录数")
    protected int pageSize;

}
