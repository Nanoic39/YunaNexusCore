package cc.nanoic.yuna.user.service;

public interface AppealService {
    void submit(Long userId, String contact, String reason);
}

