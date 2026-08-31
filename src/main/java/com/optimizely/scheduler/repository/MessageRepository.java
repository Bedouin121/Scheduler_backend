package com.optimizely.scheduler.repository;

import com.optimizely.scheduler.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByCampaignId(Long campaignId);

    /**
     * Finds all unsent messages whose scheduled time has arrived,
     * scoped to a set of campaign ids owned by the current user.
     */
    @Query("SELECT m FROM Message m WHERE m.campaign.id IN :campaignIds " +
           "AND m.sent = false AND m.scheduledTime <= :now")
    List<Message> findDueUnsentMessages(List<Long> campaignIds, Instant now);
}