package cc.nanoic.yuna.user.schedule;

import cc.nanoic.yuna.user.entity.BanRecord;
import cc.nanoic.yuna.user.service.BanRecordService;
import cc.nanoic.yuna.user.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class BanAutoUnbanScheduler {

    private final BanRecordService banRecordService;
    private final UserService userService;

    @Scheduled(fixedDelay = 60000)
    public void autoUnbanExpired() {
        LocalDateTime now = LocalDateTime.now();
        List<BanRecord> openBans = banRecordService.list(new LambdaQueryWrapper<BanRecord>()
                .and(w -> w.isNull(BanRecord::getEndTime).or().gt(BanRecord::getEndTime, now)));
        Set<Long> openUserIds = new HashSet<>();
        for (BanRecord br : openBans) {
            if (br.getUserId() != null) {
                openUserIds.add(br.getUserId());
            }
        }
        List<cc.nanoic.yuna.user.entity.User> bannedUsers = userService
                .list(new LambdaQueryWrapper<cc.nanoic.yuna.user.entity.User>()
                        .eq(cc.nanoic.yuna.user.entity.User::getStatus, 2)
                        .notIn(!openUserIds.isEmpty(), cc.nanoic.yuna.user.entity.User::getId, openUserIds));
        for (cc.nanoic.yuna.user.entity.User u : bannedUsers) {
            userService.updateStatus(u.getId(), 1);
        }
    }
}
