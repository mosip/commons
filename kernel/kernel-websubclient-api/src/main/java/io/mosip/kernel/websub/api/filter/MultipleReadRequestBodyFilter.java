package io.mosip.kernel.websub.api.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.filter.GenericFilterBean;

import io.mosip.kernel.websub.api.model.MultipleReadHttpRequest;

/**
 * This filter is to convert {@link ServletRequest} to {@link MultipleReadHttpRequest} to override default behavior of spring
 * which is request body can be read only once. 
 * 
 * @author Urvil Joshi
 *
 */
public class MultipleReadRequestBodyFilter extends GenericFilterBean {
	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
			throws IOException, ServletException {
		MultipleReadHttpRequest cachedBodyHttpServletRequest = new MultipleReadHttpRequest(
				(HttpServletRequest) servletRequest);
		chain.doFilter(cachedBodyHttpServletRequest, servletResponse);
	}
}