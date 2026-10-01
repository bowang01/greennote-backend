package com.greennote.system.site.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ConfigMapper {

    String findValue(String configKey);

    int updateValue(@Param("configKey") String configKey, @Param("configValue") String configValue);

    int insert(@Param("id") String id, @Param("configKey") String configKey, @Param("configValue") String configValue);
}
