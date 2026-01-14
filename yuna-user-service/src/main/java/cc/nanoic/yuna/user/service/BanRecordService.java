package cc.nanoic.yuna.user.service;

import cc.nanoic.yuna.user.entity.BanRecord;
import com.baomidou.mybatisplus.extension.service.IService;

public interface BanRecordService extends IService<BanRecord> {
    void recordBan(Long userId, Long operatorId, Integer type, Integer range, String service, String reason,
            java.time.LocalDateTime endTime);

    void closeOpenBans(Long userId, Long operatorId);
}
