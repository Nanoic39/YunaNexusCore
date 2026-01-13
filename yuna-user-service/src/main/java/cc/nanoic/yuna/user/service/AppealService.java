package cc.nanoic.yuna.user.service;

public interface AppealService {
    void submit(Long userId, String contact, String reason);

    void claim(Long appealId, Long operatorId);

    void release(Long appealId, Long operatorId);

    void approve(Long appealId, Long operatorId, String remark);

    void reject(Long appealId, Long operatorId, String remark);
}
