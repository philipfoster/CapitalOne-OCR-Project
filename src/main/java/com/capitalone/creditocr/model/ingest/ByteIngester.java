package com.capitalone.creditocr.model.ingest;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.TesseractException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
/*
* Created by: Jacob Camacho
* Grabs instance of Tesseract object when initialized,
* and ingest will attempt to convert an image of a letter
* to a String and return it.
* Comment last updated: 10/26/2018
* */
@Service
public class ByteIngester {

    private ThreadLocal<ITesseract> tes;

    private static final Logger logger = LoggerFactory.getLogger(ByteIngester.class);

    @Autowired
    public ByteIngester(ThreadLocal<ITesseract> tes) {
        this.tes = tes;
    }

    @Nullable
    public String ingest(BufferedImage img) {
        String output = null;
        try {
            output = tes.get().doOCR(img);
            logger.trace(String.format("Document output = %s", output));
        } catch (TesseractException e){
            logger.error("Could not extract OCR text", e);
        }
        return output;
    }

}