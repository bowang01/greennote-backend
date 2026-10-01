package com.greennote.system.topic.mapper;

import com.greennote.system.topic.controller.vo.TopicResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TopicMapper {

    List<TopicResponse> list(@Param("status") Integer status);

    TopicResponse findById(String id);

    int countByName(@Param("name") String name, @Param("excludeId") String excludeId);

    int insert(@Param("id") String id, @Param("name") String name, @Param("intro") String intro, @Param("sortNo") int sortNo);

    int update(@Param("id") String id, @Param("name") String name, @Param("intro") String intro, @Param("sortNo") int sortNo);

    int updateStatus(@Param("id") String id, @Param("status") int status);
}
