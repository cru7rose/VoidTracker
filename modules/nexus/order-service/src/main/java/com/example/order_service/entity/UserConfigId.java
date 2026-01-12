package com.example.order_service.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

public class UserConfigId implements Serializable {
    private String userId;
    private String key;

    public UserConfigId() {
    }

    public UserConfigId(String userId, String key) {
        this.userId = userId;
        this.key = key;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
