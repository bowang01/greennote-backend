package com.greennote.system.catalog.service;

import com.greennote.system.catalog.controller.vo.DepartmentRow;
import com.greennote.system.catalog.controller.vo.DictRow;
import com.greennote.system.catalog.controller.vo.LogRow;
import com.greennote.system.file.controller.vo.StoredFile;

import java.util.List;

public interface CatalogService {

    List<DepartmentRow> departments();

    List<DictRow> dictionaries();

    List<StoredFile> files();

    List<LogRow> logs();
}
