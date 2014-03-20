package org.cloudfoundry.identity.uaa;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource("classpath:/test-override.xml")
public class TestOverrideConfiguration {

}
