package cc.nanoic.yuna.user.service.impl;

import cc.nanoic.yuna.common.core.exception.BusinessException;
import cc.nanoic.yuna.user.service.AuthService;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final String REGISTER_CODE_PREFIX = "auth:register:code:";
    private static final long CODE_EXPIRE_MINUTES = 5;

    @Override
    public void sendEmailVerifyCode(String email) {
        if (StrUtil.isBlank(email)) {
            throw new BusinessException("邮箱不能为空");
        }

        String key = REGISTER_CODE_PREFIX + email;

        // 完整防刷：检查是否已存在且未过期，若存在则拒绝重复发送
        Long ttl = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        if (ttl != null && ttl > CODE_EXPIRE_MINUTES * 60 - 60) {
            // 距离上次发送不足1分钟视为频繁操作(前端设置90秒按钮锁定)
            throw new BusinessException("操作过于频繁，请稍后再试");
        }
        String code = RandomUtil.randomNumbers(6);

        // 保存到Redis
        stringRedisTemplate.opsForValue().set(key, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 发送邮件
        try {
            jakarta.mail.internet.MimeMessage mimeMessage = mailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(
                    mimeMessage, true);

            // 设置发送人别名 "YunaNexus <email>"
            helper.setFrom(fromEmail, "YunaNexus");
            helper.setTo(email);
            helper.setSubject("【YunaNexus】欢迎注册 - 您的验证码");

            // 构建 HTML 内容
            String htmlContent = String.format(
                    """
                            <!DOCTYPE html>
                            <html>
                            <head>
                                <meta charset="UTF-8">
                                <title>YunaNexus 验证码</title>
                            </head>
                            <body style="margin: 0; padding: 0; font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; background-color: #f6f8fa;">
                                <div style="max-width: 600px; margin: 0 auto; padding: 40px 20px;">
                                    <!-- 顶部 Logo/标题区域 -->
                                    <div style="text-align: center; margin-bottom: 30px;">
                                        <h1 style="color: #333; margin: 0; font-size: 28px;">YunaNexus Core</h1>
                                        <p style="color: #666; font-size: 14px; margin-top: 5px;">下一代微服务架构系统</p>
                                    </div>

                                    <!-- 主内容卡片 -->
                                    <div style="background-color: #ffffff; border-radius: 8px; padding: 40px; box-shadow: 0 4px 12px rgba(0,0,0,0.05);">
                                        <h2 style="color: #333; margin-top: 0; font-size: 20px;">你好，未来的成员！</h2>
                                        <p style="color: #555; line-height: 1.6; font-size: 16px;">
                                            感谢您注册 YunaNexus。我们需要验证您的电子邮件地址以完成账户创建。
                                        </p>
                                        <p style="color: #555; line-height: 1.6; font-size: 16px;">
                                            请在注册页面输入以下 6 位验证码：
                                        </p>

                                        <!-- 验证码展示区 -->
                                        <div style="background-color: #f0f7ff; border-radius: 6px; padding: 20px; text-align: center; margin: 30px 0;">
                                            <span style="font-size: 32px; font-weight: bold; letter-spacing: 8px; color: #0066cc;">%s</span>
                                        </div>

                                        <p style="color: #888; font-size: 14px; margin-bottom: 0;">
                                            * 此验证码将在 5 分钟后失效。<br>
                                            * 如果这不是您的操作，请忽略此邮件。
                                        </p>
                                    </div>

                                    <!-- 底部信息卡片 -->
                                    <div style="margin-top: 30px; text-align: center; color: #999; font-size: 12px;">
                                        <p style="margin: 5px 0;">© 2025 YunaNexus Team. All rights reserved.</p>
                                        <p style="margin: 5px 0;">
                                            <a href="#" style="color: #999; text-decoration: none;">官方网站</a> |
                                            <a href="#" style="color: #999; text-decoration: none;">联系支持</a> |
                                            <a href="#" style="color: #999; text-decoration: none;">隐私政策</a>
                                        </p>
                                        <p style="margin-top: 15px; border-top: 1px solid #eee; padding-top: 15px;">
                                            此邮件由系统自动发送，请勿回复。<br>
                                            Powered by YunaNexus Notification Service
                                        </p>
                                    </div>
                                </div>
                            </body>
                            </html>
                            """,
                    code);

            helper.setText(htmlContent, true); // true 表示支持 HTML

            mailSender.send(mimeMessage);
            log.info("验证码已发送至: {}, code: {}", email, code);
        } catch (Exception e) {
            log.error("邮件发送失败", e);
            throw new BusinessException("验证码发送失败，请稍后重试");
        }
    }

    @Override
    public boolean verifyCode(String email, String code) {
        if (StrUtil.hasBlank(email, code)) {
            return false;
        }
        String key = REGISTER_CODE_PREFIX + email;
        String cachedCode = stringRedisTemplate.opsForValue().get(key);

        // 校验是否相等
        if (code.equals(cachedCode)) {
            // 验证通过后删除验证码，防止重复使用
            stringRedisTemplate.delete(key);
            return true;
        }
        return false;
    }
}