package cc.nanoic.yuna.file.task;

import cc.nanoic.yuna.file.entity.YunaFile;
import cc.nanoic.yuna.file.mapper.UserFileShardMapper;
import cc.nanoic.yuna.file.util.UserFileTableUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class RecycleBinScheduler {

    private final UserFileShardMapper shardMapper;

    /**
     * 每天凌晨 3:00 执行
     * 1. 30天前进入回收站的文件 -> 隐藏 (Status 2)
     * 2. 180天前隐藏的文件 -> 备份并彻底删除 (Status 3 or Physical Delete)
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void processRecycleBin() {
        log.info("开始执行回收站清理任务...");

        for (int i = 0; i <= 3; i++) {
            String table = UserFileTableUtil.userFileTable(i);
            processShard(table);
        }

        log.info("回收站清理任务结束");
    }

    private void processShard(String table) {
        // 大于30天 且 小于180天
        try {
            List<YunaFile> toHide = shardMapper.selectExpiredForHide(table);
            for (YunaFile f : toHide) {
                log.info("文件超过30天，转为隐藏状态: uuid={}, originName={}", f.getUuid(), f.getOriginName());
                shardMapper.updateStatus(table, f.getUuid(), f.getUserId(), 2, f.getUserId());
            }
        } catch (Exception e) {
            log.error("处理30天过期文件失败: table={}", table, e);
        }

        // 大于180天
        try {
            List<YunaFile> toDelete = shardMapper.selectExpiredForDelete(table);
            for (YunaFile f : toDelete) {
                log.info("文件超过180天，进行备份并彻底删除: uuid={}, originName={}", f.getUuid(), f.getOriginName());
                // TODO: 实现压缩并保存数据备份逻辑
                backupFile(f); 

                shardMapper.updateStatus(table, f.getUuid(), f.getUserId(), 3, f.getUserId());
            }
        } catch (Exception e) {
            log.error("处理180天过期文件失败: table={}", table, e);
        }
    }

    private void backupFile(YunaFile f) {
        // TODO: 实现压缩并保存数据备份逻辑(冷储存)
    }
}
