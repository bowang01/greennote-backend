package com.greennote.system.note.mapper;

import com.greennote.system.note.NoteInsert;
import com.greennote.system.note.NoteRecord;
import com.greennote.system.note.controller.vo.TopicBrief;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NoteMapper {

    int insert(NoteInsert note);

    int updateContent(NoteInsert note);

    NoteRecord findById(String id);

    long countPublished(@Param("channelId") String channelId);

    List<NoteRecord> pagePublished(@Param("channelId") String channelId,
                                   @Param("limit") int limit,
                                   @Param("offset") int offset);

    long countByAuthor(String userId);

    List<NoteRecord> pageByAuthor(@Param("userId") String userId,
                                  @Param("limit") int limit,
                                  @Param("offset") int offset);

    long countAdmin(@Param("status") Integer status, @Param("channelId") String channelId);

    List<NoteRecord> pageAdmin(@Param("status") Integer status,
                               @Param("channelId") String channelId,
                               @Param("limit") int limit,
                               @Param("offset") int offset);

    long countLiked(String userId);

    List<NoteRecord> pageLiked(@Param("userId") String userId,
                               @Param("limit") int limit,
                               @Param("offset") int offset);

    long countCollected(String userId);

    List<NoteRecord> pageCollected(@Param("userId") String userId,
                                   @Param("limit") int limit,
                                   @Param("offset") int offset);

    int approve(String id);

    int reject(@Param("id") String id, @Param("reason") String reason);

    int offline(@Param("id") String id, @Param("reason") String reason);

    int addLikeCount(@Param("id") String id, @Param("delta") int delta);

    int addCollectCount(@Param("id") String id, @Param("delta") int delta);

    int addCommentCount(@Param("id") String id, @Param("delta") int delta);

    List<TopicBrief> listTopics(String noteId);

    int deleteTopics(String noteId);

    int insertTopic(@Param("id") String id, @Param("noteId") String noteId, @Param("topicId") String topicId);
}
