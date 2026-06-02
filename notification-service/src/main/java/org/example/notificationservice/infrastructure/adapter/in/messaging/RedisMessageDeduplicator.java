package org.example.notificationservice.infrastructure.adapter.in.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisMessageDeduplicator {

    private static final String PROCESSING = "PROCESSING";
    private static final String PROCESSED = "PROCESSED";
    private static final Duration PROCESSING_TTL = Duration.ofMinutes(15);
    private static final Duration PROCESSED_TTL = Duration.ofDays(7);

    private final StringRedisTemplate redisTemplate;

    public boolean claimMessageForProcessing(MessageDeduplicationKey messageKey) {
        return Boolean.TRUE.equals(
                redisTemplate.opsForValue().setIfAbsent(messageKey.asRedisKey(), PROCESSING, PROCESSING_TTL)
        );
    }

    public void rememberMessageAsProcessed(MessageDeduplicationKey messageKey) {
        redisTemplate.opsForValue().set(messageKey.asRedisKey(), PROCESSED, PROCESSED_TTL);
    }

    public void releaseMessageClaim(MessageDeduplicationKey messageKey) {
        try {
            redisTemplate.delete(messageKey.asRedisKey());
        } catch (RuntimeException e) {
            log.warn("Failed to release Redis deduplication key: {}", messageKey, e);
        }
    }
}
