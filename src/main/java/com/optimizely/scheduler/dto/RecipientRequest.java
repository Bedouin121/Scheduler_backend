package com.optimizely.scheduler.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Payload for adding a recipient to a campaign.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecipientRequest {

    @NotBlank(message = "Recipient name is required")
    private String name;

    @NotBlank(message = "Recipient email is required")
    @Email(message = "Must be a valid email address")
    private String email;
}