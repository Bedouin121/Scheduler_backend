package com.optimizely.scheduler.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Payload for creating or updating a campaign.
 * On update, either or both fields may be provided.
 * The status field is optional on create (defaults to DRAFT)
 * and optional on update (only changed when present).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CampaignRequest {

    @NotBlank(message = "Campaign name is required")
    private String name;

    /**
     * Campaign status, accepted values: DRAFT, ACTIVE, COMPLETED.
     * Null means leave unchanged on update, or default to DRAFT on create.
     */
    private String status;
}