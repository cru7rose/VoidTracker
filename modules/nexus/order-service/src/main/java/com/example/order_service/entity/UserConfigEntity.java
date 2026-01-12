package com.example.order_service.entity;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "vt_user_configs")
@IdClass(UserConfigId.class)
public class UserConfigEntity {

    @Id
    private String userId;

    @Id
    @Column(name = "config_key")
    private String key;

    @Column(columnDefinition = "TEXT")
    private String value;

    public UserConfigEntity() {
    }

    public UserConfigEntity(String userId, String key, String value) {
        this.userId = userId;
        this.key = key;
        this.value = value;
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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
