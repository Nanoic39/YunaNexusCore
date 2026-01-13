package cc.nanoic.yuna.user.service.impl;

import cc.nanoic.yuna.common.security.service.SecurityPermissionService;
import cc.nanoic.yuna.user.model.vo.PermissionVO;
import cc.nanoic.yuna.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthPermissionServiceImpl implements SecurityPermissionService {

    private final UserService userService;

    @Override
    public Set<String> getPermissions(Long userId) {
        List<PermissionVO> userPermissions = userService.getUserPermissions(userId);
        return userPermissions.stream()
                .map(PermissionVO::getPermCode)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isUserBanned(Long userId) {
        var user = userService.getById(userId);
        if (user == null) {
            return false;
        }
        return Integer.valueOf(2).equals(user.getStatus());
    }
}
