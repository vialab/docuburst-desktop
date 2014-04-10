package com.imagero.uio.io;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;

/**
 * Read one or more App13 block(s)
 * @author Andrey Kuznetsov
 */
public class App13InputStream extends FilterInputStream {

    boolean finished;

    /**
     * Create new App13InputStream.
     * Note that <code>in</code> should support <code>mark()</code>
     * @param in InputStream
     * @throws IOException
     */
    public App13InputStream(InputStream in) throws IOException {
        super(in);
    }

    private void initBlock() throws IOException {
        in.mark(3);
        int marker = in.read();
        int app13 = in.read();
        if (marker != 0xFF || app13 != 0xED) {
            finished = true;
            in.reset();
            return;
        }
        length = (in.read() << 8) | (in.read() & 0xFF) - 2;
        for (int i = 0; i < 14; i++) {
            in.read();
        }
        length -= 14;
    }

    int length;

    public int read() throws IOException {
        if (finished) {
            return -1;
        }
        if (length == 0) {
            initBlock();
            if (finished) {
                return -1;
            }
        }
        length--;
        return super.read();
    }

    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    public int read(byte b[], int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        }
        if (off + len > b.length || off < 0) {
            throw new ArrayIndexOutOfBoundsException();
        }
        int read = 1;
        int a = read();
        if(a == -1) {
            return -1;
        }
        b[off] = (byte) a;
        for (int i = off + 1; i < len; i++) {
            a = read();
            if (a == -1) {
                break;
            }
            read++;
            b[i] = (byte) a;
        }
        return read;
    }

    public long skip(long n) throws IOException {
        long remaining = n;
        while (remaining > 0) {
            int a = read();
            if (a == -1) {
                break;
            }
            remaining--;
        }
        return n - remaining;
    }

    public int available() throws IOException {
        if (finished) {
            return 0;
        }
        if (length == 0) {
            initBlock();
            if (finished) {
                return 0;
            }
        }
        return length;
    }

    public synchronized void mark(int readlimit) {

    }

    public synchronized void reset() throws IOException {

    }

    public boolean markSupported() {
        return false;
    }
}
