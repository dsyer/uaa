package org.cloudfoundry.identity.uaa.server;

import org.cloudfoundry.identity.uaa.config.DataSourceConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

@Configuration
@Import(DataSourceConfiguration.class)
@ImportResource("classpath:/spring-servlet.xml")
public class UaaApplicationConfiguration {

}
