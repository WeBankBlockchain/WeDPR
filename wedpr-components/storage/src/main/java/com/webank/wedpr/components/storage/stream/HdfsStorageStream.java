package com.webank.wedpr.components.storage.stream;

import com.webank.wedpr.components.storage.api.StorageStreamApi;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class HdfsStorageStream implements StorageStreamApi  {

    private static final Logger logger = LoggerFactory.getLogger(HdfsStorageStream.class);

    public HdfsStorageStream(FileSystem fileSystem, FSDataInputStream fsDataInputStream) {
        this.fileSystem = fileSystem;
        this.fsDataInputStream = fsDataInputStream;
    }

    private FileSystem fileSystem;

    private FSDataInputStream fsDataInputStream;

    @Override
    public InputStream stream() {
        return fsDataInputStream;
    }

    @Override
    public long length() throws IOException {
        return fsDataInputStream.available();
    }

    @Override
    public long skip(long length) throws IOException {
        return fsDataInputStream.skip(length);
    }

    @Override
    public int read(byte[] buffer) throws IOException {
        return fsDataInputStream.read(buffer);
    }

    @Override
    public void close() throws IOException {

        logger.info("close hdfs storage stream: {}", fsDataInputStream);

        if (fsDataInputStream != null) {
            fsDataInputStream.close();
            fsDataInputStream = null;
        }

        if (fileSystem != null) {
            fileSystem.close();
            fileSystem = null;
        }
    }
}
