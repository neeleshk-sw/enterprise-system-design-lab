package com.enterprise.customer.config;

import com.enterprise.common.constant.Constants;
import com.enterprise.common.util.TraceIdGenerator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Structured per-request logging. Establishes a trace id in the MDC (so every log
 * line on the request thread is correlated), echoes it back as a response header,
 * and emits one summary line with method, URI, status, and execution time.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String traceId = resolveTraceId(request);
        MDC.put(Constants.TRACE_ID, traceId);
        response.setHeader(Constants.TRACE_ID_HEADER, traceId);

        long startNanos = System.nanoTime();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = (System.nanoTime() - startNanos) / 1_000_000;
            log.info("{} {} -> {} ({} ms)",
                    request.getMethod(), request.getRequestURI(), response.getStatus(), durationMs);
            MDC.remove(Constants.TRACE_ID);
        }
    }

    private String resolveTraceId(HttpServletRequest request) {
        String incoming = request.getHeader(Constants.TRACE_ID_HEADER);
        return (incoming != null && !incoming.isBlank()) ? incoming : TraceIdGenerator.generate();
    }
}
