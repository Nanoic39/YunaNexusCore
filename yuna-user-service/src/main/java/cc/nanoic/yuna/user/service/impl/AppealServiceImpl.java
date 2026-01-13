package cc.nanoic.yuna.user.service.impl;

import cc.nanoic.yuna.user.service.AppealService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AppealServiceImpl implements AppealService {

    private final StringRedisTemplate redisTemplate;

    @Override
    public void submit(Long userId, String contact, String reason) {
        String key = userId != null ? "yuna:appeal:user:" + userId : "yuna:appeal:contact:" + (contact == null ? "unknown" : contact);
        String value = String.format("%d|%s|%s|%s",
                Instant.now().toEpochMilli(),
                userId == null ? "" : String.valueOf(userId),
                contact == null ? "" : contact,
                reason == null ? "" : reason);
        redisTemplate.opsForList().leftPush(key, value);
    }
}

