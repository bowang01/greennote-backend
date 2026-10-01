package com.greennote.system.note.mapper;

import com.greennote.system.note.CommentInsert;
import com.greennote.system.note.CommentRecord;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface NoteCommentMapper {

    int insert(CommentInsert comment);

    List<CommentRecord> listByNote(String noteId);
}
