package cc.nanoic.yuna.user.service;

import cc.nanoic.yuna.user.entity.Permission;
import cc.nanoic.yuna.user.model.vo.PermissionVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface PermissionService extends IService<Permission> {

    /**
     * 获取权限树
     * @return 权限树列表
     */
    List<PermissionVO> getPermissionTree();
}
