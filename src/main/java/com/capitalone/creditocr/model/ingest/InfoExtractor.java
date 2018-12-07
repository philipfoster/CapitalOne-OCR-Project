package com.capitalone.creditocr.model.ingest;

import com.capitalone.creditocr.util.TimeUtils;
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
import java.io.InputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class InfoExtractor {

    private static final Logger logger = LoggerFactory.getLogger(InfoExtractor.class);

    private Pattern[] datePatterns = {
            Pattern.compile("\\d{1,2}[-|/]\\d{1,2}[-|/]\\d{4}"), // Dates in m/d/y, or m-d-y format.
            Pattern.compile("[a-z|A-Z]{3,9}+\\s+\\d{1,2}[,]?\\s+[\\d{2}|\\d{4}]") // spelled out date (April 12 2019)
    };

    /*
    Method to extract name and address data from the text. These
    two items are grouped due to their frequent proximity to
    each other. This method should get values for firstName,
    lastName, and address.
    */
    @SuppressWarnings("Duplicates")
    public LetterData extractAddress(LetterData letter) {
        try (
                InputStream addressStream = getClass().getResourceAsStream("/BOOT-INF/classes/static/opennlp/en-ner-address.bin");
                InputStream tokenStream = getClass().getResourceAsStream("/BOOT-INF/classes/static/opennlp/en-token.bin")
        ) {
            TokenNameFinderModel nerModel = new TokenNameFinderModel(addressStream);
            TokenizerModel tokenizerModel = new TokenizerModel(tokenStream);

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
    @SuppressWarnings("Duplicates")
    public LetterData extractDate(LetterData letter) {

        List<Instant> dates = new ArrayList<>(  );
        for (Pattern datePattern : datePatterns) {
            Matcher matcher = datePattern.matcher( letter.getText() );

            while (matcher.find()) {
                String match = matcher.group();
                var instant = TimeUtils.string2Instant( match );

                if (matcher.start() > 0) {
                    if (("" + letter.getText().charAt( matcher.start() - 1 )).matches( "\\d" )) {
                        // Filter out the case where the regex matches a SSN.
                        continue;
                    }
                }
                dates.add( instant );
            }
        }

        var now = Instant.now();

        // Filter dates in the future, since they can't be either dob, letter date, or postmark
        List<Instant> instantList = new ArrayList<>();
        for (Instant date : dates) {
            if (date != null && date.isBefore( now )) {
                instantList.add( date );
            }
        }
        instantList.sort( Comparator.reverseOrder() );

        logger.info( String.format( "dates = %s", instantList) );

        if (instantList.isEmpty()) {
            return letter;
        }

        Instant birthDate = null;
        Instant postmarkDate = null;
        Instant letterDate = null;


        // TODO: Fix this...
        for (Instant inst : instantList) {

            if (inst.isBefore( now.minus( 18 * 365, ChronoUnit.DAYS ) )) { //Date is more than 18 years ago
                birthDate = inst; //Date is almost certainly the customer's birth date.
            } else if (inst.isAfter( now.minus( 365, ChronoUnit.DAYS ) )) { //Date is within the last year
                if (letterDate == null) {
                    //Assume the date is the letter date
                    letterDate = inst;
                } else if (postmarkDate == null) {
                    if (inst.isAfter( letterDate )) {
                        //If date is after the letter date, assume
                        //it is the postmark date
                        postmarkDate = inst;
                    } else if (inst.isBefore( letterDate )) {
                        //If date is before the letter date, assume
                        //it is the actual letter date and make the
                        //old letter date the postmark date.
                        postmarkDate = letterDate;
                        letterDate = inst;
                    }
                } else if (inst.isAfter( postmarkDate )) { //3rd or more date in last year
                    //Keep 2 most recent dates
                    letterDate = postmarkDate;
                    postmarkDate = inst;
                } else if (inst.isAfter( letterDate )) { //3rd or more date in last year
                    //Keep 2 most recent dates
                    letterDate = inst;
                }
            }
        }


        if (birthDate != null) {
            letter.setBirthDate( birthDate );
        }
        if (postmarkDate != null) {
            letter.setPostmarkDate( postmarkDate );
        }
        if (letterDate != null) {
            letter.setLetterDate( letterDate );
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
