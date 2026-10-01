package com.greennote.system.channel.mapper;

import com.greennote.system.channel.controller.vo.ChannelResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChannelMapper {

    List<ChannelResponse> list(@Param("status") Integer status);

    ChannelResponse findById(String id);

    int countByName(@Param("name") String name, @Param("excludeId") String excludeId);

    int insert(@Param("id") String id, @Param("name") String name, @Param("sortNo") int sortNo);

    int update(@Param("id") String id, @Param("name") String name, @Param("sortNo") int sortNo);

    int updateStatus(@Param("id") String id, @Param("status") int status);
}
