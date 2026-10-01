package com.greennote.system.file.service;

import com.greennote.common.exception.BusinessException;
import com.greennote.security.AuthPrincipal;
import com.greennote.system.file.FileInsert;
import com.greennote.system.file.StorageProperties;
import com.greennote.system.file.controller.vo.StoredFile;
import com.greennote.system.file.mapper.FileMapper;
import com.greennote.system.log.service.OperateLogService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    private final FileMapper fileMapper;
    private final StorageProperties properties;
    private final OperateLogService operateLogService;
    private Path root;

    public FileServiceImpl(FileMapper fileMapper, StorageProperties properties, OperateLogService operateLogService) {
        this.fileMapper = fileMapper;
        this.properties = properties;
        this.operateLogService = operateLogService;
    }

    @PostConstruct
    void init() throws IOException {
        root = Path.of(properties.getLocation()).toAbsolutePath().normalize();
        Files.createDirectories(root);
    }

    @Override
    public Path root() {
        return root;
    }

    @Override
    public StoredFile store(MultipartFile file, AuthPrincipal uploader) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "Choose a file to upload");
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!contentType.startsWith("image/")) {
            throw new BusinessException(400, "Only image files are allowed");
        }
        String extension = extensionOf(file.getOriginalFilename());
        String storedName = UUID.randomUUID() + extension;
        try {
            Files.copy(file.getInputStream(), root.resolve(storedName));
        } catch (IOException ex) {
            throw new BusinessException(500, "Failed to store the file");
        }
        String url = "/uploads/" + storedName;
        String originalName = file.getOriginalFilename() == null ? storedName : file.getOriginalFilename();
        FileInsert row = new FileInsert();
        row.setOriginalName(originalName);
        row.setStoredName(storedName);
        row.setUrl(url);
        row.setSizeBytes(file.getSize());
        row.setContentType(contentType);
        row.setUploaderKind(uploader.kind());
        row.setUploaderId(uploader.userId());
        fileMapper.insert(row);
        operateLogService.record(uploader.userId(), uploader.username(), "file.upload", url);
        return new StoredFile(row.getId(), originalName, url, file.getSize(), contentType);
    }

    private static String extensionOf(String filename) {
        if (filename == null) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return "";
        }
        String extension = filename.substring(dot).toLowerCase(Locale.ROOT);
        if (extension.length() > 8 || !extension.matches("\\.[a-z0-9]+")) {
            return "";
        }
        return extension;
    }
}
