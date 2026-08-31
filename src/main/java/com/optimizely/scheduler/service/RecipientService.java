package com.optimizely.scheduler.service;

import com.optimizely.scheduler.dto.RecipientRequest;
import com.optimizely.scheduler.dto.RecipientResponse;
import com.optimizely.scheduler.entity.Campaign;
import com.optimizely.scheduler.entity.Recipient;
import com.optimizely.scheduler.entity.User;
import com.optimizely.scheduler.exception.NotFoundException;
import com.optimizely.scheduler.repository.RecipientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Manages recipients within a campaign.
 * Validates that the parent campaign belongs to the authenticated
 * user before allowing any changes.
 */
@Service
@RequiredArgsConstructor
public class RecipientService {

    private final RecipientRepository recipientRepository;
    private final CampaignService campaignService;

    /**
     * Add a recipient to the specified campaign.
     */
    public RecipientResponse add(Long campaignId, RecipientRequest request, User owner) {
        Campaign campaign = campaignService.findOwnedCampaign(campaignId, owner);

        Recipient recipient = Recipient.builder()
                .name(request.getName())
                .email(request.getEmail())
                .campaign(campaign)
                .build();

        Recipient saved = recipientRepository.save(recipient);
        return RecipientResponse.from(saved);
    }

    /**
     * List all recipients for a given campaign.
     */
    public List<RecipientResponse> listByCampaign(Long campaignId, User owner) {
        // Verify ownership of the parent campaign first
        campaignService.findOwnedCampaign(campaignId, owner);

        return recipientRepository.findByCampaignId(campaignId).stream()
                .map(RecipientResponse::from)
                .toList();
    }

    /**
     * Delete a single recipient by id.
     */
    public void delete(Long recipientId, User owner) {
        Recipient recipient = recipientRepository.findById(recipientId)
                .orElseThrow(() -> new NotFoundException("Recipient not found"));

        // Verify the recipient's campaign belongs to this user
        campaignService.findOwnedCampaign(recipient.getCampaign().getId(), owner);
        recipientRepository.delete(recipient);
    }
}