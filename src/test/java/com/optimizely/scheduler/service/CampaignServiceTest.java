package com.optimizely.scheduler.service;

import com.optimizely.scheduler.dto.CampaignRequest;
import com.optimizely.scheduler.dto.CampaignResponse;
import com.optimizely.scheduler.entity.Campaign;
import com.optimizely.scheduler.entity.User;
import com.optimizely.scheduler.repository.CampaignRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CampaignService}.
 * Verifies that creating a campaign assigns the correct owner and
 * default status, and that listing scopes results to the current user.
 */
@ExtendWith(MockitoExtension.class)
class CampaignServiceTest {

    @Mock
    private CampaignRepository campaignRepository;

    @InjectMocks
    private CampaignService campaignService;

    private User owner;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(1L).email("demo@example.com").build();
    }

    @Test
    void create_assignsOwnerAndDefaultDraftStatus() {
        CampaignRequest request = new CampaignRequest("Product Launch", null);
        when(campaignRepository.save(any(Campaign.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CampaignResponse response = campaignService.create(request, owner);

        ArgumentCaptor<Campaign> captor = ArgumentCaptor.forClass(Campaign.class);
        verify(campaignRepository).save(captor.capture());

        Campaign saved = captor.getValue();
        assertEquals("Product Launch", saved.getName());
        assertEquals(owner, saved.getOwner());
        assertEquals(Campaign.Status.DRAFT, saved.getStatus());
        assertEquals("DRAFT", response.getStatus());
    }

    @Test
    void create_appliesProvidedStatusWhenGiven() {
        CampaignRequest request = new CampaignRequest("Active Launch", "ACTIVE");
        when(campaignRepository.save(any(Campaign.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CampaignResponse response = campaignService.create(request, owner);

        ArgumentCaptor<Campaign> captor = ArgumentCaptor.forClass(Campaign.class);
        verify(campaignRepository).save(captor.capture());
        assertEquals(Campaign.Status.ACTIVE, captor.getValue().getStatus());
        assertEquals("ACTIVE", response.getStatus());
    }

    @Test
    void listByOwner_returnsOnlyCurrentUsersCampaigns() {
        Campaign owned = Campaign.builder().id(1L).name("Mine").owner(owner).build();
        when(campaignRepository.findByOwnerId(1L)).thenReturn(List.of(owned));

        List<CampaignResponse> result = campaignService.listByOwner(owner);

        assertEquals(1, result.size());
        assertEquals("Mine", result.get(0).getName());
        assertNull(result.get(0).getRecipients());
        verify(campaignRepository).findByOwnerId(1L);
    }
}
