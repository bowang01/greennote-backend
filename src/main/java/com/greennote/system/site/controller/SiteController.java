package com.greennote.system.site.controller;

import com.greennote.common.api.ApiResponse;
import com.greennote.security.SecurityUtils;
import com.greennote.system.site.controller.vo.SiteConfig;
import com.greennote.system.site.service.SiteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SiteController {

    private final SiteService siteService;

    public SiteController(SiteService siteService) {
        this.siteService = siteService;
    }

    @GetMapping("/api/site")
    public ApiResponse<SiteConfig> site() {
        return ApiResponse.ok(siteService.current());
    }

    @PutMapping("/api/admin/site")
    public ApiResponse<SiteConfig> update(@RequestBody SiteConfig request) {
        return ApiResponse.ok(siteService.update(request, SecurityUtils.currentUser()));
    }
}
