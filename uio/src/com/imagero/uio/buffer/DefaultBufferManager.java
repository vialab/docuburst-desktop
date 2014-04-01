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

import java.io.IOException;

/**
 * DefaultBufferManager.java
 *
 * @author Andrei Kouznetsov
 */
public class DefaultBufferManager implements MutableBufferManager {

    MemoryAccessManager accessManager;

    int[] startIndex;
    int length;

    public DefaultBufferManager(Buffer[] ds) {
        accessManager = MemoryAccessManager.createMemoryAccessManager(); // REMOVED DROP_NEVER to make it actually behave as default (FIFO)

        for (int i = 0; i < ds.length; i++) {
            accessManager.put(new Integer(i), ds[i]);
        }

        startIndex = new int[ds.length + 1];

        for (int i = 1; i < startIndex.length; i++) {
            startIndex[i] = startIndex[i - 1] + ds[i - 1].length();
        }

        if (ds.length > 0) {
            length = startIndex[startIndex.length - 1];
        }
    }

    public long getDataStart(int i) {
        return startIndex[i];
    }

    /**
     * get data (as byte array) from i'th Buffer
     *
     * @param i Buffer index
     *
     * @return byte array
     */
    public byte[] getData(int i) throws IOException {
        try {
            Buffer buffer = accessManager.get(new Integer(i));
            if (buffer == null) {
                return empty;
            }
            return buffer.getData();
        }
        catch (IOException ex) {
            ex.printStackTrace();
            return empty;
        }
    }

    /**
     * count of Buffers contained in this BufferManager
     *
     * @return int
     */
    public int getCount() {
        return accessManager.getCount();
    }

    /**
     * get length of i'th Buffer in byte
     *
     * @param i Buffer index
     * @return int
     */
    public int getDataLength(int i) {
        return startIndex[i + 1] - startIndex[i];
    }

    /**
     * get index of Buffer which contains index <code>pos</code>
     *
     * @param pos
     *
     * @return index or -1 if <code>pos</code> is negative or greater then or equal to  <code>getLength()</code>
     */
    public int getIndex(long pos) {
        if (pos < 0) {
            return -1;
        }
        for (int i = 0; i < startIndex.length; i++) {
            if (startIndex[i] > pos) {
                return i - 1;
            }
        }
        return -1;
    }

    /**
     * length of all data
     *
     * @return int
     */
    public long getLength() {
        return length;
    }

    /**
     * releases all buffers
     */
    public void close() {
        accessManager.clear();
    }

    /**
     * does currently nothing
     *
     * @param from
     * @param to
     */
    public void setDirty(long from, long to) {

    }

    /**
     * does currently nothing
     *
     * @param index
     */
    public void setDirty(int index) {

    }

    /**
     * does currently nothing
     *
     * @throws IOException
     */
    public void flush() throws IOException {

    }

    /**
     * clears all buffers (same as close)
     */
    public void clear() {
        accessManager.clear();
    }

    /**
     *
     * @param start
     * @param end
     */
    public void clear(long start, long end) {
        //get first buffer to clear
        int bs = getIndex(start);
        long ps = getDataStart(bs);
        if(ps < start) {
            bs++;
        }
        //get last buffer to clear
        int eb = getIndex(end);
        long pe = getDataStart(eb);
        int length = getDataLength(eb);
        if(pe + length > end) {
            eb--;
        }
        for(int i = bs; i <= eb; i++) {
            Integer key = new Integer(i);
            accessManager.drop(key);
        }
    }

    /**
     * @return current buffer count
     */
    public int getMaxCache() {
        return getCount();
    }

    /**
     * does nothing, because Buffer is never dropped
     */
    public void setMaxCache(int max) {
    }
}
