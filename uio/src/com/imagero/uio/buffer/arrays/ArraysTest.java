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

import com.imagero.uio.RandomAccessByteArray;
import com.imagero.uio.RandomAccessFactory;
import com.imagero.uio.RandomAccessRO;
import com.imagero.uio.Sys;
import com.imagero.uio.io.BitInputStream;
import com.imagero.uio.io.RLE4InputStream;
import com.imagero.uio.io.RLE8InputStream;
import com.imagero.uio.io.RLEInputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * @author Andrei Kouznetsov
 *         Date: 14.06.2004
 *         Time: 20:14:34
 */
public class ArraysTest extends junit.framework.TestCase {

    public static boolean UNIT_TEST = false;

    public ArraysTest() {
        super("ArraysTest");
    }

    public void testInt() throws IOException {
        if (UNIT_TEST) {
            Sys.out.println("testInt");
            int unitSize = 4;
            int[] a = new int[2];
            byte[] b = new byte[]{(byte) 0xEE, (byte) 0x0, (byte) 0xCC, (byte) 0xBB, (byte) 0xAA, (byte) 0x99, (byte) 0x88, 0x77};

            //read ints with standard java method
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(b));
            for (int i = 0; i < a.length; i++) {
                a[i] = dis.readInt();
            }

            //create manager
            IntArrayBufferManager iadsm = new IntArrayBufferManager(a, a.length);

            //create and fill byte array with little endian data
            byte[] bLE = new byte[unitSize * a.length];
            iadsm.readLE(bLE, 0);

            //read little endian ints
            RandomAccessRO r0 = new RandomAccessByteArray(bLE, RandomAccessFactory.LITTLE_ENDIAN);
            int[] aLE = new int[a.length];
            r0.readFully(aLE);

            for (int i = 0; i < aLE.length; i++) {
                assertEquals(a[i], aLE[i]);
            }

            //create and fill byte array with big endian data
            byte[] bBE = new byte[unitSize * a.length];
            iadsm.readBE(bBE, 0);

            //read big endian ints
            RandomAccessRO r1 = new RandomAccessByteArray(bBE, RandomAccessFactory.BIG_ENDIAN);
            int[] aBE = new int[a.length];
            r1.readFully(aBE);

            for (int i = 0; i < aBE.length; i++) {
                assertEquals(a[i], aBE[i]);
            }
        }
    }

    public void testChar() throws IOException {
        if (UNIT_TEST) {
            Sys.out.println("testChar");
            int unitSize = 2;
            char[] a = new char[4];
            byte[] b = new byte[]{(byte) 0xEE, (byte) 0xDD, (byte) 0xCC, (byte) 0xBB, (byte) 0xAA, (byte) 0x99, (byte) 0x88, 0x77};

            //read chars with standard java methode
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(b));
            for (int i = 0; i < a.length; i++) {
                a[i] = dis.readChar();
            }

            //create manager
            CharArrayBufferManager iadsm = new CharArrayBufferManager(a, a.length);

            //create and fill byte array with little endian data
            byte[] bLE = new byte[unitSize * a.length];
            iadsm.readLE(bLE, 0);

            //read little endian chars
            RandomAccessRO r0 = new RandomAccessByteArray(bLE, RandomAccessFactory.LITTLE_ENDIAN);
            char[] aLE = new char[a.length];
            r0.readFully(aLE);

            for (int i = 0; i < aLE.length; i++) {
                assertEquals(a[i], aLE[i]);
            }

            //create and fill byte array with big endian data
            byte[] bBE = new byte[unitSize * a.length];
            iadsm.readBE(bBE, 0);

            //read big endian chars
            RandomAccessRO r1 = new RandomAccessByteArray(bBE, RandomAccessFactory.BIG_ENDIAN);
            char[] aBE = new char[a.length];
            r1.readFully(aBE);

            for (int i = 0; i < aBE.length; i++) {
                assertEquals(a[i], aBE[i]);
            }
        }
    }

    public void testShort() throws IOException {
        if (UNIT_TEST) {
            Sys.out.println("testShort");
            int unitSize = 2;
            short[] a = new short[4];
            byte[] b = new byte[]{(byte) 0xEE, (byte) 0xDD, (byte) 0xCC, (byte) 0xBB, (byte) 0xAA, (byte) 0x99, (byte) 0x88, 0x77};

            //read shorts with standard java methode
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(b));
            for (int i = 0; i < a.length; i++) {
                a[i] = dis.readShort();
            }

            //create manager
            ShortArrayBufferManager iadsm = new ShortArrayBufferManager(a, a.length);

            //create and fill byte array with little endian data
            byte[] bLE = new byte[unitSize * a.length];
            iadsm.readLE(bLE, 0);

            //read little endian short
            RandomAccessRO r0 = new RandomAccessByteArray(bLE, RandomAccessFactory.LITTLE_ENDIAN);
            short[] aLE = new short[a.length];
            r0.readFully(aLE);

            for (int i = 0; i < aLE.length; i++) {
                assertEquals(a[i], aLE[i]);
            }

            //create and fill byte array with big endian data
            byte[] bBE = new byte[unitSize * a.length];
            iadsm.readBE(bBE, 0);

            //read big endian short
            RandomAccessRO r1 = new RandomAccessByteArray(bBE, RandomAccessFactory.BIG_ENDIAN);
            short[] aBE = new short[a.length];
            r1.readFully(aBE);

            for (int i = 0; i < aBE.length; i++) {
                assertEquals(a[i], aBE[i]);
            }
        }
    }

    public void testDouble() throws IOException {
        if (UNIT_TEST) {
            Sys.out.println("testDouble");
            int unitSize = 8;
            double[] a = new double[]{2.2d, 3.3d};
            byte[] b;

            final ByteArrayOutputStream bout = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bout);
            dos.writeDouble(a[0]);
            dos.writeDouble(a[1]);
            dos.flush();
            dos.close();
            b = bout.toByteArray();

            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(b));
            for (int i = 0; i < a.length; i++) {
                a[i] = dis.readDouble();
            }

            DoubleArrayBufferManager iadsm = new DoubleArrayBufferManager(a, a.length);

            byte[] b1 = new byte[unitSize * a.length];
            iadsm.readBE(b1, 0);

            RandomAccessRO r0 = new RandomAccessByteArray(b1, RandomAccessFactory.BIG_ENDIAN);
            double[] aBE = new double[a.length];
            r0.readFully(aBE);

            for (int i = 0; i < aBE.length; i++) {
                assertTrue(a[i] == aBE[i]);
            }

            byte[] b2 = new byte[unitSize * a.length];
            iadsm.readLE(b2, 0);

            RandomAccessRO r01 = new RandomAccessByteArray(b2, RandomAccessFactory.LITTLE_ENDIAN);
            double[] aLE = new double[a.length];
            r01.readFully(aLE);

            for (int i = 0; i < aLE.length; i++) {
                assertTrue(a[i] == aLE[i]);
            }
        }
    }

    public void testFloat() throws IOException {
        if (UNIT_TEST) {
            Sys.out.println("testFloat");
            int unitSize = 4;
            float[] a = new float[]{2.2f, 3.3f};
            byte[] b;

            final ByteArrayOutputStream bout = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bout);
            dos.writeFloat(a[0]);
            dos.writeFloat(a[1]);
            dos.flush();
            dos.close();
            b = bout.toByteArray();

            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(b));
            for (int i = 0; i < a.length; i++) {
                a[i] = dis.readFloat();
            }

            FloatArrayBufferManager iadsm = new FloatArrayBufferManager(a, a.length);

            byte[] b1 = new byte[unitSize * a.length];
            iadsm.readBE(b1, 0);

            RandomAccessRO r0 = new RandomAccessByteArray(b1, RandomAccessFactory.BIG_ENDIAN);
            float[] aLE = new float[a.length];
            r0.readFully(aLE);

            for (int i = 0; i < aLE.length; i++) {
                assertTrue(a[i] == aLE[i]);
            }

            byte[] b2 = new byte[unitSize * a.length];
            iadsm.readLE(b2, 0);

            RandomAccessRO r01 = new RandomAccessByteArray(b2, RandomAccessFactory.LITTLE_ENDIAN);
            float[] aBE = new float[a.length];
            r01.readFully(aBE);

            for (int i = 0; i < aBE.length; i++) {
                assertTrue(a[i] == aBE[i]);
            }
        }
    }

    public void testLong() throws IOException {
        if (UNIT_TEST) {
            Sys.out.println("testLong");
            int unitSize = 8;
            long[] a = new long[2];
            byte[] b = new byte[]{
                (byte) 0xEE, (byte) 0xDD, (byte) 0xCC, (byte) 0xBB, (byte) 0xAA, (byte) 0x99, (byte) 0x88, 0x77,
                (byte) 0xEE, (byte) 0xDD, (byte) 0xCC, (byte) 0xBB, (byte) 0xAA, (byte) 0x99, (byte) 0x88, 0x77};

            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(b));
            a[0] = dis.readLong();
            a[1] = dis.readLong();

            LongArrayBufferManager iadsm = new LongArrayBufferManager(a, a.length);

            byte[] bLE = new byte[unitSize * a.length];
            iadsm.readLE(bLE, 0);

            RandomAccessRO r0 = new RandomAccessByteArray(bLE, RandomAccessFactory.LITTLE_ENDIAN);
            long[] aLE = new long[a.length];
            r0.readFully(aLE);

            for (int i = 0; i < aLE.length; i++) {
                assertEquals(a[i], aLE[i]);
            }

            byte[] bBE = new byte[unitSize * a.length];
            iadsm.readBE(bBE, 0);

            RandomAccessRO r1 = new RandomAccessByteArray(bBE, RandomAccessFactory.BIG_ENDIAN);
            long[] aBE = new long[a.length];
            r1.readFully(aBE);

            for (int i = 0; i < aBE.length; i++) {
                assertEquals(a[i], aBE[i]);
            }
        }
    }

    public void testRLE8() {
        if (UNIT_TEST) {
            Sys.out.println("testRLE8");

            String c =
                    "0F FF 00 00 " +
                    "02 FF 09 00 04 FF 00 00 " +
                    "04 FF 03 00 03 FF 02 00 03 FF 00 00 " +
                    "04 FF 03 00 04 FF 02 00 02 FF 00 00 " +
                    "04 FF 03 00 04 FF 02 00 02 FF 00 00 " +
                    "04 FF 03 00 04 FF 02 00 02 FF 00 00 " +
                    "04 FF 03 00 03 FF 02 00 03 FF 00 00 " +
                    "04 FF 03 00 01 FF 03 00 04 FF 00 00 " +
                    "04 FF 03 00 01 FF 03 00 04 FF 00 00 " +
                    "04 FF 03 00 03 FF 02 00 03 FF 00 00 " +
                    "04 FF 03 00 04 FF 02 00 02 FF 00 00 " +
                    "04 FF 03 00 04 FF 02 00 02 FF 00 00 " +
                    "04 FF 03 00 03 FF 03 00 02 FF 00 00 " +
                    "02 FF 0A 00 03 FF 00 00 " +
                    "0F FF 00 00 00 01";

            String d =
                    "FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF " +
                    "FF FF 00 00 00 00 00 00 00 00 00 FF FF FF FF " +
                    "FF FF FF FF 00 00 00 FF FF FF 00 00 FF FF FF " +
                    "FF FF FF FF 00 00 00 FF FF FF FF 00 00 FF FF " +
                    "FF FF FF FF 00 00 00 FF FF FF FF 00 00 FF FF " +
                    "FF FF FF FF 00 00 00 FF FF FF FF 00 00 FF FF " +
                    "FF FF FF FF 00 00 00 FF FF FF 00 00 FF FF FF " +
                    "FF FF FF FF 00 00 00 FF 00 00 00 FF FF FF FF " +
                    "FF FF FF FF 00 00 00 FF 00 00 00 FF FF FF FF " +
                    "FF FF FF FF 00 00 00 FF FF FF 00 00 FF FF FF " +
                    "FF FF FF FF 00 00 00 FF FF FF FF 00 00 FF FF " +
                    "FF FF FF FF 00 00 00 FF FF FF FF 00 00 FF FF " +
                    "FF FF FF FF 00 00 00 FF FF FF 00 00 00 FF FF " +
                    "FF FF 00 00 00 00 00 00 00 00 00 00 FF FF FF " +
                    "FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF ";

            ByteArrayOutputStream cout = new ByteArrayOutputStream();
            StringTokenizer cst = new StringTokenizer(c, " ", false);
            while (cst.hasMoreElements()) {
                String s = cst.nextToken();
                cout.write(Integer.parseInt(s, 16));
            }
            byte[] cb = cout.toByteArray();

            ByteArrayOutputStream dout = new ByteArrayOutputStream();
            StringTokenizer dst = new StringTokenizer(d, " ", false);
            while (dst.hasMoreElements()) {
                String s = dst.nextToken();
                dout.write(Integer.parseInt(s, 16));
            }
            byte[] db = dout.toByteArray();

            ByteArrayOutputStream eout = new ByteArrayOutputStream();

            RLE8InputStream in = new RLE8InputStream(new ByteArrayInputStream(cb));
            try {
                while (in.available() > 0) {
                    try {
                        int a = in.read() & 0xFF;
                        eout.write(a);
                    }
                    catch (RLE8InputStream.EndOfLineException ex) {
                        continue;
                    }
                    catch (RLE8InputStream.EndOfBitmapException ex) {
                        break;
                    }
                }
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
            byte[] eb = eout.toByteArray();

            for (int i = 0; i < eb.length; i++) {
                assertTrue(eb[i] == db[i]);
            }
        }
    }

    public void testRLE4() {
        if (UNIT_TEST) {
            Sys.out.println("testRLE8");

            String c =
                    "0F 11 00 00" +
                    "02 11 09 00 04 11 00 00 " +
                    "04 11 03 00 03 11 02 00 03 11 00 00" +
                    "04 11 03 00 04 11 02 00 02 11 00 00" +
                    "04 11 03 00 04 11 02 00 02 11 00 00" +
                    "04 11 03 00 04 11 02 00 02 11 00 00" +
                    "04 11 03 00 03 11 02 00 03 11 00 00" +
                    "04 11 03 00 01 11 03 00 04 11 00 00" +
                    "04 11 03 00 01 11 03 00 04 11 00 00" +
                    "04 11 03 00 03 11 02 00 03 11 00 00" +
                    "04 11 03 00 04 11 02 00 02 11 00 00" +
                    "04 11 03 00 04 11 02 00 02 11 00 00" +
                    "04 11 03 00 03 11 03 00 02 11 00 00" +
                    "02 11 0A 00 03 11 00 00" +
                    "0F 11 00 00 00 01";

            String d =
                    "FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF " +
                    "FF FF 00 00 00 00 00 00 00 00 00 FF FF FF FF " +
                    "FF FF FF FF 00 00 00 FF FF FF 00 00 FF FF FF " +
                    "FF FF FF FF 00 00 00 FF FF FF FF 00 00 FF FF " +
                    "FF FF FF FF 00 00 00 FF FF FF FF 00 00 FF FF " +
                    "FF FF FF FF 00 00 00 FF FF FF FF 00 00 FF FF " +
                    "FF FF FF FF 00 00 00 FF FF FF 00 00 FF FF FF " +
                    "FF FF FF FF 00 00 00 FF 00 00 00 FF FF FF FF " +
                    "FF FF FF FF 00 00 00 FF 00 00 00 FF FF FF FF " +
                    "FF FF FF FF 00 00 00 FF FF FF 00 00 FF FF FF " +
                    "FF FF FF FF 00 00 00 FF FF FF FF 00 00 FF FF " +
                    "FF FF FF FF 00 00 00 FF FF FF FF 00 00 FF FF " +
                    "FF FF FF FF 00 00 00 FF FF FF 00 00 00 FF FF " +
                    "FF FF 00 00 00 00 00 00 00 00 00 00 FF FF FF " +
                    "FF FF FF FF FF FF FF FF FF FF FF FF FF FF FF ";

            ByteArrayOutputStream cout = new ByteArrayOutputStream();
            StringTokenizer cst = new StringTokenizer(c, " ", false);
            while (cst.hasMoreElements()) {
                String s = cst.nextToken();
                cout.write(Integer.parseInt(s, 16));
            }
            byte[] cb = cout.toByteArray();

            ByteArrayOutputStream dout = new ByteArrayOutputStream();
            StringTokenizer dst = new StringTokenizer(d, " ", false);
            while (dst.hasMoreElements()) {
                String s = dst.nextToken();
                dout.write(Integer.parseInt(s, 16));
            }
            byte[] db = dout.toByteArray();

            ByteArrayOutputStream eout = new ByteArrayOutputStream();

            RLEInputStream in = new RLE4InputStream(new BitInputStream(new ByteArrayInputStream(cb)));
            try {
                while (in.available() > 0) {
                    try {
                        int a = in.read() & 0xFF;
                        eout.write(a);
                    }
                    catch (RLE4InputStream.EndOfLineException ex) {
                        continue;
                    }
                    catch (RLE4InputStream.EndOfBitmapException ex) {
                        break;
                    }
                }
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
            byte[] eb = eout.toByteArray();

            for (int i = 0; i < eb.length; i++) {
                assertTrue(eb[i] == db[i]);
            }
        }
    }
}
