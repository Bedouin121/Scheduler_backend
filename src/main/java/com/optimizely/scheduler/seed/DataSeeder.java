package com.optimizely.scheduler.seed;

import com.optimizely.scheduler.entity.Campaign;
import com.optimizely.scheduler.entity.Message;
import com.optimizely.scheduler.entity.Recipient;
import com.optimizely.scheduler.entity.User;
import com.optimizely.scheduler.repository.CampaignRepository;
import com.optimizely.scheduler.repository.MessageRepository;
import com.optimizely.scheduler.repository.RecipientRepository;
import com.optimizely.scheduler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

/**
 * Seeds a demo dataset on startup so the application is immediately
 * usable after a fresh deploy. Only runs when the users table is empty,
 * so it never duplicates data across restarts.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CampaignRepository campaignRepository;
    private final RecipientRepository recipientRepository;
    private final MessageRepository messageRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("DataSeeder skipped, users already present");
            return;
        }

        log.info("DataSeeder seeding demo data");

        // Demo user with a known password
        User demoUser = userRepository.save(User.builder()
                .email("demo@example.com")
                .password(passwordEncoder.encode("password"))
                .build());

        // A single demo campaign owned by the demo user
        Campaign campaign = campaignRepository.save(Campaign.builder()
                .name("Product Launch")
                .status(Campaign.Status.ACTIVE)
                .owner(demoUser)
                .build());

        // A handful of fake recipients
        Recipient r1 = recipientRepository.save(Recipient.builder()
                .name("Alice Johnson").email("alice@example.com").campaign(campaign).build());
        Recipient r2 = recipientRepository.save(Recipient.builder()
                .name("Bob Smith").email("bob@example.com").campaign(campaign).build());
        Recipient r3 = recipientRepository.save(Recipient.builder()
                .name("Carol Nguyen").email("carol@example.com").campaign(campaign).build());
        recipientRepository.save(Recipient.builder()
                .name("David Kim").email("david@example.com").campaign(campaign).build());

        // One message scheduled a few minutes in the past so the manual
        // send trigger has something to act on immediately after deploy.
        messageRepository.save(Message.builder()
                .content("Welcome to Product Launch, here is what you need to know.")
                .scheduledTime(Instant.now().minus(Duration.ofMinutes(5)))
                .campaign(campaign)
                .build());

        log.info("DataSeeder complete: user={}, campaign={}, recipients=4, message=1",
                demoUser.getEmail(), campaign.getName());
    }
}
