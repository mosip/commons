package io.mosip.kernel.core.logger.config;

import java.io.IOException;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Valve;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.AccessLogValve;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.http.MimeHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.ServletException;

/**
 * Tomcat {@link AccessLogValve} that injects B3 {@code X-B3-TraceId} and
 * {@code X-B3-SpanId} headers when the inbound request omitted them.
 * <p>
 * Contract: installed by {@link SleuthLoggingAutoConfiguration}. Mutates the
 * Coyote request headers before invoking the next valve. Does not perform
 * HTTP itself.
 * </p>
 *
 * @see SleuthLoggingAutoConfiguration
 */
public class SleuthValve extends AccessLogValve {

    /**
     * Logger for valve diagnostics.
     */
    private Logger logger = LoggerFactory.getLogger(SleuthValve.class);
    /**
     * B3 header name for the trace identifier.
     */
    private static final String TRACE_ID_NAME = "X-B3-TraceId";
    /**
     * B3 header name for the span identifier.
     */
    private static final String SPAN_ID_NAME = "X-B3-SpanId";
    /**
     * Micrometer tracer used to open a span when headers are missing.
     */
    private final Tracer tracer;

    /**
     * Constructs a valve bound to the given tracer.
     *
     * @param tracer never-null Micrometer tracer
     */
    public SleuthValve(Tracer tracer) {
        this.tracer = tracer;
    }

    /**
     * Enriches missing B3 headers then delegates to the next valve.
     *
     * @param request  never-null Tomcat request
     * @param response never-null Tomcat response
     * @throws IOException      when the next valve fails with I/O
     * @throws ServletException when the next valve fails
     */
    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        enrichWithSleuthHeaderWhenMissing(request);
        Valve next = getNext();
        if (null == next) {
            // no next valve
            return;
        }
        next.invoke(request, response);
    }

    /**
     * Adds B3 trace and span headers when {@code X-B3-TraceId} is absent.
     *
     * @param request never-null Tomcat request whose Coyote headers may be mutated
     */
    private void enrichWithSleuthHeaderWhenMissing(Request request) {
        String header = request.getHeader(TRACE_ID_NAME);
        if (null == header) {
            org.apache.coyote.Request coyoteRequest = request.getCoyoteRequest();
            MimeHeaders mimeHeaders = coyoteRequest.getMimeHeaders();
            Span span = tracer.nextSpan();
            addHeader(mimeHeaders, TRACE_ID_NAME, span.context().traceId());
            addHeader(mimeHeaders, SPAN_ID_NAME, span.context().spanId());
        }
    }

    /**
     * Writes a header value onto the Coyote mime-header set.
     *
     * @param mimeHeaders never-null Coyote mime headers
     * @param traceIdName never-null header name
     * @param value       never-null header value
     */
    private static void addHeader(MimeHeaders mimeHeaders,
                                  String traceIdName,
                                  String value) {
        MessageBytes messageBytes = mimeHeaders.addValue(traceIdName);
        messageBytes.setString(value);
    }
}
