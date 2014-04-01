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
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;

/**
 * @author Andrey Kuznetsov
 */
public class OutputStreamBufferManager extends AbstractBufferManager implements MutableBufferManager {

    OutputStream out;


    public OutputStreamBufferManager(OutputStream out) {
        this(out, defaultBufferSize);
    }

    public OutputStreamBufferManager(OutputStream out, int bufferSize) {
        this.out = out;
        this.bufferSize = bufferSize;
    }

    public byte[] getData(int i) throws IOException {
        Buffer buffer = accessManager.get(i);
        if (buffer == null) {
            buffer = new OutputStreamBuffer(out, new byte[bufferSize]);
            accessManager.put(new Integer(i), buffer);
        }
        return buffer.getData();
    }

    public int getDataLength(int i) {
        return bufferSize;
    }

    public int getIndex(long pos) {
        return (int) (pos / bufferSize);
    }

    public long getLength() {
        return 0;
    }

    public void close() {
        try {
            flush();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        IOutils.closeStream(out);
    }

    public long getDataStart(int i) {
        return bufferSize * i;
    }

    public void clear() {
        try {
            flush();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        accessManager.clear();
    }

    public void setMaxCache(int max) {
        accessManager.setMaxBufferCount(max);
    }

    long lastClearPosition;

    public void clear(long start, long end) throws IOException {
        int bs = getStart(start);
        int be = getEnd(end);
        long s = getDataStart(bs);
        long e = getDataStart(be) + getDataLength(be);
        if (s > lastClearPosition) {
            throw new IOException("Can't clear from " + s + " (possible start is: " + lastClearPosition + ")");
        }
        clearImpl(bs, be);
        lastClearPosition = e;
    }

    public void flush() throws IOException {
        byte[] buf = new byte[bufferSize];
        Enumeration enum1 = accessManager.keys();
        Vector v = new Vector();
        while (enum1.hasMoreElements()) {
            v.addElement(enum1.nextElement());
        }
        int max = 0;
        for (int i = 0; i < v.size(); i++) {
            Integer o = (Integer) v.elementAt(i);
            max = Math.max(o.intValue(), max);
        }
        max++;
        Integer[] ints = new Integer[max];
        for (int i = 0; i < v.size(); i++) {
            Integer o = (Integer) v.elementAt(i);
            ints[o.intValue()] = o;
        }

        for (int i = 0; i < ints.length; i++) {
            Integer o = ints[i];
            if (o == null) {
                out.write(buf);
                continue;
            }
            int intValue = o.intValue();
            if (intValue > maxDirtyIndex) {
                return;
            }
            Buffer b = accessManager.get(o);
            if (b == maxDirtyBuffer) {
                byte [] data = b.getData();
                int maxIndex = 0;
                for (int j = data.length - 1; j >= 0; j--) {
                    if(data[j] != 0) {
                        maxIndex = j + 1;
                        break;
                    }
                }
                out.write(data, 0, maxIndex);
                OutputStreamBuffer osb = (OutputStreamBuffer) b;
                osb.flushed = true;
                osb.dirty = false;
                osb.data = osb.empty;
                osb.out = null;
                break;
            }
            else {
                MutableBuffer buffer = (MutableBuffer) b;
                buffer.flush();
            }
        }
    }

    int maxDirtyIndex;
    Buffer maxDirtyBuffer;

    public void setDirty(int index) {
        Buffer b = accessManager.get(index);
        if (b != null && b instanceof MutableBuffer) {
            MutableBuffer buffer = (MutableBuffer) b;
            buffer.setDirty();
        }
        maxDirtyIndex = Math.max(maxDirtyIndex, index);
        if (maxDirtyIndex == index) {
            maxDirtyBuffer = b;
        }
    }
}
