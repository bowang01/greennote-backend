package com.greennote.system.member.controller;

import com.greennote.auth.controller.vo.LoginRequest;
import com.greennote.auth.controller.vo.LoginResponse;
import com.greennote.common.api.ApiResponse;
import com.greennote.common.api.PageResult;
import com.greennote.security.SecurityUtils;
import com.greennote.system.member.controller.vo.MemberRow;
import com.greennote.system.member.controller.vo.ProfileResponse;
import com.greennote.system.member.controller.vo.ProfileUpdateRequest;
import com.greennote.system.member.controller.vo.RegisterRequest;
import com.greennote.system.member.controller.vo.StatusRequest;
import com.greennote.system.member.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/api/member/auth/register")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        memberService.register(request);
        return ApiResponse.ok(null);
    }

    @PostMapping("/api/member/auth/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(memberService.login(request.username(), request.password()));
    }

    @GetMapping("/api/member/profile")
    public ApiResponse<ProfileResponse> profile() {
        return ApiResponse.ok(memberService.profile(SecurityUtils.currentUser().userId()));
    }

    @PutMapping("/api/member/profile")
    public ApiResponse<ProfileResponse> updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        return ApiResponse.ok(memberService.update(SecurityUtils.currentUser().userId(), request));
    }

    @GetMapping("/api/admin/members")
    public ApiResponse<PageResult<MemberRow>> members(@RequestParam(defaultValue = "") String keyword,
                                                      @RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(memberService.page(keyword, page, size));
    }

    @PatchMapping("/api/admin/members/{id}/status")
    public ApiResponse<Void> changeStatus(@PathVariable long id, @Valid @RequestBody StatusRequest request) {
        memberService.changeStatus(id, request.status(), SecurityUtils.currentUser());
        return ApiResponse.ok(null);
    }
}
