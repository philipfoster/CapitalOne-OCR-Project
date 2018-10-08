package com.capitalone.creditocr.model.dto.document;

public class PostalAddressDtoBuilder {
    private String firstLine;
    private String secondLine;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    public PostalAddressDtoBuilder setFirstLine(String firstLine) {
        this.firstLine = firstLine;
        return this;
    }

    public PostalAddressDtoBuilder setSecondLine(String secondLine) {
        this.secondLine = secondLine;
        return this;
    }

    public PostalAddressDtoBuilder setCity(String city) {
        this.city = city;
        return this;
    }

    public PostalAddressDtoBuilder setState(String state) {
        this.state = state;
        return this;
    }

    public PostalAddressDtoBuilder setPostalCode(String postalCode) {
        this.postalCode = postalCode;
        return this;
    }

    public PostalAddressDtoBuilder setCountry(String country) {
        this.country = country;
        return this;
    }

    public PostalAddressDto build() {
        return new PostalAddressDto(firstLine, secondLine, city, state, postalCode, country);
    }
}