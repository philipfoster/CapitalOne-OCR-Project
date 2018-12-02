package com.capitalone.creditocr.model.ingest;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class InfoExtractor {

    private static final Logger logger = LoggerFactory.getLogger(InfoExtractor.class);

    /*
    Method to extract name and address data from the text. These
    two items are grouped due to their frequent proximity to
    each other. This method should get values for firstName,
    lastName, and address.
    */
    public LetterData extractAddress(LetterData letter) {
        try {
            TokenNameFinderModel nerModel = new TokenNameFinderModel(getClass().getResourceAsStream("/resources/static/opennlp/en-ner-address.bin"));
            TokenizerModel tokenizerModel = new TokenizerModel(getClass().getResourceAsStream("/resources/static/opennlp/en-token.bin"));

            Tokenizer tokenizer = new TokenizerME(tokenizerModel);
            NameFinderME addressFinder = new NameFinderME(nerModel);

            String[] tokens = tokenizer.tokenize(letter.getText());
            Span[] nameSpans = addressFinder.find(tokens);
            String[] addresses = Span.spansToStrings(nameSpans, tokens);
            Vector<String> addressVec = new Vector<>();
            for (String address : addresses) {
                if (!address.contains("Capital One"))
                    addressVec.add(address);
            }
            /*
            If there is at least 1 address left in addresses, assume
            the first address is the correct address and assign it to
            streetAddress. If there are no addresses left, do nothing.
            */
            if(addressVec.size() >= 1)
                letter.setStreetAddress(addressVec.get(0));
        } catch (IOException ex) {

            logger.error("One or more models could not be loaded.", ex);
        }
        return letter;
    }

    /*
    Method to extract date and account number from the text.
    This should get values for letterDate and acctNum.
    */
    public LetterData extractDate(LetterData letter) {
        try {
            //Define models for OpenNLP
            TokenNameFinderModel nerModel = new TokenNameFinderModel(getClass().getResourceAsStream("/resources/static/opennlp/en-ner-date.bin"));
            TokenizerModel tokenizerModel = new TokenizerModel(getClass().getResourceAsStream("/resources/static/opennlp/en-token.bin"));
            //Create objects for OpenNLP with models
            Tokenizer tokenizer = new TokenizerME(tokenizerModel);
            NameFinderME dateFinder = new NameFinderME(nerModel);

            String[] tokens = tokenizer.tokenize(letter.getText()); //Tokenize letter text
            Span[] nameSpans = dateFinder.find(tokens); //Find dates
            String[] dates = Span.spansToStrings(nameSpans, tokens); //Convert date spans to Strings
            Vector<String> dateVec = new Vector<>(); //For elimination of duplicate dates
            Vector<Instant> instVec = new Vector<>(); //To store dates as Instants
            //Placeholders for desired data values
            Instant birthDate = null;
            Instant letterDate = null;
            Instant postmarkDate = null;
            //Instants for key dates
            Instant now = Instant.now(); //Current date
            Instant yearAgo = ZonedDateTime.now().minusYears(1).toInstant(); //1 year ago
            Instant yearsAgo18 = ZonedDateTime.now().minusYears(18).toInstant(); //18 years ago
            for(String date : dates) {
                if(!dateVec.contains(date)) {
                    dateVec.add(date); //eliminate duplicates
                    Instant inst = LocalDateTime.parse(date,
                            DateTimeFormatter.ofPattern("hh:mm a, EEE M/d/uuuu", Locale.US ))
                            .atZone(ZoneId.of("America/Toronto")
                            ).toInstant(); //Convert String date to Instant
                    if(inst.isBefore(now)) { //Ignore
                        instVec.add(inst);
                    }
                }
            }
            for(Instant inst : instVec) {
                if(inst.isAfter(yearAgo)) { //Date is within the last year
                    if(letterDate == null) {
                        //Assume the date is the letter date
                        letterDate = inst;
                    }
                    else if(postmarkDate == null) {
                        if(inst.isAfter(letterDate)) {
                            //If date is after the letter date, assume
                            //it is the postmark date
                            postmarkDate = inst;
                        }
                        else if(inst.isBefore(letterDate)) {
                            //If date is before the letter date, assume
                            //it is the actual letter date and make the
                            //old letter date the postmark date.
                            postmarkDate = letterDate;
                            letterDate = inst;
                        }
                    }
                    else if(inst.isAfter(postmarkDate)) { //3rd or more date in last year
                        //Keep 2 most recent dates
                        letterDate = postmarkDate;
                        postmarkDate = inst;
                    }
                    else if(inst.isAfter(letterDate)) { //3rd or more date in last year
                        //Keep 2 most recent dates
                        letterDate = inst;
                    }
                }
                else if(inst.isBefore(yearsAgo18)) { //Date is more than 18 years ago
                    birthDate = inst; //Date is almost certainly the customer's birth date.
                }
                /*
                If the date is between 1 and 18 years ago, it is unlikely
                to be the letter date, postmark date, or birth date. So
                unlikely, in fact, that we consider any date in this time
                period to be a red herring and ignore it.
                */
            }
            if(letterDate != null)
                letter.setLetterDate(letterDate.toString());
            if(postmarkDate != null)
                letter.setPostmarkDate(postmarkDate.toString());
            if(birthDate != null)
                letter.setBirthDate(birthDate.toString());
        } catch (IOException ex) {
            logger.error("One or more models could not be loaded.", ex);
        }
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
