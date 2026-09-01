package com.optimizely.scheduler.controller;

import com.optimizely.scheduler.dto.CampaignRequest;
import com.optimizely.scheduler.dto.CampaignResponse;
import com.optimizely.scheduler.entity.User;
import com.optimizely.scheduler.service.CampaignService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Endpoints for managing campaigns.
 * All operations are scoped to the authenticated user.
 */
@RestController
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
@Slf4j
public class CampaignController {

    private final CampaignService campaignService;

    /**
     * Create a new campaign owned by the current user.
     */
    @PostMapping
    public ResponseEntity<CampaignResponse> create(
            @Valid @RequestBody CampaignRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(campaignService.create(request, user));
    }

    /**
     * List the current user's campaigns.
     */
    @GetMapping
    public ResponseEntity<List<CampaignResponse>> list(@AuthenticationPrincipal User user) {
        if (user == null) {
            log.warn("GET /api/campaigns called but @AuthenticationPrincipal User is null");
        } else {
            log.info("GET /api/campaigns called for user: {}", user.getEmail());
        }
        return ResponseEntity.ok(campaignService.listByOwner(user));
    }

    /**
     * Get a single campaign with its recipients and messages.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CampaignResponse> get(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(campaignService.getById(id, user));
    }

    /**
     * Update a campaign's name and/or status.
     */
    @PutMapping("/{id}")
    public ResponseEntity<CampaignResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CampaignRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(campaignService.update(id, request, user));
    }

    /**
     * Delete a campaign and all related data.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        campaignService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}