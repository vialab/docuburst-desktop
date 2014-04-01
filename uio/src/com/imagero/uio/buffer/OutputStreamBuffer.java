package com.imagero.uio.buffer;

import java.io.OutputStream;
import java.io.IOException;

/**
 * @author Andrey Kuznetsov
 */
public class OutputStreamBuffer extends ByteBuffer implements MutableBuffer {

    static final byte [] empty = new byte[0];
    OutputStream out;
    boolean flushed;

    public OutputStreamBuffer(OutputStream out, byte[] data) {
        super(data);
        this.out = out;
    }

    /**
     * Writes data to outputStream and immediately releases buffer and drops reference to OutputStream.
     * OutputStreamBuffer can be flushed only once.
     * @throws IOException
     */
    public void flush() throws IOException {
        if(!flushed) {
            out.write(data);
            data = empty;
            flushed = true;
            dirty = false;
            out = null;
        }
    }

    public void setDirty() {
        dirty = true;
    }
}
