package com.greennote.system.catalog.controller.vo;

public record LogRow(Long id, String operatorName, String action, String detail, String createdAt) {
}