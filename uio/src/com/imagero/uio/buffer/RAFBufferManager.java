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
import java.io.RandomAccessFile;

/**
 * @author Andrei Kouznetsov
 *         Date: 12.11.2003
 *         Time: 12:45:21
 */
public class RAFBufferManager extends AbstractBufferManager {

    protected RandomAccessFile raf;

    Integer maxKey = new Integer(0);


    public RAFBufferManager(RandomAccessFile raf) throws IOException {
        this(raf, 0);
    }

    public RAFBufferManager(RandomAccessFile raf, long offset) throws IOException {
        this(raf, offset, raf.length());
    }

    /**
     * create MutableBuffer for RandomAccessRO (with standard Buffer length of 50k)
     *
     * @see RABufferRO
     */
    public RAFBufferManager(RandomAccessFile raf, long offset, long length) {
        this(raf, offset, length, defaultBufferSize);
    }

    /**
     * create MutableBuffer for RandomAccessRO
     *
     * @param dsLength standard length of one data block of Buffer
     *
     * @see RABufferRO
     */
    public RAFBufferManager(RandomAccessFile raf, long offset, long length, int dsLength) {
        this.bufferSize = dsLength;
        this.raf = raf;
        this.offset = offset;
        this.length = length;

        Buffer buffer = createBuffer(raf, offset, dsLength);
        accessManager.put(new Integer(0), buffer);
    }

    protected Buffer createBuffer(RandomAccessFile raf, long offset, int dsLength) {
        long maxLength = 0;
        try {
            maxLength = raf.length() - offset;
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        if(maxLength < 0) {
            return new RAFBufferRO(raf, offset, 0);
        }
        return new RAFBufferRO(raf, offset, (int) Math.min(maxLength, dsLength));
    }

    /**
     * get data (as byte array) from i'th Buffer
     *
     * @param i Buffer index
     *
     * @return byte array
     *
     * @throws java.io.IOException if i'th Buffer not exists and couldn't be read
     */
    public byte[] getData(int i) throws IOException {
        try {
            return getDataImpl(i);
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw ex;
        }
    }

    /**
     * read i'th Buffer
     *
     * @param i Buffer index
     *
     * @return byte array
     *
     * @throws java.io.IOException
     */
    protected byte[] getDataImpl(int i) throws IOException {
        Integer key = new Integer(i);
        Buffer uds = (accessManager.get(key));
        if (uds == null) {
            long k = (bufferSize * i) & 0xFFFFFFFFL;
            uds = createBuffer(raf, offset + k, bufferSize);
            if(uds == null) {
                return null;
            }
            accessManager.put(key, uds);
            if (key.intValue() > maxKey.intValue()) {
                maxKey = key;
            }
        }
        return uds.getData();
    }

    /**
     * get length of i'th Buffer
     *
     * @param i Buffer index
     *
     * @return dsLength
     */
    public int getDataLength(int i) {
        return bufferSize;
    }

    /**
     * get index of Buffer which contains <code>pos</code>
     *
     * @param pos
     *
     * @return index of Buffer or -1
     */
    public int getIndex(long pos) {
        if (pos < 0) {
            return -1;
        }
        return (int) (pos / bufferSize);
    }

    /**
     * get length of data
     */
    public long getLength() {
        return length;
    }

    /**
     * get start of i'th Buffer in byte<br>
     * I assume here that length of each Buffer (except last one) equals to <code>dsLength</code>
     *
     * @param i
     */
    public long getDataStart(int i) {
//        if (i >= accessManager.getCount()) {
//            throw new ArrayIndexOutOfBoundsException(i + " >= " + ds.size());
//        }
        return bufferSize * i;
    }

    public void close() {
        clear();
        IOutils.closeStream(raf);
    }

    public void clear() {
        accessManager.clear();
        maxKey = new Integer(0);
    }
}
