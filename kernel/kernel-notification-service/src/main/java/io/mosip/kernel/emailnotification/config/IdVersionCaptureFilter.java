package io.mosip.kernel.emailnotification.config;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1) // after ReqResFilter
public class IdVersionCaptureFilter extends OncePerRequestFilter {

    private static final JsonFactory JSON_FACTORY = new JsonFactory();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String ct = request.getContentType();
        return ct == null || !ct.toLowerCase().startsWith("application/json");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        // ReqResFilter already wrapped; if not, wrap lightly here
        ContentCachingRequestWrapper cReq = (req instanceof ContentCachingRequestWrapper)
                ? (ContentCachingRequestWrapper) req
                : new ContentCachingRequestWrapper(req, 64 * 1024);

        byte[] buf = cReq.getContentAsByteArray();
        if (buf.length > 0) {
            String id = null, ver = null;
            try (var p = JSON_FACTORY.createParser(buf)) {
                while (p.nextToken() != null) {
                    if (p.currentToken() == JsonToken.FIELD_NAME) {
                        String name = p.getCurrentName();
                        if ("id".equals(name))     { p.nextToken(); id = p.getValueAsString(); }
                        else if ("version".equals(name)) { p.nextToken(); ver = p.getValueAsString(); }
                        if (id != null && ver != null) break;
                    }
                }
            } catch (Exception ignored) {
                // best-effort; don't block the request
            }
            if (id != null)  cReq.setAttribute("REQ_ID", id);
            if (ver != null) cReq.setAttribute("REQ_VER", ver);
        }

        chain.doFilter(cReq, res);
    }
}
