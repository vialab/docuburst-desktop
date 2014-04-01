/*
 * Copyright (c) Andrey Kuznetsov. All Rights Reserved.
 *
 * http://uio.imagero.com
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  o Neither the name of imagero Andrei Kouznetsov nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.imagero.uio.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * adds ability to write streams bitewise
 * @author Andrey Kuznetsov
 */
public class BitOutputStream extends FilterOutputStream {

    static final int[] mask = {0, 1, 3, 7, 15, 31, 63, 127, 255};

    int bitbuf;
    protected int vbits;

    private int bitsToWrite = 8;


    public BitOutputStream(OutputStream out) {
        super(out);
    }

    public int getBitsToWrite() {
        return bitsToWrite;
    }

    /**
     * set how much bits should be written to stream every write() call
     * @param bitsToWrite
     */
    public void setBitsToWrite(int bitsToWrite) {
        this.bitsToWrite = bitsToWrite;
    }

    /**
     * Writes some bits (max 8) from the specified int to stream.
     * @param b int which should be written
     * @throws IOException if an I/O error occurs
     * @see #setBitsToWrite
     * @see #getBitsToWrite
     */
    public void write(int b) throws IOException {
        write(b, bitsToWrite);
    }

    /**
     * Writes some bits (max 8) from the specified int to stream.
     * @param b int which should be written
     * @param nbits bit count to write
     * @throws IOException if an I/O error occurs
     */
    public void write(int b, int nbits) throws IOException {
        if (nbits == 0) {
            return;
        }
        final int k = b & mask[nbits];
        bitbuf = (bitbuf << nbits) | k;
        vbits += nbits;
        while (vbits > 8) {
            int c = bitbuf << (32 - vbits) >>> 24;
            vbits -= 8;
            out.write(c);
        }
    }

    /**
     * writes bits from buffer to output stream
     * @throws IOException if I/O error occurs
     */
    public void flush() throws IOException {
        while (vbits > 0) {
            int c = bitbuf << (32 - vbits) >>> 24;
            vbits -= 8;
            out.write(c);
        }
        vbits = 0;
        bitbuf = 0;
        out.flush();
    }

    /**
     * Writes b.length bytes to output stream.
     * Only first getBitsToWrite() from every byte are written to stream.
     * @param b the data to be written.
     * @throws IOException if an I/O error occurs.
     */
    public void write(byte b[]) throws IOException {
        write(b, 0, b.length);
    }

    /**
     * Writes len bytes from byte array starting at given offset to output stream.
     * Only first getBitsToWrite() from every byte are written to stream.
     * @param b the data to be written.
     * @param off the start offset in the data.
     * @param len the number of bytes to write.
     * @throws IOException if I/O error occurs
     */
    public void write(byte b[], int off, int len) throws IOException {
        for (int i = 0; i < len; i++) {
            write(b[off + i]);
        }
    }
}
