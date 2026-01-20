package com.project.cinemabackend.dto.payu;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayUOrderResponse {

    @JsonProperty("status")
    private Status status;

    @JsonProperty("redirectUri")
    private String redirectUri;

    @JsonProperty("orderId")
    private String orderId;

    @JsonProperty("extOrderId")
    private String extOrderId;

    @Getter
    @Setter
    public static class Status {
        @JsonProperty("statusCode")
        private String statusCode;

        @JsonProperty("statusDesc")
        private String statusDesc;
    }
}
