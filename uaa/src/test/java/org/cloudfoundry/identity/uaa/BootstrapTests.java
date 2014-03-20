/*******************************************************************************
 *     Cloud Foundry 
 *     Copyright (c) [2009-2014] Pivotal Software, Inc. All Rights Reserved.
 *
 *     This product is licensed to you under the Apache License, Version 2.0 (the "License").
 *     You may not use this product except in compliance with the License.
 *
 *     This product includes a number of subcomponents with
 *     separate copyright notices and license terms. Your use of these
 *     subcomponents is subject to the terms and conditions of the
 *     subcomponent's license, as noted in the LICENSE file.
 *******************************************************************************/

package org.cloudfoundry.identity.uaa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.sql.DataSource;

import org.cloudfoundry.identity.uaa.config.YamlServletProfileInitializer;
import org.cloudfoundry.identity.uaa.oauth.ClientAdminBootstrap;
import org.cloudfoundry.identity.uaa.scim.ScimUserProvisioning;
import org.cloudfoundry.identity.uaa.server.GenericNonEmbeddedWebApplicationContext;
import org.cloudfoundry.identity.uaa.server.UaaApplication;
import org.cloudfoundry.identity.uaa.test.TestUtils;
import org.cloudfoundry.identity.uaa.user.JdbcUaaUserDatabase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.ConfigurableWebApplicationContext;

/**
 * @author Dave Syer
 * 
 */
public class BootstrapTests {

	private ConfigurableApplicationContext context;

	@Before
	public void setup() throws Exception {
		System.clearProperty("spring.profiles.active");
	}

	@After
	public void cleanup() throws Exception {
		System.clearProperty("spring.profiles.active");
		System.clearProperty("UAA_CONFIG_PATH");
		if (context != null) {
			if (context.containsBean("scimEndpoints")) {
				TestUtils.deleteFrom(context.getBean("dataSource", DataSource.class),
						"sec_audit");
			}
			context.close();
		}
	}

	@Test
	public void testRootContextDefaults() throws Exception {
		context = getServletContext("hsqldb", UaaApplication.class);
		assertNotNull(context.getBean("userDatabase", JdbcUaaUserDatabase.class));
		FilterChainProxy filterChain = context.getBean(FilterChainProxy.class);
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/Users");
		request.setServletPath("");
		request.setPathInfo("/Users");
		filterChain.doFilter(request, response, new MockFilterChain());
		assertEquals(401, response.getStatus());
	}

	@Test
	public void testOverrideYmlConfigPath() throws Exception {
		System.setProperty("UAA_CONFIG_PATH", "./src/test/resources/test/config");
		context = getServletContext(UaaApplication.class, TestOverrideConfiguration.class);
		assertEquals("/tmp/uaa/logs", context.getBean("foo", String.class));
		assertEquals(
				"[vmc, my, support]",
				ReflectionTestUtils.getField(context.getBean(ClientAdminBootstrap.class),
						"autoApproveClients").toString());
		ScimUserProvisioning users = context.getBean(ScimUserProvisioning.class);
		assertTrue(users.retrieveAll().size() > 0);
	}

	private ConfigurableApplicationContext getServletContext(Object... resources) {
		return getServletContext(null, resources);
	}

	private ConfigurableApplicationContext getServletContext(String profiles,
			Object... resources) {
		SpringApplicationBuilder builder = new SpringApplicationBuilder(resources);
		if (profiles != null) {
			builder.profiles(profiles);
		}
		MockServletContext servletContext = new MockServletContext();
		MockServletConfig servletConfig = new MockServletConfig(servletContext);
		servletConfig.addInitParameter("environmentConfigLocations",
				"file:${UAA_CONFIG_PATH}/uaa.yml");
		// @formatter:off
		return builder
					.contextClass(GenericNonEmbeddedWebApplicationContext.class)
					.initializers(new ServletContextInitializer(servletConfig))
					.listeners(new YamlInitializer())
				.run();
		// @formatter:on
	}
	
	private static class ServletContextInitializer implements
			ApplicationContextInitializer<ConfigurableWebApplicationContext> {

		private ServletContext servletContext;
		private ServletConfig servletConfig;

		public ServletContextInitializer(ServletConfig servletConfig) {
			this.servletContext = servletConfig.getServletContext();
			this.servletConfig = servletConfig;
		}

		@Override
		public void initialize(ConfigurableWebApplicationContext applicationContext) {
			applicationContext.setServletContext(servletContext);
			applicationContext.setServletConfig(servletConfig);
		}
	}

	private static class YamlInitializer implements
			ApplicationListener<ApplicationPreparedEvent> {

		@Override
		public void onApplicationEvent(ApplicationPreparedEvent event) {
			ConfigurableWebApplicationContext context = (ConfigurableWebApplicationContext) event.getApplicationContext();
			new YamlServletProfileInitializer().initialize(context);
		}

	}

	@Configuration
	protected static class TestOverrideConfiguration {
		
		@Value("${logging.path}")
		private String foo;

		@Bean
		public String foo() {
			return foo;
		}

	}

}
