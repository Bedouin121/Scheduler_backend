package com.optimizely.scheduler.controller;

import com.optimizely.scheduler.dto.RecipientRequest;
import com.optimizely.scheduler.dto.RecipientResponse;
import com.optimizely.scheduler.entity.User;
import com.optimizely.scheduler.service.RecipientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints for managing recipients and adding them to campaigns.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RecipientController {

    private final RecipientService recipientService;

    /**
     * Add a recipient to a campaign.
     */
    @PostMapping("/campaigns/{campaignId}/recipients")
    public ResponseEntity<RecipientResponse> add(
            @PathVariable Long campaignId,
            @Valid @RequestBody RecipientRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(recipientService.add(campaignId, request, user));
    }

    /**
     * List all recipients for a campaign.
     */
    @GetMapping("/campaigns/{campaignId}/recipients")
    public ResponseEntity<List<RecipientResponse>> list(
            @PathVariable Long campaignId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(recipientService.listByCampaign(campaignId, user));
    }

    /**
     * Delete a single recipient.
     */
    @DeleteMapping("/recipients/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        recipientService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}