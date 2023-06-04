package org.jsoup.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class for reading all bytes from an InputStream.
 */
public class InputStreamUtil {
   

    /**
     * Reads all bytes from the given InputStream and returns them as a String using the UTF-8 charset.
     *
     * @param inputStream the InputStream to read from
     * @return a String containing all the bytes read from the InputStream
     * @throws IOException if an I/O error occurs while reading the InputStream
     */
    public static String readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString("UTF_8");
    }


}
