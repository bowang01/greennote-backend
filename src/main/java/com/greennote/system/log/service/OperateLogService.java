package com.greennote.system.log.service;

public interface OperateLogService {

    void record(Long operatorId, String operatorName, String action, String detail);
}
