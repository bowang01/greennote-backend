package com.greennote.system.log.service;

import com.greennote.common.Ids;
import com.greennote.system.log.mapper.OperateLogMapper;
import org.springframework.stereotype.Service;

@Service
public class OperateLogServiceImpl implements OperateLogService {

    private final OperateLogMapper operateLogMapper;

    public OperateLogServiceImpl(OperateLogMapper operateLogMapper) {
        this.operateLogMapper = operateLogMapper;
    }

    @Override
    public void record(String operatorId, String operatorName, String action, String detail) {
        operateLogMapper.insert(Ids.newId(), operatorId, operatorName, action, detail == null ? "" : detail);
    }
}
