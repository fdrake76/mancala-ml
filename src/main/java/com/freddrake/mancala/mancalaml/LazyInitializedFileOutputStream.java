package com.freddrake.mancala.mancalaml;

import lombok.NonNull;

import java.io.*;
import java.nio.channels.FileChannel;

/**
 * This is backed by a FileOutputStream, which is not initialized until one method is called.  This is
 * useful when we want to construct and pass both a FileInputStream and a FileOutputStream into a builder
 * that operates on the same file; a FileOutputStream will clobber the file before the object being built
 * has a chance to operate its FileInputStream.
 *
 * Exception handling is also different in that a constructor will not throw an exception, even if an
 * equivalent constructor to FileOutputStream would.  Instead, any exceptions normally found in the
 * constructor will be released as a RuntimeException.
 */
public class LazyInitializedFileOutputStream extends OutputStream
        implements Closeable, Flushable, AutoCloseable {
    private FileOutputStream fos;

    private File file;
    private FileDescriptor fdObj;
    private String name;

    public LazyInitializedFileOutputStream(@NonNull File file) {
        this.file = file;
    }

    public LazyInitializedFileOutputStream(@NonNull FileDescriptor fdObj) {
        this.fdObj = fdObj;
    }

    public LazyInitializedFileOutputStream(@NonNull String name) {
        this.name = name;
    }

    @Override
    public void write(int b) throws IOException {
        initFileOutputStream();
        fos.write(b);
    }

    @Override
    public void close() throws IOException {
        initFileOutputStream();
        fos.close();
    }

    public FileChannel getChannel() {
        initFileOutputStream();
        return fos.getChannel();
    }

    public FileDescriptor getFD() throws IOException {
        initFileOutputStream();
        return fos.getFD();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        initFileOutputStream();
        fos.write(b, off, len);
    }

    @Override
    public void write(byte[] b) throws IOException {
        initFileOutputStream();
        fos.write(b);
    }

    private void initFileOutputStream() {
        if (fos != null) return;

        if (file != null) {
            try {
                fos = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }

            return;
        }

        if (fdObj != null) {
            fos = new FileOutputStream(fdObj);
            return;
        }

        try {
            fos = new FileOutputStream(name);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
