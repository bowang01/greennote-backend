package com.greennote.system.catalog.mapper;

import com.greennote.system.catalog.controller.vo.DictRow;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DictionaryMapper {

    List<DictRow> listAll();
}
