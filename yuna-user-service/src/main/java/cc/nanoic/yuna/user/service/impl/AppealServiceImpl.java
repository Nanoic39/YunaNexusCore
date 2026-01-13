package cc.nanoic.yuna.user.service.impl;

import cc.nanoic.yuna.common.core.exception.BusinessException;
import cc.nanoic.yuna.common.core.result.ResultCode;
import cc.nanoic.yuna.user.entity.Appeal;
import cc.nanoic.yuna.user.mapper.AppealMapper;
import cc.nanoic.yuna.user.service.AppealService;
import cc.nanoic.yuna.user.service.BanRecordService;
import cc.nanoic.yuna.user.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AppealServiceImpl implements AppealService {

    private final AppealMapper appealMapper;
    private final StringRedisTemplate redisTemplate;
    private final UserService userService;
    private final BanRecordService banRecordService;

    @Override
    public void submit(Long userId, String contact, String reason) {
        Long uid = userId;
        if (uid == null) {
            // 未登录用户允许提交，但做联系方式必须
            if (contact == null || contact.isBlank()) {
                throw new BusinessException(ResultCode.FAILURE, "请提供联系方式以便处理申诉");
            }
        }
        // 防重复：存在未处理或处理中申诉则拒绝
        Long targetUserId = uid;
        Appeal existing = appealMapper.selectOne(new LambdaQueryWrapper<Appeal>()
                .eq(targetUserId != null, Appeal::getUserId, targetUserId)
                .in(Appeal::getStatus, 0, 1)
                .orderByDesc(Appeal::getCreateTime)
                .last("LIMIT 1"));
        if (existing != null) {
            throw new BusinessException(ResultCode.FAILURE, "已存在待处理申诉，请勿重复提交");
        }
        Appeal a = new Appeal();
        a.setUserId(targetUserId);
        a.setContact(contact);
        a.setReason(reason);
        a.setStatus(0);
        a.setCreateTime(LocalDateTime.now());
        a.setUpdateTime(LocalDateTime.now());
        appealMapper.insert(a);
    }

    @Override
    public void claim(Long appealId, Long operatorId) {
        String lockKey = "yuna:appeal:lock:" + appealId;
        Boolean ok = redisTemplate.opsForValue().setIfAbsent(lockKey, String.valueOf(operatorId),
                Duration.ofMinutes(10));
        if (ok == null || !ok) {
            throw new BusinessException(ResultCode.FAILURE, "该申诉正在处理中，请稍后再试");
        }
        Appeal a = appealMapper.selectById(appealId);
        if (a == null) {
            redisTemplate.delete(lockKey);
            throw new BusinessException(ResultCode.NOT_FOUND, "申诉不存在");
        }
        if (a.getStatus() != null && a.getStatus() != 0) {
            redisTemplate.delete(lockKey);
            throw new BusinessException(ResultCode.FAILURE, "当前申诉不在待处理状态");
        }
        a.setStatus(1);
        a.setOperatorId(operatorId);
        a.setUpdateTime(LocalDateTime.now());
        appealMapper.updateById(a);
    }

    @Override
    public void release(Long appealId, Long operatorId) {
        String lockKey = "yuna:appeal:lock:" + appealId;
        String owner = redisTemplate.opsForValue().get(lockKey);
        if (owner == null || !owner.equals(String.valueOf(operatorId))) {
            throw new BusinessException(ResultCode.FAILURE, "未持有该申诉的处理锁");
        }
        Appeal a = appealMapper.selectById(appealId);
        if (a != null && a.getStatus() != null && a.getStatus() == 1) {
            a.setStatus(0);
            a.setOperatorId(null);
            a.setUpdateTime(LocalDateTime.now());
            appealMapper.updateById(a);
        }
        redisTemplate.delete(lockKey);
    }

    @Override
    public void approve(Long appealId, Long operatorId, String remark) {
        String lockKey = "yuna:appeal:lock:" + appealId;
        String owner = redisTemplate.opsForValue().get(lockKey);
        if (owner == null || !owner.equals(String.valueOf(operatorId))) {
            throw new BusinessException(ResultCode.FAILURE, "未持有该申诉的处理锁");
        }
        Appeal a = appealMapper.selectById(appealId);
        if (a == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "申诉不存在");
        }
        a.setStatus(2);
        a.setOperatorId(operatorId);
        a.setProcessRemark(remark);
        a.setUpdateTime(LocalDateTime.now());
        appealMapper.updateById(a);
        if (a.getUserId() != null) {
            userService.updateStatus(a.getUserId(), 1);
            banRecordService.closeOpenBans(a.getUserId(), operatorId);
        }
        redisTemplate.delete(lockKey);
    }

    @Override
    public void reject(Long appealId, Long operatorId, String remark) {
        String lockKey = "yuna:appeal:lock:" + appealId;
        String owner = redisTemplate.opsForValue().get(lockKey);
        if (owner == null || !owner.equals(String.valueOf(operatorId))) {
            throw new BusinessException(ResultCode.FAILURE, "未持有该申诉的处理锁");
        }
        Appeal a = appealMapper.selectById(appealId);
        if (a == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "申诉不存在");
        }
        a.setStatus(3);
        a.setOperatorId(operatorId);
        a.setProcessRemark(remark);
        a.setUpdateTime(LocalDateTime.now());
        appealMapper.updateById(a);
        redisTemplate.delete(lockKey);
    }
}
