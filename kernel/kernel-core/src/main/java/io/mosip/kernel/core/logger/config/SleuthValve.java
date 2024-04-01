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

public class SleuthValve extends AccessLogValve {

    private Logger logger = LoggerFactory.getLogger(SleuthValve.class);
    private static final String TRACE_ID_NAME = "X-B3-TraceId";
    private static final String SPAN_ID_NAME = "X-B3-SpanId";
    private final Tracer tracer;

    public SleuthValve(Tracer tracer) {
        this.tracer = tracer;
    }

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

    private static void addHeader(MimeHeaders mimeHeaders,
                                  String traceIdName,
                                  String value) {
        MessageBytes messageBytes = mimeHeaders.addValue(traceIdName);
        messageBytes.setString(value);
    }
}
