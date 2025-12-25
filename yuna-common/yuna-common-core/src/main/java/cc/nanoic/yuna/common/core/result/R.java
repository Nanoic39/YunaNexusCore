package cc.nanoic.yuna.common.core.result;


import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class R<T> implements Serializable {

    private Integer code;
    private boolean success;

    /**
     * 调试信息, 格式：[时间戳] {内容}
     */
    private String msg;

    /**
     * 提示信息, 可为空, 为空时前端不展示
     */
    private String tips = null;

    /**
     * 返回数据
     */
    private T data;

    /**
     * 构造函数
     * @param code 状态码
     * @param data 返回数据
     * @param debugMsg 调试信息
     * @param tips 提示信息
     */
    private R(Integer code, T data, String debugMsg, String tips) {
        this.code = code;
        this.success = (code == 200);
        this.msg = String.format("[%s] %s", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), debugMsg);
        this.tips = tips;
        this.data = data;
    }

    /* ==== 成功响应 ==== */
    /**
     * 默认无结果成功响应
     * @return 成功响应
     */
    public static <T> R<T> success() {
        return new R<>(ResultCode.SUCCESS.getCode(), null, "SUCCESS", null);
    }

    /**
     * 有数据成功响应
     * @param data 返回数据
     * @return 成功响应
     */
    public static <T> R<T> success(T data) {
        return new R<>(ResultCode.SUCCESS.getCode(), data, "SUCCESS", null);
    }

    /**
     * 有数据和提示成功响应
     * @param data 返回数据
     * @param tips 提示信息
     * @return 成功响应
     */
    public static <T> R<T> success(T data, String tips) {
        return new R<>(ResultCode.SUCCESS.getCode(), data, "SUCCESS", tips);
    }

    /* ==== 失败响应 ==== */
    /**
     * 默认业务异常失败响应
     * @param debugMsg 调试信息
     * @param tips 提示信息
     * @return 失败响应
     */
    public static <T> R<T> fail(String debugMsg, String tips) {
        return new R<>(ResultCode.FAILURE.getCode(), null, debugMsg, tips);
    }

    /**
     * 无tips提示业务异常失败响应
     * @param debugMsg 调试信息
     * @return 失败响应
     */
    public static <T> R<T> fail(String debugMsg) {
        return new R<>(ResultCode.FAILURE.code, null, debugMsg, null);
    }
    
    /**
     * 自定义状态码业务异常失败响应
     * @param resultCode 状态码
     * @param debugMsg 调试信息
     * @param userTips 提示信息
     * @return 失败响应
     */
    public static <T> R<T> fail(ResultCode resultCode, String debugMsg, String userTips) {
        return new R<>(resultCode.code, null, debugMsg, userTips);
    }

    /**
     * 失败 (使用 ResultCode 的默认消息作为调试信息)
     */
    public static <T> R<T> fail(ResultCode resultCode) {
        // 默认将枚举里的 msg 同时传给 debugMsg 和 tips
        return new R<>(resultCode.getCode(), null, resultCode.getMsg(), resultCode.getMsg());
    }
}
