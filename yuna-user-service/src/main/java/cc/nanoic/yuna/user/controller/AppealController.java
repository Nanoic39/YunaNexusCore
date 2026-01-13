package cc.nanoic.yuna.user.controller;

import cc.nanoic.yuna.common.core.constant.SecurityConstants;
import cc.nanoic.yuna.common.core.result.R;
import cc.nanoic.yuna.user.model.dto.AppealSubmitDTO;
import cc.nanoic.yuna.user.service.AppealService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/appeal")
@RequiredArgsConstructor
public class AppealController {

    private final AppealService appealService;

    @PostMapping
    public R<Void> submit(@RequestHeader(value = SecurityConstants.DETAILS_USER_ID, required = false) Long userId,
                          @RequestBody AppealSubmitDTO dto) {
        appealService.submit(userId, dto.getContact(), dto.getReason());
        return R.success(null, "申诉提交成功，我们将尽快处理");
    }
}

