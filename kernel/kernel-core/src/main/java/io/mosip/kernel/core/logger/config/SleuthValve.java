package io.mosip.kernel.core.logger.config;

import brave.Span;
import brave.Tracer;
import org.apache.catalina.Valve;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.http.MimeHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.IOException;

public class SleuthValve extends ValveBase {

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
            Span span = tracer.newTrace();
            addHeader(mimeHeaders, TRACE_ID_NAME, span.context().traceIdString());
            addHeader(mimeHeaders, SPAN_ID_NAME, span.context().spanIdString());
        }
    }

    private static void addHeader(MimeHeaders mimeHeaders,
                                  String traceIdName,
                                  String value) {
        MessageBytes messageBytes = mimeHeaders.addValue(traceIdName);
        messageBytes.setString(value);
    }
}
