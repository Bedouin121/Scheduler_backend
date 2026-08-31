package com.optimizely.scheduler.service;

import com.optimizely.scheduler.dto.CampaignRequest;
import com.optimizely.scheduler.dto.CampaignResponse;
import com.optimizely.scheduler.entity.Campaign;
import com.optimizely.scheduler.entity.User;
import com.optimizely.scheduler.exception.NotFoundException;
import com.optimizely.scheduler.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Manages campaign CRUD operations.
 * Every method scopes results to the authenticated user so that
 * one user can never see or modify another user's campaigns.
 */
@Service
@RequiredArgsConstructor
public class CampaignService {

    private final CampaignRepository campaignRepository;

    /**
     * Create a new campaign owned by the given user.
     * If a status is provided it is applied; otherwise the campaign
     * defaults to DRAFT as defined on the entity.
     */
    public CampaignResponse create(CampaignRequest request, User owner) {
        Campaign.Status status = (request.getStatus() != null)
                ? Campaign.Status.valueOf(request.getStatus())
                : Campaign.Status.DRAFT;

        Campaign campaign = Campaign.builder()
                .name(request.getName())
                .status(status)
                .owner(owner)
                .build();

        Campaign saved = campaignRepository.save(campaign);
        return CampaignResponse.from(saved);
    }

    /**
     * List all campaigns belonging to the given user.
     */
    public List<CampaignResponse> listByOwner(User owner) {
        return campaignRepository.findByOwnerId(owner.getId()).stream()
                .map(CampaignResponse::from)
                .toList();
    }

    /**
     * Retrieve a single campaign by id, including its recipients and messages.
     *
     * @throws NotFoundException if the campaign does not exist or
     *         does not belong to the given user
     */
    public CampaignResponse getById(Long id, User owner) {
        Campaign campaign = findOwnedCampaign(id, owner);
        return CampaignResponse.fromWithDetails(campaign);
    }

    /**
     * Update the name and/or status of an existing campaign.
     * Only the fields present on the request are changed, so a caller
     * can update the name, the status, or both in a single call.
     *
     * @throws NotFoundException if the campaign is not found or not owned
     */
    public CampaignResponse update(Long id, CampaignRequest request, User owner) {
        Campaign campaign = findOwnedCampaign(id, owner);
        campaign.setName(request.getName());
        if (request.getStatus() != null) {
            campaign.setStatus(Campaign.Status.valueOf(request.getStatus()));
        }
        Campaign saved = campaignRepository.save(campaign);
        return CampaignResponse.from(saved);
    }

    /**
     * Delete a campaign and cascade to its recipients, messages, and send logs.
     *
     * @throws NotFoundException if the campaign is not found or not owned
     */
    public void delete(Long id, User owner) {
        Campaign campaign = findOwnedCampaign(id, owner);
        campaignRepository.delete(campaign);
    }

    /**
     * Fetches a campaign by id and verifies ownership.
     * Centralizes the ownership check used by all campaign-scoped endpoints.
     *
     * @throws NotFoundException if the campaign does not exist or is not owned
     */
    Campaign findOwnedCampaign(Long id, User owner) {
        return campaignRepository.findByIdAndOwnerId(id, owner.getId())
                .orElseThrow(() -> new NotFoundException(
                        "Campaign not found or does not belong to you"));
    }
}