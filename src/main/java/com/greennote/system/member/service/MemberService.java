package com.greennote.system.member.service;

import com.greennote.auth.controller.vo.LoginResponse;
import com.greennote.common.api.PageResult;
import com.greennote.security.AuthPrincipal;
import com.greennote.system.member.controller.vo.MemberRow;
import com.greennote.system.member.controller.vo.ProfileResponse;
import com.greennote.system.member.controller.vo.ProfileUpdateRequest;
import com.greennote.system.member.controller.vo.RegisterRequest;

public interface MemberService {

    void register(RegisterRequest request);

    LoginResponse login(String username, String password);

    ProfileResponse profile(long userId);

    ProfileResponse update(long userId, ProfileUpdateRequest request);

    PageResult<MemberRow> page(String keyword, int page, int size);

    void changeStatus(long id, int status, AuthPrincipal operator);
}
