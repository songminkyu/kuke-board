package kuke.board.articleread.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;
import java.util.Map;

/**
 * Redis 캐시 설정을 위한 구성 클래스
 * 애플리케이션에서 캐싱 기능을 활성화하고 Redis 캐시 매니저를 설정합니다.
 */
@Configuration // Spring 설정 클래스임을 나타냄
@EnableCaching // Spring 애플리케이션에서 캐싱 기능 활성화
public class CacheConfig {

    /**
     * Redis 캐시 매니저 빈을 생성합니다.
     * 
     * @param connectionFactory Redis 서버와의 연결을 관리하는 팩토리
     * @return 구성된 RedisCacheManager 인스턴스
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        return RedisCacheManager.builder(connectionFactory)
                .withInitialCacheConfigurations(
                        Map.of(
                                // "articleViewCount" 캐시에 대해 1초의 TTL(Time To Live) 설정
                                // 이는 게시물 조회수와 같이 자주 업데이트되는 데이터에 적합
                                "articleViewCount", RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofSeconds(1))
                        )
                )
                .build();
    }
}