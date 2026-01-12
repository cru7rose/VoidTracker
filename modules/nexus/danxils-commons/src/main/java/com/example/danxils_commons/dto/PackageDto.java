package com.example.danxils_commons.dto;

/**
 * ARCHITEKTURA: Scentralizowane DTO dla danych o paczce.
 * ENHANCEMENT: Dodano brakujące pola zgodnie ze specyfikacją Oder_Map.txt:
 * - routeDistance: dystans trasy
 * - serviceType: typ usługi transportowej
 * - driverNote: uwagi dla kierowcy
 * - invoiceNote: uwagi do faktury
 * - price, currency: informacje o cenie
 */
public class PackageDto {
    private String barcode1;
    private String barcode2;
    private Integer colli;
    private Double weight;
    private Double volume;
    private Double length;
    private Double width;
    private Double height;
    private Boolean adr;
    private Double routeDistance;
    private String serviceType;
    private String driverNote;
    private String invoiceNote;
    private Double price;
    private String currency;

    public PackageDto() {
    }

    public PackageDto(String barcode1, String barcode2, Integer colli, Double weight, Double volume, Double length,
            Double width, Double height, Boolean adr, Double routeDistance, String serviceType, String driverNote,
            String invoiceNote, Double price, String currency) {
        this.barcode1 = barcode1;
        this.barcode2 = barcode2;
        this.colli = colli;
        this.weight = weight;
        this.volume = volume;
        this.length = length;
        this.width = width;
        this.height = height;
        this.adr = adr;
        this.routeDistance = routeDistance;
        this.serviceType = serviceType;
        this.driverNote = driverNote;
        this.invoiceNote = invoiceNote;
        this.price = price;
        this.currency = currency;
    }

    public static PackageDtoBuilder builder() {
        return new PackageDtoBuilder();
    }

    public String getBarcode1() {
        return barcode1;
    }

    public void setBarcode1(String barcode1) {
        this.barcode1 = barcode1;
    }

    public String getBarcode2() {
        return barcode2;
    }

    public void setBarcode2(String barcode2) {
        this.barcode2 = barcode2;
    }

    public Integer getColli() {
        return colli;
    }

    public void setColli(Integer colli) {
        this.colli = colli;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Double getVolume() {
        return volume;
    }

    public void setVolume(Double volume) {
        this.volume = volume;
    }

    public Double getLength() {
        return length;
    }

    public void setLength(Double length) {
        this.length = length;
    }

    public Double getWidth() {
        return width;
    }

    public void setWidth(Double width) {
        this.width = width;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public Boolean getAdr() {
        return adr;
    }

    public void setAdr(Boolean adr) {
        this.adr = adr;
    }

    public Double getRouteDistance() {
        return routeDistance;
    }

    public void setRouteDistance(Double routeDistance) {
        this.routeDistance = routeDistance;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getDriverNote() {
        return driverNote;
    }

    public void setDriverNote(String driverNote) {
        this.driverNote = driverNote;
    }

    public String getInvoiceNote() {
        return invoiceNote;
    }

    public void setInvoiceNote(String invoiceNote) {
        this.invoiceNote = invoiceNote;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public static class PackageDtoBuilder {
        private String barcode1;
        private String barcode2;
        private Integer colli;
        private Double weight;
        private Double volume;
        private Double length;
        private Double width;
        private Double height;
        private Boolean adr;
        private Double routeDistance;
        private String serviceType;
        private String driverNote;
        private String invoiceNote;
        private Double price;
        private String currency;

        PackageDtoBuilder() {
        }

        public PackageDtoBuilder barcode1(String barcode1) {
            this.barcode1 = barcode1;
            return this;
        }

        public PackageDtoBuilder barcode2(String barcode2) {
            this.barcode2 = barcode2;
            return this;
        }

        public PackageDtoBuilder colli(Integer colli) {
            this.colli = colli;
            return this;
        }

        public PackageDtoBuilder weight(Double weight) {
            this.weight = weight;
            return this;
        }

        public PackageDtoBuilder volume(Double volume) {
            this.volume = volume;
            return this;
        }

        public PackageDtoBuilder length(Double length) {
            this.length = length;
            return this;
        }

        public PackageDtoBuilder width(Double width) {
            this.width = width;
            return this;
        }

        public PackageDtoBuilder height(Double height) {
            this.height = height;
            return this;
        }

        public PackageDtoBuilder adr(Boolean adr) {
            this.adr = adr;
            return this;
        }

        public PackageDtoBuilder routeDistance(Double routeDistance) {
            this.routeDistance = routeDistance;
            return this;
        }

        public PackageDtoBuilder serviceType(String serviceType) {
            this.serviceType = serviceType;
            return this;
        }

        public PackageDtoBuilder driverNote(String driverNote) {
            this.driverNote = driverNote;
            return this;
        }

        public PackageDtoBuilder invoiceNote(String invoiceNote) {
            this.invoiceNote = invoiceNote;
            return this;
        }

        public PackageDtoBuilder price(Double price) {
            this.price = price;
            return this;
        }

        public PackageDtoBuilder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public PackageDto build() {
            return new PackageDto(barcode1, barcode2, colli, weight, volume, length, width, height, adr, routeDistance,
                    serviceType, driverNote, invoiceNote, price, currency);
        }

        public String toString() {
            return "PackageDto.PackageDtoBuilder(barcode1=" + this.barcode1 + ", barcode2=" + this.barcode2 + ", colli="
                    + this.colli + ", weight=" + this.weight + ", volume=" + this.volume + ", length=" + this.length
                    + ", width=" + this.width + ", height=" + this.height + ", adr=" + this.adr + ", routeDistance="
                    + this.routeDistance + ", serviceType=" + this.serviceType + ", driverNote=" + this.driverNote
                    + ", invoiceNote=" + this.invoiceNote + ", price=" + this.price + ", currency=" + this.currency
                    + ")";
        }
    }
}
