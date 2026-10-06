package com.greennote.system.note.service;

import com.greennote.common.api.PageResult;
import com.greennote.system.note.controller.vo.CommentRequest;
import com.greennote.system.note.controller.vo.CommentResponse;
import com.greennote.system.note.controller.vo.NoteCard;
import com.greennote.system.note.controller.vo.NoteDetail;
import com.greennote.system.note.controller.vo.NoteSaveRequest;

import java.util.List;

public interface NoteService {

    /** Published notes only. A blank channel lists every channel. */
    PageResult<NoteCard> feed(String channelId, int page, int size);

    /** Published notes are public. The author can also open a note in any other status. */
    NoteDetail detail(String id, String viewerId);

    /** Comments for a note the viewer is allowed to open. */
    List<CommentResponse> comments(String noteId, String viewerId);

    /** Creates a note and publishes it immediately. Returns the new note id. */
    String publish(String userId, NoteSaveRequest request);

    /** Author only, and only while the note is a draft or pending. Saving publishes it again. */
    void update(String userId, String id, NoteSaveRequest request);

    /** Every note written by this member, in any status. */
    PageResult<NoteCard> mine(String userId, int page, int size);

    /** Published notes this member has liked. */
    PageResult<NoteCard> likes(String userId, int page, int size);

    /** Published notes this member has collected. */
    PageResult<NoteCard> collects(String userId, int page, int size);

    /** Adds or removes this member's like on a published note, and updates the like count. */
    void toggleLike(String userId, String noteId);

    /** Adds or removes this member's collect on a published note, and updates the collect count. */
    void toggleCollect(String userId, String noteId);

    /** Adds a comment on a published note. A null parent is a top-level comment. Returns the comment id. */
    String comment(String userId, String noteId, CommentRequest request);

    /** Admin list of every note. A null status means every status. */
    PageResult<NoteCard> adminPage(Integer status, String channelId, int page, int size);

    /** Publishes the note: status 2, reject reason cleared, published time set. */
    void approve(String id);

    /** Sends the note back to pending and stores the reject reason. The author can edit and submit again. */
    void reject(String id, String reason);

    /** Takes the note offline. The public feed and other members can no longer open it. */
    void offline(String id, String reason);
}
