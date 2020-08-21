package io.mosip.kernel.websub.api.model;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.springframework.util.StreamUtils;

/** This class is extention to {@link HttpServletRequestWrapper} to override default behavior of spring
 * which is request body can be read only once. 
 * 
 * @author Urvil Joshi
 *
 */
public class MultipleReadHttpRequest extends HttpServletRequestWrapper {
    private byte[] cachedBody;
    public MultipleReadHttpRequest(HttpServletRequest request) throws IOException {
    	 super(request);
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
}