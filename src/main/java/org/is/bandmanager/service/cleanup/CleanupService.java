package org.is.bandmanager.service.cleanup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.is.event.EntityEvent;
import org.is.event.EventType;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CleanupService {

    private final List<CleanupStrategy<?, ?>> cleanupStrategies;
    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(cron = "${band-manager.cleanup-interval:0 0 2 * * ?}")
    @Transactional
    public void cleanupAllUnusedEntities() {
        log.info("Starting global cleanup of unused entities...");

        cleanupStrategies.forEach(strategy -> {
            try {
                executeCleanup(strategy);
            } catch (Exception e) {
                log.error("Error during cleanup of {}", strategy.getEntityName(), e);
            }
        });

        log.info("Global cleanup completed");
    }

    private <T, D> CleanupResult executeCleanup(CleanupStrategy<T, D> strategy) {
        log.info("Cleaning up unused {} entities...", strategy.getEntityName());

        var unusedEntities = strategy.findUnusedEntities();

        if (unusedEntities.isEmpty()) {
            log.info("No unused {} entities found", strategy.getEntityName());
            return new CleanupResult(strategy.getEntityName(), 0);
        }

        log.info("Found {} unused {} entities, deleting...", unusedEntities.size(), strategy.getEntityName());

        strategy.deleteEntities(unusedEntities);
        var dtos = strategy.convertToDto(unusedEntities);

        eventPublisher.publishEvent(new EntityEvent<>(EventType.BULK_DELETED, dtos));

        log.info("Successfully deleted {} unused {} entities", unusedEntities.size(), strategy.getEntityName());

        return new CleanupResult(strategy.getEntityName(), unusedEntities.size());
    }

    public record CleanupResult(String entityName, int deletedCount) {
    }

}