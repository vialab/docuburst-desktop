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

package com.imagero.uio.io;

import com.imagero.uio.RandomAccessRO;
import com.imagero.uio.Sys;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 * IOutils.java
 *
 * @author Andrei Kouznetsov
 */
public class IOutils {

    /**
     * close silently stream<br>
     * no exception it thrown
     *
     * @param bw
     */
    public static void closeStream(BufferedWriter bw) {
        try {
            if (bw != null) {
                bw.close();
            }
        }
        catch (IOException ex) {
        }
    }

    /**
     * close silently stream<br>
     * no exception it thrown
     *
     * @param br
     */
    public static void closeStream(BufferedReader br) {
        try {
            if (br != null) {
                br.close();
            }
        }
        catch (IOException ex) {
        }
    }

    /**
     * close silently stream<br>
     * no exception it thrown
     *
     * @param is
     */
    public static void closeStream(InputStream is) {
        try {
            if (is != null) {
                is.close();
            }
        }
        catch (IOException ex) {
        }
    }

    /**
     * close silently stream<br>
     * no exception it thrown
     *
     * @param os
     */
    public static void closeStream(OutputStream os) {
        try {
            if (os != null) {
                os.close();
            }
        }
        catch (IOException ex) {
        }
    }

    /**
     * close silently stream<br>
     * no exception it thrown
     *
     * @param raf
     */
    public static void closeStream(RandomAccessFile raf) {
        try {
            if (raf != null) {
                raf.close();
            }
        }
        catch (IOException ex) {
        }
    }

    /**
     * close silently stream<br>
     * no exception it thrown
     *
     * @param ro
     */
    public static void closeStream(RandomAccessRO ro) {
        try {
            if (ro != null) {
                ro.close();
            }
        }
        catch (IOException ex) {
        }
    }

    static final int[] mask = {1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768};
    static byte b0 = (byte) '0';
    static byte b1 = (byte) '1';

    public static String toBinaryString(byte value) {
        byte[] b = new byte[8];
        int cnt = 0;
        for (int i = 7; i > -1; i--) {
            b[cnt++] = (value & mask[i]) == 0 ? b0 : b1;
        }
        return new String(b);
    }

    public static String toBinaryString(char value) {
        byte[] b = new byte[16];
        int cnt = 0;
        for (int i = 15; i > -1; i--) {
            b[cnt++] = (value & mask[i]) == 0 ? b0 : b1;
        }
        return new String(b);
    }

    public static String toBinaryString(int value, int length) {
        byte[] b = new byte[length];
        int cnt = 0;
        for (int i = length - 1; i > -1; i--) {
            if (((value >> i) & 1) == 1) {
                b[cnt++] = b1;
            }
            else {
                b[cnt++] = b0;
            }
        }
        return new String(b);
    }

    final static byte[] digits = {
        (byte) '0', (byte) '1', (byte) '2', (byte) '3', (byte) '4', (byte) '5',
        (byte) '6', (byte) '7', (byte) '8', (byte) '9', (byte) 'a', (byte) 'b',
        (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f'
    };

    public static String toHexString(byte value) {
        return toUnsignedString(value & 0xFF, 4);
    }

    private static String toUnsignedString(int i, int shift) {
        byte[] buf = new byte[]{(byte) '0', (byte) '0'};
        int charPos = 2;
        int radix = 1 << shift;
        int mask = radix - 1;
        do {
            buf[--charPos] = digits[i & mask];
            i >>>= shift;
        }
        while (i != 0);

        return new String(buf);
    }

    public static void printHexByte(int value) {
        printHexImpl(value & 0xFFFF, 2);
    }

    public static void printlnHexByte(int value) {
        printHexImpl(value & 0xFFFF, 2);
        Sys.out.println("");
    }

    public static void printHexShort(int value) {
        printHexImpl(value & 0xFFFF, 4);
    }

    public static void printlnHexShort(int value) {
        printHexImpl(value & 0xFFFF, 4);
        Sys.out.println("");
    }

    public static void printHexInt(int value) {
        printHexImpl(value & 0xFFFFFFFF, 8);
    }

    public static void printlnHexInt(int value) {
        printHexImpl(value & 0xFFFFFFFF, 8);
        Sys.out.println("");
    }

    public static void printHexLong(long value) {
        printHexImpl(value & 0xFFFFFFFFFFFFFFFFL, 16);
    }

    public static void printlnHexLong(long value) {
        printHexImpl(value & 0xFFFFFFFFFFFFFFFFL, 16);
        Sys.out.println("");
    }

    static void printHexImpl(long value, int length) {
        String s = Long.toHexString(value);
        //Sys.out.println("***********************" + s + " " + value);
        for (int i = 0, size = length - s.length(); i < size; i++) {
            Sys.out.print("0");
        }
        Sys.out.print(s);
    }

    static void printHexImpl(int value, int length) {
        String s = Integer.toHexString(value);
        if (s.length() > length) {
            s = s.substring(s.length() - length);
        }
        //Sys.out.println("***********************" + s + " " + value);
        for (int i = 0, size = length - s.length(); i < size; i++) {
            Sys.out.print("0");
        }
        Sys.out.print(s);
    }

    public static String getExtension(File f) {
        String s = f.getName();
        return s.substring(s.lastIndexOf(".") + 1).toUpperCase();
    }

    /**
     * read little-endian short
     */
    public static int readShort4D(InputStream in) throws IOException {
        return ((in.read() & 0xFF) << 8) + ((in.read() & 0xFF) << 0);
    }

    /**
     * read little-endian short
     */
    public static int readShort4D(RandomAccessFile in) throws IOException {
        return ((in.read() & 0xFF) << 8) + ((in.read() & 0xFF) << 0);
    }

    /**
     * read little-endian short
     */
    public static int readShort4D(RandomAccessRO in) throws IOException {
        return ((in.read() & 0xFF) << 8) + ((in.read() & 0xFF) << 0);
    }

    /**
     * read big-endian short
     */
    public static int readShort49(InputStream in) throws IOException {
        return ((in.read() & 0xFF) << 0) + ((in.read() & 0xFF) << 8);
    }

    /**
     * read big-endian short
     */
    public static int readShort49(RandomAccessFile in) throws IOException {
        return ((in.read() & 0xFF) << 0) + ((in.read() & 0xFF) << 8);
    }

    /**
     * read big-endian short
     */
    public static int readShort49(RandomAccessRO in) throws IOException {
        return ((in.read() & 0xFF) << 0) + ((in.read() & 0xFF) << 8);
    }

    /**
     * read little-endian int
     */
    public static int readInt4D(InputStream in) throws IOException {
        return (((in.read() & 0xFF) << 24) + ((in.read() & 0xFF) << 16) + ((in.read() & 0xFF) << 8) + ((in.read() & 0xFF) << 0));
    }

    /**
     * read little-endian int
     */
    public static int readInt4D(RandomAccessFile in) throws IOException {
        int b0 = (in.read() & 0xFF);
        int b1 = (in.read() & 0xFF);
        int b2 = (in.read() & 0xFF);
        int b3 = (in.read() & 0xFF);
        return (b0 << 24) + (b1 << 16) + (b2 << 8) + b3;
    }

    /**
     * read little-endian int
     */
    public static int readInt4D(RandomAccessRO in) throws IOException {
        int b0 = (in.read() & 0xFF);
        int b1 = (in.read() & 0xFF);
        int b2 = (in.read() & 0xFF);
        int b3 = (in.read() & 0xFF);
        return (b0 << 24) + (b1 << 16) + (b2 << 8) + b3;
    }

    /**
     * read big-endian int
     */
    public static int readInt49(InputStream in) throws IOException {
        return ((in.read() & 0xFF) << 0) + ((in.read() & 0xFF) << 8) + ((in.read() & 0xFF) << 16) + ((in.read() & 0xFF) << 24);
    }

    /**
     * read big-endian int
     */
    public static int readInt49(RandomAccessFile in) throws IOException {
        return ((in.read() & 0xFF) << 0) + ((in.read() & 0xFF) << 8) + ((in.read() & 0xFF) << 16) + ((in.read() & 0xFF) << 24);
    }

    /**
     * read big-endian int
     */
    public static int readInt49(RandomAccessRO in) throws IOException {
        return ((in.read() & 0xFF) << 0) + ((in.read() & 0xFF) << 8) + ((in.read() & 0xFF) << 16) + ((in.read() & 0xFF) << 24);
    }

    /**
     * read little-endian long
     */
    public static long readLong4D(InputStream in) throws IOException {
        return ((long) (readInt4D(in)) << 32) + (readInt4D(in) & 0xFFFFFFFFL);
    }

    /**
     * read little-endian long
     */
    public static long readLong4D(RandomAccessFile in) throws IOException {
        return ((long) (readInt4D(in)) << 32) + (readInt4D(in) & 0xFFFFFFFFL);
    }

    /**
     * read little-endian long
     */
    public static long readLong4D(RandomAccessRO in) throws IOException {
        return ((long) (readInt4D(in)) << 32) + (readInt4D(in) & 0xFFFFFFFFL);
    }

    /**
     * read big-endian long
     */
    public static long readLong49(InputStream in) throws IOException {
        return ((long) (readInt49(in)) & 0xFFFFFFFFL) + (readInt49(in) << 32);
    }

    /**
     * read big-endian long
     */
    public static long readLong49(RandomAccessFile in) throws IOException {
        return ((long) (readInt49(in)) & 0xFFFFFFFFL) + (readInt49(in) << 32);
    }

    /**
     * read big-endian long
     */
    public static long readLong49(RandomAccessRO in) throws IOException {
        return ((long) (readInt49(in)) & 0xFFFFFFFFL) + (readInt49(in) << 32);
    }

    public static byte readSByte(RandomAccessRO ro) throws IOException {
        byte b = ro.readByte();
        if (b < 0) {
            b = (byte) -(~(b + 1));
        }
        return b;
    }

    public static short readSShort(RandomAccessRO ro) throws IOException {
        short b = ro.readShort();
        if (b < 0) {
            b = (short) -(~(b + 1));
        }
        return b;
    }

    public static int readSInt(RandomAccessRO ro) throws IOException {
        int b = ro.readInt();
        if (b < 0) {
            b = -(~(b + 1));
        }
        return b;
    }

    public static long readSLong(RandomAccessRO ro) throws IOException {
        long b = ro.readLong();
        if (b < 0) {
            b = -(~(b + 1));
        }
        return b;
    }

    /**
     * Read byte array and convert from 2's complement
     * @param ro RandomAccessRO
     * @param b0 byte array
     * @throws IOException
     */
    public static void readFullyS(RandomAccessRO ro, byte [] b0) throws IOException {
        ro.readFully(b0);
        for (int i = 0; i < b0.length; i++) {
            if(b0[i] < 0) {
                b0[i] = (byte) -(~(b0[i] + 1));
            }
        }
    }

    /**
     * Read short array and convert from 2's complement.
     * @param ro RandomAccessRO
     * @param b0 short array
     * @throws IOException
     */
    public static void readFullyS(RandomAccessRO ro, short [] b0) throws IOException {
        ro.readFully(b0);
        for (int i = 0; i < b0.length; i++) {
            if(b0[i] < 0) {
                b0[i] = (short) -(~(b0[i] + 1));
            }
        }
    }

    /**
     * Read int array and convert from 2's complement
     * @param ro RandomAccessRO
     * @param b0 int array
     * @throws IOException
     */
    public static void readFullyS(RandomAccessRO ro, int [] b0) throws IOException {
        ro.readFully(b0);
        for (int i = 0; i < b0.length; i++) {
            if(b0[i] < 0) {
                b0[i] = -(~(b0[i] + 1));
            }
        }
    }

    /**
     * Read short array and convert from 2's complement
     * @param ro RandomAccessRO
     * @param b0 long array
     * @throws IOException
     */
    public static void readFullyS(RandomAccessRO ro, long [] b0) throws IOException {
        ro.readFully(b0);
        for (int i = 0; i < b0.length; i++) {
            if(b0[i] < 0) {
                b0[i] = -(~(b0[i] + 1));
            }
        }
    }

    public static void readFully(InputStream in, byte b[]) throws IOException {
        readFully(in, b, 0, b.length);
    }

    public static void readFully(InputStream in, byte b[], int off, int len) throws IOException {
        int n = 0;
        do {
            int count = in.read(b, off + n, len - n);
            if (count < 0) {
                throw new EOFException("eof");
//				return;
            }
            n += count;
        }
        while (n < len);
    }

    /**
     * this method is like readFully, but instead of throwing <code>EOFException</code> it returns count of read bytes
     *
     * @param in InputStream to read
     * @param b  byte array to fill
     *
     * @return number of bytes read into the buffer, or -1 if EOF was reached
     *
     * @throws IOException
     */
    public static int readFully2(InputStream in, byte b[]) throws IOException {
        return readFully2(in, b, 0, b.length);
    }

    /**
     * this method is like readFully, but instead of throwing <code>EOFException</code> it returns count of read bytes
     *
     * @param in  InputStream to read
     * @param b   byte array to fill
     * @param off start offset in byte array
     * @param len number of bytes to read
     *
     * @return number of bytes read into the buffer, or -1 if EOF was reached
     *
     * @throws IOException
     */
    public static int readFully2(InputStream in, byte b[], int off, int len) throws IOException {
        int n = 0;
        int cnt0 = 0;
        do {
            int count = in.read(b, off + n, len - n);
            if (count == 0) {
                cnt0++;
                if (cnt0 >= 3) {
                    break;
                }
            }
            else {
                cnt0 = 0;
            }
            if (count < 0) {
                return n == 0 ? -1 : n;
            }
            n += count;
        }
        while (n < len);
        return n;
    }
}
