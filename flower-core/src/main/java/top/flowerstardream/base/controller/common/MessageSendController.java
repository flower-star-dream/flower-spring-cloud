package top.flowerstardream.base.controller.common;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.flowerstardream.base.ao.req.BaseMessageSendREQ;
import top.flowerstardream.base.enums.ITCEnum;
import top.flowerstardream.base.properties.OtherProperties;
import top.flowerstardream.base.service.VerificationCodeService;

import static top.flowerstardream.base.exception.ExceptionEnum.EMPTY_PARAMETER;

/**
 * @Author: 花海
 * @Date: 2026/03/18/03:17
 * @Description: 消息发送接口
 */
@RestController
@RequestMapping("/api/base/v1/common/message")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "spring.mail", name = "host")
public class MessageSendController {

    @Resource
    private VerificationCodeService verificationCodeServiceImpl;

    /**
     * 验证码发送
     * POST /api/base/v1/common/message/send
     */
    @Operation(summary = "发送验证码")
    @PostMapping("/send")
    public void sendCode(@Valid @RequestBody BaseMessageSendREQ request){
        if (request.getPhone().isEmpty() && request.getEmail().isEmpty()) {
            throw EMPTY_PARAMETER.toException();
        }
        ITCEnum type = request.getPhone().isEmpty() ? ITCEnum.EMAIL : ITCEnum.SMS;
        String target = request.getPhone().isEmpty() ? request.getEmail() : request.getPhone();
        verificationCodeServiceImpl.sendCode(target, type);
    }

}
