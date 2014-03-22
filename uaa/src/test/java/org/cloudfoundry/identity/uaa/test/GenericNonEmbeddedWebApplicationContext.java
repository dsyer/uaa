package org.cloudfoundry.identity.uaa.test;

import javax.servlet.ServletConfig;

import org.springframework.web.context.support.GenericWebApplicationContext;

public class GenericNonEmbeddedWebApplicationContext extends GenericWebApplicationContext {

	private ServletConfig servletConfig;

	@Override
	public void setServletConfig(ServletConfig servletConfig) {
		this.servletConfig = servletConfig;
	}

	@Override
	public ServletConfig getServletConfig() {
		return this.servletConfig;
	}


}