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

import java.io.IOException;
import java.util.Enumeration;

/**
 * @author Andrey Kuznetsov
 */
public abstract class AbstractBufferManager implements BufferManager {
    protected static int defaultBufferSize = 1024 * 50;


    public static int getDefaultBufferSize() {
        return RAFBufferManager.defaultBufferSize;
    }

    public static void setDefaultBufferSize(int defaultBufferSize) {
        MutableRAFBufferManager.defaultBufferSize = defaultBufferSize;
    }

    MemoryAccessManager accessManager;

    long offset;
    long length;
    int bufferSize;
    RandomAccessRO ro;

    public AbstractBufferManager() {
        accessManager = createAccessManager();
    }

    protected MemoryAccessManager createAccessManager() {
        return MemoryAccessManager.createMemoryAccessManager();
    }

    /**
     * get count of Buffers CURRENTLY contained in this BufferManager
     *
     * @return int
     */
    public int getCount() {
        return accessManager.getCount();
    }

    public void clear() {
        accessManager.clear();
    }

    public void clear(long start, long end) throws IOException {
        //get first buffer to clear
        int bs = getStart(start);
        //get last buffer to clear
        int eb = getEnd(end);
        clearImpl(bs, eb);
    }

    protected void clearImpl(int bs, int eb) throws IOException {
        for (int i = bs; i <= eb; i++) {
            Integer key = new Integer(i);
            Buffer b = accessManager.get(key);
            if (b.isDirty() && b instanceof MutableBuffer) {
                MutableBuffer buffer = (MutableBuffer) b;
                buffer.flush();
            }
            accessManager.drop(key);
        }
    }

    /**
     * get index of buffer which end is less or equals as given position
     * @param pos position
     * @return buffer index
     */
    protected int getEnd(long pos) {
        int eb = getIndex(pos);
        long pe = getDataStart(eb);
        int length = getDataLength(eb);
        if (pe + length > pos) {
            eb--;
        }
        return eb;
    }

    /**
     * get index of buffer which start is greater or equals as given position
     * @param pos position
     * @return buffer index
     */
    protected int getStart(long pos) {
        int bs = getIndex(pos);
        long ps = getDataStart(bs);
        if (ps < pos) {
            bs++;
        }
        return bs;
    }

    public int getMaxCache() {
        return accessManager.getMaxBufferCount();
    }

    public void setMaxCache(int max) {
        accessManager.setMaxBufferCount(max);
    }

    public void flush() throws IOException {
        Enumeration enum1 = accessManager.keys();
        while (enum1.hasMoreElements()) {
            Buffer b = accessManager.get((Integer) enum1.nextElement());
            if (b.isDirty() && b instanceof MutableBuffer) {
                MutableBuffer buffer = (MutableBuffer) b;
                buffer.flush();
            }
        }
    }

    public void setDirty(int index) {
        Buffer b = accessManager.get(index);
        if (b != null && b instanceof MutableBuffer) {
            MutableBuffer buffer = (MutableBuffer) b;
            buffer.setDirty();
        }
    }

    public void setDirty(long from, long to) {
        int start = getIndex(from);
        int end = getIndex(to);
        for (int i = start; i <= end; i++) {
            setDirty(i);
        }
    }
}
