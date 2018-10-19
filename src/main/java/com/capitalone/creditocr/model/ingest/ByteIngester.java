package com.capitalone.creditocr.model.ingest;

import net.sourceforge.tess4j.ITesseract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ByteIngester {
    private ITesseract tess;
    private String output;

    @Autowired
    public ByteIngester(ITesseract t) {
        tess = t;
    }

    //TODO: doOCR accepts type File. Must convert input to File type.
    public String ingest(byte[] byteArray) {
//        try {
//            output = tess.doOCR();
//            System.out.println("Output:\n" + output);
//        } catch (TesseractException e){
//            System.err.println(e.getMessage());
//        }
        return null;
    }

}