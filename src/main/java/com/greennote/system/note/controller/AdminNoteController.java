package com.greennote.system.note.controller;

import com.greennote.common.api.ApiResponse;
import com.greennote.common.api.PageResult;
import com.greennote.system.note.controller.vo.NoteCard;
import com.greennote.system.note.controller.vo.ReasonRequest;
import com.greennote.system.note.service.NoteService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminNoteController {

    private final NoteService noteService;

    public AdminNoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping("/api/admin/notes")
    public ApiResponse<PageResult<NoteCard>> page(@RequestParam(required = false) Integer status,
                                                  @RequestParam(required = false) String channelId,
                                                  @RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(noteService.adminPage(status, channelId, page, size));
    }

    @PutMapping("/api/admin/notes/{id}/approve")
    public ApiResponse<Void> approve(@PathVariable String id) {
        noteService.approve(id);
        return ApiResponse.ok(null);
    }

    @PutMapping("/api/admin/notes/{id}/reject")
    public ApiResponse<Void> reject(@PathVariable String id, @Valid @RequestBody ReasonRequest request) {
        noteService.reject(id, request.reason());
        return ApiResponse.ok(null);
    }

    @PutMapping("/api/admin/notes/{id}/offline")
    public ApiResponse<Void> offline(@PathVariable String id, @Valid @RequestBody ReasonRequest request) {
        noteService.offline(id, request.reason());
        return ApiResponse.ok(null);
    }
}
