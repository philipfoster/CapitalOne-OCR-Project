package com.capitalone.creditocr.model.ingest;

public class LetterData {

    //Letter text
    private String text = "";
    //Customer's first name
    private String firstName = "";
    //Customer's last name
    private String lastName = "";
    //Date of the letter
    private String letterDate = "";
    //First line of address (Ex: 1234 Main St.)
    private String addressLine1 = "";
    //Zipcode of the address
    private String zipcode = "";
    //City and State of the address (Ex: Dallas, TX)
    private String cityState = "";
    //Customer's 17-digit Capital One account number
    private String acctNum = "";
    //Social Security Number (Ex: 123-45-6789)
    private String SSN = "";

    public LetterData() {
        //TODO: Get text via ByteIngester
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getLetterDate() {
        return letterDate;
    }

    public void setLetterDate(String letterDate) {
        this.letterDate = letterDate;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public String getCityState() {
        return cityState;
    }

    public void setCityState(String cityState) {
        this.cityState = cityState;
    }

    public String getAcctNum() {
        return acctNum;
    }

    public void setAcctNum(String acctNum) {
        this.acctNum = acctNum;
    }

    public String getSSN() {
        return SSN;
    }

    public void setSSN(String SSN) {
        this.SSN = SSN;
    }
}
