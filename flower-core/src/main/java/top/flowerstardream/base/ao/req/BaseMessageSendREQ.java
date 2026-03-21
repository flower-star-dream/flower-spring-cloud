package top.flowerstardream.base.ao.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;

/**
 * 短信发送请求
 * @Author: 花海
 * @Date: 2026/03/17/15:44
 * @Description: 短信发送请求
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "短信发送请求")
public class BaseMessageSendREQ implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "手机号")
    private String phone;
    @Schema(description = "邮箱")
    private String email;
    // 可选：短信模板
    @Schema(description = "短信模板")
    private String templateCode;
}