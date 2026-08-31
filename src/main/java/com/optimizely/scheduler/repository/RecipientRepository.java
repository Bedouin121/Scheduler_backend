package com.optimizely.scheduler.repository;

import com.optimizely.scheduler.entity.Recipient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipientRepository extends JpaRepository<Recipient, Long> {

    List<Recipient> findByCampaignId(Long campaignId);
}