package com.university.booking.enums;

import lombok.Getter;

@Getter
public enum RedisKeyPrefix {
    CART("cart:"),
    RATE_LIMIT_PERSON("rate:limit:person:"),
    RATE_LIMIT_IP("rate:limit:ip:");

    private final String prefix;

    RedisKeyPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getKey(String id) {
        return this.prefix + id;
    }
}
