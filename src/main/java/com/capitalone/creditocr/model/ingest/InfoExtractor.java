package com.capitalone.creditocr.model.ingest;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InfoExtractor {

    /*
    Method to extract name and address data from the text. These
    two items are grouped due to their frequent proximity to
    each other. This method should get values for firstName,
    lastName, addressLine1, zipcode, and cityState.

    Normal spaces are used in the Strings were the name should be,
    instead of the \s statement, as name/address blocks are usually
    preceded by one or more newlines. This should prevent unwanted
    words in the name field.
    */
    public void extractNameAndAddress(LetterData letter) {
        String text = letter.getText();
        /*
        FirstName LastName
        1234 Street St
        City, TX 12345
        */
        String pattern1 = "\\w+ \\w+\\s\\d+\\s\\w+\\s\\w+\\s\\w+,\\s\\w{2}\\s\\d{5}";
        /*
        FirstName LastName      FirstName LastName
        1234 Street St          1234 Street Name St
        City Name, TX 12345     City, TX 12345
        */
        String pattern2 = "\\w+ \\w+\\s\\d+\\s\\w+\\s\\w+\\s\\w+\\s\\w+,\\s\\w{2}\\s\\d{5}";
        /*
        FirstName LastName      FirstName LastName
        1234 Street Name St     1234 N Street Name St
        City Name, TX 12345     City, TX 12345
        */
        String pattern3 = "\\w+ \\w+\\s\\d+\\s\\w+\\s\\w+\\s\\w+\\s\\w+\\s\\w+,\\s\\w{2}\\s\\d{5}";
        /*
        FirstName LastName
        1234 N Street Name St
        City Name, TX 12345
        */
        String pattern4 = "\\w+ \\w+\\s\\d+\\s\\w+\\s\\w+\\s\\w+\\s\\w+\\s\\w+\\s\\w+,\\s\\w{2}\\s\\d{5}";
        /*
        FirstName Mid LastName
        1234 Street St
        City, TX 12345
        */
        String pattern5 = "\\w+ \\w+ \\w+\\s\\d+\\s\\w+\\s\\w+\\s\\w+,\\s\\w{2}\\s\\d{5}";
        /*
        FirstName Mid LastName  FirstName Mid LastName
        1234 Street St          1234 Street Name St
        City Name, TX 12345     City, TX 12345
        */
        String pattern6 = "\\w+ \\w+ \\w+\\s\\d+\\s\\w+\\s\\w+\\s\\w+\\s\\w+,\\s\\w{2}\\s\\d{5}";
        /*
        FirstName Mid LastName  FirstName Mid LastName
        1234 Street Name St     1234 N Street Name St
        City Name, TX 12345     City, TX 12345
        */
        String pattern7 = "\\w+ \\w+ \\w+\\s\\d+\\s\\w+\\s\\w+\\s\\w+\\s\\w+\\s\\w+,\\s\\w{2}\\s\\d{5}";
        /*
        FirstName Mid LastName
        1234 N Street Name St
        City Name, TX 12345
        */
        String pattern8 = "\\w+ \\w+ \\w+\\s\\d+\\s\\w+\\s\\w+\\s\\w+\\s\\w+\\s\\w+\\s\\w+,\\s\\w{2}\\s\\d{5}";
        Pattern p1 = Pattern.compile(pattern1);
        Pattern p2 = Pattern.compile(pattern2);
        Pattern p3 = Pattern.compile(pattern3);
        Pattern p4 = Pattern.compile(pattern4);
        Pattern p5 = Pattern.compile(pattern5);
        Pattern p6 = Pattern.compile(pattern6);
        Pattern p7 = Pattern.compile(pattern7);
        Pattern p8 = Pattern.compile(pattern8);
        Matcher m1 = p1.matcher(text);
        Matcher m2 = p2.matcher(text);
        Matcher m3 = p3.matcher(text);
        Matcher m4 = p4.matcher(text);
        Matcher m5 = p5.matcher(text);
        Matcher m6 = p6.matcher(text);
        Matcher m7 = p7.matcher(text);
        Matcher m8 = p8.matcher(text);
        String[] arr = new String[4];
        if(m1.find()) {
            String result = m1.group(0);
            arr = result.split("\\s", 3);
        }
        else if(m2.find()) {
            String result = m2.group(0);
            arr = result.split("\\s", 3);
        }
        else if(m3.find()) {
            String result = m3.group(0);
            arr = result.split("\\s", 3);
        }
        else if(m4.find()) {
            String result = m4.group(0);
            arr = result.split("\\s", 3);
        }
        else if(m5.find()) {
            String result = m5.group(0);
            arr = result.split("\\s", 4);
        }
        else if(m6.find()) {
            String result = m6.group(0);
            arr = result.split("\\s", 4);
        }
        else if(m7.find()) {
            String result = m7.group(0);
            arr = result.split("\\s", 4);
        }
        else if(m8.find()) {
            String result = m8.group(0);
            arr = result.split("\\s", 4);
        }
        if (arr.length == 3) {
            letter.setFirstName(arr[0]);
            letter.setLastName(arr[1]);
            letter.setStreetAddress(arr[2]);
        }
        else if(arr.length == 4) {
            letter.setFirstName(arr[0]);
            letter.setMiddle(arr[1]);
            letter.setLastName(arr[2]);
            letter.setStreetAddress(arr[3]);
        }
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
        String pattern4 = "\\w+\\s\\d+\\s\\d+";  // December 25 2018
        Pattern p1 = Pattern.compile(pattern1);
        Pattern p2 = Pattern.compile(pattern2);
        Pattern p3 = Pattern.compile(pattern3);
        Pattern p4 = Pattern.compile(pattern4);
        Matcher m1 = p1.matcher(text);
        Matcher m2 = p2.matcher(text);
        Matcher m3 = p3.matcher(text);
        Matcher m4 = p4.matcher(text);
        if(m1.find())
            letter.setLetterDate(m1.group(0));
        else if(m2.find())
            letter.setLetterDate(m2.group(0));
        else if(m3.find())
            letter.setLetterDate(m3.group(0));
        else if(m4.find())
            letter.setLetterDate(m4.group(0));
        return letter;
    }

    /*
    Method to extract a 17-Digit Capital One account number,
    and if applicable, a Social Security Number, from the text.
    This should get values for acctNum and SSN.
    */
    public LetterData extractNumbers(LetterData letter) {
        //Extract Social Security Number (SSN) from text
        String SSNpattern = "\\d{3}-\\d{2}-\\d{4}"; //Regex for ###-##-####
        String text = letter.getText();
        Pattern ssnp = Pattern.compile(SSNpattern);
        Matcher ssnm = ssnp.matcher(text);
        if(ssnm.find())
            letter.setSSN(ssnm.group(0));
        //Extract Capital One account number from text
        //Full account number is 17 digits long.
        String AcctNumPat1 = "\\d{17}"; //Full account number
        String AcctNumPat2 = "ending in \\d+"; //Number ending in 1234
        String AcctNumPat3 = "ends in \\d+"; //Number ending in 1234
        String AcctNumPat4 = "starting with \\d+"; //Number starting with 1234
        String AcctNumPat5 = "starts with \\d+"; //Number starting with 1234
        Pattern ANP1 = Pattern.compile(AcctNumPat1);
        Pattern ANP2 = Pattern.compile(AcctNumPat2);
        Pattern ANP3 = Pattern.compile(AcctNumPat3);
        Pattern ANP4 = Pattern.compile(AcctNumPat4);
        Pattern ANP5 = Pattern.compile(AcctNumPat5);
        Matcher ANM1 = ANP1.matcher(text);
        Matcher ANM2 = ANP2.matcher(text);
        Matcher ANM3 = ANP3.matcher(text);
        Matcher ANM4 = ANP4.matcher(text);
        Matcher ANM5 = ANP5.matcher(text);
        if(ANM1.find())
            letter.setAcctNum(ANM1.group(0)); //AcctNum is full number
        else if(ANM2.find())
            letter.setAcctNum(ANM2.group(0)); //AcctNum matches pattern 2
        else if(ANM3.find())
            letter.setAcctNum(ANM3.group(0)); //AcctNum matches pattern 3
        else if(ANM4.find())
            letter.setAcctNum(ANM4.group(0)); //AcctNum matches pattern 4
        else if(ANM5.find())
            letter.setAcctNum(ANM5.group(0)); //AcctNum matches pattern 5
        return letter;
    }
}
