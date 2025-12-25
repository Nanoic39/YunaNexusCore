package cc.nanoic.yuna.common.core.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAILURE(400, "业务异常"),
    UN_AUTHORIZED(401, "请求未授权"),
    NOT_FOUND(404, "404 没找到请求"),
    MSG_NOT_READABLE(400, "消息不能读取"),
    INTERNAL_SERVER_ERROR(500, "服务器异常"),
    
    // 用户服务相关异常 1000~1999
    USER_EXIST(1001, "用户已存在"),
    USER_NOT_EXIST(1002, "用户不存在"),
    PASSWORD_ERROR(1003, "密码错误");

    final int code;
    final String msg;
}