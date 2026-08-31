package com.optimizely.scheduler.service;

import com.optimizely.scheduler.dto.SendLogResponse;
import com.optimizely.scheduler.entity.Campaign;
import com.optimizely.scheduler.entity.Message;
import com.optimizely.scheduler.entity.Recipient;
import com.optimizely.scheduler.entity.SendLog;
import com.optimizely.scheduler.entity.User;
import com.optimizely.scheduler.repository.CampaignRepository;
import com.optimizely.scheduler.repository.MessageRepository;
import com.optimizely.scheduler.repository.SendLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles the manual "run due sends now" action.
 * Finds all unsent messages whose scheduled time has passed across
 * the current user's campaigns, writes a send log per recipient,
 * and marks those messages as sent.
 */
@Service
@RequiredArgsConstructor
public class SendService {

    private final CampaignRepository campaignRepository;
    private final MessageRepository messageRepository;
    private final SendLogRepository sendLogRepository;

    /**
     * Process all due, unsent messages for the given user.
     * Returns the list of send logs created by this invocation.
     */
    @Transactional
    public List<SendLogResponse> runDueSends(User owner) {
        // Collect all campaign ids owned by this user
        List<Long> campaignIds = campaignRepository.findByOwnerId(owner.getId()).stream()
                .map(Campaign::getId)
                .toList();

        if (campaignIds.isEmpty()) {
            return List.of();
        }

        // Find every message that is due and still unsent
        List<Message> dueMessages = messageRepository.findDueUnsentMessages(
                campaignIds, Instant.now());

        List<SendLog> logs = new ArrayList<>();

        for (Message message : dueMessages) {
            List<Recipient> recipients = message.getCampaign().getRecipients();

            for (Recipient recipient : recipients) {
                SendLog log = SendLog.builder()
                        .recipient(recipient)
                        .message(message)
                        .status(SendLog.Status.SENT)
                        .build();
                logs.add(log);
            }

            // Mark the message as sent so it is not picked up again
            message.setSent(true);
            messageRepository.save(message);
        }

        return sendLogRepository.saveAll(logs).stream()
                .map(SendLogResponse::from)
                .toList();
    }

    /**
     * Retrieve all send logs for the current user's campaigns,
     * ordered most recent first.
     */
    public List<SendLogResponse> getLogs(User owner) {
        return sendLogRepository.findByOwnerIdOrderDesc(owner.getId()).stream()
                .map(SendLogResponse::from)
                .toList();
    }
}