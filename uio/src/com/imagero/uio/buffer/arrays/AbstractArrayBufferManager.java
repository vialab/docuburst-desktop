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
package com.imagero.uio.buffer.arrays;

import com.imagero.uio.RandomAccessFactory;
import com.imagero.uio.Sys;
import com.imagero.uio.buffer.Buffer;
import com.imagero.uio.buffer.MemoryAccessManager;
import com.imagero.uio.buffer.MutableBuffer;
import com.imagero.uio.buffer.MutableBufferManager;
import com.imagero.uio.buffer.MutableByteBuffer;

import java.io.IOException;
import java.util.Enumeration;

/**
 * @author Andrei Kouznetsov
 *         Date: 02.06.2004
 *         Time: 11:37:27
 */
public abstract class AbstractArrayBufferManager implements MutableBufferManager {
    int tileSize;
    protected int unitSize;

    MemoryAccessManager accessManager;

    private int byteOrder = RandomAccessFactory.BIG_ENDIAN;
    int offset;
    int length;
    protected static int TILE_SIZE = 4096;



    /**
     * read block of data into byte array
     *
     * @param b     byte array
     * @param index index of Buffer to read
     */
    protected void readLE(byte[] b, int index) {
        int start = (int) getDataStart(index);
        int c = 0;
        long length = getLength() / getUnitSize();
        long max = Math.min(length - start, tileSize);
        for (int i = 0; i < max; i++) {
            c = readUnitLE(start + i, b, c);
        }
    }

    public long getDataStart(int i) {
        return offset + i * tileSize * unitSize;
    }

    public int getIndex(long pos) {
        return (int) (pos / (tileSize * unitSize));
    }

    /**
     * mark region as dirty (changed)
     *
     * @param from start of changed region
     * @param to   end of changed region
     */
    public void setDirty(long from, long to) {
        int start = getIndex(from);
        int end = getIndex(to);
        for (int i = start; i <= end; i++) {
            MutableBuffer buffer = (MutableBuffer) accessManager.get(i);
            if (buffer != null) {
                buffer.setDirty();
            }
        }
    }

    /**
     * mark Buffer as dirty (changed)
     *
     * @param index Buffer to mark as dirty
     */
    public void setDirty(int index) {
        MutableBuffer buffer = (MutableBuffer) accessManager.get(index);
        if(buffer != null) {
            buffer.setDirty();
        }
    }

    public void flush() {
        flush(false);
    }

    /**
     * read block of data into byte array
     *
     * @param b     byte array
     * @param index tile index to read
     */
    protected void readBE(byte[] b, int index) {
        int start = (int) getDataStart(index);
        int c = 0;
        long length = getLength() / getUnitSize();
        long max = Math.min(length - start, tileSize);
        for (int i = 0; i < max; i++) {
            c = readUnitBE(start + i, b, c);
        }
    }

    /**
     * read appropriate unit (short, int or long) in  BIG_ENDIAN order
     *
     * @param offset     offset in source array
     * @param dest       byte array (destination)
     * @param destOffset offset in destination array
     * @return offset in destination array (updated)
     */
    protected abstract int readUnitBE(int offset, byte[] dest, int destOffset);

    /**
     * read appropriate unit (short, int or long) in  LITTLE_ENDIAN order
     *
     * @param offset     offset in source array
     * @param dest       byte array (destination)
     * @param destOffset offset in destination array
     * @return offset in destination array (updated)
     */
    protected abstract int readUnitLE(int offset, byte[] dest, int destOffset);

    public int getCount() {
        return accessManager.getCount();
    }

    /**
     * get length of i'th Buffer
     *
     * @param i Buffer index
     * @return length of i'th Buffer (in bytes)
     */
    public int getDataLength(int i) {
//        int count = accessManager.getCount();
        if((i + 1)* tileSize < length || length % tileSize == 0) {
            return tileSize * unitSize;
        }
//        if (i < count - 1 || length % tileSize == 0) {
//
//        }
        else {
            return (length % tileSize) * unitSize;
        }
    }

    /**
     * read block of data into byte array.
     *
     * @param b     byte array
     * @param index index of Buffer to read
     */
    protected void read(byte[] b, int index) {
        if (byteOrder == RandomAccessFactory.LITTLE_ENDIAN) {
            readLE(b, index);
        }
        else if (byteOrder == RandomAccessFactory.BIG_ENDIAN) {
            readBE(b, index);
        }
        else {
            throw new RuntimeException("unknown byte order:" + byteOrder);
        }
    }

    public byte[] getData(int i) throws IOException {
        Buffer buffer = accessManager.get(i);
        if (buffer == null) {
            byte[] b = new byte[getDataLength(i)];
            read(b, i);
            accessManager.put(new Integer(i), new MutableByteBuffer(b));
        }
        return accessManager.get(i).getData();
    }

    /**
     * write block of data back to int array
     *
     * @param b     data to write
     * @param index tile index to write
     */
    protected void write(byte[] b, int index) {
        if (byteOrder == RandomAccessFactory.LITTLE_ENDIAN) {
            writeLE(b, index);
        }
        else if (byteOrder == RandomAccessFactory.BIG_ENDIAN) {
            writeBE(b, index);
        }
        else {
            throw new RuntimeException("unknown byte order:" + byteOrder);
        }
    }

    /**
     * write back dirty data and possibly free resources
     *
     * @param free if true free resources
     */
    public void flush(boolean free) {
        Enumeration keys = accessManager.keys();
        while (keys.hasMoreElements()) {
            Integer key = (Integer) keys.nextElement();
            if (accessManager.isDirty(key)) {
                try {
                    write(accessManager.get(key).getData(), key.intValue());
                }
                catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        if (free) {
            accessManager.clear();
        }
    }

    /**
     * write back dirty data and free resources
     */
    public void close() {
        Enumeration keys = accessManager.keys();
        while (keys.hasMoreElements()) {
            Integer key = (Integer) keys.nextElement();
            if (accessManager.isDirty(key)) {
                try {
                    write(accessManager.get(key).getData(), key.intValue());
                }
                catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        accessManager.clear();
    }

    public void clear() {
        flush(true);
    }

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
            if (accessManager.isDirty(key)) {
                try {
                    write(accessManager.get(key).getData(), key.intValue());
                }
                catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            accessManager.drop(key);
        }
    }

    protected void writeLE(byte[] b, int index) {
        int start = (int) getDataStart(index);
        int c = 0;
        final int len = b.length / unitSize;
        for (int i = 0; i < len; i++) {
            c = writeUnitLE(b, c, start + i);
        }
    }

    protected abstract int writeUnitLE(byte[] b, int c, int offset);

    protected void writeBE(byte[] b, int index) {
        int start = (int) getDataStart(index);
        int c = 0;
        final int len = b.length / unitSize;
        for (int i = 0; i < len; i++) {
            c = writeUnitBE(b, c, start + i);
        }
    }

    protected abstract int writeUnitBE(byte[] b, int c, int offset);

    /**
     * Get length of data
     *
     * @return length of data in bytes
     */
    public long getLength() {
        return length * unitSize;
    }

    /**
     * get unit size
     *
     * @return 2 for short, 4 for int and 8 for long data
     */
    public int getUnitSize() {
        return unitSize;
    }

    public int getByteOrder() {
        return byteOrder;
    }

    public void setByteOrder(int byteOrder) {
        this.byteOrder = byteOrder;
    }


    static void printHex(long[] vl) {
        for (int i = 0; i < vl.length; i++) {
            Sys.out.print(Long.toHexString(vl[i]));
            Sys.out.print(" ");
        }
        Sys.out.println("\n*************************");
    }

    static void printHex(int[] vi) {
        for (int i = 0; i < vi.length; i++) {
            Sys.out.print(Integer.toHexString(vi[i]));
            Sys.out.print(" ");
        }
        Sys.out.println("\n*************************");
    }

    static void printHex(float[] vf) {
        for (int i = 0; i < vf.length; i++) {
            Sys.out.print(vf[i]);
            Sys.out.print(" ");
        }
        Sys.out.println();

        for (int i = 0; i < vf.length; i++) {
            Sys.out.print(Integer.toHexString(Float.floatToIntBits(vf[i])));
            Sys.out.print(" ");
        }
        Sys.out.println("\n*************************");
    }

    static void printHex(double[] vd) {
        for (int i = 0; i < vd.length; i++) {
            Sys.out.print(vd[i]);
            Sys.out.print(" ");
        }
        Sys.out.println();
        for (int i = 0; i < vd.length; i++) {
            Sys.out.print(Long.toHexString(Double.doubleToLongBits(vd[i])));
            Sys.out.print(" ");
        }
        Sys.out.println("\n*************************");
    }

    static void printHex(short[] vs) {
        for (int i = 0; i < vs.length; i++) {
            Sys.out.print(Integer.toHexString(vs[i] & 0xFFFF));
            Sys.out.print(" ");
        }
        Sys.out.println("\n*************************");
    }

    static void printHex(char[] vs) {
        for (int i = 0; i < vs.length; i++) {
            Sys.out.print(Integer.toHexString(vs[i]));
            Sys.out.print(" ");
        }
        Sys.out.println("\n*************************");
    }

    static void printHex(byte[] vb) {
        for (int i = 0; i < vb.length; i++) {
            if (vb[i] == 0) {
                Sys.out.print("00");
            }
            else {
                Sys.out.print(Integer.toHexString(vb[i] & 0xFF));
            }
            Sys.out.print(" ");
        }
        Sys.out.println("\n*************************");
    }


    public int getMaxCache() {
        return accessManager.getMaxBufferCount();
    }

    public void setMaxCache(int max) {
        accessManager.setMaxBufferCount(max);
    }
}
