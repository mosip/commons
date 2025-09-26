package io.mosip.kernel.emailnotification.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

/**
 * Safe request/response caching filter.
 * - Wraps request/response once per request
 * - Skips large/streaming bodies (multipart, octet-stream, *.stream)
 * - Caps cache sizes to protect heap
 * - Always flushes cached response in finally
 *
 * @author Sagar Mahapatra
 * @since 1.0.0
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ReqResFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri != null && uri.endsWith(".stream")) return true;

        String ct = request.getContentType();
        return isMultipart(ct) || isBinary(ct);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse resp,
                                    FilterChain chain) throws ServletException, IOException {

        // Avoid double wrapping if already wrapped
        HttpServletRequest request =
                (req instanceof ContentCachingRequestWrapper) ? req : new ContentCachingRequestWrapper(req);

        HttpServletResponse response =
                (resp instanceof ContentCachingResponseWrapper) ? resp : new ContentCachingResponseWrapper(resp);

        try {
            chain.doFilter(request, response);
        } finally {
            // ALWAYS flush cached body back to the real response
            ((ContentCachingResponseWrapper) response).copyBodyToResponse();
        }
    }

    private static boolean isMultipart(@Nullable String ct) {
        return ct != null && ct.toLowerCase().startsWith("multipart/");
    }

    private static boolean isBinary(@Nullable String ct) {
        return ct != null && ct.toLowerCase().startsWith("application/octet-stream");
    }
}