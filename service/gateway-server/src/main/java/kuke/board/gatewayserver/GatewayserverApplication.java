package kuke.board.gatewayserver;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;

@SpringBootApplication
public class GatewayserverApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayserverApplication.class, args);
	}

	@Bean
	public RouteLocator kukeBoardRouteConfig(RouteLocatorBuilder routeLocatorBuilder) {
		return routeLocatorBuilder.routes()
					.route(p -> p
						.path("/v1/articles/**")
						.filters( f -> f.rewritePath("/v1/articles/(?<segment>.*)","/v1/articles/${segment}")
								.addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
								.circuitBreaker(config -> config.setName("articlesCircuitBreaker")
										.setFallbackUri("forward:/contactSupport")))
						.uri("lb://kuke-board-article-service"))
					.route(p -> p
						.path("/v1/article-read/**")
						.filters( f -> f.rewritePath("/v1/article-read/(?<segment>.*)","/v1/article-read/${segment}")
								.addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
								.circuitBreaker(config -> config.setName("articleReadCircuitBreaker")
										.setFallbackUri("forward:/contactSupport")))
						.uri("lb://kuke-board-article-read-service"))
					.route(p -> p
						.path("/v2/comments/**")
						.filters( f -> f.rewritePath("/v2/comments/(?<segment>.*)","/v2/comments/${segment}")
								.addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
								.circuitBreaker(config -> config.setName("commentCircuitBreaker")
										.setFallbackUri("forward:/contactSupport")))
						.uri("lb://kuke-board-comment-service"))
					.route(p -> p
						.path("/v1/hot-articles/**")
						.filters( f -> f.rewritePath("/v1/hot-articles/(?<segment>.*)","/v1/hot-articles/${segment}")
								.addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
								.circuitBreaker(config -> config.setName("hotArticlesCircuitBreaker")
										.setFallbackUri("forward:/contactSupport")))
						.uri("lb://kuke-board-hot-article-service"))
					.route(p -> p
						.path("/v1/article-likes/**")
						.filters( f -> f.rewritePath("/v1/article-likes/(?<segment>.*)","/v1/article-likes/${segment}")
								.addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
								.circuitBreaker(config -> config.setName("articleLikesCircuitBreaker")
										.setFallbackUri("forward:/contactSupport")))
						.uri("lb://kuke-board-like-service"))
					.route(p -> p
						.path("/v1/article-views/**")
						.filters( f -> f.rewritePath("/v1/article-views/(?<segment>.*)","/v1/article-views/${segment}")
								.addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
								.circuitBreaker(config -> config.setName("articleViewsCircuitBreaker")
										.setFallbackUri("forward:/contactSupport")))
						.uri("lb://kuke-board-view-service")).build();
	}

	@Bean
	public RedisRateLimiter redisRateLimiter() {
		return new RedisRateLimiter(1, 1, 1);
	}

	@Bean
	KeyResolver userKeyResolver() {
		return exchange -> Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst("user"))
				.defaultIfEmpty("anonymous");
	}

}
