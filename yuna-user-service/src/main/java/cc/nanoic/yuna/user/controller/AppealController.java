package cc.nanoic.yuna.user.controller;

import cc.nanoic.yuna.common.core.constant.SecurityConstants;
import cc.nanoic.yuna.common.core.result.R;
import cc.nanoic.yuna.user.model.dto.AppealSubmitDTO;
import cc.nanoic.yuna.user.entity.Appeal;
import cc.nanoic.yuna.user.mapper.AppealMapper;
import cc.nanoic.yuna.user.mapper.UserMapper;
import cc.nanoic.yuna.user.model.dto.UserDetailDTO;
import cc.nanoic.yuna.user.entity.User;
import cc.nanoic.yuna.user.service.AppealService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.List;

@RestController
@RequestMapping("/appeal")
@RequiredArgsConstructor
public class AppealController {

    private final AppealService appealService;
    private final AppealMapper appealMapper;
    private final UserMapper userMapper;

    @PostMapping
    public R<Void> submit(@RequestHeader(value = SecurityConstants.DETAILS_USER_ID, required = false) Long userId,
            @RequestBody AppealSubmitDTO dto) {
        appealService.submit(userId, dto.getContact(), dto.getReason());
        return R.success(null, "申诉提交成功，我们将尽快处理");
    }

    @GetMapping("/query")
    public R<List<cc.nanoic.yuna.user.model.vo.AppealQueryVO>> query(
            @RequestParam(value = "contact", required = false) String contact,
            @RequestParam(value = "account", required = false) String account) {
        Long userId = null;
        UserDetailDTO matchedUser = null;
        if (account != null && !account.isEmpty()) {
            matchedUser = account.contains("@")
                    ? userMapper.selectUserDetailByEmail(account)
                    : userMapper.selectUserDetailByUsername(account);
            if (matchedUser != null) {
                userId = matchedUser.getId();
            }
        }
        LambdaQueryWrapper<Appeal> qw = new LambdaQueryWrapper<Appeal>()
                .eq(contact != null && !contact.isEmpty(), Appeal::getContact, contact)
                .eq(userId != null, Appeal::getUserId, userId)
                .orderByDesc(Appeal::getCreateTime);
        List<Appeal> list = appealMapper.selectList(qw);
        List<cc.nanoic.yuna.user.model.vo.AppealQueryVO> voList = new java.util.ArrayList<>();
        for (Appeal a : list) {
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
                User userEntity = userMapper.selectById(a.getUserId());
                if (userEntity != null) {
                    vo.setUuid(userEntity.getUuid());
                    vo.setUserExist(true);
                } else {
                    vo.setUserExist(false);
                }
            } else if (matchedUser != null) {
                vo.setUuid(matchedUser.getUuid());
                vo.setUserExist(true);
            } else {
                vo.setUserExist(false);
            }
            voList.add(vo);
        }
        return R.success(voList);
    }
}
