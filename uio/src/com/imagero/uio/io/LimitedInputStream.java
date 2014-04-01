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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * InputStream that reads specified number of bytes
 *
 * @author Kouznetsov Andrei
 */
public class LimitedInputStream extends FilterInputStream {
    protected int limit;

    /**
     * create new LimitedInputStream
     *
     * @param in
     * @param limit read limit
     */
    public LimitedInputStream(InputStream in, int limit) {
        super(in);
        this.limit = limit;
    }

    public int available() throws IOException {
        return limit;
    }

    public int read() throws IOException {
        if(limit-- <= 0) {
            return -1;
        }
        return in.read();
    }

    public int read(byte b[]) throws IOException {
        return in.read(b, 0, b.length);
    }

    public int read(byte b[], int off, int len) throws IOException {
        if(limit > 0) {
            int length = Math.min(len, limit);
            int res = in.read(b, off, length);
            if(res > 0) {
                limit -= res;
            }
            return res;
        }
        return -1;
    }

    public long skip(long n) throws IOException {
        if(limit > 0) {
            long length = Math.min(n, limit);
            long res = in.skip(length);
            if(res > 0) {
                limit -= res;
            }
            return res;
        }
        return -1;
    }

    int mark;

    public synchronized void mark(int readlimit) {
        in.mark(readlimit);
        mark = limit;
    }

    public synchronized void reset() throws IOException {
        in.reset();
        limit = mark;
    }
}