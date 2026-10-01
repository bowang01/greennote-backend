package com.greennote.system.member.mapper;

import com.greennote.system.member.MemberCredential;
import com.greennote.system.member.controller.vo.MemberRow;
import com.greennote.system.member.MemberProfileRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MemberMapper {

    long countByUsername(String username);

    int insert(@Param("username") String username,
               @Param("password") String password,
               @Param("nickname") String nickname);

    MemberCredential findByUsername(String username);

    MemberProfileRow findById(long id);

    int updateProfile(@Param("id") long id,
                      @Param("nickname") String nickname,
                      @Param("bio") String bio,
                      @Param("avatar") String avatar);

    long countByKeyword(String keyword);

    List<MemberRow> pageByKeyword(@Param("keyword") String keyword,
                                  @Param("limit") int limit,
                                  @Param("offset") int offset);

    int updateStatus(@Param("id") long id, @Param("status") int status);
}
