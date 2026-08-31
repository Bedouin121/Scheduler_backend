package com.optimizely.scheduler.repository;

import com.optimizely.scheduler.entity.SendLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SendLogRepository extends JpaRepository<SendLog, Long> {

    /**
     * Returns all send logs for campaigns owned by the given user,
     * ordered from most recent to oldest.
     */
    @Query("SELECT sl FROM SendLog sl WHERE sl.message.campaign.owner.id = :ownerId " +
           "ORDER BY sl.timestamp DESC")
    List<SendLog> findByOwnerIdOrderDesc(Long ownerId);
}