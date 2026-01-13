package cc.nanoic.yuna.user.service;

import cc.nanoic.yuna.user.entity.User;
import cc.nanoic.yuna.user.model.dto.UserChangeEmailDTO;
import cc.nanoic.yuna.user.model.dto.UserChangePasswordDTO;
import cc.nanoic.yuna.user.model.dto.UserCodeLoginDTO;
import cc.nanoic.yuna.user.model.dto.UserLoginDTO;
import cc.nanoic.yuna.user.model.dto.UserRegisterDTO;
import cc.nanoic.yuna.user.model.dto.UserDetailDTO;
import cc.nanoic.yuna.user.model.dto.UserUpdateDTO;
import cc.nanoic.yuna.user.model.vo.PermissionVO;
import cc.nanoic.yuna.user.model.vo.RoleVO;
import cc.nanoic.yuna.user.model.vo.UserLoginVO;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;

public interface UserService extends IService<User> {
    /**
     * 用户注册
     * 
     * @param registerDTO 注册信息
     * @return 注册成功的用户ID
     */
    Long register(UserRegisterDTO registerDTO);

    /**
     * 账号密码登录
     * 
     * @param loginDTO 登录信息
     * @return 登录结果
     */
    UserLoginVO login(UserLoginDTO loginDTO);

    /**
     * 邮箱验证码登录
     * 
     * @param loginDTO 登录信息
     * @return 登录结果
     */
    UserLoginVO loginByEmail(UserCodeLoginDTO loginDTO);

    /**
     * 检查邮箱是否已注册
     * 
     * @param email 邮箱
     * @return true: 已注册, false: 未注册
     */
    boolean checkEmail(String email);

    /**
     * 刷新访问令牌
     * 
     * @param refreshToken 刷新令牌
     * @return 刷新后的登录结果
     */
    UserLoginVO refreshAccessToken(String refreshToken);

    /**
     * 更新用户信息
     * 
     * @param userId    用户ID
     * @param updateDTO 更新信息
     */
    void updateUserInfo(Long userId, UserUpdateDTO updateDTO);

    /**
     * 获取用户详情
     * 
     * @param userId 用户ID
     * @return 用户详情
     */
    UserDetailDTO getUserDetail(Long userId);

    /**
     * 根据UUID获取用户ID
     * 
     * @param uuid 用户UUID
     * @return 用户ID
     */
    Long getUserIdByUuid(String uuid);

    /**
     * 修改密码
     * @param userId 用户ID
     * @param changePasswordDTO 密码修改信息
     */
    void changePassword(Long userId, UserChangePasswordDTO changePasswordDTO);

    /**
     * 修改邮箱
     * @param userId 用户ID
     * @param changeEmailDTO 邮箱修改信息
     */
    void changeEmail(Long userId, UserChangeEmailDTO changeEmailDTO);

    /**
     * 分配角色
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     */
    void assignRoles(Long userId, List<Long> roleIds);

    /**
     * 获取用户角色
     * @param userId 用户ID
     * @return 角色列表
     */
    List<RoleVO> getUserRoles(Long userId);

    /**
     * 获取用户权限
     * @param userId 用户ID
     * @return 权限列表
     */
    List<PermissionVO> getUserPermissions(Long userId);

    /**
     * 分配权限（特殊权限）
     * @param userId 用户ID
     * @param permissionIds 权限ID列表
     */
    void assignPermissions(Long userId, List<Long> permissionIds);

    /**
     * 更新用户账号状态
     * @param userId 用户ID
     * @param status 状态(0:注销,1:正常,2:封禁)
     */
    void updateStatus(Long userId, Integer status);

    /**
     * 判断用户是否为超级管理员
     * @param userId 用户ID
     * @return 是否为超级管理员
     */
    boolean isSuperAdmin(Long userId);
}
