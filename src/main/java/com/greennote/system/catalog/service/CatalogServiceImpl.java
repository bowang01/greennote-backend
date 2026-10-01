package com.greennote.system.catalog.service;

import com.greennote.system.catalog.controller.vo.DepartmentRow;
import com.greennote.system.catalog.controller.vo.DictRow;
import com.greennote.system.catalog.controller.vo.LogRow;
import com.greennote.system.catalog.mapper.DepartmentMapper;
import com.greennote.system.catalog.mapper.DictionaryMapper;
import com.greennote.system.file.controller.vo.StoredFile;
import com.greennote.system.file.mapper.FileMapper;
import com.greennote.system.log.mapper.OperateLogMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatalogServiceImpl implements CatalogService {

    private final DepartmentMapper departmentMapper;
    private final DictionaryMapper dictionaryMapper;
    private final FileMapper fileMapper;
    private final OperateLogMapper operateLogMapper;

    public CatalogServiceImpl(DepartmentMapper departmentMapper,
                              DictionaryMapper dictionaryMapper,
                              FileMapper fileMapper,
                              OperateLogMapper operateLogMapper) {
        this.departmentMapper = departmentMapper;
        this.dictionaryMapper = dictionaryMapper;
        this.fileMapper = fileMapper;
        this.operateLogMapper = operateLogMapper;
    }

    @Override
    public List<DepartmentRow> departments() {
        return departmentMapper.listAll();
    }

    @Override
    public List<DictRow> dictionaries() {
        return dictionaryMapper.listAll();
    }

    @Override
    public List<StoredFile> files() {
        return fileMapper.listRecent(100);
    }

    @Override
    public List<LogRow> logs() {
        return operateLogMapper.listRecent(100).stream()
                .map(row -> new LogRow(
                        row.getId(),
                        row.getOperatorName(),
                        row.getAction(),
                        row.getDetail(),
                        row.getCreatedAt().toInstant().toString()
                ))
                .toList();
    }
}
