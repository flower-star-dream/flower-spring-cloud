package top.flowerstardream.base.service.Impl;

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
import top.flowerstardream.base.service.MailService;

import java.util.Date;

/**
 *  邮件服务实现类
 *  使用 Thymeleaf 模板
 *  邮件模板：templates/mail/verification.html
 *  邮件内容：使用 Thymeleaf 模板渲染后的 HTML 内容
 *  邮件标题：【您的应用名称】安全验证码
 * @Author: 花海
 * @Date: 2026/02/09/20:32
 * @Description: 邮件服务实现类
 */
@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    @Resource
    private JavaMailSender mailSender;

    @Resource
    private TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String from;

    /**
     * 发送验证码邮件（使用 Thymeleaf 模板）
     */
    @Override
    public void sendVerificationCode(String to, String code, int expireMinutes, String appName) throws MessagingException {
        // 创建 Thymeleaf 上下文对象，用于存储变量
        Context context = new Context();
        context.setVariable("code", code);
        context.setVariable("expireMinutes", expireMinutes);
        context.setVariable("sendTime", new Date());

        // 渲染模板（templates/mail/verification.html）
        String emailContent = templateEngine.process("mail/verification", context);

        // 发送邮件
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject("【" + appName + "】安全验证码");
        helper.setText(emailContent, true);

        mailSender.send(message);
    }
}