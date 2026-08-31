package com.optimizely.scheduler.controller;

import com.optimizely.scheduler.dto.SendLogResponse;
import com.optimizely.scheduler.entity.User;
import com.optimizely.scheduler.service.SendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints for the manual send trigger and for viewing send history.
 */
@RestController
@RequestMapping("/api/send")
@RequiredArgsConstructor
public class SendController {

    private final SendService sendService;

    /**
     * Trigger delivery of all due, unsent messages across the current
     * user's campaigns. Returns the send logs created by this call.
     */
    @PostMapping("/run")
    public ResponseEntity<List<SendLogResponse>> runDueSends(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(sendService.runDueSends(user));
    }

    /**
     * List all send logs for the current user's campaigns, newest first.
     */
    @GetMapping("/logs")
    public ResponseEntity<List<SendLogResponse>> logs(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(sendService.getLogs(user));
    }
}