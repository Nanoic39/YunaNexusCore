package cc.nanoic.yuna.common.core.exception;

import cc.nanoic.yuna.common.core.result.R;
import cc.nanoic.yuna.common.core.result.ResultCode;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常处理
     * 
     * @param e
     * @return
     */
    @ExceptionHandler(BusinessException.class)
    public R<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, msg={}, tips={}", e.getCode(), e.getMessage(), e.getTips());
        return R.fail(ResultCode.FAILURE, e.getMessage(), e.getTips());
    }

    /**
     * 资源未找到异常
     */
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public R<Void> handleNoResourceFoundException(org.springframework.web.servlet.resource.NoResourceFoundException e) {
        log.warn("资源未找到: {}", e.getResourcePath());
        return R.fail(ResultCode.NOT_FOUND, e.getResourcePath(), "接口不存在!");
    }

    /**
     * 兜底异常处理, 处理所有未被其他异常处理程序捕获的异常
     * 
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return R.fail("系统内部异常: " + e.getMessage(), "系统繁忙，请稍后再试");
    }
}
