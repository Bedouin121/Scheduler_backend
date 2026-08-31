package com.optimizely.scheduler.service;

import com.optimizely.scheduler.dto.MessageRequest;
import com.optimizely.scheduler.dto.MessageResponse;
import com.optimizely.scheduler.entity.Campaign;
import com.optimizely.scheduler.entity.Message;
import com.optimizely.scheduler.entity.User;
import com.optimizely.scheduler.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Manages message scheduling within a campaign.
 * Messages are created with a scheduled time and a sent flag;
 * the send trigger is responsible for flipping that flag.
 */
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final CampaignService campaignService;

    /**
     * Schedule a new message on the given campaign.
     */
    public MessageResponse create(Long campaignId, MessageRequest request, User owner) {
        Campaign campaign = campaignService.findOwnedCampaign(campaignId, owner);

        Message message = Message.builder()
                .content(request.getContent())
                .scheduledTime(request.getScheduledTime())
                .campaign(campaign)
                .build();

        Message saved = messageRepository.save(message);
        return MessageResponse.from(saved);
    }

    /**
     * List all messages for a given campaign.
     */
    public List<MessageResponse> listByCampaign(Long campaignId, User owner) {
        // Verify ownership
        campaignService.findOwnedCampaign(campaignId, owner);

        return messageRepository.findByCampaignId(campaignId).stream()
                .map(MessageResponse::from)
                .toList();
    }
}