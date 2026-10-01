package com.greennote.system.file.mapper;

import com.greennote.system.file.FileInsert;
import com.greennote.system.file.controller.vo.StoredFile;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FileMapper {

    int insert(FileInsert file);

    List<StoredFile> listRecent(int limit);
}
