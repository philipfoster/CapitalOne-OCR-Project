package com.capitalone.creditocr.model.ingest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LetterQueueProcessor {

    // Holds the possible queues to be sorted in. The natural order indicates priority.
    public enum queueType {FRAUD, VALIDATION, GENERAL, INQUIRIES, UNASSIGNED}
    // Holds the number of matches for each queue
    private Map<Enum, Integer> queueCount = new HashMap<Enum, Integer>(4);
    // Holds the matching text and the queue that it belongs to
    private Map<String, Enum> descriptions = new HashMap<String, Enum>(84);
    // A list of each matching text that is found
    private List<String> matchingDesctiptions = new ArrayList<String>();


    /**
     * This internal method sets up the counters and descriptions if not already.
     */
    private void initialize() {
        if (queueCount.isEmpty()) {
            queueCount.put(queueType.FRAUD, 0);
            queueCount.put(queueType.VALIDATION, 0);
            queueCount.put(queueType.GENERAL, 0);
            queueCount.put(queueType.INQUIRIES, 0);
        }

        if (descriptions.isEmpty()) {
            descriptions.put("fraud", queueType.FRAUD);
            descriptions.put("identity theft", queueType.FRAUD);
            descriptions.put("stolen", queueType.FRAUD);
            descriptions.put("fraudulently", queueType.FRAUD);
            descriptions.put("id theft", queueType.FRAUD);
            descriptions.put("stole my identity", queueType.FRAUD);
            descriptions.put("identity thief", queueType.FRAUD);

            descriptions.put("validation", queueType.VALIDATION);
            descriptions.put("validate", queueType.VALIDATION);
            descriptions.put("validated", queueType.VALIDATION);
            descriptions.put("verification", queueType.VALIDATION);
            descriptions.put("documentary review", queueType.VALIDATION);
            descriptions.put("provide documentation", queueType.VALIDATION);
            descriptions.put("in-depth documentation", queueType.VALIDATION);
            descriptions.put("basic abstract", queueType.VALIDATION);
            descriptions.put("attest that this debt", queueType.VALIDATION);
            descriptions.put("attest this debt", queueType.VALIDATION);
            descriptions.put("reporting errors may be within material you reported", queueType.VALIDATION);
            descriptions.put("more than an account review", queueType.VALIDATION);
            descriptions.put("in lieu", queueType.VALIDATION);
            descriptions.put("not claimed in a loss", queueType.VALIDATION);
            descriptions.put("detailed documentary review", queueType.VALIDATION);
            descriptions.put("overview lacking depth", queueType.VALIDATION);
            descriptions.put("verify", queueType.VALIDATION);
            descriptions.put("tax deduction", queueType.VALIDATION);
            descriptions.put("careful documentary review", queueType.VALIDATION);
            descriptions.put("provide all relevant data", queueType.VALIDATION);
            descriptions.put("only a summary", queueType.VALIDATION);
            descriptions.put("in-depth audit", queueType.VALIDATION);
            descriptions.put("substantive documentary review", queueType.VALIDATION);

            descriptions.put("isolated", queueType.GENERAL);
            descriptions.put("couple of isolated late pay", queueType.GENERAL);
            descriptions.put("several negative notations", queueType.GENERAL);
            descriptions.put("acquire sporadic late", queueType.GENERAL);
            descriptions.put("minor late marks", queueType.GENERAL);
            descriptions.put("revisit my history", queueType.GENERAL);
            descriptions.put("goodwill", queueType.GENERAL);
            descriptions.put("good will", queueType.GENERAL);
            descriptions.put("amass", queueType.GENERAL);
            descriptions.put("amassed", queueType.GENERAL);
            descriptions.put("reassess", queueType.GENERAL);
            descriptions.put("our relationship", queueType.GENERAL);
            descriptions.put("a few minor", queueType.GENERAL);
            descriptions.put("foresee additional opportunities", queueType.GENERAL);
            descriptions.put("history", queueType.GENERAL);
            descriptions.put("re-evaluate", queueType.GENERAL);
            descriptions.put("reexamine", queueType.GENERAL);
            descriptions.put("re-examine", queueType.GENERAL);
            descriptions.put("substantiate", queueType.GENERAL);
            descriptions.put("substantiation", queueType.GENERAL);
            descriptions.put("kindness", queueType.GENERAL);
            descriptions.put("examine our relationship", queueType.GENERAL);
            descriptions.put("examine the relationship", queueType.GENERAL);
            descriptions.put("reconsider", queueType.GENERAL);
            descriptions.put("accumulate # of late payments", queueType.GENERAL);
            descriptions.put("historical", queueType.GENERAL);
            descriptions.put("redact", queueType.GENERAL);
            descriptions.put("company can help", queueType.GENERAL);
            descriptions.put("grateful for any action", queueType.GENERAL);
            descriptions.put("favor", queueType.GENERAL);
            descriptions.put("please consider", queueType.GENERAL);
            descriptions.put("guarded my account", queueType.GENERAL);
            descriptions.put("maintained this account appropriately", queueType.GENERAL);
            descriptions.put("good customer", queueType.GENERAL);
            descriptions.put("occasional", queueType.GENERAL);
            descriptions.put("negative notations", queueType.GENERAL);
            descriptions.put("guarded our agreement", queueType.GENERAL);
            descriptions.put("scattered late pay", queueType.GENERAL);
            descriptions.put("bearing my signature", queueType.GENERAL);
            descriptions.put("scattered negative", queueType.GENERAL);
            descriptions.put("scattered", queueType.GENERAL);
            descriptions.put("managed to accumulate", queueType.GENERAL);
            descriptions.put("managed to accrue", queueType.GENERAL);
            descriptions.put("accumulated seldom", queueType.GENERAL);
            descriptions.put("managed to acquire", queueType.GENERAL);
            descriptions.put("minor late notations", queueType.GENERAL);
            descriptions.put("seldom negative", queueType.GENERAL);

            descriptions.put("inquiry", queueType.INQUIRIES);
            descriptions.put("inquiries", queueType.INQUIRIES);
            descriptions.put("i did not apply for credit", queueType.INQUIRIES);
            descriptions.put("unauthorized view of credit", queueType.INQUIRIES);
            descriptions.put("didn't authorize", queueType.INQUIRIES);
            descriptions.put("visit a dealership", queueType.INQUIRIES);
        }
    }

    /**
     * Removes punctuation and makes all letters lower case
     * @param letterdata Letter containing the text to process
     * @return String containing lowercase text without punctuation
     */
    public String normalizeLetterText(LetterData letterdata) {
        return letterdata.getText().toLowerCase().replaceAll("\\p{IsPunctuation}", "");
    }

    /**
     * Main method which will fill the queueCount map with the appropriate number of found instances for a particular key
     * @param normalizedText This must be a String run through the normalizeLetterText() method.
     */
    public void processText(String normalizedText) {
        // Check to make sure all maps have already been initialized
        this.initialize();

        // Go through each element in the map and check if the string is in the text
        for (HashMap.Entry<String, Enum> entry : descriptions.entrySet()) {
            // This performance is bad and I would love a better way.
            // If the text has the key words, add 1 to the count of it's respective queue.
            if (normalizedText.contains(entry.getKey())) {
                Integer count = queueCount.get(entry.getValue());
                ++count;
                queueCount.put(entry.getValue(), count);

                // Also, add it to the list of matching descriptions.
                matchingDesctiptions.add(entry.getKey());
            }
        }
    }

    /**
     * Return all key words found in the letter. <b>Must run processText() before calling!</b>
     * @return List of all relevant key words found in the letter.
     */
    public List<String> getMatchingDescriptions() {
        return matchingDesctiptions;
    }

    /**
     * Return all queues that have entries found in the letter. <b>Must run processText() before calling!</b>
     * @return List of all queues that could match the letter.
     */
    public List<String> getMatchingQueues() {
        List<String> matchingQueues = new ArrayList<String>();

        // Add all queue types found to the list
        for (HashMap.Entry<Enum, Integer> entry : queueCount.entrySet()) {
            if (entry.getValue() > 0) {
                matchingQueues.add(entry.getKey().toString());
            }
        }
        // If the document matched no queues, then place it in a catch all unassigned queue
        if (matchingQueues.isEmpty()) {
            matchingQueues.add(queueType.UNASSIGNED.toString());
        }

        return matchingQueues;
    }

    /**
     * A document should only be placed into 1 queue. This method will determine which queue it should go in based
     * on an intrinsic priority. <b>Must run processText() before calling!</b>
     * @return String with the correct queue name.
     */
    public String getFinalQueue() {
        String finalQueue;
        if (queueCount.get(queueType.FRAUD) > 0) {
            finalQueue = queueType.FRAUD.toString();
        }
        else if (queueCount.get(queueType.VALIDATION) > 0) {
            finalQueue = queueType.VALIDATION.toString();
        }
        else if (queueCount.get(queueType.GENERAL) > 0) {
            finalQueue = queueType.GENERAL.toString();
        }
        else if (queueCount.get(queueType.INQUIRIES) > 0) {
            finalQueue = queueType.INQUIRIES.toString();
        }
        else {
            finalQueue = queueType.UNASSIGNED.toString();
        }
        return finalQueue;
    }
}
