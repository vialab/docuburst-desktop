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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * implementation of RandomAccess
 *
 * @author Andrei Kouznetsov
 *         Date: 08.11.2003
 *         Time: 21:37:03
 */
public abstract class AbstractRandomAccess extends AbstractRandomAccessRO implements RandomAccess {

    private DataOutputBE dobe = new DataOutputBE();
    private DataOutputLE dole = new DataOutputLE();

    EDataOutput dataOutput;

    public void writeBoolean(boolean b) throws IOException {
        write(b ? 1 : 0);
    }

    public void writeByte(int b) throws IOException {
        write(b);
    }

    public void write(short[] sh) throws IOException {
        write(sh, getByteOrder());
    }

    public void write(short[] sh, int byteOrder) throws IOException {
        write(sh, 0, sh.length, byteOrder);
    }

    public void write(short[] sh, int offset, int length) throws IOException {
        write(sh, offset, length, getByteOrder());
    }

    public void write(short[] sh, int offset, int length, int byteOrder) throws IOException {
        RandomAccessRO ro = RandomAccessFactory.createBufferedRO(sh, offset, length);
        ro.setByteOrder(byteOrder);
        byte[] b = new byte[length << 1];
        ro.readFully(b);
        write(b);
    }

    public void write(char[] sh) throws IOException {
        write(sh, getByteOrder());
    }

    public void write(char[] sh, int byteOrder) throws IOException {
        write(sh, 0, sh.length, byteOrder);
    }

    public void write(char[] sh, int offset, int length) throws IOException {
        write(sh, offset, length, getByteOrder());
    }

    public void write(char[] sh, int offset, int length, int byteOrder) throws IOException {
        RandomAccessRO ro = RandomAccessFactory.createBufferedRO(sh, offset, length);
        ro.setByteOrder(byteOrder);
        byte[] b = new byte[length << 1];
        ro.readFully(b);
        write(b);
    }

    public void write(int[] source) throws IOException {
        write(source, getByteOrder());
    }

    public void write(int[] source, int byteOrder) throws IOException {
        write(source, 0, source.length, byteOrder);
    }

    public void write(int[] source, int offset, int length) throws IOException {
        write(source, offset, length, getByteOrder());
    }

    public void write(int[] source, int offset, int length, int byteOrder) throws IOException {
        RandomAccessRO ro = RandomAccessFactory.createBufferedRO(source, offset, length);
        ro.setByteOrder(byteOrder);
        byte[] b = new byte[length << 2];
        ro.readFully(b);
        write(b);
    }

    public void write(float[] source) throws IOException {
        write(source, getByteOrder());
    }

    public void write(float[] source, int byteOrder) throws IOException {
        write(source, 0, source.length, byteOrder);
    }

    public void write(float[] source, int offset, int length) throws IOException {
        write(source, offset, length, getByteOrder());
    }

    public void write(float[] source, int offset, int length, int byteOrder) throws IOException {
        RandomAccessRO ro = RandomAccessFactory.createBufferedRO(source, offset, length);
        ro.setByteOrder(byteOrder);
        byte[] b = new byte[length << 2];
        ro.readFully(b);
        write(b);
    }

    public void write(long[] source) throws IOException {
        write(source, getByteOrder());
    }

    public void write(long[] source, int byteOrder) throws IOException {
        write(source, 0, source.length, byteOrder);
    }

    public void write(long[] source, int offset, int length) throws IOException {
        write(source, offset, length, getByteOrder());
    }

    public void write(long[] source, int offset, int length, int byteOrder) throws IOException {
        RandomAccessRO ro = RandomAccessFactory.createBufferedRO(source, offset, length);
        ro.setByteOrder(byteOrder);
        byte[] b = new byte[length << 3];
        ro.readFully(b);
        write(b);
    }

    public void write(double[] source) throws IOException {
        write(source, getByteOrder());
    }

    public void write(double[] source, int byteOrder) throws IOException {
        write(source, 0, source.length, byteOrder);
    }

    public void write(double[] source, int offset, int length) throws IOException {
        write(source, offset, length, getByteOrder());
    }

    public void write(double[] source, int offset, int length, int byteOrder) throws IOException {
        RandomAccessRO ro = RandomAccessFactory.createBufferedRO(source, offset, length);
        ro.setByteOrder(byteOrder);
        byte[] b = new byte[length << 3];
        ro.readFully(b);
        write(b);
    }

    public final void writeFloat(float v) throws IOException {
        final int a = Float.floatToIntBits(v);
        writeInt(a);
    }

    public final void writeDouble(double v) throws IOException {
        writeLong(Double.doubleToLongBits(v));
    }

    public void writeBytes(String s) throws IOException {
        write(s.getBytes());
    }

    public void writeChars(String s) throws IOException {
        for(int i = 0; i < s.length(); i++) {
            writeChar(s.charAt(i));
        }
    }

    public void writeUTF(String str) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(str.length());
        DataOutputStream dataOut = new DataOutputStream(out);
        dataOut.writeUTF(str);
        dataOut.flush();
        dataOut.close();
        byte[] b = out.toByteArray();
        write(b);
    }

    public void writeShort(int v) throws IOException {
        dataOutput.writeShort(v);
    }

    public void writeChar(int v) throws IOException {
        dataOutput.writeChar(v);
    }

    public void writeInt(int v) throws IOException {
        dataOutput.writeInt(v);
    }

    public void writeLong(long v) throws IOException {
        dataOutput.writeLong(v);
    }

    public void setByteOrder(int byteOrder) throws IOException {
        if(byteOrder == RandomAccessFactory.AUTO_ENDIAN) {
            byteOrder = readByteOrder();
        }
        if(this.byteOrder == byteOrder) {
            return;
        }
        switch(byteOrder) {
            case BIG_ENDIAN:
                dataOutput = dobe;
                break;
            case LITTLE_ENDIAN:
                dataOutput = dole;
                break;
            default:
                throw new RuntimeException("Wrong byteOrder:" + byteOrder);
        }
        super.setByteOrder(byteOrder);
    }

    /**
     * not all images are tiffs, so we don't throw exception if called from constructor
     * @param byteOrder
     * @throws IOException
     */
    protected void _setByteOrder(int byteOrder) throws IOException {
        if(byteOrder == RandomAccessFactory.AUTO_ENDIAN) {
            byteOrder = readByteOrder();
        }
        if(this.byteOrder == byteOrder) {
            return;
        }
        switch(byteOrder) {
            case BIG_ENDIAN:
                dataOutput = dobe;
                break;
            case LITTLE_ENDIAN:
                dataOutput = dole;
                break;
            default:
                byteOrder = BIG_ENDIAN;
                dataOutput = dobe;
        }
        super._setByteOrder(byteOrder);
    }

    class DataOutputLE implements EDataOutput {
        public final void writeShort(int a) throws IOException {
            write(a & 0xFF);
            write((a >> 8) & 0xFF);
        }

        public final void writeChar(int a) throws IOException {
            write(a & 0xFF);
            write((a >> 8) & 0xFF);
        }

        public final void writeInt(int a) throws IOException {
            write(a & 0xFF);
            write((a >>> 8) & 0xFF);
            write((a >>> 16) & 0xFF);
            write((a >>> 24) & 0xFF);
        }

        public final void writeLong(long a) throws IOException {
            writeInt((int) (a & 0xFFFFFFFF));
            writeInt((int) ((a >>> 32) & 0xFFFFFFFF));
        }
    }

    class DataOutputBE implements EDataOutput {
        public void writeShort(int a) throws IOException {
            write((a >>> 8) & 0xFF);
            write(a & 0xFF);
        }

        public void writeChar(int a) throws IOException {
            write((a >>> 8) & 0xFF);
            write(a & 0xFF);
        }

        public void writeInt(int a) throws IOException {
            write((a >>> 24) & 0xFF);
            write((a >>> 16) & 0xFF);
            write((a >>> 8) & 0xFF);
            write(a & 0xFF);
        }

        public void writeLong(long a) throws IOException {
            writeInt((int) ((a >>> 32) & 0xFFFFFFFF));
            writeInt((int) (a & 0xFFFFFFFF));
        }
    }
}
