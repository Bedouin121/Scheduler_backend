package com.optimizely.scheduler.dto;

import com.optimizely.scheduler.entity.Campaign;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

/**
 * Campaign data returned to the client, including nested recipients and messages
 * when fetching a single campaign by id.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignResponse {

    private Long id;
    private String name;
    private String status;
    private Instant createdAt;
    private List<RecipientResponse> recipients;
    private List<MessageResponse> messages;

    /**
     * Maps a Campaign entity to its response representation.
     */
    public static CampaignResponse from(Campaign campaign) {
        return CampaignResponse.builder()
                .id(campaign.getId())
                .name(campaign.getName())
                .status(campaign.getStatus().name())
                .createdAt(campaign.getCreatedAt())
                .build();
    }

    /**
     * Maps a Campaign entity to a response including its nested children.
     */
    public static CampaignResponse fromWithDetails(Campaign campaign) {
        CampaignResponse response = from(campaign);
        response.setRecipients(
            campaign.getRecipients().stream()
                .map(RecipientResponse::from)
                .toList()
        );
        response.setMessages(
            campaign.getMessages().stream()
                .map(MessageResponse::from)
                .toList()
        );
        return response;
    }
}