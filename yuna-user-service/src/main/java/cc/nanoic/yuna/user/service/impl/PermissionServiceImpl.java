package cc.nanoic.yuna.user.service.impl;

import cc.nanoic.yuna.user.entity.Permission;
import cc.nanoic.yuna.user.mapper.PermissionMapper;
import cc.nanoic.yuna.user.model.vo.PermissionVO;
import cc.nanoic.yuna.user.service.PermissionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {

    @Override
    public List<PermissionVO> getPermissionTree() {
        List<Permission> all = this.list();
        List<PermissionVO> vos = all.stream().map(this::toVO).collect(Collectors.toList());

        // build tree
        Map<Long, List<PermissionVO>> grouped = vos.stream()
            .filter(v -> v.getParentId() != null)
            .collect(Collectors.groupingBy(PermissionVO::getParentId));

        vos.forEach(v -> v.setChildren(grouped.getOrDefault(v.getId(), new ArrayList<>())));

        return vos.stream().filter(v -> v.getParentId() == 0).collect(Collectors.toList());
    }

    private PermissionVO toVO(Permission p) {
        PermissionVO vo = new PermissionVO();
        BeanUtils.copyProperties(p, vo);
        return vo;
    }
}
