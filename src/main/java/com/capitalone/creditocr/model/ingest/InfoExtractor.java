package com.capitalone.creditocr.model.ingest;

public class InfoExtractor {

    /*
    Method to extract first and last name from the text. This
    should get values for firstName and lastName.
    */
    public void extractName(LetterData letter) {
        //TODO
    }

    /*
    Method to extract address data from the text. This should
    get values for addressLine1, zipcode, and cityState.
    */
    public void extractAddress(LetterData letter) {
        //TODO
    }

    /*
    Method to extract date and account number from the text.
    This should get values for letterDate and acctNum.
    */
    public void extractDate(LetterData letter) {
        //TODO
    }

    /*
    Method to extract a 17-Digit Capital One account number,
    and if applicable, a Social Security Number, from the text.
    This should get values for acctNum and SSN.
    */
    public void extractNumbers(LetterData letter) {
        //TODO
    }
}
