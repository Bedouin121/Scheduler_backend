package com.optimizely.scheduler.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * A scheduled message associated with a campaign.
 * The send trigger checks scheduledTime and the sent flag
 * to determine which messages are ready for delivery.
 */
@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Instant scheduledTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @Column(nullable = false)
    @Builder.Default
    private boolean sent = false;
}