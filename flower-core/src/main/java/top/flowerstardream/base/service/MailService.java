package top.flowerstardream.base.service;

import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;

/**
 *  邮件服务接口
 * @Author: 花海
 * @Date: 2026/02/09/20:32
 * @Description: 邮件服务接口
 */
public interface MailService {

    /**
     * 发送验证码邮件
     */
    void sendVerificationCode(String to, String code, int expireMinutes, String appName) throws MessagingException;

}