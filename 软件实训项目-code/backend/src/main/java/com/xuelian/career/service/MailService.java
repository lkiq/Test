package com.xuelian.career.service;

import com.xuelian.career.common.BusinessException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * 邮件服务 - 发送验证码邮件
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    /**
     * 发送验证码邮件
     * @param to   收件人邮箱
     * @param code 6位验证码
     */
    public void sendVerificationCode(String to, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("IT求职平台 - 密码重置验证码");

            String htmlContent = buildEmailContent(code);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("验证码邮件已发送至: {}", to);
        } catch (MessagingException e) {
            log.error("邮件发送失败: {}", e.getMessage());
            throw new BusinessException(500, "邮件发送失败，请稍后重试");
        }
    }

    /**
     * 发送邮箱登录验证码
     * @param to   收件人邮箱
     * @param code 6位验证码
     */
    public void sendLoginCode(String to, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("IT求职平台 - 邮箱登录验证码");

            String htmlContent = buildLoginEmailContent(code);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("登录验证码邮件已发送至: {}", to);
        } catch (MessagingException e) {
            log.error("邮件发送失败: {}", e.getMessage());
            throw new BusinessException(500, "邮件发送失败，请稍后重试");
        }
    }

    /**
     * 生成6位随机数字验证码
     */
    public String generateCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    /**
     * 构建登录验证码 HTML 邮件内容
     */
    private String buildLoginEmailContent(String code) {
        return """
            <div style="max-width:600px;margin:0 auto;font-family:'Microsoft YaHei',Arial,sans-serif;">
                <div style="background:linear-gradient(135deg,#7c3aed,#6d28d9);padding:30px;text-align:center;border-radius:12px 12px 0 0;">
                    <h1 style="color:#fff;margin:0;font-size:24px;">IT求职平台</h1>
                    <p style="color:rgba(255,255,255,0.85);margin:8px 0 0;font-size:14px;">邮箱验证码登录</p>
                </div>
                <div style="background:#fff;padding:40px 30px;border:1px solid #e5e7eb;border-top:none;border-radius:0 0 12px 12px;">
                    <p style="color:#374151;font-size:15px;margin:0 0 20px;">您好，您正在通过邮箱验证码登录 IT求职平台。</p>
                    <div style="background:#f5f3ff;border:1px dashed #7c3aed;border-radius:8px;padding:24px;text-align:center;margin-bottom:24px;">
                        <p style="color:#6b7280;font-size:13px;margin:0 0 12px;">您的登录验证码为（5分钟内有效）</p>
                        <span style="font-size:32px;font-weight:700;color:#7c3aed;letter-spacing:8px;">%s</span>
                    </div>
                    <p style="color:#9ca3af;font-size:13px;margin:0 0 8px;">• 验证码5分钟内有效，请尽快使用</p>
                    <p style="color:#9ca3af;font-size:13px;margin:0 0 8px;">• 如非本人操作，请忽略此邮件</p>
                    <p style="color:#9ca3af;font-size:13px;margin:0;">• 请勿将验证码泄露给他人</p>
                    <hr style="border:none;border-top:1px solid #e5e7eb;margin:24px 0;">
                    <p style="color:#9ca3af;font-size:12px;text-align:center;margin:0;">© 2026 IT求职平台 · 技术人才专属</p>
                </div>
            </div>
            """.formatted(code);
    }

    /**
     * 构建 HTML 邮件内容
     */
    private String buildEmailContent(String code) {
        return """
            <div style="max-width:600px;margin:0 auto;font-family:'Microsoft YaHei',Arial,sans-serif;">
                <div style="background:linear-gradient(135deg,#2563eb,#1d4ed8);padding:30px;text-align:center;border-radius:12px 12px 0 0;">
                    <h1 style="color:#fff;margin:0;font-size:24px;">IT求职平台</h1>
                    <p style="color:rgba(255,255,255,0.85);margin:8px 0 0;font-size:14px;">密码重置验证</p>
                </div>
                <div style="background:#fff;padding:40px 30px;border:1px solid #e5e7eb;border-top:none;border-radius:0 0 12px 12px;">
                    <p style="color:#374151;font-size:15px;margin:0 0 20px;">您好，您正在申请重置密码。</p>
                    <div style="background:#f0f9ff;border:1px dashed #2563eb;border-radius:8px;padding:24px;text-align:center;margin-bottom:24px;">
                        <p style="color:#6b7280;font-size:13px;margin:0 0 12px;">您的验证码为（5分钟内有效）</p>
                        <span style="font-size:32px;font-weight:700;color:#2563eb;letter-spacing:8px;">%s</span>
                    </div>
                    <p style="color:#9ca3af;font-size:13px;margin:0 0 8px;">• 验证码5分钟内有效，请尽快使用</p>
                    <p style="color:#9ca3af;font-size:13px;margin:0 0 8px;">• 如非本人操作，请忽略此邮件</p>
                    <p style="color:#9ca3af;font-size:13px;margin:0;">• 请勿将验证码泄露给他人</p>
                    <hr style="border:none;border-top:1px solid #e5e7eb;margin:24px 0;">
                    <p style="color:#9ca3af;font-size:12px;text-align:center;margin:0;">© 2026 IT求职平台 · 技术人才专属</p>
                </div>
            </div>
            """.formatted(code);
    }
}
