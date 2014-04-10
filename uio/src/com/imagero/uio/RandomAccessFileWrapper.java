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
package com.imagero.uio;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;

/**
 * Wrap RandomAccessFile in RandomAccess<br>
 * Attention - this class is not buffered.
 * That means if you make extensive use of writeInt, writeLong, writeChar, ...,
 * then performance will be pretty poor. Use buffered classes instead (MutableRAFBufferManager).
 *
 * @author Andrei Kouznetsov
 *         Date: 08.11.2003
 *         Time: 13:04:44
 */
public class RandomAccessFileWrapper extends AbstractRandomAccess {

    RandomAccessFile in;

    public RandomAccessFileWrapper(RandomAccessFile in, int byteOrder) throws IOException {
        this.in = in;
        _setByteOrder(byteOrder);
    }

    protected int _read() throws IOException {
        int i = in.read();
        if(i < 0) {
            throw new EOFException();
        }
        return i;
    }

    public void write(int b) throws IOException {
        in.write(b);
    }

    public void write(byte b[]) throws IOException {
        in.write(b);
    }

    public void write(byte b[], int off, int len) throws IOException {
        in.write(b, off, len);
    }

    public long getFilePointer() throws IOException {
        return in.getFilePointer();
    }

    public long length() throws IOException {
        return in.length();
    }

    public void seek(long offset) throws IOException {
        in.seek(offset);
    }

    public int read() throws IOException {
        return in.read();
    }

    public int skip(int n) throws IOException {
        return in.skipBytes(n);
    }

    public void close() throws IOException {
        in.close();
    }

    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    public int read(byte[] b, int off, int len) throws IOException {
        return in.read(b, off, len);
    }

    /**
     * with 1.2 and later this method works as expected,
     * with 1.1 it can only grow the file, but can not truncate it.
     *
     * @param newLength
     *
     * @throws java.io.IOException
     */
    public void setLength(long newLength) throws IOException {
        try {
            Class aClass = Class.forName("java.io.RandomAccessFile");
            Method method = aClass.getMethod("setLength", new Class[]{Long.class});
            method.invoke(in, new Object[]{new Long(newLength)});
        }
        catch(Exception ex) {
            if(newLength > in.length()) {
                long pos = getFilePointer();
                seek(newLength);
                write(0);
                seek(pos);
            }
        }
    }
}
