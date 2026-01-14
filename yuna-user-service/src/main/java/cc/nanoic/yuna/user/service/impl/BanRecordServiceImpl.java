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
    public void recordBan(Long userId, Long operatorId, Integer type, Integer range, String service, String reason,
            LocalDateTime endTime) {
        BanRecord br = new BanRecord();
        br.setUserId(userId);
        br.setBanType(type == null ? 2 : type);
        br.setBanRange(range == null ? 1 : range);
        br.setBanService(service);
        br.setBanReason(reason);
        br.setBanOperatorId(operatorId);
        br.setStartTime(LocalDateTime.now());
        br.setEndTime(endTime);
        this.save(br);
    }

    @Override
    public void closeOpenBans(Long userId, Long operatorId) {
        LocalDateTime now = LocalDateTime.now();
        List<BanRecord> opens = this.list(new LambdaQueryWrapper<BanRecord>()
                .eq(BanRecord::getUserId, userId)
                .and(w -> w.isNull(BanRecord::getEndTime).or().gt(BanRecord::getEndTime, now)));
        for (BanRecord br : opens) {
            br.setEndTime(now);
            br.setUnbanOperatorId(operatorId);
            this.updateById(br);
        }
    }
}
