package com.greennote.system.file.service;

import com.greennote.security.AuthPrincipal;
import com.greennote.system.file.controller.vo.StoredFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface FileService {

    Path root();

    StoredFile store(MultipartFile file, AuthPrincipal uploader);
}
