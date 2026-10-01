package com.greennote.system.note.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface NoteLikeMapper {

    int insert(@Param("id") String id, @Param("userId") String userId, @Param("noteId") String noteId);

    int delete(@Param("userId") String userId, @Param("noteId") String noteId);

    int exists(@Param("userId") String userId, @Param("noteId") String noteId);
}
