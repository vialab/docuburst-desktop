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

import java.io.DataInput;
import java.io.IOException;

/**
 * Interface for read only access to data.
 * Allows unified read access to data - arrays, files, streams and many other.
 *
 * @author Andrei Kouznetsov
 *         <br>
 *         Date: 08.11.2003
 *         Time: 12:59:40
 */
public interface RandomAccessRO extends Input, DataInput {
    final int BIG_ENDIAN = 0x4D4D;
    final int LITTLE_ENDIAN = 0x4949;

    /**
     * Since this is an interface and is not restricted to files, "getFilePointer" is wrong name for this method.
     * But I leaved it so for easier porting from RandomAccessFile
     *
     * @return current cursor position
     *
     * @throws IOException
     */
    long getFilePointer() throws IOException;

    /**
     * get length of data
     *
     * @return data length (in bytes)
     *
     * @throws IOException
     */
    long length() throws IOException;

    /**
     * set current cursor position to specified <code>offset</code>
     *
     * @param offset new cursor position
     *
     * @throws IOException
     */
    void seek(long offset) throws IOException;

    /**
     * get byte order
     *
     * @return RandomAccessFactory.BIG_ENDIAN or RandomAccessFactory.LITTLE_ENDIAN
     *
     * @see RandomAccessFactory#BIG_ENDIAN
     * @see RandomAccessFactory#LITTLE_ENDIAN
     */
    int getByteOrder();

    /**
     * set byte order
     *
     * @param byteOrder
     *
     * @throws IOException
     * @see RandomAccessFactory#BIG_ENDIAN
     * @see RandomAccessFactory#LITTLE_ENDIAN
     */
    void setByteOrder(int byteOrder) throws IOException;

    /**
     * read data into short array
     *
     * @param dest buffer to store data
     *
     * @throws IOException
     */
    void readFully(short[] dest) throws IOException;

    /**
     * read data into short array
     *
     * @param dest      buffer to store data
     * @param byteOrder byte order
     *
     * @throws IOException
     */
    void readFully(short[] dest, int byteOrder) throws IOException;

    /**
     * read data into short array
     *
     * @param dest       buffer to store data
     * @param destOffset offset in buffer
     * @param len        number of shorts to read
     *
     * @throws IOException
     */
    void readFully(short[] dest, int destOffset, int len) throws IOException;

    /**
     * read data into short array
     *
     * @param dest       buffer to store data
     * @param destOffset offset in buffer
     * @param len        number of shorts to read
     * @param byteOrder  byte order
     *
     * @throws IOException
     */
    void readFully(short[] dest, int destOffset, int len, int byteOrder) throws IOException;

    /**
     * read data into char array
     *
     * @param dest buffer to store data
     *
     * @throws IOException
     */
    void readFully(char[] dest) throws IOException;

    /**
     * read data into char array
     *
     * @param dest      buffer to store data
     * @param byteOrder byte order
     *
     * @throws IOException
     */
    void readFully(char[] dest, int byteOrder) throws IOException;

    /**
     * read data into char array
     *
     * @param dest       buffer to store data
     * @param destOffset offset in buffer
     * @param len        number of chars to read
     *
     * @throws IOException
     */
    void readFully(char[] dest, int destOffset, int len) throws IOException;

    /**
     * read data into char array
     *
     * @param dest       buffer to store data
     * @param destOffset offset in buffer
     * @param len        number of chars to read
     * @param byteOrder  byte order
     *
     * @throws IOException
     */
    void readFully(char[] dest, int destOffset, int len, int byteOrder) throws IOException;

    /**
     * read data into int array
     *
     * @param dest buffer to store data
     *
     * @throws IOException
     */
    void readFully(int[] dest) throws IOException;

    /**
     * read data into int array
     *
     * @param dest      buffer to store data
     * @param byteOrder byte order
     *
     * @throws IOException
     */
    void readFully(int[] dest, int byteOrder) throws IOException;

    /**
     * read data into int array
     *
     * @param dest       buffer to store data
     * @param destOffset offset in buffer
     * @param len        number of ints to read
     *
     * @throws IOException
     */
    void readFully(int[] dest, int destOffset, int len) throws IOException;

    /**
     * read data into int array
     *
     * @param dest       buffer to store data
     * @param destOffset offset in buffer
     * @param len        number of ints to read
     * @param byteOrder  byte order
     *
     * @throws IOException
     */
    void readFully(int[] dest, int destOffset, int len, int byteOrder) throws IOException;

    /**
     * read data into long array
     *
     * @param dest buffer to store data
     *
     * @throws IOException
     */
    void readFully(long[] dest) throws IOException;

    /**
     * read data into long array
     *
     * @param dest      buffer to store data
     * @param byteOrder byte order
     *
     * @throws IOException
     */
    void readFully(long[] dest, int byteOrder) throws IOException;

    /**
     * read data into long array
     *
     * @param dest       buffer to store data
     * @param destOffset offset in buffer
     * @param len        number of longs to read
     *
     * @throws IOException
     */
    void readFully(long[] dest, int destOffset, int len) throws IOException;

    /**
     * read data into long array
     *
     * @param dest       buffer to store data
     * @param destOffset offset in buffer
     * @param len        number of longs to read
     * @param byteOrder  byte order
     *
     * @throws IOException
     */
    void readFully(long[] dest, int destOffset, int len, int byteOrder) throws IOException;

    /**
     * read data into float array
     *
     * @param dest buffer to store data
     *
     * @throws IOException
     */
    void readFully(float[] dest) throws IOException;

    /**
     * read data into float array
     *
     * @param dest      buffer to store data
     * @param byteOrder byte order
     *
     * @throws IOException
     */
    void readFully(float[] dest, int byteOrder) throws IOException;

    /**
     * read data into float array
     *
     * @param dest       buffer to store data
     * @param destOffset offset in buffer
     * @param len        number of floats to read
     *
     * @throws IOException
     */
    void readFully(float[] dest, int destOffset, int len) throws IOException;

    /**
     * read data into float array
     *
     * @param dest       buffer to store data
     * @param destOffset offset in buffer
     * @param len        number of floats to read
     * @param byteOrder  byte order
     *
     * @throws IOException
     */
    void readFully(float[] dest, int destOffset, int len, int byteOrder) throws IOException;

    /**
     * read data into double array
     *
     * @param dest buffer to store data
     *
     * @throws IOException
     */
    void readFully(double[] dest) throws IOException;

    /**
     * read data into double array
     *
     * @param dest      buffer to store data
     * @param byteOrder byte order
     *
     * @throws IOException
     */
    void readFully(double[] dest, int byteOrder) throws IOException;

    /**
     * read data into double array
     *
     * @param dest       buffer to store data
     * @param destOffset offset in buffer
     * @param len        number of doubles to read
     *
     * @throws IOException
     */
    void readFully(double[] dest, int destOffset, int len) throws IOException;

    /**
     * read data into double array
     *
     * @param dest       buffer to store data
     * @param destOffset offset in buffer
     * @param len        number of doubles to read
     * @param byteOrder  byte order
     *
     * @throws IOException
     */
    void readFully(double[] dest, int destOffset, int len, int byteOrder) throws IOException;

    /**
     * like readLine but returns byte array
     * @return byte array
     */
    byte [] readByteLine() throws IOException;
}
