package com.imagero.uio.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Utility to write data into App13 block(s).
 * If data is longer as given App13 size then multiple App13 blocks are written.
 * @author Andrey Kuznetsov
 */
public class App13OutputStream extends FilterOutputStream {

    int count;
    byte[] buffer;
    private byte[] header = {(byte) 0xFF, (byte) 0xED, 0, 0, 'P', 'h', 'o', 't', 'o', 's', 'h', 'o', 'p', ' ', '3', '.', '0', 0};

    /**
     * create new App13OutputStream with default App13 size (32000)
     * @param out OutputStream
     */
    public App13OutputStream(OutputStream out) {
        this(out, 32000);
    }

    /**
     * create App13OutputStream with user defined App13 size
     * @param out OutputStream
     * @param length length of App13
     */
    public App13OutputStream(OutputStream out, int length) {
        super(out);
        buffer = new byte[length];
        for (int i = 0; i < header.length; i++) {
            buffer[i] = header[i];
        }
        count = header.length;
    }

    public synchronized void write(int b) throws IOException {
        if (count >= buffer.length) {
            flushBuffer();
        }
        buffer[count++] = (byte) b;
    }

    protected void flushBuffer() throws IOException {
        if (count > header.length) {
            int cnt = count - 2;
            buffer[2] = (byte) ((cnt >> 8) & 0xFF);
            buffer[3] = (byte) (cnt & 0xFF);
            out.write(buffer, 0, count);
            count = header.length;
        }
    }

    public void write(byte b[]) throws IOException {
        write(b, 0, b.length);
    }

    public void write(byte b[], int off, int len) throws IOException {
        for (int i = off; i < len; i++) {
            write(b[i]);
        }
    }

    public void flush() throws IOException {
        flushBuffer();
        out.flush();
    }
}
