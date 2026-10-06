package com.greennote.system.note.service;

import com.greennote.common.Ids;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greennote.common.api.PageResult;
import com.greennote.common.exception.BusinessException;
import com.greennote.system.channel.controller.vo.ChannelResponse;
import com.greennote.system.channel.mapper.ChannelMapper;
import com.greennote.system.note.CommentInsert;
import com.greennote.system.note.NoteInsert;
import com.greennote.system.note.NoteRecord;
import com.greennote.system.note.controller.vo.CommentRequest;
import com.greennote.system.note.controller.vo.CommentResponse;
import com.greennote.system.note.controller.vo.NoteCard;
import com.greennote.system.note.controller.vo.NoteDetail;
import com.greennote.system.note.controller.vo.NoteSaveRequest;
import com.greennote.system.note.mapper.NoteCollectMapper;
import com.greennote.system.note.mapper.NoteCommentMapper;
import com.greennote.system.note.mapper.NoteLikeMapper;
import com.greennote.system.note.mapper.NoteMapper;
import com.greennote.system.topic.controller.vo.TopicResponse;
import com.greennote.system.topic.mapper.TopicMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Service
public class NoteServiceImpl implements NoteService {

    private static final DateTimeFormatter COMMENT_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final NoteMapper noteMapper;
    private final NoteLikeMapper noteLikeMapper;
    private final NoteCollectMapper noteCollectMapper;
    private final NoteCommentMapper noteCommentMapper;
    private final ChannelMapper channelMapper;
    private final TopicMapper topicMapper;
    private final ObjectMapper objectMapper;

    public NoteServiceImpl(NoteMapper noteMapper,
                           NoteLikeMapper noteLikeMapper,
                           NoteCollectMapper noteCollectMapper,
                           NoteCommentMapper noteCommentMapper,
                           ChannelMapper channelMapper,
                           TopicMapper topicMapper,
                           ObjectMapper objectMapper) {
        this.noteMapper = noteMapper;
        this.noteLikeMapper = noteLikeMapper;
        this.noteCollectMapper = noteCollectMapper;
        this.noteCommentMapper = noteCommentMapper;
        this.channelMapper = channelMapper;
        this.topicMapper = topicMapper;
        this.objectMapper = objectMapper;
    }

    /** Published notes only. A blank channel lists every channel. */
    @Override
    public PageResult<NoteCard> feed(String channelId, int page, int size) {
        int limit = pageLimit(size);
        int offset = pageOffset(page, limit);
        String channel = isBlank(channelId) ? null : channelId;
        List<NoteCard> list = noteMapper.pagePublished(channel, limit, offset).stream()
                .map(this::toCard)
                .toList();
        return new PageResult<>(list, noteMapper.countPublished(channel));
    }

    /** Published notes are public. The author can also open a note in any other status. */
    @Override
    public NoteDetail detail(String id, String viewerId) {
        return toDetail(requireVisible(id, viewerId), viewerId);
    }

    /** Comments for a note the viewer is allowed to open. */
    @Override
    public List<CommentResponse> comments(String noteId, String viewerId) {
        requireVisible(noteId, viewerId);
        return noteCommentMapper.listByNote(noteId).stream()
                .map(row -> new CommentResponse(
                        row.id(),
                        row.userId(),
                        row.authorName(),
                        row.parentId(),
                        row.replyToUserId(),
                        row.replyToName(),
                        row.content(),
                        row.likeCount(),
                        row.createdAt() == null ? null : row.createdAt().format(COMMENT_TIME),
                        row.updatedAt() == null ? null : row.updatedAt().format(COMMENT_TIME)
                ))
                .toList();
    }

    /** Creates a note and publishes it immediately. Returns the new note id. */
    @Override
    @Transactional
    public String publish(String userId, NoteSaveRequest request) {
        NoteInsert note = buildNote(Ids.newId(), userId, 2, null, request);
        List<String> topicIds = requireEnabledTopics(request.topicIds());
        noteMapper.insert(note);
        replaceTopics(note.getId(), topicIds);
        return note.getId();
    }

    /** Author only, and only while the note is a draft or pending. Saving publishes it again. */
    @Override
    @Transactional
    public void update(String userId, String id, NoteSaveRequest request) {
        NoteRecord noteRecord = noteMapper.findById(id);
        if (noteRecord == null || !userId.equals(noteRecord.userId())) {
            throw new BusinessException(404, "Note not found");
        }
        if (noteRecord.status() != 0 && noteRecord.status() != 1) {
            throw new BusinessException(400, "Note cannot be edited");
        }

        NoteInsert note = buildNote(id, userId, 2, null, request);
        List<String> topicIds = requireEnabledTopics(request.topicIds());
        int updatedRowCount = noteMapper.updateContent(note);
        if (updatedRowCount == 0) {
            throw new BusinessException(404, "Note not found");
        }
        replaceTopics(id, topicIds);
    }

    /** Every note written by this member, in any status. */
    @Override
    public PageResult<NoteCard> mine(String userId, int page, int size) {
        int limit = pageLimit(size);
        int offset = pageOffset(page, limit);
        long total = noteMapper.countByAuthor(userId);
        List<NoteRecord> noteRecords = noteMapper.pageByAuthor(userId, limit, offset);
        return toCardPage(noteRecords, total);
    }

    /** Published notes this member has liked. */
    @Override
    public PageResult<NoteCard> likes(String userId, int page, int size) {
        int limit = pageLimit(size);
        int offset = pageOffset(page, limit);
        long total = noteMapper.countLiked(userId);
        List<NoteRecord> noteRecords = noteMapper.pageLiked(userId, limit, offset);
        return toCardPage(noteRecords, total);
    }

    /** Published notes this member has collected. */
    @Override
    public PageResult<NoteCard> collects(String userId, int page, int size) {
        int limit = pageLimit(size);
        int offset = pageOffset(page, limit);
        long total = noteMapper.countCollected(userId);
        List<NoteRecord> noteRecords = noteMapper.pageCollected(userId, limit, offset);
        return toCardPage(noteRecords, total);
    }

    /** Adds or removes this member's like on a published note, and updates the like count. */
    @Override
    @Transactional
    public void toggleLike(String userId, String noteId) {
        requirePublished(noteId);
        if (noteLikeMapper.exists(userId, noteId) > 0) {
            noteLikeMapper.delete(userId, noteId);
            noteMapper.addLikeCount(noteId, -1);
            return;
        }
        noteLikeMapper.insert(Ids.newId(), userId, noteId);
        noteMapper.addLikeCount(noteId, 1);
    }

    /** Adds or removes this member's collect on a published note, and updates the collect count. */
    @Override
    @Transactional
    public void toggleCollect(String userId, String noteId) {
        requirePublished(noteId);
        if (noteCollectMapper.exists(userId, noteId) > 0) {
            noteCollectMapper.delete(userId, noteId);
            noteMapper.addCollectCount(noteId, -1);
            return;
        }
        noteCollectMapper.insert(Ids.newId(), userId, noteId);
        noteMapper.addCollectCount(noteId, 1);
    }

    /** Adds a comment on a published note. A null parent is a top-level comment. Returns the comment id. */
    @Override
    @Transactional
    public String comment(String userId, String noteId, CommentRequest request) {
        requirePublished(noteId);
        String parentId = trimToNull(request.parentId());
        if (parentId != null && noteCommentMapper.listByNote(noteId).stream().noneMatch(row -> parentId.equals(row.id()))) {
            throw new BusinessException(404, "Comment not found");
        }
        CommentInsert comment = new CommentInsert();
        comment.setId(Ids.newId());
        comment.setNoteId(noteId);
        comment.setUserId(userId);
        comment.setParentId(parentId);
        comment.setReplyToUserId(trimToNull(request.replyToUserId()));
        comment.setContent(request.content().trim());
        noteCommentMapper.insert(comment);
        noteMapper.addCommentCount(noteId, 1);
        return comment.getId();
    }

    /** Admin list of every note. A null status means every status. */
    @Override
    public PageResult<NoteCard> adminPage(Integer status, String channelId, int page, int size) {
        int limit = pageLimit(size);
        int offset = pageOffset(page, limit);
        String channel = isBlank(channelId) ? null : channelId;
        long total = noteMapper.countAdmin(status, channel);
        List<NoteRecord> noteRecords = noteMapper.pageAdmin(status, channel, limit, offset);
        return toCardPage(noteRecords, total);
    }

    /** Publishes the note: status 2, reject reason cleared, published time set. */
    @Override
    public void approve(String id) {
        requireExisting(id);
        int updatedRowCount = noteMapper.approve(id);
        if (updatedRowCount == 0) {
            throw new BusinessException(404, "Note not found");
        }
    }

    /** Sends the note back to pending and stores the reject reason. The author can edit and submit again. */
    @Override
    public void reject(String id, String reason) {
        requireExisting(id);
        String rejectReason = requireReason(reason, "Reject reason is required");
        int updatedRowCount = noteMapper.reject(id, rejectReason);
        if (updatedRowCount == 0) {
            throw new BusinessException(404, "Note not found");
        }
    }

    /** Takes the note offline. The public feed and other members can no longer open it. */
    @Override
    public void offline(String id, String reason) {
        requireExisting(id);
        String offlineReason = requireReason(reason, "Offline reason is required");
        int updatedRowCount = noteMapper.offline(id, offlineReason);
        if (updatedRowCount == 0) {
            throw new BusinessException(404, "Note not found");
        }
    }

    private NoteRecord requireExisting(String id) {
        NoteRecord note = noteMapper.findById(id);
        if (note == null) {
            throw new BusinessException(404, "Note not found");
        }
        return note;
    }

    private String requireReason(String reason, String message) {
        String trimmedReason = trimToEmpty(reason);
        if (trimmedReason.isEmpty()) {
            throw new BusinessException(400, message);
        }
        return trimmedReason;
    }

    private NoteRecord requireVisible(String id, String viewerId) {
        NoteRecord note = noteMapper.findById(id);
        if (note == null) {
            throw new BusinessException(404, "Note not found");
        }
        if (note.status() == 2 || (viewerId != null && viewerId.equals(note.userId()))) {
            return note;
        }
        throw new BusinessException(404, "Note not found");
    }

    private NoteRecord requirePublished(String id) {
        NoteRecord note = noteMapper.findById(id);
        if (note == null || note.status() != 2) {
            throw new BusinessException(404, "Note not found");
        }
        return note;
    }

    private int pageLimit(int size) {
        return Math.min(Math.max(size, 1), 50);
    }

    private int pageOffset(int page, int limit) {
        int safePage = Math.max(page, 1);
        return (safePage - 1) * limit;
    }

    private PageResult<NoteCard> toCardPage(List<NoteRecord> noteRecords, long total) {
        List<NoteCard> list = noteRecords.stream().map(this::toCard).toList();
        PageResult<NoteCard> noteCardPageResult = new PageResult<>(list, total);
        return noteCardPageResult;
    }

    private NoteCard toCard(NoteRecord note) {
        return new NoteCard(
                note.id(),
                note.title(),
                note.coverUrl(),
                note.authorName(),
                note.authorAvatar(),
                note.likeCount(),
                note.status()
        );
    }

    private NoteDetail toDetail(NoteRecord note, String viewerId) {
        boolean signedIn = viewerId != null;
        return new NoteDetail(
                note.id(),
                note.userId(),
                note.authorName(),
                note.authorAvatar(),
                note.channelId(),
                note.channelName(),
                note.type(),
                note.title(),
                note.content(),
                note.coverUrl(),
                readImages(note.mediaJson()),
                note.videoUrl(),
                noteMapper.listTopics(note.id()),
                note.placeName(),
                note.cityName(),
                note.longitude(),
                note.latitude(),
                note.status(),
                note.rejectReason(),
                note.likeCount(),
                note.collectCount(),
                note.commentCount(),
                signedIn && noteLikeMapper.exists(viewerId, note.id()) > 0,
                signedIn && noteCollectMapper.exists(viewerId, note.id()) > 0,
                note.publishedAt(),
                note.createdAt(),
                note.updatedAt()
        );
    }

    private List<String> readImages(String mediaJson) {
        if (isBlank(mediaJson)) {
            return List.of();
        }
        try {
            List<String> images = objectMapper.readValue(mediaJson, objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, String.class));
            return images == null ? List.of() : images;
        } catch (JsonProcessingException ex) {
            return List.of();
        }
    }

    private NoteInsert buildNote(String id, String userId, int status, String rejectReason, NoteSaveRequest request) {
        int type = request.type() == null ? 1 : request.type();
        if (type != 1 && type != 2) {
            throw new BusinessException(400, "Note type is invalid");
        }
        if (type == 2 && isBlank(request.videoUrl())) {
            throw new BusinessException(400, "Video URL is required");
        }
        requireEnabledChannel(request.channelId());

        List<String> images = cleanImages(request.imageUrls());
        NoteInsert note = new NoteInsert();
        note.setId(id);
        note.setUserId(userId);
        note.setChannelId(request.channelId());
        note.setType(type);
        note.setTitle(request.title().trim());
        note.setContent(trimToEmpty(request.content()));
        note.setCoverUrl(images.isEmpty() ? null : images.get(0));
        note.setMediaJson(images.isEmpty() ? null : toJson(images));
        note.setVideoUrl(trimToNull(request.videoUrl()));
        note.setPlaceName(trimToNull(request.placeName()));
        note.setCityName(trimToNull(request.cityName()));
        note.setLongitude(request.longitude());
        note.setLatitude(request.latitude());
        note.setStatus(status);
        note.setRejectReason(rejectReason);
        return note;
    }

    private void requireEnabledChannel(String channelId) {
        if (channelId == null) {
            return;
        }
        ChannelResponse channel = channelMapper.findById(channelId);
        if (channel == null) {
            throw new BusinessException(404, "Channel not found");
        }
        if (channel.status() != 0) {
            throw new BusinessException(400, "Channel is disabled");
        }
    }

    private List<String> requireEnabledTopics(List<String> topicIds) {
        List<String> distinctTopicIds = distinctIds(topicIds);
        for (String topicId : distinctTopicIds) {
            TopicResponse topic = topicMapper.findById(topicId);
            if (topic == null) {
                throw new BusinessException(404, "Topic not found");
            }
            if (topic.status() != 0) {
                throw new BusinessException(400, "Topic is disabled");
            }
        }
        return distinctTopicIds;
    }

    private void replaceTopics(String noteId, List<String> topicIds) {
        noteMapper.deleteTopics(noteId);
        for (String topicId : topicIds) {
            noteMapper.insertTopic(Ids.newId(), noteId, topicId);
        }
    }

    private List<String> distinctIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (String id : ids) {
            if (id == null) {
                throw new BusinessException(404, "Topic not found");
            }
            unique.add(id);
        }
        return List.copyOf(unique);
    }

    private List<String> cleanImages(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return List.of();
        }
        List<String> images = new ArrayList<>();
        for (String url : imageUrls) {
            if (!isBlank(url)) {
                images.add(url.trim());
            }
        }
        return images;
    }

    private String toJson(List<String> images) {
        try {
            String json = objectMapper.writeValueAsString(images);
            if (json.length() > 4000) {
                throw new BusinessException(400, "Images are too long");
            }
            return json;
        } catch (JsonProcessingException ex) {
            throw new BusinessException(400, "Images are invalid");
        }
    }

    private static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private static String trimToNull(String value) {
        if (isBlank(value)) {
            return null;
        }
        return value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
