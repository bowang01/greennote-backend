package com.greennote.system.file.controller;

import com.greennote.common.api.ApiResponse;
import com.greennote.security.SecurityUtils;
import com.greennote.system.file.controller.vo.StoredFile;
import com.greennote.system.file.service.FileService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/api/admin/files")
    public ApiResponse<StoredFile> uploadForAdmin(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(fileService.store(file, SecurityUtils.currentUser()));
    }

    @PostMapping("/api/member/files")
    public ApiResponse<StoredFile> uploadForMember(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(fileService.store(file, SecurityUtils.currentUser()));
    }
}
