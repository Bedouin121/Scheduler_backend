package com.optimizely.scheduler.repository;

import com.optimizely.scheduler.entity.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    List<Campaign> findByOwnerId(Long ownerId);

    Optional<Campaign> findByIdAndOwnerId(Long id, Long ownerId);
}