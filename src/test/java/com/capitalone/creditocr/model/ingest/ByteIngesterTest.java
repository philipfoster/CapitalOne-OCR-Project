package com.capitalone.creditocr.model.ingest;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.TesseractException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.awt.image.BufferedImage;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class ByteIngesterTest {
    @MockBean
    private ThreadLocal<ITesseract> tes;

    @MockBean
    private ITesseract localTes;

    @Test
    public void nullImageTest() {
        ByteIngester ingester = new ByteIngester(tes);

        try {
            when(localTes.doOCR(any(BufferedImage.class))).thenReturn(null);
        } catch (TesseractException e) {
            fail("Tesseract failed to process image.");
        }

        doAnswer(invocation -> localTes).when(tes).get();
        assertNull(ingester.ingest(null));
    }

    @Test
    public void ImageTest() {
        ByteIngester ingester = new ByteIngester(tes);

        try {
            when(localTes.doOCR(any(BufferedImage.class))).thenReturn("Hello World!");
        } catch (TesseractException e) {
            fail("Tesseract failed to process image.");
        }

        doAnswer(invocation -> localTes).when(tes).get();
        assertEquals("Hello World!", ingester.ingest(new BufferedImage(5, 5, BufferedImage.TYPE_3BYTE_BGR)));
    }
}