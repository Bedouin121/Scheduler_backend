package com.optimizely.scheduler.dto;

import com.optimizely.scheduler.entity.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Message data returned to the client.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageResponse {

    private Long id;
    private String content;
    private Instant scheduledTime;
    private boolean sent;

    public static MessageResponse from(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .content(message.getContent())
                .scheduledTime(message.getScheduledTime())
                .sent(message.isSent())
                .build();
    }
}