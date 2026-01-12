package com.example.danxils_commons.dto;

/**
 * ARCHITEKTURA: Scentralizowane DTO dla danych adresowych.
 */
public class AddressDto {
    private String customerName;
    private String attention;
    private String street;
    private String streetNumber;
    private String apartment; // Added apartment field
    private String postalCode;
    private String city;
    private String country;
    private String mail;
    private String phone;
    private String note;
    private Double lat;
    private Double lon;
    private Double confidenceScore;
    private String source;

    public AddressDto() {
    }

    public AddressDto(String customerName, String attention, String street, String streetNumber, String apartment,
            String postalCode,
            String city, String country, String mail, String phone, String note, Double lat, Double lon,
            Double confidenceScore, String source) {
        this.customerName = customerName;
        this.attention = attention;
        this.street = street;
        this.streetNumber = streetNumber;
        this.apartment = apartment;
        this.postalCode = postalCode;
        this.city = city;
        this.country = country;
        this.mail = mail;
        this.phone = phone;
        this.note = note;
        this.lat = lat;
        this.lon = lon;
        this.confidenceScore = confidenceScore;
        this.source = source;
    }

    public static AddressDtoBuilder builder() {
        return new AddressDtoBuilder();
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getAttention() {
        return attention;
    }

    public void setAttention(String attention) {
        this.attention = attention;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getStreetNumber() {
        return streetNumber;
    }

    public void setStreetNumber(String streetNumber) {
        this.streetNumber = streetNumber;
    }

    public String getApartment() {
        return apartment;
    }

    public void setApartment(String apartment) {
        this.apartment = apartment;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public static class AddressDtoBuilder {
        private String customerName;
        private String attention;
        private String street;
        private String streetNumber;
        private String apartment;
        private String postalCode;
        private String city;
        private String country;
        private String mail;
        private String phone;
        private String note;
        private Double lat;
        private Double lon;
        private Double confidenceScore;
        private String source;

        AddressDtoBuilder() {
        }

        public AddressDtoBuilder customerName(String customerName) {
            this.customerName = customerName;
            return this;
        }

        public AddressDtoBuilder attention(String attention) {
            this.attention = attention;
            return this;
        }

        public AddressDtoBuilder street(String street) {
            this.street = street;
            return this;
        }

        public AddressDtoBuilder streetNumber(String streetNumber) {
            this.streetNumber = streetNumber;
            return this;
        }

        public AddressDtoBuilder apartment(String apartment) {
            this.apartment = apartment;
            return this;
        }

        public AddressDtoBuilder postalCode(String postalCode) {
            this.postalCode = postalCode;
            return this;
        }

        public AddressDtoBuilder city(String city) {
            this.city = city;
            return this;
        }

        public AddressDtoBuilder country(String country) {
            this.country = country;
            return this;
        }

        public AddressDtoBuilder mail(String mail) {
            this.mail = mail;
            return this;
        }

        public AddressDtoBuilder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public AddressDtoBuilder note(String note) {
            this.note = note;
            return this;
        }

        public AddressDtoBuilder lat(Double lat) {
            this.lat = lat;
            return this;
        }

        public AddressDtoBuilder lon(Double lon) {
            this.lon = lon;
            return this;
        }

        public AddressDtoBuilder confidenceScore(Double confidenceScore) {
            this.confidenceScore = confidenceScore;
            return this;
        }

        public AddressDtoBuilder source(String source) {
            this.source = source;
            return this;
        }

        public AddressDto build() {
            return new AddressDto(customerName, attention, street, streetNumber, apartment, postalCode, city, country,
                    mail, phone,
                    note, lat, lon, confidenceScore, source);
        }

        public String toString() {
            return "AddressDto.AddressDtoBuilder(customerName=" + this.customerName + ", attention=" + this.attention
                    + ", street=" + this.street + ", streetNumber=" + this.streetNumber + ", apartment="
                    + this.apartment + ", postalCode="
                    + this.postalCode + ", city=" + this.city + ", country=" + this.country + ", mail=" + this.mail
                    + ", phone=" + this.phone + ", note=" + this.note + ", lat=" + this.lat + ", lon=" + this.lon
                    + ", confidenceScore=" + this.confidenceScore + ", source=" + this.source + ")";
        }
    }
}
