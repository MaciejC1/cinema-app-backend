package com.project.cinemabackend.dto.payu;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class PayUOrderRequest {

    @JsonProperty("continueUrl")
    private String continueUrl;

    @JsonProperty("notifyUrl")
    private String notifyUrl;

    @JsonProperty("customerIp")
    private String customerIp;

    @JsonProperty("merchantPosId")
    private String merchantPosId;

    @JsonProperty("description")
    private String description;

    @JsonProperty("currencyCode")
    private String currencyCode;

    @JsonProperty("totalAmount")
    private String totalAmount;

    @JsonProperty("extOrderId")
    private String extOrderId;

    @JsonProperty("buyer")
    private Buyer buyer;

    @JsonProperty("products")
    private List<Product> products;

    @JsonProperty("validityTime")
    private String validityTime;

    public PayUOrderRequest() {
        this.currencyCode = "PLN";
        this.validityTime = "120"; // 2 min
    }


    @Getter
    @Setter
    public static class Buyer {
        @JsonProperty("email")
        private String email;

        @JsonProperty("phone")
        private String phone;

        @JsonProperty("firstName")
        private String firstName;

        @JsonProperty("lastName")
        private String lastName;

        @JsonProperty("language")
        private String language;

        public Buyer() {
            this.language = "pl";
        }
    }

    @Getter
    @Setter
    public static class Product {
        @JsonProperty("name")
        private String name;

        @JsonProperty("unitPrice")
        private String unitPrice;

        @JsonProperty("quantity")
        private String quantity;
    }
}
