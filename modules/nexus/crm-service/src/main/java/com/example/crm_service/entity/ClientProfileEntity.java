package com.example.crm_service.entity;

import com.example.danxils_commons.entity.BaseVoidEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "vt_client_profile")
@Getter
@Setter
public class ClientProfileEntity extends BaseVoidEntity {

    @Column(name = "client_id", nullable = false, unique = true)
    private UUID clientId; // Link to Nexus ClientEntity

    @Column(name = "satisfaction_score")
    private Double satisfactionScore;

    // EAV used for preferences:
    // "ramp_hours", "notification_email", "preferred_carrier", etc.

    public void setNotificationEmail(String email) {
        addProperty("notification_email", email);
    }

    public String getNotificationEmail() {
        return getProperty("notification_email", String.class);
    }

    public void setPreferredCarrier(String carrier) {
        addProperty("preferred_carrier", carrier);
    }

    public String getPreferredCarrier() {
        return getProperty("preferred_carrier", String.class);
    }

    public void setDefaultServiceLevel(String serviceLevel) {
        addProperty("default_service_level", serviceLevel);
    }

    public String getDefaultServiceLevel() {
        return getProperty("default_service_level", String.class);
    }

    public void setRampHours(String rampHours) {
        addProperty("ramp_hours", rampHours);
    }

    public String getRampHours() {
        return getProperty("ramp_hours", String.class);
    }
}
