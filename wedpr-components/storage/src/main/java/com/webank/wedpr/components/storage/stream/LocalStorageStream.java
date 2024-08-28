package com.webank.wedpr.components.storage.stream;

import com.webank.wedpr.components.storage.api.StorageStreamApi;
import java.io.IOException;
import java.io.InputStream;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Data
public class LocalStorageStream implements StorageStreamApi {

    private static final Logger logger = LoggerFactory.getLogger(LocalStorageStream.class);

    public LocalStorageStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    private InputStream inputStream;

    @Override
    public InputStream stream() {
        return inputStream;
    }

    @Override
    public long length() throws IOException {
        return inputStream.available();
    }

    @Override
    public long skip(long length) throws IOException {
        return inputStream.skip(length);
    }

    @Override
    public int read(byte[] buffer) throws IOException {
        return inputStream.read(buffer);
    }

    @Override
    public void close() throws IOException {

        logger.info("close local storage stream: {}", inputStream);

        if (inputStream != null) {
            inputStream.close();
            inputStream = null;
        }
    }
}
