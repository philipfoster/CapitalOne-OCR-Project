package com.capitalone.creditocr.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UnzipUtil {

    private static final Logger logger = LoggerFactory.getLogger(UnzipUtil.class);
    private static final int BUFFER_SIZE = 2048;


    /**
     * Unzip a byte array containing a zip file
     * @param destDir the directory to write the files to
     * @param zipContents The zip file
     * @return The extracted file
     * @throws IOException if an error occurred while extracting the bytes.
     */
    public static File unzip(File destDir, byte[] zipContents) throws IOException {

        ZipInputStream zipStream = new ZipInputStream(new ByteArrayInputStream(zipContents));
        ZipEntry entry = zipStream.getNextEntry();
        byte[] buffer = new byte[BUFFER_SIZE];
        while (entry != null) {
            String fileName = destDir + File.separator + entry.getName();
            File filePath = new File(fileName);

            //noinspection ResultOfMethodCallIgnored
            new File(filePath.getParent()).mkdirs();
            FileOutputStream fos = new FileOutputStream(filePath);

            int len;
            while ((len = zipStream.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }

            fos.close();
            zipStream.closeEntry();
            entry = zipStream.getNextEntry();
        }
        zipStream.closeEntry();

        return destDir;
    }



}
