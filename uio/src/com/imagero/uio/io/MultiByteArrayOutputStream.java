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

import java.io.OutputStream;
import java.util.Vector;

/**
 * MultiByteArrayOutputStream.java
 * <br>
 * It's like ByteArrayOutputStream, but with multiple arrays <br>
 * Array size is defined through <code>sizeX</code>;
 *
 * @author Kouznetsov Andrei
 */
public class MultiByteArrayOutputStream extends OutputStream {

    Vector v;
    int totalCount;

    int sizeX = 1024;

    protected byte buf[];

    protected int pos;


    public MultiByteArrayOutputStream() {
        this(1024);
    }

    public MultiByteArrayOutputStream(int sizeX) {
        this.sizeX = sizeX;
        this.v = new Vector();
        nextArray();
    }

    protected void nextArray() {
        byte[] b = new byte[this.sizeX];
        v.addElement(b);
        this.buf = b;
        this.pos = 0;
    }

    public synchronized void write(byte b[], int off, int len) {
        for(int i = off; i < len; i++) {
            write(b[off + i]);
        }
    }

    public synchronized void write(int b) {
        if(pos == sizeX) {
            nextArray();
        }
        totalCount++;
        buf[pos++] = (byte) (b & 0xFF);
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

    public void reset() {
        totalCount = 0;
        v = new Vector();
        nextArray();
    }

    public Vector getVector() {

        int lastIndex = this.v.size() - 1;
        byte[] b = (byte[]) this.v.elementAt(lastIndex);
        byte[] b2 = new byte[pos];
        System.arraycopy(b, 0, b2, 0, pos);
        this.v.setElementAt(b2, lastIndex);

        return this.v;
    }

    public void flush() {
    }

    public void close() {
    }

    public int length() {
        return (this.v.size() - 1) * this.sizeX + this.pos;
    }
}
