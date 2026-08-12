package com.jobintel.common.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@Order(1)
public class RequestLoggingFilter extends OncePerRequestFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String USER_ID_HEADER = "X-User-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();

        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        String userId = request.getHeader(USER_ID_HEADER);
        if (userId == null || userId.isBlank()) {
            userId = "ANONYMOUS";
        }

        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isBlank()) {
            clientIp = request.getRemoteAddr();
        }

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String fullPath = queryString != null ? uri + "?" + queryString : uri;

        MDC.put("correlationId", correlationId);
        MDC.put("userId", userId);
        MDC.put("httpMethod", method);
        MDC.put("httpUri", uri);
        MDC.put("clientIp", clientIp);

        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        log.info("[HTTP INCOMING] {} {} | user={} | clientIp={}", method, fullPath, userId, clientIp);

        try {
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            log.error("[HTTP ERROR] {} {} | error={}", method, fullPath, ex.getMessage(), ex);
            throw ex;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();
            log.info("[HTTP COMPLETED] {} {} | status={} | duration={}ms | user={}",
                    method, fullPath, status, duration, userId);
            MDC.clear();
        }
    }
}
