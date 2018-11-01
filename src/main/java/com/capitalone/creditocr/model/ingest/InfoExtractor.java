package com.capitalone.creditocr.model.ingest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InfoExtractor {

    /*
    Method to extract name and address data from the text. These
    two items are grouped due to their frequent proximity to
    each other. This method should get values for firstName,
    lastName, addressLine1, zipcode, and cityState.
    */
    public void extractNameAndAddress(LetterData letter) {
        //TODO
    }

    /*
    Method to extract date and account number from the text.
    This should get values for letterDate and acctNum.
    */
    public LetterData extractDate(LetterData letter) {
        String text = letter.getText();
        String pattern1 = "\\d+/\\d+/\\d+";      // 12/25/2018
        String pattern2 = "\\d+-\\d+-\\d+";      // 12-25-2018
        String pattern3 = "\\w+\\s\\d+,\\s\\d+"; // December 25, 2018
        Pattern p1 = Pattern.compile(pattern1);
        Pattern p2 = Pattern.compile(pattern2);
        Pattern p3 = Pattern.compile(pattern3);
        Matcher m1 = p1.matcher(text);
        Matcher m2 = p2.matcher(text);
        Matcher m3 = p3.matcher(text);
        if(m1.find())
            letter.setLetterDate(m1.group(0));
        else if(m2.find())
            letter.setLetterDate(m2.group(0));
        else if(m3.find())
            letter.setLetterDate(m3.group(0));
        return letter;
    }

    /*
    Method to extract a 17-Digit Capital One account number,
    and if applicable, a Social Security Number, from the text.
    This should get values for acctNum and SSN.
    */
    public LetterData extractNumbers(LetterData letter) {
        //Extract SSN from text
        String SSNpattern = "\\d{3}-\\d{2}-\\d{4}"; //Regex for ###-##-####
        String text = letter.getText(); //Get letter text
        Pattern ssnp = Pattern.compile(SSNpattern); //Create regex Pattern
        Matcher ssnm = ssnp.matcher(text); //Look for matches in the letter
        if(ssnm.find())
            letter.setSSN(ssnm.group(0)); //Set SSN to the matched substring
        //TODO Account Number portion

        return letter;
    }
}
