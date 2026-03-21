package top.flowerstardream.base.ao.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @Author: 花海
 * @Date: 2026/03/19/04:29
 * @Description: 基础校验响应
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "基础校验响应")
public class BaseVerificationRES implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "id列表")
    private List<Long> ids;

    @Schema(description = "id")
    private Long id;

    @Schema(description = "响应结果列表")
    private List<Boolean> booleansList;

    @Schema(description = "响应结果")
    private Boolean booleanValue;
}
