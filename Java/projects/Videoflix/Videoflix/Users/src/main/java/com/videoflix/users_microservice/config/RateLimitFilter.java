package com.videoflix.users_microservice.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.time.Duration;

@Component
public class RateLimitFilter implements Filter {

    private final Bucket bucket;

    public RateLimitFilter() {
        Bandwidth limit = Bandwidth.builder()
            .capacity(10)
            .refillGreedy(10, Duration.ofMinutes(1))
            .build();
        this.bucket = Bucket.builder().addLimit(limit).build();
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        if (bucket.tryConsume(1)) {
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
            httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            httpResponse.getWriter().write("Too many requests");
        }
    }
}