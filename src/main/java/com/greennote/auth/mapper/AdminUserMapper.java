package com.greennote.auth.mapper;

import com.greennote.auth.AdminAccount;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminUserMapper {

    AdminAccount findByUsername(String username);

    String findNicknameById(String id);
}
