package com.greennote.system.catalog.controller;

import com.greennote.common.api.ApiResponse;
import com.greennote.system.catalog.controller.vo.DepartmentRow;
import com.greennote.system.catalog.controller.vo.DictRow;
import com.greennote.system.catalog.controller.vo.LogRow;
import com.greennote.system.catalog.service.CatalogService;
import com.greennote.system.file.controller.vo.StoredFile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AdminCatalogController {

    private final CatalogService catalogService;

    public AdminCatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/api/admin/departments")
    public ApiResponse<List<DepartmentRow>> departments() {
        return ApiResponse.ok(catalogService.departments());
    }

    @GetMapping("/api/admin/dictionaries")
    public ApiResponse<List<DictRow>> dictionaries() {
        return ApiResponse.ok(catalogService.dictionaries());
    }

    @GetMapping("/api/admin/files")
    public ApiResponse<List<StoredFile>> files() {
        return ApiResponse.ok(catalogService.files());
    }

    @GetMapping("/api/admin/logs")
    public ApiResponse<List<LogRow>> logs() {
        return ApiResponse.ok(catalogService.logs());
    }
}
