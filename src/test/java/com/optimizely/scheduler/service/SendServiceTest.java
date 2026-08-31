package com.optimizely.scheduler.service;

import com.optimizely.scheduler.dto.SendLogResponse;
import com.optimizely.scheduler.entity.Campaign;
import com.optimizely.scheduler.entity.Message;
import com.optimizely.scheduler.entity.Recipient;
import com.optimizely.scheduler.entity.User;
import com.optimizely.scheduler.repository.CampaignRepository;
import com.optimizely.scheduler.repository.MessageRepository;
import com.optimizely.scheduler.repository.SendLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link SendService}.
 * Verifies that the manual trigger only processes due, unsent messages,
 * writes exactly one send log per recipient, and flips the sent flag so
 * a second invocation does not resend the same message.
 */
@ExtendWith(MockitoExtension.class)
class SendServiceTest {

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private SendLogRepository sendLogRepository;

    @InjectMocks
    private SendService sendService;

    private User owner;
    private Campaign campaign;
    private Recipient recipient1;
    private Recipient recipient2;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(1L).build();
        campaign = Campaign.builder().id(10L).owner(owner).build();

        recipient1 = Recipient.builder()
                .id(1L).name("Alice").email("alice@example.com").campaign(campaign).build();
        recipient2 = Recipient.builder()
                .id(2L).name("Bob").email("bob@example.com").campaign(campaign).build();
        campaign.setRecipients(List.of(recipient1, recipient2));
    }

    @Test
    void runDueSends_createsOneLogPerRecipientAndFlipsSent() {
        Message dueMessage = dueMessage();

        // Simulate the repository query: it only returns messages that are
        // due and still unsent. After the first run flips the flag, it
        // returns nothing.
        when(campaignRepository.findByOwnerId(1L)).thenReturn(List.of(campaign));
        when(messageRepository.findDueUnsentMessages(anyList(), any(Instant.class)))
                .thenAnswer(inv -> dueMessage.isSent() ? List.of() : List.of(dueMessage));
        when(sendLogRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<SendLogResponse> logs = sendService.runDueSends(owner);

        // Exactly one log per recipient, all marked SENT
        assertEquals(2, logs.size());
        assertTrue(logs.stream().allMatch(log -> "SENT".equals(log.getStatus())));

        // The message is flipped to sent and persisted
        assertTrue(dueMessage.isSent());
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(messageCaptor.capture());
        assertTrue(messageCaptor.getValue().isSent());

        // The query is scoped to the owner's campaign ids
        verify(messageRepository).findDueUnsentMessages(eq(List.of(10L)), any(Instant.class));
    }

    @Test
    void runDueSends_secondCallDoesNotResend() {
        Message dueMessage = dueMessage();

        when(campaignRepository.findByOwnerId(1L)).thenReturn(List.of(campaign));
        when(messageRepository.findDueUnsentMessages(anyList(), any(Instant.class)))
                .thenAnswer(inv -> dueMessage.isSent() ? List.of() : List.of(dueMessage));
        when(sendLogRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<SendLogResponse> firstRun = sendService.runDueSends(owner);
        List<SendLogResponse> secondRun = sendService.runDueSends(owner);

        // First run created logs for both recipients; the second created none
        assertEquals(2, firstRun.size());
        assertEquals(0, secondRun.size());
        assertTrue(dueMessage.isSent());
    }

    @Test
    void runDueSends_ignoresNotYetDueAndAlreadySentMessages() {
        // The repository query filters to due + unsent; the service must
        // only produce logs for whatever that query returns. Here only the
        // due message comes back, so a not-yet-due message is never sent.
        Message dueMessages = dueMessage();
        when(campaignRepository.findByOwnerId(1L)).thenReturn(List.of(campaign));
        when(messageRepository.findDueUnsentMessages(eq(List.of(10L)), any(Instant.class)))
                .thenReturn(List.of(dueMessages));
        when(sendLogRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<SendLogResponse> logs = sendService.runDueSends(owner);

        // Only the due message is sent; the future one is not even considered.
        assertEquals(2, logs.size());
        verify(messageRepository).findDueUnsentMessages(eq(List.of(10L)), any(Instant.class));
    }

    private Message dueMessage() {
        return Message.builder()
                .id(5L)
                .content("Welcome")
                .scheduledTime(Instant.now().minusSeconds(60))
                .campaign(campaign)
                .build();
    }
}
