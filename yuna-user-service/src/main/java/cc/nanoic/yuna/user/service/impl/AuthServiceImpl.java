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

        // 简单防刷：检查是否已存在且过期时间较长（例如刚发过）
        // 这里简化处理，直接生成覆盖

        String code = RandomUtil.randomNumbers(6);

        // 保存到Redis
        stringRedisTemplate.opsForValue().set(key, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 发送邮件
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("【YunaNexus】注册验证码");
            message.setText("您的注册验证码是：" + code + "，有效期5分钟。请勿泄露给他人。");
            mailSender.send(message);
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