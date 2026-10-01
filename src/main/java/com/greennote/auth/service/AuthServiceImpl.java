package com.greennote.auth.service;

import com.greennote.auth.AdminAccount;
import com.greennote.auth.controller.vo.CurrentUserResponse;
import com.greennote.auth.controller.vo.LoginRequest;
import com.greennote.auth.controller.vo.LoginResponse;
import com.greennote.auth.mapper.AdminUserMapper;
import com.greennote.auth.mapper.MenuMapper;
import com.greennote.common.exception.BusinessException;
import com.greennote.security.JwtService;
import com.greennote.security.SecurityUtils;
import com.greennote.system.log.service.OperateLogService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {

    private final AdminUserMapper adminUserMapper;
    private final MenuMapper menuMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OperateLogService operateLogService;

    public AuthServiceImpl(AdminUserMapper adminUserMapper,
                           MenuMapper menuMapper,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService,
                           OperateLogService operateLogService) {
        this.adminUserMapper = adminUserMapper;
        this.menuMapper = menuMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.operateLogService = operateLogService;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        AdminAccount account = adminUserMapper.findByUsername(request.username());
        if (account == null || !passwordEncoder.matches(request.password(), account.password())) {
            throw new BusinessException(401, "Incorrect username or password");
        }
        if (account.status() != 0) {
            throw new BusinessException(401, "Account is disabled");
        }
        operateLogService.record(account.id(), account.username(), "admin.login", "Admin signed in");
        String accessToken = jwtService.createToken(account.id(), account.username(), "admin");

        return new LoginResponse(account.id(), account.username(), accessToken, jwtService.expiresAt());
    }

    @Override
    public CurrentUserResponse current() {
        var principal = SecurityUtils.currentUser();
        String nickname = adminUserMapper.findNicknameById(principal.userId());
        if (nickname == null) {
            nickname = principal.username();
        }
        List<CurrentUserResponse.MenuItem> menus = menuMapper.findByAdminUserId(principal.userId()).stream()
                .map(row -> new CurrentUserResponse.MenuItem(row.id(), row.name(), row.path()))
                .toList();
        return new CurrentUserResponse(principal.userId(), principal.username(), nickname, menus);
    }
}
