package cc.nanoic.yuna.common.core.exception;

import cc.nanoic.yuna.common.core.result.ResultCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final Integer code;
    private final String tips;

    public BusinessException(String tips) {
        super(tips);
        this.code = ResultCode.FAILURE.getCode();
        this.tips = tips;
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMsg());
        this.code = resultCode.getCode();
        this.tips = resultCode.getMsg();
    }

    public BusinessException(ResultCode resultCode, String tips) {
        super(resultCode.getMsg());
        this.code = resultCode.getCode();
        this.tips = tips;
    }

}