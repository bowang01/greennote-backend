package com.greennote.system.note.controller;

import com.greennote.common.api.ApiResponse;
import com.greennote.common.api.PageResult;
import com.greennote.security.SecurityUtils;
import com.greennote.system.note.controller.vo.CommentRequest;
import com.greennote.system.note.controller.vo.InboxItem;
import com.greennote.system.note.controller.vo.NoteCard;
import com.greennote.system.note.controller.vo.NoteSaveRequest;
import com.greennote.system.note.service.NoteService;
import jakarta.validation.Valid;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MemberNoteController {

    private final NoteService noteService;

    public MemberNoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @PostMapping("/api/member/notes")
    public ApiResponse<String> publish(@Valid @RequestBody NoteSaveRequest request) {
        return ApiResponse.ok(noteService.publish(SecurityUtils.currentUser().userId(), request));
    }

    @PutMapping("/api/member/notes/{id}")
    public ApiResponse<Void> update(@PathVariable String id, @Valid @RequestBody NoteSaveRequest request) {
            noteService.update(SecurityUtils.currentUser().userId(), id, request);
        return ApiResponse.ok(null);
    }

    @GetMapping("/api/member/notes/mine")
    public ApiResponse<PageResult<NoteCard>> mine(@RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(noteService.mine(SecurityUtils.currentUser().userId(), page, size));
    }

    @GetMapping("/api/member/notes/likes")
    public ApiResponse<PageResult<NoteCard>> likes(@RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(noteService.likes(SecurityUtils.currentUser().userId(), page, size));
    }

    @GetMapping("/api/member/notes/collects")
    public ApiResponse<PageResult<NoteCard>> collects(@RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(noteService.collects(SecurityUtils.currentUser().userId(), page, size));
    }

    @GetMapping("/api/member/inbox")
    public ApiResponse<List<InboxItem>> inbox(@RequestParam(defaultValue = "like") String kind) {
        return ApiResponse.ok(noteService.inbox(SecurityUtils.currentUser().userId(), kind));
    }

    @PutMapping("/api/member/notes/{id}/like")
    public ApiResponse<Void> toggleLike(@PathVariable String id) {
        noteService.toggleLike(SecurityUtils.currentUser().userId(), id);
        return ApiResponse.ok(null);
    }

    @PutMapping("/api/member/notes/{id}/collect")
    public ApiResponse<Void> toggleCollect(@PathVariable String id) {
        noteService.toggleCollect(SecurityUtils.currentUser().userId(), id);
        return ApiResponse.ok(null);
    }

    @PostMapping("/api/member/notes/{id}/comments")
    public ApiResponse<String> comment(@PathVariable String id, @Valid @RequestBody CommentRequest request) {
        return ApiResponse.ok(noteService.comment(SecurityUtils.currentUser().userId(), id, request));
    }
}
