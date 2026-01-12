package com.example.order_service.dto;

import java.util.Map;

public class HarmonogramScheduleDto {
    // Key: DayOfWeek (MONDAY, TUESDAY, etc.)
    // Value: TimeWindow (e.g., "08:00-16:00")
    private Map<String, DaySchedule> weekSchedule;

    public HarmonogramScheduleDto() {
    }

    public HarmonogramScheduleDto(Map<String, DaySchedule> weekSchedule) {
        this.weekSchedule = weekSchedule;
    }

    public static HarmonogramScheduleDtoBuilder builder() {
        return new HarmonogramScheduleDtoBuilder();
    }

    public Map<String, DaySchedule> getWeekSchedule() {
        return weekSchedule;
    }

    public void setWeekSchedule(Map<String, DaySchedule> weekSchedule) {
        this.weekSchedule = weekSchedule;
    }

    public static class HarmonogramScheduleDtoBuilder {
        private Map<String, DaySchedule> weekSchedule;

        HarmonogramScheduleDtoBuilder() {
        }

        public HarmonogramScheduleDtoBuilder weekSchedule(Map<String, DaySchedule> weekSchedule) {
            this.weekSchedule = weekSchedule;
            return this;
        }

        public HarmonogramScheduleDto build() {
            return new HarmonogramScheduleDto(weekSchedule);
        }

        public String toString() {
            return "HarmonogramScheduleDto.HarmonogramScheduleDtoBuilder(weekSchedule=" + this.weekSchedule + ")";
        }
    }

    public static class DaySchedule {
        private String pickupWindow; // e.g. "08:00-16:00"
        private String deliveryWindow; // e.g. "09:00-17:00"
        private boolean isWorkingDay;

        public DaySchedule() {
        }

        public DaySchedule(String pickupWindow, String deliveryWindow, boolean isWorkingDay) {
            this.pickupWindow = pickupWindow;
            this.deliveryWindow = deliveryWindow;
            this.isWorkingDay = isWorkingDay;
        }

        public String getPickupWindow() {
            return pickupWindow;
        }

        public void setPickupWindow(String pickupWindow) {
            this.pickupWindow = pickupWindow;
        }

        public String getDeliveryWindow() {
            return deliveryWindow;
        }

        public void setDeliveryWindow(String deliveryWindow) {
            this.deliveryWindow = deliveryWindow;
        }

        public boolean isWorkingDay() {
            return isWorkingDay;
        }

        public void setWorkingDay(boolean workingDay) {
            isWorkingDay = workingDay;
        }
    }
}
