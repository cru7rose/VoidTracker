package com.example.danxils_commons.enums;

public enum ScanType {
    PICKUP, // Driver picked up from Client
    HUB_INBOUND, // Arrived at Hub (Inbound Dock)
    HUB_SORTED, // Sorted to a specific Lane/Zone
    HUB_OUTBOUND, // Loaded onto Linehaul/Delivery Truck
    DELIVERY_ATTEMPT, // Driver attempted delivery
    DELIVERY_SUCCESS, // Driver successfully delivered (POD)
    TERM_SCAN // "Terminal" scan - items sitting on floor
}
