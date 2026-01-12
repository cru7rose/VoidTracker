package com.example.order_service.entity;

import com.example.danxils_commons.entity.BaseVoidEntity;
import com.example.danxils_commons.enums.OrderStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "vt_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@jakarta.persistence.AttributeOverride(name = "id", column = @jakarta.persistence.Column(name = "order_id"))
public class OrderEntity extends BaseVoidEntity {

    private String externalId;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordering_customer_id", nullable = false)
    private ClientEntity client;

    private String injectionHub;

    @jakarta.persistence.Column(name = "tenant_id")
    private UUID tenantId;

    // Legacy fields (optional support, mostly replaced by properties)
    private String legacyId;

    // === PROPER RELATIONAL DESIGN ===
    // Address and Package relationships (replaces EAV JSONB storage)

    @ManyToOne(cascade = jakarta.persistence.CascadeType.ALL)
    @JoinColumn(name = "pickup_address_id")
    private AddressEntity pickupAddress;

    @ManyToOne(cascade = jakarta.persistence.CascadeType.ALL)
    @JoinColumn(name = "delivery_address_id")
    private AddressEntity deliveryAddress;

    @ManyToOne(cascade = jakarta.persistence.CascadeType.ALL)
    @JoinColumn(name = "package_id")
    private PackageEntity packageDetails;

    // Time windows (moved from properties map)
    @jakarta.persistence.Column(name = "pickup_time_from")
    private java.time.Instant pickupTimeFrom;

    @jakarta.persistence.Column(name = "pickup_time_to")
    private java.time.Instant pickupTimeTo;

    @jakarta.persistence.Column(name = "delivery_time_from")
    private java.time.Instant deliveryTimeFrom;

    @jakarta.persistence.Column(name = "delivery_time_to")
    private java.time.Instant deliveryTimeTo;

    // --- Shims for Legacy Services ---
    public UUID getOrderId() {
        return getId();
    }

    public void setOrderId(UUID id) {
        setId(id);
    }

    public void setAssignedDriver(String driverId) {
        addProperty("assignedDriver", driverId);
    }

    public ClientEntity getOrderingCustomer() {
        return getClient();
    }

    // Getter/Setter for DeliveryAddress - now returns actual entity
    public AddressEntity getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(AddressEntity address) {
        this.deliveryAddress = address;
    }

    // Getter/Setter for PickupAddress - now returns actual entity
    public AddressEntity getPickupAddress() {
        return pickupAddress;
    }

    public void setPickupAddress(AddressEntity address) {
        this.pickupAddress = address;
    }

    // Getter/Setter for PackageDetails
    public PackageEntity getPackageDetails() {
        return packageDetails;
    }

    public void setPackageDetails(PackageEntity pkg) {
        this.packageDetails = pkg;
    }

    // Relationship restored for legacy support
    @jakarta.persistence.OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<AssetEntity> assets;

    public List<AssetEntity> getAssets() {
        return assets != null ? assets : Collections.emptyList();
    }

    public String getAssignedDriver() {
        return getProperty("assignedDriver", String.class);
    }

    public String getPartNumber() {
        return getProperty("partNumber", String.class);
    }

    public java.time.Instant getWarehouseAcceptanceDate() {
        return getProperty("warehouseAcceptanceDate", java.time.Instant.class);
    }

    public String getDepartment() {
        return null;
    }

    // Manual Lombok Shims
    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public ClientEntity getClient() {
        return client;
    }

    public void setClient(ClientEntity client) {
        this.client = client;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getInjectionHub() {
        return injectionHub;
    }

    public void setInjectionHub(String injectionHub) {
        this.injectionHub = injectionHub;
    }

    // --- Manual Getters/Setters for new fields (Lombok Fallback) ---

    public java.time.Instant getPickupTimeFrom() {
        return pickupTimeFrom;
    }

    public void setPickupTimeFrom(java.time.Instant pickupTimeFrom) {
        this.pickupTimeFrom = pickupTimeFrom;
    }

    public java.time.Instant getPickupTimeTo() {
        return pickupTimeTo;
    }

    public void setPickupTimeTo(java.time.Instant pickupTimeTo) {
        this.pickupTimeTo = pickupTimeTo;
    }

    public java.time.Instant getDeliveryTimeFrom() {
        return deliveryTimeFrom;
    }

    public void setDeliveryTimeFrom(java.time.Instant deliveryTimeFrom) {
        this.deliveryTimeFrom = deliveryTimeFrom;
    }

    public java.time.Instant getDeliveryTimeTo() {
        return deliveryTimeTo;
    }

    public void setDeliveryTimeTo(java.time.Instant deliveryTimeTo) {
        this.deliveryTimeTo = deliveryTimeTo;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

}