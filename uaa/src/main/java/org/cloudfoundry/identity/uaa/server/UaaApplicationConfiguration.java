
package org.cloudfoundry.identity.uaa.server;

import java.util.Arrays;

import org.cloudfoundry.identity.uaa.UaaConfiguration;
import org.cloudfoundry.identity.uaa.UaaConfiguration.UaaConfigConstructor;
import org.cloudfoundry.identity.uaa.config.DataSourceConfiguration;
import org.cloudfoundry.identity.uaa.config.YamlConfigurationValidator;
import org.cloudfoundry.identity.uaa.security.web.SecurityFilterChainPostProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;


@Configuration
@Import(DataSourceConfiguration.class)
@ImportResource("classpath:/spring-servlet.xml")
@EnableAutoConfiguration(exclude=SecurityAutoConfiguration.class)
public class UaaApplicationConfiguration {

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

}
