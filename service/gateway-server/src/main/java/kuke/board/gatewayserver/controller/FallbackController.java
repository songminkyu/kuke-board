package kuke.board.gatewayserver.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Map;

@RestController
@Slf4j
public class FallbackController {

    @RequestMapping("/contactSupport")
    public Mono<ResponseEntity<Map<String, Object>>> contactSupport(
            ServerWebExchange exchange) {

        // 원본 요청 URI 가져오기
        LinkedHashSet<URI> uris = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR);
        String originalPath = "/unknown";

        if (uris != null && !uris.isEmpty()) {
            originalPath = uris.iterator().next().getPath();
        }

        Map<String, Object> fallbackResponse = Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", 503,
                "error", "Service Unavailable",
                "message", "서비스가 일시적으로 이용 불가능합니다. 잠시 후 다시 시도해주세요.",
                "path", originalPath,
                "service", determineFailedService(originalPath)
        );

        log.error("Circuit breaker activated for original path: {}", originalPath);

        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .header("Content-Type", "application/json")
                .body(fallbackResponse));
    }

    private String determineFailedService(String path) {
        if (path.contains("/v1/articles")) return "게시글 서비스";
        if (path.contains("/v1/article-read")) return "게시글 조회 서비스";
        if (path.contains("/v2/comments")) return "댓글 서비스";
        if (path.contains("/v1/hot-articles")) return "인기 게시글 서비스";
        if (path.contains("/v1/article-likes")) return "좋아요 서비스";
        if (path.contains("/v1/article-views")) return "조회수 서비스";
        return "요청하신 서비스";
    }
}
