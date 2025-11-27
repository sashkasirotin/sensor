package com.sensor.services.security.filter;

import com.sensor.services.security.implementations.JwtApplicationService;
import io.github.bucket4j.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimiterFilter implements Filter {

    @Autowired
    private JwtApplicationService jwtService;

    // Stores a separate bucket for each username
    private final Map<String, Bucket> userBuckets = new ConcurrentHashMap<>();

    private Bucket createBucket() {
        return Bucket4j.builder()
                .addLimit(
                        Bandwidth.classic(
                                10,
                                Refill.intervally(10, Duration.ofMinutes(1)) // …per minute
                        )
                )
                .build();
    }

    private Bucket resolveBucket(String username) {
        return userBuckets.computeIfAbsent(username, u -> createBucket());
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest http = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String username = "anonymous";

        try {
            String auth = http.getHeader("Authorization");

            if (auth != null && auth.startsWith("Bearer ")) {
                String token = auth.substring(7);
                username = jwtService.extractUsername(token);
            }
        } catch (Exception ignored) {
            // if token invalid → keep username = "anonymous"
        }

        Bucket bucket = resolveBucket(username);

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            res.setStatus(429);
            res.setContentType("application/json");
            res.getWriter().write("""
                    {
                      "success": false,
                      "message": "Rate limit exceeded",
                      "errorCode": "RATE_LIMIT",
                      "data": null
                    }
                    """);
        }
    }
}
