package cc.nanoic.yuna.user.controller;

import cc.nanoic.yuna.common.core.constant.SecurityConstants;
import cc.nanoic.yuna.common.core.result.R;
import cc.nanoic.yuna.common.security.annotation.RequiresPermissions;
import cc.nanoic.yuna.user.entity.Appeal;
import cc.nanoic.yuna.user.mapper.AppealMapper;
import cc.nanoic.yuna.user.service.AppealService;
import cc.nanoic.yuna.user.mapper.UserMapper;
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
    private final UserMapper userMapper;

    @GetMapping("/list")
    @RequiresPermissions("sys:appeal:list")
    public R<Page<cc.nanoic.yuna.user.model.vo.AppealQueryVO>> list(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
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
        Page<Appeal> result = appealMapper.selectPage(p, qw);
        Page<cc.nanoic.yuna.user.model.vo.AppealQueryVO> voPage = new Page<>(result.getCurrent(), result.getSize(),
                result.getTotal());
        java.util.List<cc.nanoic.yuna.user.model.vo.AppealQueryVO> voList = new java.util.ArrayList<>();
        for (Appeal a : result.getRecords()) {
            cc.nanoic.yuna.user.model.vo.AppealQueryVO vo = new cc.nanoic.yuna.user.model.vo.AppealQueryVO();
            vo.setId(a.getId());
            vo.setContact(a.getContact());
            vo.setReason(a.getReason());
            vo.setStatus(a.getStatus());
            vo.setOperatorId(a.getOperatorId());
            vo.setProcessRemark(a.getProcessRemark());
            vo.setCreateTime(a.getCreateTime());
            vo.setUpdateTime(a.getUpdateTime());
            if (a.getUserId() != null) {
                cc.nanoic.yuna.user.entity.User userEntity = userMapper.selectById(a.getUserId());
                if (userEntity != null) {
                    vo.setUuid(userEntity.getUuid());
                    vo.setUserExist(true);
                } else {
                    vo.setUserExist(false);
                }
            } else if (a.getContact() != null && !a.getContact().isEmpty()) {
                cc.nanoic.yuna.user.model.dto.UserDetailDTO matchedUser = a.getContact().contains("@")
                        ? userMapper.selectUserDetailByEmail(a.getContact())
                        : userMapper.selectUserDetailByUsername(a.getContact());
                if (matchedUser != null) {
                    vo.setUuid(matchedUser.getUuid());
                    vo.setUserExist(true);
                } else {
                    vo.setUserExist(false);
                }
            } else {
                vo.setUserExist(false);
            }
            voList.add(vo);
        }
        voPage.setRecords(voList);
        return R.success(voPage);
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
