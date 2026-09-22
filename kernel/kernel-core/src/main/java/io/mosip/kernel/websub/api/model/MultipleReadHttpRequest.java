package io.mosip.kernel.websub.api.model;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import org.springframework.util.StreamUtils;

/** This class is extention to {@link HttpServletRequestWrapper} to override default behavior of spring
 * which is request body can be read only once. 
 * 
 * @author Urvil Joshi
 *
 */
public class MultipleReadHttpRequest extends HttpServletRequestWrapper {
    /**
     * Cached body bytes so {@link #getInputStream()} and {@link #getReader()} can replay.
     */
    private byte[] cachedBody;
    /**
     * Snapshot of form/query parameters from the original request.
     */
    private Map<String, String[]> parameterMap;

    /**
     * Copies body bytes and parameter map so later readers see the original request.
     *
     * @param request incoming servlet request
     * @throws IOException if the original body cannot be read
     */
    public MultipleReadHttpRequest(HttpServletRequest request) throws IOException {
    	 super(request);
        parameterMap = request.getParameterMap();
        InputStream requestInputStream = request.getInputStream();
         this.cachedBody = StreamUtils.copyToByteArray(requestInputStream);
    }

    /**
     * Replays the cached body as a servlet input stream.
     *
     * @return stream over the captured request body
     * @throws IOException never thrown; declared to match {@link HttpServletRequest#getInputStream()}
     */
    @Override
    public ServletInputStream getInputStream() throws IOException {
    	return new MultipleReadServletInputStream(this.cachedBody);
    }

    /**
     * Replays the cached body as a character reader (platform default charset).
     *
     * @return reader over the captured request body
     * @throws IOException never thrown; declared to match {@link HttpServletRequest#getReader()}
     */
    @Override
    public BufferedReader getReader() throws IOException {
    	ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.cachedBody);
        return new BufferedReader(new InputStreamReader(byteArrayInputStream));
    }

    /**
     * Returns the parameter map captured at wrap time (not re-parsed from the body).
     *
     * @return original query/form parameter map
     */
    @Override
    public Map<String, String[]> getParameterMap() {
        return this.parameterMap;
    }
}