package ru.semstore.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import ru.semstore.gateway.dto.UserDto;

import java.time.Duration;
import java.util.Locale;

@Component
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {

    private final WebClient webClient;

    public AuthFilter(WebClient.Builder webClientBuilder) {
        super(Config.class);
        this.webClient = webClientBuilder
                .baseUrl("lb://USER-SERVICE")
                .build();
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            HttpHeaders headers = exchange.getRequest().getHeaders();
            String auth = headers.getFirst(HttpHeaders.AUTHORIZATION);
            if (auth == null || auth.isBlank()) {
                return Mono.error(new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Missing authorization header"));
            }
            String[] parts = auth.trim().split("\\s+");
            if (parts.length != 2 || !"bearer".equals(parts[0].toLowerCase(Locale.ROOT))) {
                return Mono.error(new ResponseStatusException(
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
                    .flatMap(chain::filter);
        };
    }

    public static class Config {}
}
