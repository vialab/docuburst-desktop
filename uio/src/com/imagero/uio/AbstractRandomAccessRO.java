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

import com.imagero.uio.buffer.arrays.AbstractArrayBufferManager;
import com.imagero.uio.buffer.arrays.CharArrayBufferManager;
import com.imagero.uio.buffer.arrays.DoubleArrayBufferManager;
import com.imagero.uio.buffer.arrays.FloatArrayBufferManager;
import com.imagero.uio.buffer.arrays.IntArrayBufferManager;
import com.imagero.uio.buffer.arrays.LongArrayBufferManager;
import com.imagero.uio.buffer.arrays.ShortArrayBufferManager;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;

/**
 * @author Andrei Kouznetsov
 *         Date: 13.11.2003
 *         Time: 20:02:12
 */
public abstract class AbstractRandomAccessRO implements RandomAccessRO {

    protected abstract int _read() throws IOException;

    private DataInputBE dibe = new DataInputBE();
    private DataInputLE dile = new DataInputLE();

    EDataInput dataInput;

    int byteOrder;

    public final byte readByte() throws IOException {
        return (byte) (_read());
    }

    public final int readUnsignedByte() throws IOException {
        return _read();
    }

    public boolean readBoolean() throws IOException {
        return (_read() != 0);
    }

    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    public int skipBytes(int n) throws IOException {
        if (n <= 0) {
            return 0;
        }
        long len = length();
        long pos = getFilePointer();
        long newpos = pos + n;
        if (newpos > len) {
            newpos = len;
        }
        seek(newpos);
        return (int) (newpos - pos);
    }

    public void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    public void readFully(byte[] b, int off, int len) throws IOException {
        int n = 0;
        while (n < len) {
            int count = read(b, off + n, len - n);
            if (count < 0) {
                throw new EOFException();
            }
            n += count;
        }
    }

    public void readFully(short[] dest) throws IOException {
        readFully(dest, getByteOrder());
    }

    public void readFully(short[] dest, int byteOrder) throws IOException {
        readFully(dest, 0, dest.length, byteOrder);
    }

    public void readFully(short[] dest, int destOffset, int len) throws IOException {
        readFully(dest, destOffset, len, getByteOrder());
    }

    public void readFully(short[] dest, int destOffset, int len, int byteOrder) throws IOException {
        AbstractArrayBufferManager sm = new ShortArrayBufferManager(dest);
        writeTo(sm, destOffset, len, byteOrder);
    }

    public void readFully(char[] dest) throws IOException {
        readFully(dest, getByteOrder());
    }

    public void readFully(char[] dest, int byteOrder) throws IOException {
        readFully(dest, 0, dest.length, byteOrder);
    }

    public void readFully(char[] dest, int destOffset, int len) throws IOException {
        readFully(dest, destOffset, len, getByteOrder());
    }

    public void readFully(char[] dest, int destOffset, int len, int byteOrder) throws IOException {
        AbstractArrayBufferManager sm = new CharArrayBufferManager(dest, 0, len);
        writeTo(sm, destOffset, len, byteOrder);
    }


    public void readFully(int[] dest) throws IOException {
        readFully(dest, getByteOrder());
    }

    public void readFully(int[] dest, int byteOrder) throws IOException {
        readFully(dest, 0, dest.length, byteOrder);
    }

    public void readFully(int[] dest, int destOffset, int len) throws IOException {
        readFully(dest, destOffset, len, getByteOrder());
    }

    public void readFully(int[] dest, int destOffset, int len, int byteOrder) throws IOException {
        AbstractArrayBufferManager sm = new IntArrayBufferManager(dest, 0, len);
        writeTo(sm, destOffset, len, byteOrder);
    }

    public void readFully(float[] dest) throws IOException {
        readFully(dest, getByteOrder());
    }

    public void readFully(float[] dest, int byteOrder) throws IOException {
        readFully(dest, 0, dest.length, byteOrder);
    }

    public void readFully(float[] dest, int destOffset, int len) throws IOException {
        readFully(dest, destOffset, len, getByteOrder());
    }

    public void readFully(float[] dest, int destOffset, int len, int byteOrder) throws IOException {
        AbstractArrayBufferManager sm = new FloatArrayBufferManager(dest);
        writeTo(sm, destOffset, len, byteOrder);
    }

    public void readFully(long[] dest) throws IOException {
        readFully(dest, getByteOrder());
    }

    public void readFully(long[] dest, int byteOrder) throws IOException {
        readFully(dest, 0, dest.length, byteOrder);
    }

    public void readFully(long[] dest, int destOffset, int len) throws IOException {
        readFully(dest, destOffset, len, getByteOrder());
    }

    public void readFully(long[] dest, int destOffset, int len, int byteOrder) throws IOException {
        AbstractArrayBufferManager sm = new LongArrayBufferManager(dest);
        writeTo(sm, destOffset, len, byteOrder);
    }

    public void readFully(double[] dest) throws IOException {
        readFully(dest, getByteOrder());
    }

    public void readFully(double[] dest, int byteOrder) throws IOException {
        readFully(dest, 0, dest.length, byteOrder);
    }

    public void readFully(double[] dest, int destOffset, int len) throws IOException {
        readFully(dest, destOffset, len, getByteOrder());
    }

    public void readFully(double[] dest, int destOffset, int len, int byteOrder) throws IOException {
        AbstractArrayBufferManager sm = new DoubleArrayBufferManager(dest);
        writeTo(sm, destOffset, len, byteOrder);
    }

    private void writeTo(AbstractArrayBufferManager sm, int destOffset, int len, int byteOrder) throws IOException {
        int length = len * sm.getUnitSize();
        int offset = destOffset * sm.getUnitSize();
        byte[] b = new byte[length];
        readFully(b);
        RandomAccess ra = new RandomAccessBuffer(sm, byteOrder);
        ra.write(b, offset, length);
        ra.close();
    }

    public String readLine() throws IOException {
        return new String(readByteLine());
    }

    public byte[] readByteLine() throws IOException {
        long start = getFilePointer();
        long end = start;
        boolean finished = false;
        int length = 0;
        while (!finished) {
            switch (read()) {
                case -1:
                case '\n':
                    finished = true;
                    end = getFilePointer();
                    length = (int) (end - start);
                    break;
                case '\r':
                    finished = true;
                    end = getFilePointer();
                    length = (int) (end - start);
                    if ((read()) == '\n') {
                        end = getFilePointer();
                    }
                    break;
            }
        }
        byte[] b = new byte[length];
        seek(start);
        readFully(b);
        seek(end);
        return b;
    }

    public String readUTF() throws IOException {
        return DataInputStream.readUTF(this);
    }

    public short readShort() throws IOException {
        return dataInput.readShort();
    }

    public int readUnsignedShort() throws IOException {
        return dataInput.readUnsignedShort();
    }

    public char readChar() throws IOException {
        return dataInput.readChar();
    }

    public int readInt() throws IOException {
        return dataInput.readInt();
    }

    public long readLong() throws IOException {
        return dataInput.readLong();
    }

    /**
     * not all images are tiffs, so we don't throw exception if called from constructor
     * @param byteOrder
     * @throws IOException
     */
    protected void _setByteOrder(int byteOrder) throws IOException {
        if (byteOrder == RandomAccessFactory.AUTO_ENDIAN) {
            byteOrder = readByteOrder();
        }
        if (this.byteOrder == byteOrder) {
            if (this.dataInput != null) {
                return;
            }
        }
        switch (byteOrder) {
            case BIG_ENDIAN:
                this.byteOrder = byteOrder;
                dataInput = dibe;
                break;
            case LITTLE_ENDIAN:
                this.byteOrder = byteOrder;
                dataInput = dile;
                break;
            default:
                this.byteOrder = BIG_ENDIAN;
                dataInput = dibe;
        }
    }

    public void setByteOrder(int byteOrder) throws IOException {
        if (this.byteOrder == RandomAccessFactory.AUTO_ENDIAN) {
            this.byteOrder = readByteOrder();
        }
        if (this.byteOrder == byteOrder) {
            if (this.dataInput != null) {
                return;
            }
        }
        switch (byteOrder) {
            case BIG_ENDIAN:
                this.byteOrder = byteOrder;
                dataInput = dibe;
                break;
            case LITTLE_ENDIAN:
                this.byteOrder = byteOrder;
                dataInput = dile;
                break;
            default:
                throw new RuntimeException("Wrong byteOrder:" + Integer.toHexString(byteOrder));
        }
    }

    /**
     * try to read byte order from file (assuming it is a tiff file)
     *
     * @throws IOException
     */
    protected int readByteOrder() throws IOException {
        long pos = getFilePointer();
        seek(0);
        int byteOrder = (_read() << 8) | _read();
        seek(pos);
        return byteOrder == LITTLE_ENDIAN ? byteOrder : BIG_ENDIAN;
    }

    public int getByteOrder() {
        return byteOrder;
    }

    class DataInputLE implements EDataInput {
        public short readShort() throws IOException {
            int ch = _read() | (_read() << 8);
            return (short) ch;
        }

        public int readUnsignedShort() throws IOException {
            int ch = _read() | (_read() << 8);
            return ch;
        }

        public char readChar() throws IOException {
            int ch = _read() | (_read() << 8);
            return (char) ch;
        }

        public int readInt() throws IOException {
            return _read() | (_read() << 8) | (_read() << 16) | (_read() << 24);
        }

        public long readLong() throws IOException {
            return (readInt() & 0xFFFFFFFFL) | ((long) readInt() << 32);
        }
    }

    class DataInputBE implements EDataInput {
        public short readShort() throws IOException {
            return (short) ((_read() << 8) | _read());
        }

        public int readUnsignedShort() throws IOException {
            return (_read() << 8) | _read();
        }

        public char readChar() throws IOException {
            return (char) ((_read() << 8) | _read());
        }

        public int readInt() throws IOException {
            return (_read() << 24) | (_read() << 16) | (_read() << 8) | _read();
        }

        public long readLong() throws IOException {
            return (((long) readInt()) << 32) + (readInt() & 0xFFFFFFFFL);
        }
    }
}
