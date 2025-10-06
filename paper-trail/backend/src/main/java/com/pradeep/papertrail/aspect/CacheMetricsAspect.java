package com.pradeep.papertrail.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Aspect
@Component
public class CacheMetricsAspect {

    private static final Logger logger = LoggerFactory.getLogger(CacheMetricsAspect.class);

    // Metrics storage
    private final ConcurrentHashMap<String, AtomicLong> cacheHits = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> cacheMisses = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> cacheTimes = new ConcurrentHashMap<>();

    /**
     * Monitor all @Cacheable method executions
     */
    @Around("@annotation(org.springframework.cache.annotation.Cacheable)")
    public Object monitorCacheableMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;

            // Track execution time
            cacheTimes.computeIfAbsent(methodName, k -> new AtomicLong()).addAndGet(executionTime);

            // Heuristic: If execution was fast (< 5ms), likely a cache hit
            if (executionTime < 5) {
                cacheHits.computeIfAbsent(methodName, k -> new AtomicLong()).incrementAndGet();
                logger.debug("Cache HIT for {} in {}ms", methodName, executionTime);
            } else {
                cacheMisses.computeIfAbsent(methodName, k -> new AtomicLong()).incrementAndGet();
                logger.debug("Cache MISS for {} in {}ms", methodName, executionTime);
            }

            return result;
        } catch (Throwable e) {
            logger.error("Error in cached method {}: {}", methodName, e.getMessage());
            throw e;
        }
    }

    /**
     * Monitor all @CacheEvict method executions
     */
    @Around("@annotation(org.springframework.cache.annotation.CacheEvict)")
    public Object monitorCacheEvictMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;

            logger.debug("Cache EVICT for {} in {}ms", methodName, executionTime);

            return result;
        } catch (Throwable e) {
            logger.error("Error in cache evict method {}: {}", methodName, e.getMessage());
            throw e;
        }
    }

    /**
     * Get cache hit rate for a specific method
     */
    public double getCacheHitRate(String methodName) {
        long hits = cacheHits.getOrDefault(methodName, new AtomicLong()).get();
        long misses = cacheMisses.getOrDefault(methodName, new AtomicLong()).get();
        long total = hits + misses;

        return total == 0 ? 0.0 : (double) hits / total * 100;
    }

    /**
     * Get average execution time
     */
    public double getAverageExecutionTime(String methodName) {
        long totalTime = cacheTimes.getOrDefault(methodName, new AtomicLong()).get();
        long totalCalls = cacheHits.getOrDefault(methodName, new AtomicLong()).get()
                + cacheMisses.getOrDefault(methodName, new AtomicLong()).get();

        return totalCalls == 0 ? 0.0 : (double) totalTime / totalCalls;
    }

    /**
     * Get all metrics
     */
    public ConcurrentHashMap<String, Object> getAllMetrics() {
        ConcurrentHashMap<String, Object> metrics = new ConcurrentHashMap<>();

        cacheHits.keySet().forEach(methodName -> {
            ConcurrentHashMap<String, Object> methodMetrics = new ConcurrentHashMap<>();
            methodMetrics.put("hits", cacheHits.get(methodName).get());
            methodMetrics.put("misses", cacheMisses.getOrDefault(methodName, new AtomicLong()).get());
            methodMetrics.put("hitRate", getCacheHitRate(methodName));
            methodMetrics.put("avgExecutionTime", getAverageExecutionTime(methodName));

            metrics.put(methodName, methodMetrics);
        });

        return metrics;
    }

    /**
     * Reset all metrics
     */
    public void resetMetrics() {
        cacheHits.clear();
        cacheMisses.clear();
        cacheTimes.clear();
        logger.info("Cache metrics reset");
    }
}
