package com.greennote.system.note.service;

import com.greennote.common.api.PageResult;
import com.greennote.system.note.controller.vo.CommentRequest;
import com.greennote.system.note.controller.vo.CommentResponse;
import com.greennote.system.note.controller.vo.NoteCard;
import com.greennote.system.note.controller.vo.NoteDetail;
import com.greennote.system.note.controller.vo.NoteSaveRequest;

import java.util.List;

public interface NoteService {

    PageResult<NoteCard> feed(String channelId, int page, int size);

    NoteDetail detail(String id, String viewerId);

    List<CommentResponse> comments(String noteId, String viewerId);

    String publish(String userId, NoteSaveRequest request);

    void update(String userId, String id, NoteSaveRequest request);

    PageResult<NoteCard> mine(String userId, int page, int size);

    PageResult<NoteCard> likes(String userId, int page, int size);

    PageResult<NoteCard> collects(String userId, int page, int size);

    void toggleLike(String userId, String noteId);

    void toggleCollect(String userId, String noteId);

    String comment(String userId, String noteId, CommentRequest request);

    PageResult<NoteCard> adminPage(Integer status, String channelId, int page, int size);

    void approve(String id);

    void reject(String id, String reason);

    void offline(String id, String reason);
}
