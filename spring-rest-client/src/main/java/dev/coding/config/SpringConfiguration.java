package dev.coding.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties({
        ServiceConfiguration.class
})
@Configuration
public class SpringConfiguration {
}
