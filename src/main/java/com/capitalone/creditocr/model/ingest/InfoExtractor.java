package com.capitalone.creditocr.model.ingest;

public interface InfoExtractor {

    //Letter text
    public String text = "";
    //Customer's first name
    public String firstName = "";
    //Customer's last name
    public String lastName = "";
    //Date of the letter
    public String letterDate = "";
    //First line of address (Ex: 1234 Main St.)
    public String addressLine1 = "";
    //Zipcode of the address
    public String zipcode = "";
    //City and State of the address (Ex: Dallas, TX)
    public String cityState = "";
    //Customer's 17-digit Capital One account number
    public String acctNum = "";
    //Social Security Number (Ex: 123-45-6789)
    public String SSN = "";

    /*
    Method to extract first and last name from the text. This
    should get values for firstName and lastName.
    */
    void extractName();

    /*
    Method to extract address data from the text. This should
    get values for addressLine1, zipcode, and cityState.
    */
    void extractAddress();

    /*
    Method to extract date and account number from the text.
    This should get values for letterDate and acctNum.
    */
    void extractDate();

    /*
    Method to extract a 17-Digit Capital One account number,
    and if applicable, a Social Security Number, from the text.
    This should get values for acctNum and SSN.
    */
    void extractNumbers();
}
