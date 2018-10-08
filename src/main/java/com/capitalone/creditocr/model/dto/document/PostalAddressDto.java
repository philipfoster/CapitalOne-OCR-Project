package com.capitalone.creditocr.model.dto.document;

import org.springframework.lang.Nullable;

import java.util.Objects;

/**
 * DTO for the addresses table.
 */
public class PostalAddressDto {

    @Nullable
    private String firstLine;

    @Nullable
    private String secondLine;

    @Nullable
    private String city;

    @Nullable
    private String state;

    @Nullable
    private String postalCode;

    @Nullable
    private String country;

    private int id;

    public PostalAddressDto(@Nullable String firstLine, @Nullable String secondLine, @Nullable String city,
                            @Nullable String state, @Nullable String postalCode, @Nullable String country) {
        this.firstLine = firstLine;
        this.secondLine = secondLine;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
    }

    public static PostalAddressDtoBuilder builder() {
        return new PostalAddressDtoBuilder();
    }

    public void setId(int id) {
        this.id = id;
    }

    @Nullable
    public String getFirstLine() {
        return firstLine;
    }

    @Nullable
    public String getSecondLine() {
        return secondLine;
    }

    @Nullable
    public String getCity() {
        return city;
    }

    @Nullable
    public String getState() {
        return state;
    }

    @Nullable
    public String getPostalCode() {
        return postalCode;
    }

    @Nullable
    public String getCountry() {
        return country;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PostalAddressDto that = (PostalAddressDto) o;
        return getId() == that.getId() &&
                Objects.equals(getFirstLine(), that.getFirstLine()) &&
                Objects.equals(getSecondLine(), that.getSecondLine()) &&
                Objects.equals(getCity(), that.getCity()) &&
                Objects.equals(getState(), that.getState()) &&
                Objects.equals(getPostalCode(), that.getPostalCode()) &&
                Objects.equals(getCountry(), that.getCountry());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFirstLine(), getSecondLine(), getCity(), getState(), getPostalCode(), getCountry(), getId());
    }

    @Override
    public String toString() {
        return "PostalAddressDto{" +
                "firstLine='" + firstLine + '\'' +
                ", secondLine='" + secondLine + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", postalCode='" + postalCode + '\'' +
                ", country='" + country + '\'' +
                ", id=" + id +
                '}';
    }
}
