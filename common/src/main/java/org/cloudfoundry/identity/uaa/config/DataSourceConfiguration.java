package org.cloudfoundry.identity.uaa.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;


@Configuration
@ImportResource(value={ "classpath:/spring/env.xml", "classpath:/spring/data-source.xml" })
public class DataSourceConfiguration {

}
