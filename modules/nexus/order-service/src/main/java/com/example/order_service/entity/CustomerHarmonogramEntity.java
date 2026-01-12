package com.example.order_service.entity;

import com.example.order_service.dto.HarmonogramScheduleDto;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "vt_customer_harmonograms")
public class CustomerHarmonogramEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "client_id", nullable = false, unique = true)
    private ClientEntity client;

    @Column(nullable = false)
    private String alias;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private HarmonogramScheduleDto schedule;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<String> nonDeliveryDates;

    public CustomerHarmonogramEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ClientEntity getClient() {
        return client;
    }

    public void setClient(ClientEntity client) {
        this.client = client;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public HarmonogramScheduleDto getSchedule() {
        return schedule;
    }

    public void setSchedule(HarmonogramScheduleDto schedule) {
        this.schedule = schedule;
    }

    public List<String> getNonDeliveryDates() {
        return nonDeliveryDates;
    }

    public void setNonDeliveryDates(List<String> dates) {
        this.nonDeliveryDates = dates;
    }
}
