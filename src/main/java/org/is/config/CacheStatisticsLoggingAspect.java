package org.is.config;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class CacheStatisticsLoggingAspect {

    private final EntityManagerFactory entityManagerFactory;

    @Value("${band-manager.cache.stats-logging-enabled:false}")
    private boolean statsLoggingEnabled;

    @Pointcut("execution(* org.is..repository..*(..))")
    public void repositoryLayer() {
        // pointcut marker
    }

    @Around("repositoryLayer()")
    public Object logCacheStatistics(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!statsLoggingEnabled) {
            return joinPoint.proceed();
        }

        Statistics statistics = getStatistics();
        long hitsBefore = statistics.getSecondLevelCacheHitCount();
        long missesBefore = statistics.getSecondLevelCacheMissCount();
        long putsBefore = statistics.getSecondLevelCachePutCount();
        long queriesBefore = statistics.getQueryExecutionCount();

        Object result = joinPoint.proceed();

        long hitDelta = statistics.getSecondLevelCacheHitCount() - hitsBefore;
        long missDelta = statistics.getSecondLevelCacheMissCount() - missesBefore;
        long putDelta = statistics.getSecondLevelCachePutCount() - putsBefore;
        long queryDelta = statistics.getQueryExecutionCount() - queriesBefore;

        if (hitDelta != 0 || missDelta != 0 || putDelta != 0) {
            log.info("L2 cache stats [{}]: hits={}, misses={}, puts={}, queries={}",
                    joinPoint.getSignature().toShortString(), hitDelta, missDelta, putDelta, queryDelta);
        }

        return result;
    }

    private Statistics getStatistics() {
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        Statistics statistics = sessionFactory.getStatistics();
        statistics.setStatisticsEnabled(true);
        return statistics;
    }
}
