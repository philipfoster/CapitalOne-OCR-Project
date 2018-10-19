package com.capitalone.creditocr.model.ingest;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;

@Service
public class ByteIngester {
    private ITesseract tess;

    @Autowired
    public ByteIngester(ITesseract t) {
        tess = t;
    }

    public String ingest(BufferedImage img) {
        String output = null;
        try {
            output = tess.doOCR(img);
            System.out.println("Output:\n" + output);
        } catch (TesseractException e){
            System.err.println(e.getMessage());
        }
        return output;
    }

}