package main.java.co.sridhar.spring3.controller;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

@Component
public class SimpleCORSFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
	      
	        final HttpServletRequest req = (HttpServletRequest) request;
	        
	        final HttpServletResponse res = (HttpServletResponse) response;	        
	        res.setHeader("Access-Control-Allow-Origin", "*");
	        res.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
	        res.setHeader("Access-Control-Max-Age", "3600");
	        res.setHeader("Access-Control-Allow-Headers", "x-requested-with");
	        chain.doFilter(req, res);
	} 

	@Override
	public void destroy() {}

}
