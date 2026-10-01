package com.greennote.system.note.service;

import com.greennote.common.Ids;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greennote.common.api.PageResult;
import com.greennote.common.exception.BusinessException;
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

    @Override
    public PageResult<NoteCard> feed(String channelId, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 50);
        String channel = isBlank(channelId) ? null : channelId;
        int offset = (safePage - 1) * safeSize;
        List<NoteCard> list = noteMapper.pagePublished(channel, safeSize, offset).stream()
                .map(this::toCard)
                .toList();
        return new PageResult<>(list, noteMapper.countPublished(channel));
    }

    @Override
    public NoteDetail detail(String id, String viewerId) {
        return toDetail(requireVisible(id, viewerId), viewerId);
    }

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

    @Override
    @Transactional
    public String publish(String userId, NoteSaveRequest request) {
        int type = request.type() == null ? 1 : request.type();
        if (type != 1 && type != 2) {
            throw new BusinessException(400, "Note type is invalid");
        }
        if (type == 2 && isBlank(request.videoUrl())) {
            throw new BusinessException(400, "Video URL is required");
        }
        if (request.channelId() != null) {
            var channel = channelMapper.findById(request.channelId());
            if (channel == null) {
                throw new BusinessException(404, "Channel not found");
            }
            if (channel.status() != 0) {
                throw new BusinessException(400, "Channel is disabled");
            }
        }

        List<String> topicIds = distinctIds(request.topicIds());
        for (String topicId : topicIds) {
            var topic = topicMapper.findById(topicId);
            if (topic == null) {
                throw new BusinessException(404, "Topic not found");
            }
            if (topic.status() != 0) {
                throw new BusinessException(400, "Topic is disabled");
            }
        }

        List<String> images = cleanImages(request.imageUrls());
        NoteInsert note = new NoteInsert();
        note.setId(Ids.newId());
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
        note.setStatus(1);
        noteMapper.insert(note);

        noteMapper.deleteTopics(note.getId());
        for (String topicId : topicIds) {
            noteMapper.insertTopic(Ids.newId(), note.getId(), topicId);
        }
        return note.getId();
    }

    @Override
    public void update(String userId, String id, NoteSaveRequest request) {
        // Author only. Allowed while status is 0 or 1. Saving sets status back to 1 and clears reject_reason.
        pending();
    }

    @Override
    public PageResult<NoteCard> mine(String userId, int page, int size) {
        // All of the author's notes, including draft, pending, published, and offline.
        return pending();
    }

    @Override
    public PageResult<NoteCard> likes(String userId, int page, int size) {
        // Published notes this member liked. noteMapper.pageLiked is ready.
        return pending();
    }

    @Override
    public PageResult<NoteCard> collects(String userId, int page, int size) {
        // Published notes this member collected. noteMapper.pageCollected is ready.
        return pending();
    }

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

    @Override
    public PageResult<NoteCard> adminPage(Integer status, String channelId, int page, int size) {
        // noteMapper.pageAdmin / countAdmin. Null status means every status.
        return pending();
    }

    @Override
    public void approve(String id) {
        // status becomes 2 and published_at is set. noteMapper.approve is ready.
        pending();
    }

    @Override
    public void reject(String id, String reason) {
        // Stay at status 1 and store reject_reason. The author can edit and submit again.
        pending();
    }

    @Override
    public void offline(String id, String reason) {
        // status becomes 3. The public feed and other members' detail requests hide it.
        pending();
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

    private static <T> T pending() {
        throw new BusinessException(501, "Not implemented");
    }
}
