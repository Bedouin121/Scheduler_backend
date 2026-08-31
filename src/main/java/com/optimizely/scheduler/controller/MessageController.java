package com.optimizely.scheduler.controller;

import com.optimizely.scheduler.dto.MessageRequest;
import com.optimizely.scheduler.dto.MessageResponse;
import com.optimizely.scheduler.entity.User;
import com.optimizely.scheduler.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints for scheduling and listing campaign messages.
 */
@RestController
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * Schedule a message on a campaign.
     */
    @PostMapping("/{campaignId}/messages")
    public ResponseEntity<MessageResponse> create(
            @PathVariable Long campaignId,
            @Valid @RequestBody MessageRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(messageService.create(campaignId, request, user));
    }

    /**
     * List all messages for a campaign.
     */
    @GetMapping("/{campaignId}/messages")
    public ResponseEntity<List<MessageResponse>> list(
            @PathVariable Long campaignId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(messageService.listByCampaign(campaignId, user));
    }
}