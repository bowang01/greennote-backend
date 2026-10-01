package com.greennote.system.topic.service;

import com.greennote.common.Ids;
import com.greennote.common.exception.BusinessException;
import com.greennote.system.topic.controller.vo.TopicResponse;
import com.greennote.system.topic.controller.vo.TopicSaveRequest;
import com.greennote.system.topic.mapper.TopicMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TopicServiceImpl implements TopicService {

    private final TopicMapper topicMapper;

    public TopicServiceImpl(TopicMapper topicMapper) {
        this.topicMapper = topicMapper;
    }

    @Override
    public List<TopicResponse> listEnabled() {
        return topicMapper.list(0);
    }

    @Override
    public List<TopicResponse> listAll() {
        return topicMapper.list(null);
    }

    @Override
    public void create(TopicSaveRequest request) {
        String name = request.name().trim();
        String intro = request.intro() == null ? "" : request.intro().trim();
        rejectDuplicate(name, null);
        topicMapper.insert(Ids.newId(), name, intro, request.sortNo());
    }

    @Override
    public void update(String id, TopicSaveRequest request) {
        require(id);
        String name = request.name().trim();
        String intro = request.intro() == null ? "" : request.intro().trim();
        rejectDuplicate(name, id);
        topicMapper.update(id, name, intro, request.sortNo());
    }

    @Override
    public void changeStatus(String id, int status) {
        if (topicMapper.updateStatus(id, status) == 0) {
            throw new BusinessException(404, "Topic not found");
        }
    }

    private void require(String id) {
        if (topicMapper.findById(id) == null) {
            throw new BusinessException(404, "Topic not found");
        }
    }

    private void rejectDuplicate(String name, String excludeId) {
        if (topicMapper.countByName(name, excludeId) > 0) {
            throw new BusinessException(400, "Topic name already exists");
        }
    }
}
