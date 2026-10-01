package com.greennote.system.site.service;

import com.greennote.common.exception.BusinessException;
import com.greennote.security.AuthPrincipal;
import com.greennote.system.log.service.OperateLogService;
import com.greennote.system.site.controller.vo.SiteConfig;
import com.greennote.system.site.mapper.ConfigMapper;
import org.springframework.stereotype.Service;

@Service
public class SiteServiceImpl implements SiteService {

    private final ConfigMapper configMapper;
    private final OperateLogService operateLogService;

    public SiteServiceImpl(ConfigMapper configMapper, OperateLogService operateLogService) {
        this.configMapper = configMapper;
        this.operateLogService = operateLogService;
    }

    @Override
    public SiteConfig current() {
        return new SiteConfig(value("site.name", "GreenNote"), value("site.logo", ""), value("site.theme", "#1b6b45"));
    }

    @Override
    public SiteConfig update(SiteConfig request, AuthPrincipal operator) {
        if (request.name() == null || request.name().isBlank()) {
            throw new BusinessException(400, "Site name is required");
        }
        if (request.themeColor() == null || !request.themeColor().matches("#[0-9a-fA-F]{6}")) {
            throw new BusinessException(400, "Theme color must be a hex color");
        }
        save("site.name", request.name().trim());
        save("site.logo", request.logo() == null ? "" : request.logo().trim());
        save("site.theme", request.themeColor().toLowerCase());
        operateLogService.record(operator.userId(), operator.username(), "site.update", request.name().trim());
        return current();
    }

    private String value(String key, String fallback) {
        String stored = configMapper.findValue(key);
        return stored == null ? fallback : stored;
    }

    private void save(String key, String value) {
        int updated = configMapper.updateValue(key, value);
        if (updated == 0) {
            configMapper.insert(key, value);
        }
    }
}
