package com.greennote.system.channel.service;

import com.greennote.system.channel.controller.vo.ChannelResponse;
import com.greennote.system.channel.controller.vo.ChannelSaveRequest;

import java.util.List;

public interface ChannelService {

    List<ChannelResponse> listEnabled();

    List<ChannelResponse> listAll();

    void create(ChannelSaveRequest request);

    void update(String id, ChannelSaveRequest request);

    void changeStatus(String id, int status);
}
