package com.greennote.auth.service;

import com.greennote.auth.controller.vo.CurrentUserResponse;
import com.greennote.auth.controller.vo.LoginRequest;
import com.greennote.auth.controller.vo.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    CurrentUserResponse current();
}
