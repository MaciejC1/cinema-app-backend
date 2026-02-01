package com.project.cinemabackend.config;

import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Duration;

@Configuration
@Profile("!test")
public class OpenAiConfig {
    @Value("${gpt.api-key}") String key;

    @Bean
    public OpenAiService openAiService() {
        return new OpenAiService(key, Duration.ofSeconds(120));
    }
}
