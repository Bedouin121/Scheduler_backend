package com.optimizely.scheduler.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * A recipient belongs to a single campaign.
 * Deleting the parent campaign cascades here; individual recipients
 * also cascade to their send logs.
 */
@Entity
@Table(name = "recipients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recipient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;
}