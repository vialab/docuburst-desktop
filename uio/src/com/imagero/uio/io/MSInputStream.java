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

import com.imagero.uio.Sys;
import com.imagero.uio.buffer.Buffer;
import com.imagero.uio.buffer.BufferManager;
import com.imagero.uio.buffer.ByteBuffer;
import com.imagero.uio.buffer.DefaultBufferManager;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

/**
 * MSInputStream.java <br>
 * MS = MultipleSource
 *
 * @author Kouznetsov Andrei
 */
public class MSInputStream extends InputStream {

    BufferManager bufferManager;

    int vpos;
    int vcount;
    int totalRead;
    int totalCount;

    protected byte buf[];

    protected int pos;
    protected int count;
    protected int mark = 0;

    /**
     * create InputStream over multiple byte arrays
     *
     * @param v Vector that contains some byte arrays
     */
    public MSInputStream(Vector v) {
        Buffer [] ds = new Buffer[v.size()];
        for (int i = 0; i < ds.length; i++) {
            ds[i] = new ByteBuffer((byte[]) v.elementAt(i));
        }
        this.bufferManager = new DefaultBufferManager(ds);

        this.vcount = v.size();
        this.totalCount = countBytes();
        nextArray();
    }

    /**
     * create InputStream over multiple Buffers
     *
     * @param ds Buffer array
     */
    public MSInputStream(Buffer[] ds) {
        this(new DefaultBufferManager(ds));
    }

    public MSInputStream(BufferManager bufferManager) {
        this.bufferManager = bufferManager;
        this.vcount = bufferManager.getCount();
        this.totalCount = countBytes();
        nextArray();
    }

    protected int countBytes() {
        int count = 0;
        for(int i = 0; i < bufferManager.getCount(); i++) {
            count += bufferManager.getDataLength(i);
        }
        return count;
    }

    protected void nextArray() {
        try {
            this.buf = bufferManager.getData(vpos++);
            this.pos = 0;
            this.count = buf.length;
        }
        catch(Exception ex) {
            //ex.printStackTrace();
            this.buf = null;
            this.pos = 0;
            this.count = 0;
        }
    }

    public int read(byte b[], int off, int len) throws IOException {
        if(totalRead >= totalCount) { //???
            throw new EOFException();
        }

        int i = 0;
        for(; i < len; i++) {
            int c = read();
            if(c == -1) {
                break;
            }
            b[off + i] = (byte) c;
        }
        return i;
    }

    public int read() throws IOException {
        if(totalRead < totalCount) {
            if(pos == count) {
                nextArray();
                if(buf == null) {
                    return -1;
                }
            }
            totalRead++;
            return buf[pos++] & 0xFF;
        }
        return -1;
    }

    public static void printHex(int value) {
        value = value & 0xFF;
        String s = Integer.toHexString(value);
        if(s.length() == 1) {
            Sys.out.print("0");
        }
        Sys.out.print(s);
        Sys.out.print(" ");
    }

    public synchronized long skip(long n) {
        int skipped = 0;
        while(n + pos > count) {
            int skp = count - pos;

            n -= skp;
            skipped += skp;
            totalRead += skp;

            nextArray();
            if(buf == null) {
                return skipped;
            }
        }
        return skipped;
    }

    public int available() {
        return totalCount - totalRead;
    }

    public void mark(int readAheadLimit) {
        mark = totalRead;
    }

    protected synchronized void gotoAbsPos(int absPos) throws IOException {
        int i = 0;
        int count = 0;

        int size = bufferManager.getCount();
        for(; i < size; i++) {
            int length = bufferManager.getDataLength(i);
            if((count + length) < absPos) {
                count += length;
            }
            else {
                break;
            }
        }

        this.buf = bufferManager.getData(i);
        pos = absPos - count;
        totalRead = absPos;
    }

    public void reset() throws IOException {
        gotoAbsPos(mark);
    }

    public boolean markSupported() {
        return true;
    }

    public void debug(int len) throws IOException {
        byte[] b = this.buf;
        int p = pos;
        int l = len;

        //Sys.out.println("pos = " + totalRead + ";	count = " + totalCount);

        if((p + len) > b.length) {
            len = b.length - p;
        }

        if(len == 0) {
            b = bufferManager.getData(vpos);
            len = l;
        }

        //Sys.out.println("len: " + len);

        for(int i = 0; i < len; i++) {
            int c = b[i + p] & 0xFF;
            printHex(c);
        }
    }


    public void debug() throws IOException {
        int size = bufferManager.getCount();
        for(int i = 0; i < size; i++) {
            byte[] b = bufferManager.getData(i);
            //Sys.out.println("###############          " + i);
            for(int j = 0; j < b.length; j++) {
                int c = b[j] & 0xFF;
                printHex(c);
            }
        }
    }

    public void debug(OutputStream out) throws IOException {
        int size = bufferManager.getCount();
        for(int i = 0; i < size; i++) {
            byte[] b = bufferManager.getData(i);
            for(int j = 0; j < b.length; j++) {
                int c = b[j] & 0xFF;
                out.write(c);
            }
        }
    }
}






