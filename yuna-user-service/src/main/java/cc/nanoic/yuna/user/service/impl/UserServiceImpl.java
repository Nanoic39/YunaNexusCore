package cc.nanoic.yuna.user.service.impl;

import cc.nanoic.yuna.common.core.exception.BusinessException;
import cc.nanoic.yuna.common.core.result.ResultCode;
import cc.nanoic.yuna.user.entity.User;
import cc.nanoic.yuna.user.entity.UserInfo;
import cc.nanoic.yuna.user.mapper.UserInfoMapper;
import cc.nanoic.yuna.user.mapper.UserMapper;
import cc.nanoic.yuna.user.model.dto.UserRegisterDTO;
import cc.nanoic.yuna.user.service.AuthService;
import cc.nanoic.yuna.user.service.UserService;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    // 导入
    private final UserInfoMapper userInfoMapper;
    private final AuthService authService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(UserRegisterDTO registerDTO) {
        // 校验验证码
        if (!authService.verifyCode(registerDTO.getEmail(), registerDTO.getVerifyCode())) {
            throw new BusinessException(ResultCode.FAILURE, "验证码错误或已失效");
        }

        // 校验用户名是否已存在
        if (count(new LambdaQueryWrapper<User>().eq(User::getUsername, registerDTO.getUsername())) > 0) {
            throw new BusinessException(ResultCode.USER_EXIST, "该用户名已被注册");
        }
        
        // 校验邮箱是否已存在
        if (count(new LambdaQueryWrapper<User>().eq(User::getEmail, registerDTO.getEmail())) > 0) {
            throw new BusinessException(ResultCode.USER_EXIST, "该邮箱已被注册");
        }

        // 2. 创建User对象
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        // 密码加密
        user.setPassword(BCrypt.hashpw(registerDTO.getPassword()));
        user.setEmail(registerDTO.getEmail());
        user.setUuid(UUID.fastUUID().toString(true));
        user.setStatus(1); // 正常
        user.setCreateTime(LocalDateTime.now());
        
        baseMapper.insert(user);

        // 3. 创建UserInfo对象
        UserInfo userInfo = new UserInfo();
        userInfo.setUser_id(user.getId());
        userInfo.setNickname(StrUtil.isBlank(registerDTO.getNickname()) ? "Yuna#default" : registerDTO.getNickname());
        userInfo.setGender(0); // 未知
        userInfo.setExperience(0); // 注册时默认零，注册成功后可以手动获得注册奖励
        userInfo.setUpdateTime(LocalDateTime.now());
        
        userInfoMapper.insert(userInfo);

        return user.getId();
    }
    
}
