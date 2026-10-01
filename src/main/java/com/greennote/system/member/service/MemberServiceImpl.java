package com.greennote.system.member.service;

import com.greennote.auth.controller.vo.LoginResponse;
import com.greennote.common.api.PageResult;
import com.greennote.common.exception.BusinessException;
import com.greennote.security.AuthPrincipal;
import com.greennote.security.JwtService;
import com.greennote.system.log.service.OperateLogService;
import com.greennote.system.member.MemberCredential;
import com.greennote.system.member.controller.vo.MemberRow;
import com.greennote.system.member.controller.vo.ProfileResponse;
import com.greennote.system.member.controller.vo.ProfileUpdateRequest;
import com.greennote.system.member.controller.vo.RegisterRequest;
import com.greennote.system.member.MemberProfileRow;
import com.greennote.system.member.mapper.MemberMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberServiceImpl implements MemberService {

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OperateLogService operateLogService;

    public MemberServiceImpl(MemberMapper memberMapper,
                             PasswordEncoder passwordEncoder,
                             JwtService jwtService,
                             OperateLogService operateLogService) {
        this.memberMapper = memberMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.operateLogService = operateLogService;
    }

    @Override
    public void register(RegisterRequest request) {
        if (memberMapper.countByUsername(request.username()) > 0) {
            throw new BusinessException(400, "Username already exists");
        }
        memberMapper.insert(request.username(), passwordEncoder.encode(request.password()), request.nickname().trim());
        operateLogService.record(null, request.username(), "member.register", "Member registered");
    }

    @Override
    public LoginResponse login(String username, String password) {
        MemberCredential account = memberMapper.findByUsername(username);
        if (account == null || !passwordEncoder.matches(password, account.password())) {
            throw new BusinessException(401, "Incorrect username or password");
        }
        if (account.status() != 0) {
            throw new BusinessException(401, "Account is disabled");
        }
        operateLogService.record(account.id(), account.username(), "member.login", "Member signed in");
        return new LoginResponse(
                account.id(),
                account.username(),
                jwtService.createToken(account.id(), account.username(), "member"),
                jwtService.expiresAt()
        );
    }

    @Override
    public ProfileResponse profile(long userId) {
        return toProfile(requireProfile(userId));
    }

    @Override
    public ProfileResponse update(long userId, ProfileUpdateRequest request) {
        String bio = request.bio() == null ? "" : request.bio();
        String avatar = request.avatar() == null ? "" : request.avatar();
        int updated = memberMapper.updateProfile(userId, request.nickname().trim(), bio, avatar);
        if (updated == 0) {
            throw new BusinessException(404, "Member not found");
        }
        return profile(userId);
    }

    @Override
    public PageResult<MemberRow> page(String keyword, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 50);
        String text = keyword == null ? "" : keyword.trim();
        long total = memberMapper.countByKeyword(text);
        List<MemberRow> list = memberMapper.pageByKeyword(text, safeSize, (safePage - 1) * safeSize);
        return new PageResult<>(list, total);
    }

    @Override
    public void changeStatus(long id, int status, AuthPrincipal operator) {
        int updated = memberMapper.updateStatus(id, status);
        if (updated == 0) {
            throw new BusinessException(404, "Member not found");
        }
        operateLogService.record(operator.userId(), operator.username(), "member.status", "Member " + id + " status " + status);
    }

    private MemberProfileRow requireProfile(long userId) {
        MemberProfileRow row = memberMapper.findById(userId);
        if (row == null) {
            throw new BusinessException(404, "Member not found");
        }
        return row;
    }

    private static ProfileResponse toProfile(MemberProfileRow row) {
        return new ProfileResponse(row.id(), row.username(), row.nickname(), row.avatar(), row.bio(), row.status());
    }
}
