package com.project.cinemabackend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.cinemabackend.config.PayUProperties;
import com.project.cinemabackend.dto.payu.PayUNotification;
import com.project.cinemabackend.dto.payu.PayUOrderRequest;
import com.project.cinemabackend.dto.payu.PayUOrderResponse;
import com.project.cinemabackend.dto.payu.PayUTokenResponse;
import com.project.cinemabackend.exception.InvalidSignatureException;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PayUService {

    private final RestTemplate restTemplate;
    private final PayUProperties payUProperties;

    public PayUService(RestTemplate restTemplate, PayUProperties payUProperties) {
        this.restTemplate = restTemplate;
        this.payUProperties = payUProperties;
    }

    public PayUOrderResponse createOrder(PayUOrderRequest orderRequest) {
        String token = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<PayUOrderRequest> entity = new HttpEntity<>(orderRequest, headers);

        String url = payUProperties.getApiUrl() + "/api/v2_1/orders";

        ResponseEntity<PayUOrderResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                PayUOrderResponse.class
        );

        return response.getBody();
    }

    public String getAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", payUProperties.getClientId());
        body.add("client_secret", payUProperties.getClientSecret());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        String url = payUProperties.getApiUrl() + "/pl/standard/user/oauth/authorize";

        ResponseEntity<PayUTokenResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                PayUTokenResponse.class
        );

        return response.getBody() != null ? response.getBody().getAccessToken() : null;
    }

    public boolean verifySignature(String signature, String body, String algorithm) {
        try {
            String data = body + payUProperties.getMd5Key();

            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] digest = md.digest(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString().equalsIgnoreCase(signature);
        } catch (Exception e) {
            return false;
        }
    }

    public PayUNotification validateAndParsePayUNotification (String signatureHeader, String rawBody) throws JsonProcessingException {
        if (signatureHeader == null || signatureHeader.isBlank()) {
            throw new InvalidSignatureException("Missing signature");
        }

        Map<String, String> signatureParts = Arrays.stream(signatureHeader.split(";"))
                .map(p -> p.split("=", 2))
                .collect(Collectors.toMap(p -> p[0], p -> p[1]));

        String signature = signatureParts.get("signature");
        String algorithm = signatureParts.getOrDefault("algorithm", "MD5");

        if (!verifySignature(signature, rawBody, algorithm)) {
            throw new InvalidSignatureException("Invalid signature");
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return mapper.readValue(rawBody, PayUNotification.class);
    }
}