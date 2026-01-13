package cc.nanoic.yuna.user.service.impl;

import cc.nanoic.yuna.user.entity.BanRecord;
import cc.nanoic.yuna.user.mapper.BanRecordMapper;
import cc.nanoic.yuna.user.service.BanRecordService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BanRecordServiceImpl extends ServiceImpl<BanRecordMapper, BanRecord> implements BanRecordService {

    @Override
    public void recordBan(Long userId, Long operatorId, Integer type, Integer range, String service, String reason) {
        BanRecord br = new BanRecord();
        br.setUserId(userId);
        br.setBanType(type == null ? 2 : type);
        br.setBanRange(range == null ? 1 : range);
        br.setBanService(service);
        br.setBanReason(reason == null ? "系统封禁" : reason);
        br.setBanOperatorId(operatorId);
        br.setStartTime(LocalDateTime.now());
        this.save(br);
    }

    @Override
    public void closeOpenBans(Long userId, Long operatorId) {
        List<BanRecord> opens = this.list(new LambdaQueryWrapper<BanRecord>()
                .eq(BanRecord::getUserId, userId)
                .isNull(BanRecord::getEndTime));
        LocalDateTime now = LocalDateTime.now();
        for (BanRecord br : opens) {
            br.setEndTime(now);
            br.setUnbanOperatorId(operatorId);
            this.updateById(br);
        }
    }
}
