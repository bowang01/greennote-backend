package com.greennote.system.topic.controller;

import com.greennote.common.api.ApiResponse;
import com.greennote.system.topic.controller.vo.StatusRequest;
import com.greennote.system.topic.controller.vo.TopicResponse;
import com.greennote.system.topic.controller.vo.TopicSaveRequest;
import com.greennote.system.topic.service.TopicService;
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
public class TopicController {

    private final TopicService topicService;

    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    @GetMapping("/api/topics")
    public ApiResponse<List<TopicResponse>> enabled() {
        return ApiResponse.ok(topicService.listEnabled());
    }

    @GetMapping("/api/admin/topics")
    public ApiResponse<List<TopicResponse>> all() {
        return ApiResponse.ok(topicService.listAll());
    }

    @PostMapping("/api/admin/topics")
    public ApiResponse<Void> create(@Valid @RequestBody TopicSaveRequest request) {
        topicService.create(request);
        return ApiResponse.ok(null);
    }

    @PutMapping("/api/admin/topics/{id}")
    public ApiResponse<Void> update(@PathVariable String id, @Valid @RequestBody TopicSaveRequest request) {
        topicService.update(id, request);
        return ApiResponse.ok(null);
    }

    @PatchMapping("/api/admin/topics/{id}/status")
    public ApiResponse<Void> changeStatus(@PathVariable String id, @Valid @RequestBody StatusRequest request) {
        topicService.changeStatus(id, request.status());
        return ApiResponse.ok(null);
    }
}
