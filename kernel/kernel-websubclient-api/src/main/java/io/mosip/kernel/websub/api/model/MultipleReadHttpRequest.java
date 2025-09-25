package io.mosip.kernel.websub.api.model;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import io.mosip.kernel.websub.api.constants.WebSubClientErrorCode;
import io.mosip.kernel.websub.api.exception.WebSubClientException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;

/**
 * Extension of {@link HttpServletRequestWrapper} to enable multiple reads of the request body.
 * <p>
 * Caches the HTTP request body in memory to allow multiple reads, used by
 * {@link io.mosip.kernel.websub.api.filter.MultipleReadRequestBodyFilter} for WebSub callback processing
 * (RFC 7033). Integrates with {@link MultipleReadServletInputStream} to provide a reusable input stream.
 * Limits cached body size to prevent memory issues. Used in components like
 * {@link io.mosip.kernel.websub.api.filter.IntentVerificationFilter} for intent verification.
 * </p>
 *
 * @author Urvil Joshi
 * @since 1.0.0
 * @see MultipleReadServletInputStream
 * @see io.mosip.kernel.websub.api.filter.MultipleReadRequestBodyFilter
 */
public class MultipleReadHttpRequest extends HttpServletRequestWrapper {

    /**
     * Logger for debugging and error reporting.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MultipleReadHttpRequest.class);

    /**
     * Maximum cached body size (1MB).
     */
    private static final int MAX_BODY_SIZE = 1024 * 1024;

    /**
     * Cached request body.
     */
    private final byte[] cachedBody;

    /**
     * Cached parameter map.
     */
    private final Map<String, String[]> parameterMap;

    /**
     * Constructs a wrapper for the given request, caching its body and parameters.
     *
     * @param request the HTTP servlet request
     * @throws IOException if reading the request body fails
     * @throws IllegalArgumentException if the request or its input stream is null
     */
    public MultipleReadHttpRequest(HttpServletRequest request) throws IOException {
        super(request);
        if (request == null) {
            LOGGER.error("Request is null");
            throw new WebSubClientException(WebSubClientErrorCode.IO_ERROR.getErrorCode(),
                    WebSubClientErrorCode.IO_ERROR.getErrorMessage() + ": Request must not be null" );
        }
        this.parameterMap = request.getParameterMap();
        if (request.getInputStream() == null) {
            LOGGER.error("Request input stream is null");
            throw new WebSubClientException(WebSubClientErrorCode.IO_ERROR.getErrorCode(),
                    WebSubClientErrorCode.IO_ERROR.getErrorMessage() + ": Request input stream must not be null" );
        }
        this.cachedBody = StreamUtils.copyToByteArray(request.getInputStream());
        if (cachedBody.length > MAX_BODY_SIZE) {
            LOGGER.error("Request body size {} bytes exceeds maximum {}", cachedBody.length, MAX_BODY_SIZE);
            throw new WebSubClientException(WebSubClientErrorCode.IO_ERROR.getErrorCode(),
                    WebSubClientErrorCode.IO_ERROR.getErrorMessage() + ": Request body size exceeds maximum of" + MAX_BODY_SIZE + " bytes");
        }
        LOGGER.debug("Cached request body of size {} bytes", cachedBody.length);
    }

    /**
     * Returns a reusable input stream for the cached request body.
     *
     * @return the servlet input stream
     * @throws IOException if an I/O error occurs
     */
    @Override
    public ServletInputStream getInputStream() throws IOException {
    	return new MultipleReadServletInputStream(this.cachedBody);
    }

    /**
     * Returns a buffered reader for the cached request body.
     *
     * @return the buffered reader
     * @throws IOException if an I/O error occurs
     */
    @Override
    public BufferedReader getReader() throws IOException {
    	ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.cachedBody);
        return new BufferedReader(new InputStreamReader(byteArrayInputStream));
    }

    /**
     * Returns the cached parameter map.
     *
     * @return the parameter map
     */
    @Override
    public Map<String, String[]> getParameterMap() {
        return this.parameterMap;
    }
}