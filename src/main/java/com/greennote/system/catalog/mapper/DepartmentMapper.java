package com.greennote.system.catalog.mapper;

import com.greennote.system.catalog.controller.vo.DepartmentRow;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DepartmentMapper {

    List<DepartmentRow> listAll();
}
