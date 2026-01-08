package cc.nanoic.yuna.user.service.impl;

import cc.nanoic.yuna.common.core.exception.BusinessException;
import cc.nanoic.yuna.common.security.utils.JwtUtil;
import cc.nanoic.yuna.common.core.result.ResultCode;
import cc.nanoic.yuna.user.entity.User;
import cc.nanoic.yuna.user.entity.UserInfo;
import cc.nanoic.yuna.user.mapper.UserInfoMapper;
import cc.nanoic.yuna.user.mapper.UserMapper;
import cc.nanoic.yuna.user.model.dto.UserChangeEmailDTO;
import cc.nanoic.yuna.user.model.dto.UserChangePasswordDTO;
import cc.nanoic.yuna.user.model.dto.UserCodeLoginDTO;
import cc.nanoic.yuna.user.model.dto.UserDetailDTO;
import cc.nanoic.yuna.user.model.dto.UserLoginDTO;
import cc.nanoic.yuna.user.model.dto.UserRegisterDTO;
import cc.nanoic.yuna.user.model.dto.UserUpdateDTO;
import cc.nanoic.yuna.user.model.vo.UserInfoVO;
import cc.nanoic.yuna.user.model.vo.UserLoginVO;
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
    private final JwtUtil jwtUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(UserRegisterDTO registerDTO) {
        // 校验验证码
        if (!authService.verifyCode(registerDTO.getEmail(), registerDTO.getVerifyCode())) {
            throw new BusinessException(ResultCode.FAILURE, "验证码错误或已失效");
        }

        return createUser(registerDTO);
    }

    @Override
    public UserLoginVO login(UserLoginDTO loginDTO) {
        String account = loginDTO.getAccount();
        UserDetailDTO user;

        // 根据账号格式判断是邮箱还是用户名
        if (account.contains("@") && account.contains(".")) {
            user = baseMapper.selectUserDetailByEmail(account);
        } else {
            user = baseMapper.selectUserDetailByUsername(account);
        }

        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 验证密码
        if (!BCrypt.checkpw(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.FAILURE, "密码错误");
        }

        // 检查状态
        if (user.getStatus() != 1) {
            throw new BusinessException(ResultCode.FAILURE, "账号已被禁用或注销");
        }

        return buildUserLoginVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserLoginVO loginByEmail(UserCodeLoginDTO loginDTO) {
        String email = loginDTO.getEmail();
        // 查询用户
        UserDetailDTO user = baseMapper.selectUserDetailByEmail(email);

        if (user == null) {
            // 未注册，检查是否携带了用户名和密码
            if (StrUtil.isBlank(loginDTO.getUsername()) || StrUtil.isBlank(loginDTO.getPassword())) {
                throw new BusinessException(ResultCode.USER_NOT_EXIST, "该邮箱尚未注册，请提供用户名和密码进行注册");
            }

            // 校验验证码 (复用逻辑)
            if (!authService.verifyCode(email, loginDTO.getCode())) {
                throw new BusinessException(ResultCode.FAILURE, "验证码错误或已失效");
            }

            // 执行注册逻辑
            UserRegisterDTO registerDTO = new UserRegisterDTO();
            registerDTO.setEmail(email);
            registerDTO.setUsername(loginDTO.getUsername());
            registerDTO.setPassword(loginDTO.getPassword());

            // 注册用户 (内部包含用户名校验、重复校验、创建User和UserInfo)
            createUser(registerDTO);

            // 重新查询用户以获取完整信息
            user = baseMapper.selectUserDetailByEmail(email);

        } else {
            // 已注册，校验验证码
            if (!authService.verifyCode(email, loginDTO.getCode())) {
                throw new BusinessException(ResultCode.FAILURE, "验证码错误或已失效");
            }

            // 检查状态
            if (user.getStatus() != 1) {
                throw new BusinessException(ResultCode.FAILURE, "账号已被禁用或注销");
            }
        }

        return buildUserLoginVO(user);
    }

    /**
     * 刷新访问令牌
     * 
     * @param refreshToken 刷新令牌
     * @return 新的登录信息
     */
    @Override
    public UserLoginVO refreshAccessToken(String refreshToken) {
        // 验证 RefreshToken 是否有效且类型正确
        if (!jwtUtil.validateToken(refreshToken, null, "refresh")) {
            throw new BusinessException(ResultCode.UN_AUTHORIZED, "登录状态无效或已过期, 请重新登录");
        }

        // 获取用户ID
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        if (userId == null) {
            throw new BusinessException(ResultCode.UN_AUTHORIZED, "登录状态无效");
        }

        // 查询用户信息 (确保用户还存在且未被禁用)
        User user = baseMapper.selectById(userId);
        if (user == null || user.getStatus() != 1) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST, "用户不存在或已被禁用");
        }

        // 查询用户详细信息 (包含UserInfo)
        UserDetailDTO userDetail = baseMapper.selectUserDetailByUsername(user.getUsername());
        return buildUserLoginVO(userDetail);
    }

    /**
     * 更新用户信息
     * 
     * @param userId 用户ID
     * @param updateDTO 更新信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserInfo(Long userId, UserUpdateDTO updateDTO) {
        // 获取 UserInfo
        UserInfo userInfo = userInfoMapper.selectById(userId);
        // 获取用户信息失败说明出现异常
        if (userInfo == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST, "用户不存在或已被禁用");
        }

        boolean changed = false;
        if (updateDTO.getNickname() != null) {
            if (StrUtil.isNotBlank(updateDTO.getNickname())) {
                userInfo.setNickname(updateDTO.getNickname());
            } else {
                userInfo.setNickname("Yuna#default");
            }
            changed = true;
        }
        if (updateDTO.getGender() != null) {
            userInfo.setGender(updateDTO.getGender());
            changed = true;
        }
        if (updateDTO.getBiography() != null) { // 允许清空简介
            userInfo.setBiography(updateDTO.getBiography());
            changed = true;
        }
        if(updateDTO.getAvatar() != null) {
            userInfo.setAvatarId(updateDTO.getAvatar());
            changed = true;
        }
        if (updateDTO.getBirthday() != null) {
            userInfo.setBirthday(updateDTO.getBirthday());
            changed = true;
        }

        if (changed) {
            userInfo.setUpdateTime(LocalDateTime.now());
            userInfoMapper.updateById(userInfo);
        } else {
            throw new BusinessException(ResultCode.FAILURE, "未更新任何信息");
        }
    }

    /**
     * 获取用户详细信息
     * 
     * @param userId 用户ID
     * @return 用户详细信息
     */
    @Override
    public UserDetailDTO getUserDetail(Long userId) {
        User user = baseMapper.selectById(userId);
        if (user == null || user.getStatus() != 1) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST, "用户不存在或已被禁用");
        }
        return baseMapper.selectUserDetailByUsername(user.getUsername());
    }

    /**
     * 根据UUID获取用户ID
     * 
     * @param uuid 用户UUID
     * @return 用户ID
     */
    @Override
    public Long getUserIdByUuid(String uuid) {
        User user = baseMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUuid, uuid));
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST, "用户不存在");
        }
        return user.getId();
    }

    /**
     * 修改密码
     * 
     * @param userId 用户ID
     * @param changePasswordDTO 密码修改信息
     */
    @Override
    public void changePassword(Long userId, UserChangePasswordDTO changePasswordDTO) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        if (!BCrypt.checkpw(changePasswordDTO.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        user.setPassword(BCrypt.hashpw(changePasswordDTO.getNewPassword()));
        updateById(user);
    }

    /**
     * 修改邮箱
     * 
     * @param userId 用户ID
     * @param changeEmailDTO 邮箱修改信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeEmail(Long userId, UserChangeEmailDTO changeEmailDTO) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 如果原邮箱存在，验证原邮箱
        if (StrUtil.isNotBlank(user.getEmail())) {
            if (StrUtil.isBlank(changeEmailDTO.getOldEmailCode())) {
                throw new BusinessException("请验证原邮箱");
            }
            if (!authService.verifyCode(user.getEmail(), changeEmailDTO.getOldEmailCode())) {
                throw new BusinessException("原邮箱验证码错误");
            }
        }

        // 验证新邮箱
        String newEmail = changeEmailDTO.getNewEmail();
        if (StrUtil.isBlank(newEmail)) {
            throw new BusinessException("新邮箱不能为空");
        }
        if (checkEmail(newEmail)) {
            throw new BusinessException("该邮箱已被占用");
        }
        if (!authService.verifyCode(newEmail, changeEmailDTO.getNewEmailCode())) {
            throw new BusinessException("新邮箱验证码错误");
        }

        user.setEmail(newEmail);
        updateById(user);
    }

    // ========== 方法 ========== //

    /**
     * 检查邮箱是否已注册
     * 
     * @param email 邮箱
     * @return 是否已注册(true/false)
     */
    @Override
    public boolean checkEmail(String email) {
        return count(new LambdaQueryWrapper<User>().eq(User::getEmail, email)) > 0;
    }

    /**
     * 创建用户/用户注册的逻辑封装
     * 
     * @param registerDTO 注册信息
     * @return 用户ID
     */
    private Long createUser(UserRegisterDTO registerDTO) {
        // 校验用户名格式（不允许包含@或.）
        if (StrUtil.containsAny(registerDTO.getUsername(), "@", ".")) {
            throw new BusinessException(ResultCode.FAILURE, "用户名不能包含特殊字符'@'或'.'");
        }

        // 校验用户名是否已存在
        if (count(new LambdaQueryWrapper<User>().eq(User::getUsername, registerDTO.getUsername())) > 0) {
            throw new BusinessException(ResultCode.USER_EXIST, "该用户名已被注册");
        }

        // 校验邮箱是否已存在
        if (count(new LambdaQueryWrapper<User>().eq(User::getEmail, registerDTO.getEmail())) > 0) {
            throw new BusinessException(ResultCode.USER_EXIST, "该邮箱已被注册");
        }

        // 创建User对象
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        // 密码加密
        user.setPassword(BCrypt.hashpw(registerDTO.getPassword()));
        user.setEmail(registerDTO.getEmail());
        // 使用 Hutool 生成基于时间的 UUID (v1)，带横杠保证长度36位匹配
        user.setUuid(UUID.fastUUID().toString(false));
        user.setStatus(1); // 正常
        user.setCreateTime(LocalDateTime.now());

        baseMapper.insert(user);

        // 创建UserInfo对象
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(user.getId());
        // 未填写昵称时设置为默认格式：Yuna#default_用户名
        userInfo.setNickname(StrUtil.isBlank(registerDTO.getNickname())
                ? ("Yuna#default_" + registerDTO.getUsername())
                : registerDTO.getNickname());
        userInfo.setGender(registerDTO.getGender() != null ? registerDTO.getGender() : 0); // 默认0:未知
        userInfo.setExperience(0); // 注册时默认零
        userInfo.setUpdateTime(LocalDateTime.now());

        userInfoMapper.insert(userInfo);

        return user.getId();
    }

    /**
     * 构建用户登录VO
     * 
     * @param user 用户详情DTO
     * @return 用户登录VO
     */
    private UserLoginVO buildUserLoginVO(UserDetailDTO user) {
        // 生成 AccessToken 和 RefreshToken
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

        // 直接从 DTO 获取 UserInfo
        UserInfo userInfo = user.getUserInfo();

        UserInfoVO userInfoVO;
        // 处理信息以匹配格式
        userInfoVO = UserInfoVO.builder()
                .nickname(userInfo.getNickname())
                .avatar(null) // TODO: 集成文件存储服务，自动拼接URL
                .gender(
                        // 自动转换为字符串
                        switch (userInfo.getGender()) {
                            case 1 -> "男";
                            case 2 -> "女";
                            default -> "未知";
                        })
                .biography(userInfo.getBiography())
                .experience(userInfo.getExperience())
                .build();

        return UserLoginVO.builder()
                .uuid(user.getUuid())
                .username(user.getUsername())
                .email(user.getEmail())
                .token(accessToken)
                .refreshToken(refreshToken)
                .userInfo(userInfoVO)
                .build();
    }

}
