package com.webank.wedpr.components.storage.api;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public interface StorageStreamApi extends Closeable {

    InputStream stream();

    long length() throws IOException;

    long skip(long length) throws IOException;

    int read(byte[] buffer) throws IOException;

    void close() throws IOException;
}
