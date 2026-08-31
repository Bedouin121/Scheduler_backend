package com.optimizely.scheduler.dto;

import com.optimizely.scheduler.entity.Recipient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Recipient data returned to the client.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipientResponse {

    private Long id;
    private String name;
    private String email;

    public static RecipientResponse from(Recipient recipient) {
        return RecipientResponse.builder()
                .id(recipient.getId())
                .name(recipient.getName())
                .email(recipient.getEmail())
                .build();
    }
}