package com.capitalone.creditocr.model.ingest;

import net.sourceforge.tess4j.ITesseract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ByteIngester {
    ITesseract tess;

    @Autowired
    public ByteIngester(ITesseract t) {
        tess = t;
    }

    public String ingest(byte[] byteArray) {
        return null;
    }

}