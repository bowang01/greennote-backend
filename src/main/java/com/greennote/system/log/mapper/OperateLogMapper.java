package com.greennote.system.log.mapper;

import com.greennote.system.log.OperateLogRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OperateLogMapper {

    int insert(@Param("id") String id,
               @Param("operatorId") String operatorId,
               @Param("operatorName") String operatorName,
               @Param("action") String action,
               @Param("detail") String detail);

    List<OperateLogRecord> listRecent(int limit);
}
