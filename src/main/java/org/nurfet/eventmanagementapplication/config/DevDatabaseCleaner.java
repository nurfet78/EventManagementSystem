package org.nurfet.eventmanagementapplication.config;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDatabaseCleaner {
    private final Flyway flyway;

    @PreDestroy
    public void cleanup() {
        flyway.clean();
    }
}
