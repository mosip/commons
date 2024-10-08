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
    private byte[] cachedBody;
    private Map<String, String[]> parameterMap;
    public MultipleReadHttpRequest(HttpServletRequest request) throws IOException {
    	 super(request);
        parameterMap = request.getParameterMap();
        InputStream requestInputStream = request.getInputStream();
         this.cachedBody = StreamUtils.copyToByteArray(requestInputStream);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
    	return new MultipleReadServletInputStream(this.cachedBody);
    }

    @Override
    public BufferedReader getReader() throws IOException {
    	ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.cachedBody);
        return new BufferedReader(new InputStreamReader(byteArrayInputStream));
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return this.parameterMap;
    }
}