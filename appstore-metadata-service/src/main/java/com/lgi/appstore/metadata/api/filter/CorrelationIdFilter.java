package com.lgi.appstore.metadata.api.filter;

import org.apache.logging.log4j.util.Strings;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String X_REQUEST_ID_HEADER_NAME = "x-request-id";
    private static final String CORRELATION_ID = "correlationId";

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest,
                                    HttpServletResponse httpServletResponse,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String correlationId = httpServletRequest.getHeader(X_REQUEST_ID_HEADER_NAME);
        if (Strings.isBlank(correlationId)) {
            correlationId = generateUniqueCorrelationId();
        }
        MDC.put(CORRELATION_ID, correlationId);
        httpServletResponse.addHeader(X_REQUEST_ID_HEADER_NAME, correlationId);
        try {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        } finally {
            MDC.remove(CORRELATION_ID);
        }
    }

    private String generateUniqueCorrelationId() {
        return UUID.randomUUID().toString();
    }
}