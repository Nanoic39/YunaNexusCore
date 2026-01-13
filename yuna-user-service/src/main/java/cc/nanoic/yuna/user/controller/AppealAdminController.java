package cc.nanoic.yuna.user.controller;

import cc.nanoic.yuna.common.core.constant.SecurityConstants;
import cc.nanoic.yuna.common.core.result.R;
import cc.nanoic.yuna.common.security.annotation.RequiresPermissions;
import cc.nanoic.yuna.user.entity.Appeal;
import cc.nanoic.yuna.user.mapper.AppealMapper;
import cc.nanoic.yuna.user.service.AppealService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/appeal/admin")
@RequiredArgsConstructor
public class AppealAdminController {

    private final AppealMapper appealMapper;
    private final AppealService appealService;

    @GetMapping("/list")
    @RequiresPermissions("sys:appeal:list")
    public R<Page<Appeal>> list(@RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "keyword", required = false) String keyword) {
        Page<Appeal> p = new Page<>(page, size);
        LambdaQueryWrapper<Appeal> qw = new LambdaQueryWrapper<Appeal>()
                .eq(status != null, Appeal::getStatus, status)
                .and(keyword != null && !keyword.isEmpty(), w -> w
                        .like(Appeal::getContact, keyword)
                        .or().like(Appeal::getReason, keyword))
                .orderByDesc(Appeal::getCreateTime);
        return R.success(appealMapper.selectPage(p, qw));
    }

    @PostMapping("/{id}/claim")
    @RequiresPermissions("sys:appeal:process")
    public R<Void> claim(@RequestHeader(SecurityConstants.DETAILS_USER_ID) Long operatorId,
            @PathVariable("id") Long id) {
        appealService.claim(id, operatorId);
        return R.success(null, "锁定成功");
    }

    @PostMapping("/{id}/release")
    @RequiresPermissions("sys:appeal:process")
    public R<Void> release(@RequestHeader(SecurityConstants.DETAILS_USER_ID) Long operatorId,
            @PathVariable("id") Long id) {
        appealService.release(id, operatorId);
        return R.success(null, "释放成功");
    }

    @PostMapping("/{id}/approve")
    @RequiresPermissions("sys:appeal:process")
    public R<Void> approve(@RequestHeader(SecurityConstants.DETAILS_USER_ID) Long operatorId,
            @PathVariable("id") Long id,
            @RequestBody String remark) {
        appealService.approve(id, operatorId, remark);
        return R.success(null, "已通过申诉并解封");
    }

    @PostMapping("/{id}/reject")
    @RequiresPermissions("sys:appeal:process")
    public R<Void> reject(@RequestHeader(SecurityConstants.DETAILS_USER_ID) Long operatorId,
            @PathVariable("id") Long id,
            @RequestBody String remark) {
        appealService.reject(id, operatorId, remark);
        return R.success(null, "已驳回申诉");
    }
}
