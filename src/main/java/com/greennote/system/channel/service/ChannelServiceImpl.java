package com.greennote.system.channel.service;

import com.greennote.common.Ids;
import com.greennote.common.exception.BusinessException;
import com.greennote.system.channel.controller.vo.ChannelResponse;
import com.greennote.system.channel.controller.vo.ChannelSaveRequest;
import com.greennote.system.channel.mapper.ChannelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChannelServiceImpl implements ChannelService {

    private final ChannelMapper channelMapper;

    public ChannelServiceImpl(ChannelMapper channelMapper) {
        this.channelMapper = channelMapper;
    }

    @Override
    public List<ChannelResponse> listEnabled() {
        return channelMapper.list(0);
    }

    @Override
    public List<ChannelResponse> listAll() {
        return channelMapper.list(null);
    }

    @Override
    public void create(ChannelSaveRequest request) {
        String name = request.name().trim();
        rejectDuplicate(name, null);
        channelMapper.insert(Ids.newId(), name, request.sortNo());
    }

    @Override
    public void update(String id, ChannelSaveRequest request) {
        require(id);
        String name = request.name().trim();
        rejectDuplicate(name, id);
        channelMapper.update(id, name, request.sortNo());
    }

    @Override
    public void changeStatus(String id, int status) {
        if (channelMapper.updateStatus(id, status) == 0) {
            throw new BusinessException(404, "Channel not found");
        }
    }

    private void require(String id) {
        if (channelMapper.findById(id) == null) {
            throw new BusinessException(404, "Channel not found");
        }
    }

    private void rejectDuplicate(String name, String excludeId) {
        if (channelMapper.countByName(name, excludeId) > 0) {
            throw new BusinessException(400, "Channel name already exists");
        }
    }
}
