package com.greennote.system.file.controller.vo;

public record StoredFile(Long id, String name, String url, Long size, String contentType) {
}
