package org.cloudfoundry.identity.uaa.server;


import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration.Dynamic;

import org.cloudfoundry.identity.uaa.config.YamlServletProfileInitializer;
import org.springframework.util.ClassUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.support.AbstractDispatcherServletInitializer;

/**
 * @author Dave Syer
 * 
 */
public class ServletInitializer extends AbstractDispatcherServletInitializer {

	@Override
	protected WebApplicationContext createServletApplicationContext() {
		AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
		context.scan(ClassUtils.getPackageName(getClass()));
		return context;
	}

	@Override
	protected String[] getServletMappings() {
		return new String[] { "/" };
	}

	@Override
	protected WebApplicationContext createRootApplicationContext() {
		return null;
	}
	
	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		super.onStartup(servletContext);
		DelegatingFilterProxy filter = new DelegatingFilterProxy("springSecurityFilterChain");
		filter.setContextAttribute("org.springframework.web.servlet.FrameworkServlet.CONTEXT.dispatcher");
		servletContext.addFilter("springSecurityFilterChain", filter).addMappingForUrlPatterns(null, false, "/*");
	}
	
	@Override
	protected void customizeRegistration(Dynamic registration) {
		registration.setInitParameter("contextInitializerClasses", YamlServletProfileInitializer.class.getName());
		registration.setInitParameter("environmentConfigDefaults", "uaa.yml");
		registration.setInitParameter("environmentConfigLocations", "${UAA_CONFIG_URL},file:${UAA_CONFIG_PATH}/uaa.yml,file:${CLOUD_FOUNDRY_CONFIG_PATH}/uaa.yml");	
	}
	
}
