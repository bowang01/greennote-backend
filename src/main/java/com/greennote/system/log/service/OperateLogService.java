package com.greennote.system.log.service;

public interface OperateLogService {

    void record(String operatorId, String operatorName, String action, String detail);
}
