package org.cloudfoundry.identity.uaa.server;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource("classpath:/spring-servlet.xml")
public class UaaApplication {

}
