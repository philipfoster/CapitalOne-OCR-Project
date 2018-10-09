package com.capitalone.creditocr.model.dto.document;

public class PostalAddressBuilder {
    private String firstLine;
    private String secondLine;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    public PostalAddressBuilder setFirstLine(String firstLine) {
        this.firstLine = firstLine;
        return this;
    }

    public PostalAddressBuilder setSecondLine(String secondLine) {
        this.secondLine = secondLine;
        return this;
    }

    public PostalAddressBuilder setCity(String city) {
        this.city = city;
        return this;
    }

    public PostalAddressBuilder setState(String state) {
        this.state = state;
        return this;
    }

    public PostalAddressBuilder setPostalCode(String postalCode) {
        this.postalCode = postalCode;
        return this;
    }

    public PostalAddressBuilder setCountry(String country) {
        this.country = country;
        return this;
    }

    public PostalAddress build() {
        return new PostalAddress(firstLine, secondLine, city, state, postalCode, country);
    }
}