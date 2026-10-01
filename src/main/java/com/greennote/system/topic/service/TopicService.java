package com.greennote.system.topic.service;

import com.greennote.system.topic.controller.vo.TopicResponse;
import com.greennote.system.topic.controller.vo.TopicSaveRequest;

import java.util.List;

public interface TopicService {

    List<TopicResponse> listEnabled();

    List<TopicResponse> listAll();

    void create(TopicSaveRequest request);

    void update(String id, TopicSaveRequest request);

    void changeStatus(String id, int status);
}
