package com.greennote.system.channel.controller;

import com.greennote.common.api.ApiResponse;
import com.greennote.system.channel.controller.vo.ChannelResponse;
import com.greennote.system.channel.controller.vo.ChannelSaveRequest;
import com.greennote.system.channel.controller.vo.StatusRequest;
import com.greennote.system.channel.service.ChannelService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ChannelController {

    private final ChannelService channelService;

    public ChannelController(ChannelService channelService) {
        this.channelService = channelService;
    }

    @GetMapping("/api/channels")
    public ApiResponse<List<ChannelResponse>> enabled() {
        return ApiResponse.ok(channelService.listEnabled());
    }

    @GetMapping("/api/admin/channels")
    public ApiResponse<List<ChannelResponse>> all() {
        return ApiResponse.ok(channelService.listAll());
    }

    @PostMapping("/api/admin/channels")
    public ApiResponse<Void> create(@Valid @RequestBody ChannelSaveRequest request) {
        channelService.create(request);
        return ApiResponse.ok(null);
    }

    @PutMapping("/api/admin/channels/{id}")
    public ApiResponse<Void> update(@PathVariable String id, @Valid @RequestBody ChannelSaveRequest request) {
        channelService.update(id, request);
        return ApiResponse.ok(null);
    }

    @PatchMapping("/api/admin/channels/{id}/status")
    public ApiResponse<Void> changeStatus(@PathVariable String id, @Valid @RequestBody StatusRequest request) {
        channelService.changeStatus(id, request.status());
        return ApiResponse.ok(null);
    }
}
