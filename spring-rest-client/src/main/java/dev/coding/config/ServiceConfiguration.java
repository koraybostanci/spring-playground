package dev.coding.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.Map;

@Getter
@Validated
@ConfigurationProperties("services")
public class ServiceConfiguration {

    @Valid
    private final ServiceProperties httpBin = new ServiceProperties();
    @Valid
    private final ServiceProperties payments = new ServiceProperties();

    @Getter
    @Setter
    @Validated
    @Component
    public static class ServiceProperties {

        @NotBlank
        private String baseUrl;
        private String version;
        private String accessToken;
        private Map<String, String> paths;

        public String getPath(final String key) {
            return paths.get(key);
        }
    }
}
