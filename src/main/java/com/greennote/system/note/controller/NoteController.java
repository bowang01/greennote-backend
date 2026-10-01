package com.greennote.system.note.controller;

import com.greennote.common.api.ApiResponse;
import com.greennote.common.api.PageResult;
import com.greennote.security.SecurityUtils;
import com.greennote.system.note.controller.vo.CommentResponse;
import com.greennote.system.note.controller.vo.NoteCard;
import com.greennote.system.note.controller.vo.NoteDetail;
import com.greennote.system.note.service.NoteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping("/api/notes")
    public ApiResponse<PageResult<NoteCard>> feed(@RequestParam(required = false) String channelId,
                                                  @RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(noteService.feed(channelId, page, size));
    }

    @GetMapping("/api/notes/{id}")
    public ApiResponse<NoteDetail> detail(@PathVariable String id) {
        String viewerId = SecurityUtils.currentUserOrNull();
        return ApiResponse.ok(noteService.detail(id, viewerId));
    }

    @GetMapping("/api/notes/{id}/comments")
    public ApiResponse<List<CommentResponse>> comments(@PathVariable String id) {
        return ApiResponse.ok(noteService.comments(id, SecurityUtils.currentUserOrNull()));
    }
}
