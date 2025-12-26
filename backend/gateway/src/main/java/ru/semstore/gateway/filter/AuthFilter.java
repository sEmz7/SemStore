package ru.semstore.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.semstore.gateway.dto.ErrorResponse;
import ru.semstore.gateway.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;

@Component
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public AuthFilter(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        super(Config.class);
        this.webClient = webClientBuilder
                .baseUrl("lb://USER-SERVICE")
                .build();
        this.objectMapper = objectMapper;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            HttpHeaders headers = exchange.getRequest().getHeaders();
            String auth = headers.getFirst(HttpHeaders.AUTHORIZATION);
            if (auth == null || auth.isBlank()) {
                return handleException(exchange, new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Missing authorization header"));
            }
            String[] parts = auth.trim().split("\\s+");
            if (parts.length != 2 || !"bearer".equals(parts[0].toLowerCase(Locale.ROOT))) {
                return handleException(exchange, new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid authorization format"));
            }
            String token = parts[1];
            return webClient.post()
                    .uri("/auth/validateToken")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::is4xxClientError,
                            resp -> Mono.error(new ResponseStatusException(
                                    HttpStatus.UNAUTHORIZED, "Invalid or expired token"))
                    )
                    .onStatus(
                            HttpStatusCode::is5xxServerError,
                            resp -> Mono.error(new ResponseStatusException(
                                    HttpStatus.SERVICE_UNAVAILABLE, "User service unavailable"))
                    )
                    .bodyToMono(UserDto.class)
                    .timeout(Duration.ofSeconds(2))
                    .onErrorMap(WebClientRequestException.class,
                            ex -> new ResponseStatusException(
                                    HttpStatus.SERVICE_UNAVAILABLE, "User service unreachable"))
                    .map(user -> {
                        ServerHttpRequest.Builder builder = exchange.getRequest().mutate();
                        builder.headers(http -> http.remove("X-User-Id"));
                        builder.header("X-User-Id", String.valueOf(user.getId()));
                        return exchange.mutate().request(builder.build()).build();
                    })
                    .flatMap(chain::filter)
                    .onErrorResume(ex -> handleException(exchange, ex));
        };
    }

    private Mono<Void> handleException(ServerWebExchange exchange, Throwable ex) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "Unexpected error";

        if (ex instanceof ResponseStatusException rse) {
            status = HttpStatus.valueOf(rse.getStatusCode().value());
            if (rse.getReason() != null && !rse.getReason().isBlank()) {
                message = rse.getReason();
            } else {
                message = status.getReasonPhrase();
            }
        }

        ErrorResponse body = new ErrorResponse(message, LocalDateTime.now());

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException e) {
            String fallbackJson = String.format(
                    "{\"message\":\"%s\",\"date\":\"%s\"}",
                    message,
                    LocalDateTime.now()
            );
            bytes = fallbackJson.getBytes(StandardCharsets.UTF_8);
        }

        var response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        var buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {}
}
