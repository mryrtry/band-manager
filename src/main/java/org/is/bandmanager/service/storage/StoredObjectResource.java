package org.is.bandmanager.service.storage;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public record StoredObjectResource(String filename, String contentType, long size, InputStream inputStream)
        implements Closeable {

    @Override
    public void close() throws IOException {
        if (inputStream != null) {
            inputStream.close();
        }
    }
}
