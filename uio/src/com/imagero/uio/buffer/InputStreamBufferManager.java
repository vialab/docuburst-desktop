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
package com.imagero.uio.buffer;

import com.imagero.uio.io.IOutils;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Andrei Kouznetsov
 *         Date: 12.11.2003
 *         Time: 12:45:21
 */
public class InputStreamBufferManager extends AbstractBufferManager {

    InputStream in;

    long sumLength;
    int bufferCount;

    /**
     * create BufferManager for given InputStream (with standard length of 50k)
     *
     * @param in InputStream
     */
    public InputStreamBufferManager(InputStream in) {
        this(defaultBufferSize, in);
    }

    /**
     * create BufferManager for given InputStream
     *
     * @param bufferSize standard length of Buffer
     * @param in       InputStream
     */
    public InputStreamBufferManager(int bufferSize, InputStream in) {
        super();
        this.bufferSize = bufferSize;
        this.in = in;
    }

//    protected MemoryAccessManager createAccessManager() {
//        return MemoryAccessManager.createMemoryAccessManager(MemoryAccessManager.DROP_NEVER);
//    }

    /**
     * get data (as byte array) from i'th Buffer
     *
     * @param i Buffer index
     * @return byte array
     * @throws IOException if i'th Buffer not exists and couldn't be read from InputStream
     */
    public byte[] getData(int i) throws IOException {
        try {
            return getDataImpl(i);
        }
        catch(ArrayIndexOutOfBoundsException ex) {
            ex.printStackTrace();
            throw new IOException();
        }
    }

    /**
     * read all data sequential
     *
     * @param i Buffer index
     *
     * @return byte array
     *
     * @throws IOException
     */
    protected byte[] getDataImpl(int i) throws IOException {
        if(i >= bufferCount) {
            fillBuffer(i);
        }
        Buffer buffer = accessManager.get(i);
        if(buffer != null) {
            return buffer.getData();
        }
        return empty;
    }

    /**
     * read all Buffers from current index till <code>tillIndex</code> from InputStream
     *
     * @param tillIndex
     */
    protected void fillBuffer(int tillIndex) {
        int count = tillIndex - bufferCount + 1;
        int read = 0;
        for(int i = 0; i < count; i++) {
            byte[] data = new byte[bufferSize];
            try {
                read = IOutils.readFully2(in, data);
                //System.out.println("reading buffer: " + i + " bytes: " + read);
                if(read < 0) {
                    break;
                }
                else {
                    byte[] data0 = data;
                    if(read != data.length) {
                        data0 = new byte[read];
                        System.arraycopy(data, 0, data0, 0, read);
                    }
                    Buffer ds0 = new ByteBuffer(data0);
                    accessManager.add(ds0);
                    bufferCount++;
                    sumLength += read;
                }
            }
            catch(IOException ex) {
                ex.printStackTrace();
                break;
            }
        }
    }

    /**
     * get length of i'th Buffer
     *
     * @param i Buffer index
     *
     * @return int
     *
     * @throws ArrayIndexOutOfBoundsException if i'th Buffer not exists (e.g. wasn't yet read)
     */
    public int getDataLength(int i) {
        return accessManager.getBufferLength(i);
    }

    /**
     * get index of Buffer which contains <code>pos</code>
     *
     * @param pos
     *
     * @return index of Buffer or -1
     */
    public int getIndex(long pos) {
        if(pos < 0) {
            return -1;
        }

        return (int) (pos / bufferSize);
    }

    /**
     * get length of data of all already read Buffer together (may change)
     *
     */
    public long getLength() {
        return sumLength;
    }

    /**
     * get start of i'th Buffer in byte<br>
     * I assume here that length of each Buffer (except last one) equals to <code>dsLength</code>
     *
     * @param i
     *
     */
    public long getDataStart(int i) {
        return bufferSize * i;
    }

    /**
     * closes underlined InputStream and drops all Buffers
     */
    public void close() {
        clear();
        IOutils.closeStream(in);
    }
}
