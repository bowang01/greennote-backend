package com.greennote.auth.mapper;

import com.greennote.auth.MenuRow;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MenuMapper {

    List<MenuRow> findByAdminUserId(String adminUserId);
}
