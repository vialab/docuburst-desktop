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

import com.imagero.uio.RandomAccess;

import java.io.IOException;
import java.io.OutputStream;

/**
 * RandomAccessOutputStream.java
 * Can be used as bridge between RandomAccessFile and OutputStream.
 * @author Andrey Kuznetsov
 */
public class RandomAccessOutputStream extends OutputStream {

    protected RandomAccess ra;
    protected long pos;

    public RandomAccessOutputStream(RandomAccess ra) {
        this(ra, 0L);
    }

    public RandomAccessOutputStream(RandomAccess ra, long startPos) {
        this.ra = ra;
        this.pos = startPos;
    }

    protected void checkPos() throws IOException {
        long fp = ra.getFilePointer();
        if (fp != pos) {
            ra.seek(pos);
        }
    }

    public void write(int b) throws IOException {
        checkPos();
        writeImpl(b);
        pos++;
    }

    private void writeImpl(int b) throws IOException {
        ra.write(b);
    }

    public void write(byte b[]) throws IOException {
        write(b, 0, b.length);
    }

    public void write(byte b[], int off, int len) throws IOException {
        checkPos();
        ra.write(b, off, len);
        pos += len;
    }

    public void close() throws IOException {
        ra = null;
    }
}
