package com.project.cinemabackend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "payu")
public class PayUProperties {

    @Value("${payu.pos-id}") private String posId;
    @Value("${payu.md5-key}") private String md5Key;
    @Value("${payu.client-id}") private String clientId;
    @Value("${payu.client-secret}") private String clientSecret;
    @Value("${payu.api-url}") private String apiUrl;
    @Value("${payu.continue-url}") private String continueUrl;
    @Value("${payu.notify-url}") private String notifyUrl;
}