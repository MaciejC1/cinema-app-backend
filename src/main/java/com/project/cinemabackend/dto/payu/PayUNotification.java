package com.project.cinemabackend.dto.payu;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter

public class PayUNotification {

    @JsonProperty("order")
    private Order order;

    @JsonProperty("localReceiptDateTime")
    private String localReceiptDateTime;

    @JsonProperty("properties")
    private List<Property> properties;


    @Getter
    @Setter
    public static class Order {
        @JsonProperty("orderId")
        private String orderId;

        @JsonProperty("extOrderId")
        private String extOrderId;

        @JsonProperty("orderCreateDate")
        private String orderCreateDate;

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

        @JsonProperty("buyer")
        private Buyer buyer;

        @JsonProperty("products")
        private List<Product> products;

        @JsonProperty("status")
        private String status;
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


    @Getter
    @Setter
    public static class Property {
        @JsonProperty("name")
        private String name;

        @JsonProperty("value")
        private String value;
    }
}
