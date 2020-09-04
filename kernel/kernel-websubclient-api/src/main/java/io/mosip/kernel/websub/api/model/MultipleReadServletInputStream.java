package io.mosip.kernel.websub.api.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;

/**
 * This class is wrapper to modification to {@link ServletInputStream} to
 * convert {@link ServletRequest} to {@link MultipleReadHttpRequest}.
 * 
 * 
 * @author Urvil Joshi
 *
 */
public class MultipleReadServletInputStream extends ServletInputStream {

	private InputStream cachedBodyInputStream;

	public MultipleReadServletInputStream(byte[] cachedBody) {
		this.cachedBodyInputStream = new ByteArrayInputStream(cachedBody);
	}

	@Override
	public boolean isFinished() {
		try {
			return cachedBodyInputStream.available() == 0;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;

	}

	@Override
	public boolean isReady() {
		return true;
	}

	@Override
	public void setReadListener(ReadListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public int read() throws IOException {
		return cachedBodyInputStream.read();
	}

}
