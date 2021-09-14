package dev.coding.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.Map;

@Getter
@Validated
@ConfigurationProperties("rest-clients")
public class RestClientConfiguration {

    @Valid
    private final Properties httpBin = new Properties();

    @Getter
    @Setter
    @Validated
    public static class Properties {

        @NotBlank
        private String baseUrl;
        private Map<String, String> paths;

        public String getPath(final String key) {
            return paths.get(key);
        }
    }
}
