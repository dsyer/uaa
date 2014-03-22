
package org.cloudfoundry.identity.uaa.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.cloudfoundry.identity.uaa.UaaConfiguration;
import org.cloudfoundry.identity.uaa.UaaConfiguration.UaaConfigConstructor;
import org.cloudfoundry.identity.uaa.authentication.UaaAuthenticationDetailsSource;
import org.cloudfoundry.identity.uaa.authentication.login.LoginInfoEndpoint;
import org.cloudfoundry.identity.uaa.config.DataSourceConfiguration;
import org.cloudfoundry.identity.uaa.config.YamlConfigurationValidator;
import org.cloudfoundry.identity.uaa.security.web.SecurityFilterChainPostProcessor;
import org.cloudfoundry.identity.uaa.web.ForwardAwareInternalResourceViewResolver;
import org.cloudfoundry.identity.uaa.web.HealthzEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint;
import org.springframework.security.oauth2.provider.vote.ScopeVoter;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;


@Configuration
@Import(DataSourceConfiguration.class)
@ImportResource("classpath:/spring-servlet.xml")
@EnableAutoConfiguration(exclude=SecurityAutoConfiguration.class)
public class UaaApplicationConfiguration {

	@Value("${spring.view.prefix:/WEB-INF/jsp/}")
	private String prefix = "";

	@Value("${spring.view.suffix:}")
	private String suffix = ".jsp";
	
	@Autowired
	private ContentNegotiatingViewResolver contentNegotiatingViewResolver;
	
	@SuppressWarnings("deprecation")
	@PostConstruct
	public void init() {
		// TODO: Upgrade to Jackson 2
		org.springframework.web.servlet.view.json.MappingJacksonJsonView view = new org.springframework.web.servlet.view.json.MappingJacksonJsonView();
		view.setExtractValueFromSingleKeyModel(true);
		contentNegotiatingViewResolver.setDefaultViews(Arrays.<View>asList(view));
	}

	@Bean
	protected YamlConfigurationValidator<UaaConfiguration> yamlConfigurationValidator(
			@Value("${environmentYamlKey}") String yaml) {
		YamlConfigurationValidator<UaaConfiguration> validator = new YamlConfigurationValidator<UaaConfiguration>(
				new UaaConfigConstructor());
		validator.setYaml(yaml);
		return validator;
	}

	@Bean
	protected SecurityFilterChainPostProcessor securityFilterChainPostProcessor(
			@Value("${require_https:false}") boolean requireHttps, @Value("${dump_requests:false}") boolean dumpRequests) {
		SecurityFilterChainPostProcessor processor = new SecurityFilterChainPostProcessor();
		processor.setRequireHttps(requireHttps);
		processor.setDumpRequests(dumpRequests);
		processor.setRedirectToHttps(Arrays.asList("uiSecurity"));
		return processor;
	}
	
	@Bean
	public UaaAuthenticationDetailsSource authenticationDetailsSource() {
		return new UaaAuthenticationDetailsSource();
	}
	
	@Bean
	public OAuth2AuthenticationEntryPoint basicAuthenticationEntryPoint() {
		OAuth2AuthenticationEntryPoint entry = new OAuth2AuthenticationEntryPoint();
		entry.setRealmName("UAA/client");
		entry.setTypeName("Basic");
		return entry;
	}
	
	@Bean
	public OAuth2AuthenticationEntryPoint oauthAuthenticationEntryPoint() {
		OAuth2AuthenticationEntryPoint entry = new OAuth2AuthenticationEntryPoint();
		entry.setRealmName("UAA/oauth");
		return entry;
	}
	
	@Bean
	public AccessDecisionManager accessDecisionManager() {
		@SuppressWarnings("rawtypes")
		List<AccessDecisionVoter> voters = new ArrayList<AccessDecisionVoter>();
		ScopeVoter scopes = new ScopeVoter();
		scopes.setScopePrefix("scope=");
		voters.add(scopes);
		voters.add(new RoleVoter());
		voters.add(new AuthenticatedVoter());
		return new UnanimousBased(voters);
	}
	
	@Bean
	public OAuth2AccessDeniedHandler oauthAccessDeniedHandler() {
		return new OAuth2AccessDeniedHandler();
	}
	
	@Bean
	public Http403ForbiddenEntryPoint http403EntryPoint() {
		return new Http403ForbiddenEntryPoint();
	}
	
	@Bean
	public SimpleUrlLogoutSuccessHandler logoutHandler() {
		SimpleUrlLogoutSuccessHandler handler = new SimpleUrlLogoutSuccessHandler();
		handler.setDefaultTargetUrl("/");
		handler.setTargetUrlParameter("redirect");
		return handler;
	}
	
	@Bean
	public BCryptPasswordEncoder bcryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public InternalResourceViewResolver defaultViewResolver() {
		ForwardAwareInternalResourceViewResolver resolver = new ForwardAwareInternalResourceViewResolver();
		resolver.setPrefix(this.prefix);
		resolver.setSuffix(this.suffix);
		return resolver;
	}
	
	@Bean
	public LoginInfoEndpoint loginInfoEndpoint() {
		return new LoginInfoEndpoint();
	}
	
	@Bean
	public HealthzEndpoint healthzEndpoint() {
		return new HealthzEndpoint();
	}

}
