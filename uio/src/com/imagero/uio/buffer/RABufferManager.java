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

import com.imagero.uio.RandomAccessRO;
import com.imagero.uio.io.IOutils;

import java.io.IOException;

/**
 * @author Andrei Kouznetsov
 *         Date: 12.11.2003
 *         Time: 12:45:21
 */
public class RABufferManager extends AbstractBufferManager {

    Integer maxKey = new Integer(0);

    /**
     * create BufferManager for RandomAccess (with standard buffer length of 50k)
     *
     * @see RABufferRO
     */
    public RABufferManager(RandomAccessRO ro, long offset, int length) {
        this(ro, offset, length, defaultBufferSize);
    }

    /**
     * create BufferManager for RandomAccess
     *
     * @param dsLength standard length of one data block of Buffer
     *
     * @see RABuffer
     */
    public RABufferManager(RandomAccessRO ro, long offset, int length, int dsLength) {
        this.bufferSize = dsLength;
        this.ro = ro;
        this.offset = offset;
        this.length = length;

        RABufferRO rads = createBuffer(ro, offset, dsLength);
        accessManager.add(rads);
    }

    protected RABufferRO createBuffer(RandomAccessRO ro, long offset, int dsLength) {
        return new RABufferRO(ro, offset, dsLength);
    }

    /**
     * get data (as byte array) from i'th Buffer
     *
     * @param i Buffer index
     *
     * @return byte array
     *
     * @throws IOException if i'th Buffer not exists and couldn't be read
     */
    public byte[] getData(int i) throws IOException {
        try {
            return getDataImpl(i);
        }
        catch(ArrayIndexOutOfBoundsException ex) {
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
     * @throws IOException
     */
    protected byte[] getDataImpl(int i) throws IOException {
        Integer key = new Integer(i);
        RABufferRO uds = ((RABufferRO) (accessManager.get(key)));
        if(uds == null) {
            uds = createBuffer(ro, offset + bufferSize * i, bufferSize);
            accessManager.put(key, uds);
            if(key.intValue() > maxKey.intValue()) {
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
        if(pos < 0) {
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
//        if(i >= accessManager.getCount()) {
//            throw new ArrayIndexOutOfBoundsException(i + " >= " + ds.size());
//        }
        return bufferSize * i;
    }

    public void close() {
        IOutils.closeStream(ro);
        accessManager.clear();
        maxKey = new Integer(0);
    }

    public void clear() {
        accessManager.clear();
        maxKey = new Integer(0);
    }

    public int getByteOrder() {
        return ro.getByteOrder();
    }
}
