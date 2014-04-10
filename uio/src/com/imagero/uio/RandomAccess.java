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

import java.io.DataOutput;
import java.io.IOException;

/**
 * interface for read/write access to data
 * Allows unified read/write access to data - arrays, files, streams and many other.
 *
 * @author Andrei Kouznetsov
 *         Date: 08.11.2003
 *         Time: 12:59:40
 */
public interface RandomAccess extends RandomAccessRO, DataOutput {
    /**
     * Set length of data.
     *
     * @param newLength new length of data
     *
     * @throws IOException
     */
    void setLength(long newLength) throws IOException;

    /**
     * Writes short array to the output
     *
     * @param data short array
     *
     * @throws IOException
     */
    void write(short[] data) throws IOException;

    /**
     * Writes short array to the output
     *
     * @param data      short array
     * @param byteOrder byte order in which short array read into temporary buffer
     *
     * @throws IOException
     */
    void write(short[] data, int byteOrder) throws IOException;

    /**
     * Writes short array to the output
     *
     * @param data   short array
     * @param offset start offset in array
     * @param length length number of shorts to write
     *
     * @throws IOException
     */
    void write(short[] data, int offset, int length) throws IOException;

    /**
     * Writes short array to the output
     *
     * @param data      short array
     * @param offset    start offset in array
     * @param length    length number of shorts to write
     * @param byteOrder byte order in which short array read into temporary buffer
     *
     * @throws IOException
     */
    void write(short[] data, int offset, int length, int byteOrder) throws IOException;

    /**
     * Writes char array to the output
     *
     * @param data char array
     *
     * @throws IOException
     */
    void write(char[] data) throws IOException;

    /**
     * Writes char array to the output
     *
     * @param data      char array
     * @param byteOrder byte order in which char array read into temporary buffer
     *
     * @throws IOException
     */
    void write(char[] data, int byteOrder) throws IOException;

    /**
     * Writes char array to the output
     *
     * @param data   char array
     * @param offset start offset in array
     * @param length length number of shorts to write
     *
     * @throws IOException
     */
    void write(char[] data, int offset, int length) throws IOException;

    /**
     * Writes char array to the output
     *
     * @param data      char array
     * @param offset    start offset in array
     * @param length    length number of shorts to write
     * @param byteOrder byte order in which char array read into temporary buffer
     *
     * @throws IOException
     */
    void write(char[] data, int offset, int length, int byteOrder) throws IOException;

    /**
     * Writes int array to the output
     *
     * @param data int array
     *
     * @throws IOException
     */
    void write(int[] data) throws IOException;

    /**
     * Writes int array to the output
     *
     * @param data      int array
     * @param byteOrder byte order in which int array read into temporary buffer
     *
     * @throws IOException
     */
    void write(int[] data, int byteOrder) throws IOException;

    /**
     * Writes int array to the output
     *
     * @param data   int array
     * @param offset start offset in array
     * @param length length number of ints to write
     *
     * @throws IOException
     */
    void write(int[] data, int offset, int length) throws IOException;

    /**
     * Writes int array to the output
     *
     * @param data      int array
     * @param offset    start offset in array
     * @param length    length number of ints to write
     * @param byteOrder byte order in which int array read into temporary buffer
     *
     * @throws IOException
     */
    void write(int[] data, int offset, int length, int byteOrder) throws IOException;

    /**
     * Writes float array to the output
     *
     * @param data float array
     *
     * @throws IOException
     */
    void write(float[] data) throws IOException;

    /**
     * Writes float array to the output
     *
     * @param data      float array
     * @param byteOrder byte order in which float array read into temporary buffer
     *
     * @throws IOException
     */
    void write(float[] data, int byteOrder) throws IOException;

    /**
     * Writes int array to the output
     *
     * @param data   int array
     * @param offset start offset in array
     * @param length length number of ints to write
     *
     * @throws IOException
     */
    void write(float[] data, int offset, int length) throws IOException;

    /**
     * Writes float array to the output
     *
     * @param data      float array
     * @param offset    start offset in array
     * @param length    length number of float to write
     * @param byteOrder byte order in which float array read into temporary buffer
     *
     * @throws IOException
     */
    void write(float[] data, int offset, int length, int byteOrder) throws IOException;

    /**
     * Writes long array to the output
     *
     * @param data long array
     *
     * @throws IOException
     */
    void write(long[] data) throws IOException;

    /**
     * Writes long array to the output
     *
     * @param data      long array
     * @param byteOrder byte order in which long array read into temporary buffer
     *
     * @throws IOException
     */
    void write(long[] data, int byteOrder) throws IOException;

    /**
     * Writes long array to the output
     *
     * @param data   long array
     * @param offset start offset in array
     * @param length length number of longs to write
     *
     * @throws IOException
     */
    void write(long[] data, int offset, int length) throws IOException;

    /**
     * Writes long array to the output
     *
     * @param data      long array
     * @param offset    start offset in array
     * @param length    length number of longs to write
     * @param byteOrder byte order in which long array read into temporary buffer
     *
     * @throws IOException
     */
    void write(long[] data, int offset, int length, int byteOrder) throws IOException;

    /**
     * Writes double array to the output
     *
     * @param data double array
     *
     * @throws IOException
     */
    void write(double[] data) throws IOException;

    /**
     * Writes double array to the output
     *
     * @param data      double array
     * @param byteOrder byte order in which double array read into temporary buffer
     *
     * @throws IOException
     */
    void write(double[] data, int byteOrder) throws IOException;

    /**
     * Writes double array to the output
     *
     * @param data   double array
     * @param offset start offset in array
     * @param length number of doubles to write
     *
     * @throws IOException
     */
    void write(double[] data, int offset, int length) throws IOException;

    /**
     * Writes double array to the output
     *
     * @param data      double array
     * @param offset    start offset in array
     * @param length    number of doubles to write
     * @param byteOrder byte order in which long array read into temporary buffer
     *
     * @throws IOException
     */
    void write(double[] data, int offset, int length, int byteOrder) throws IOException;

}
