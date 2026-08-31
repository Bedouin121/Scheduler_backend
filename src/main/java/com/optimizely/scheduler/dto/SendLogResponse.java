package com.optimizely.scheduler.dto;

import com.optimizely.scheduler.entity.SendLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * A single send log entry returned to the client.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendLogResponse {

    private Long id;
    private String recipientName;
    private String recipientEmail;
    private Long messageId;
    private String messageContent;
    private String status;
    private Instant timestamp;

    public static SendLogResponse from(SendLog log) {
        return SendLogResponse.builder()
                .id(log.getId())
                .recipientName(log.getRecipient().getName())
                .recipientEmail(log.getRecipient().getEmail())
                .messageId(log.getMessage().getId())
                .messageContent(log.getMessage().getContent())
                .status(log.getStatus().name())
                .timestamp(log.getTimestamp())
                .build();
    }
}