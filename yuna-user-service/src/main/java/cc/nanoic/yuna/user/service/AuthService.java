package cc.nanoic.yuna.user.service;

public interface AuthService {

    /**
     * 发送邮箱验证码
     * @param email 邮箱账号
     */
    void sendEmailVerifyCode(String email);

    /**
     * 验证邮箱验证码
     * @param email 邮箱账号
     * @param verifyCode 邮箱验证码
     * @return 是否验证成功
     */
    boolean verifyCode(String email, String verifyCode);
}
