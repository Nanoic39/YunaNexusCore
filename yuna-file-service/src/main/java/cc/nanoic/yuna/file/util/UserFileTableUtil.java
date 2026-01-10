package cc.nanoic.yuna.file.util;

import cc.nanoic.yuna.common.core.exception.BusinessException;
import cc.nanoic.yuna.common.core.result.ResultCode;

public class UserFileTableUtil {

    private UserFileTableUtil() {
    }

    public static String userFileTable(int shard) {
        if (shard < 0 || shard > 3) {
            throw new BusinessException(ResultCode.FAILURE, "非法分表编号");
        }
        return "yuna_file_" + shard;
    }
}