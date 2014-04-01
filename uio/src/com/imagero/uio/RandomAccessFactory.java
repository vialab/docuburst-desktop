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

import com.imagero.uio.buffer.Buffer;
import com.imagero.uio.buffer.BufferManager;
import com.imagero.uio.buffer.DefaultBufferManager;
import com.imagero.uio.buffer.HTTPBufferManager;
import com.imagero.uio.buffer.InputStreamBufferManager;
import com.imagero.uio.buffer.MutableBufferManager;
import com.imagero.uio.buffer.MutableRABufferManager;
import com.imagero.uio.buffer.MutableRAFBufferManager;
import com.imagero.uio.buffer.RABufferManager;
import com.imagero.uio.buffer.RAFBufferManager;
import com.imagero.uio.buffer.OutputStreamBufferManager;
import com.imagero.uio.buffer.arrays.CharArrayBufferManager;
import com.imagero.uio.buffer.arrays.DoubleArrayBufferManager;
import com.imagero.uio.buffer.arrays.FloatArrayBufferManager;
import com.imagero.uio.buffer.arrays.IntArrayBufferManager;
import com.imagero.uio.buffer.arrays.LongArrayBufferManager;
import com.imagero.uio.buffer.arrays.ShortArrayBufferManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.OutputStream;
import java.net.URL;

/**
 * Create RandomAccess/RandomAccess from various resources.<br>
 * Since all classes in uio and buffer packages was made public,
 * RandomAccesFactory is no more indispensable and gives not all possibilities.
 * However ist is very useful as example.
 *
 * @author Andrei Kouznetsov
 * @see RandomAccess
 *      Date: 12.11.2003
 *      Time: 19:32:15
 */
public class RandomAccessFactory {
    public static final int BIG_ENDIAN = 0x4D4D;
    public static final int LITTLE_ENDIAN = 0x4949;
    public static final int AUTO_ENDIAN = 0x0;

    /**
     * create RandomAccess from BufferManager
     *
     * @param sm        BufferManager
     * @param byteOrder if AUTO_ENDIAN then byte order is detected. If detection failed then byteOrder is set to BIG_ENDIAN
     *
     * @return RandomAccess
     *
     * @throws IOException
     * @see com.imagero.uio.RandomAccess
     * @see com.imagero.uio.buffer.BufferManager
     * @see com.imagero.uio.buffer.DefaultBufferManager
     * @see com.imagero.uio.buffer.InputStreamBufferManager
     * @see com.imagero.uio.buffer.Buffer
     */
    public static RandomAccessBuffer createBuffered(MutableBufferManager sm, int byteOrder) throws IOException {
        return new RandomAccessBuffer(sm, byteOrder);
    }

    /**
     * create RandomAccess from MutableBufferManager with network byte order (BIG_ENDIAN)
     *
     * @param sm MutableBufferManager
     *
     * @return RandomAccess
     *
     * @throws IOException
     * @see com.imagero.uio.RandomAccess
     * @see com.imagero.uio.buffer.BufferManager
     * @see com.imagero.uio.buffer.DefaultBufferManager
     * @see com.imagero.uio.buffer.InputStreamBufferManager
     * @see com.imagero.uio.buffer.Buffer
     */
    public static RandomAccessBuffer createBuffered(MutableBufferManager sm) throws IOException {
        return new RandomAccessBuffer(sm, BIG_ENDIAN);
    }

    /**
     * create RandomAccessRO from BufferManager
     *
     * @param sm        BufferManager
     * @param byteOrder if AUTO_ENDIAN then byte order is detected. If detection failed then byteOrder is set to BIG_ENDIAN
     *
     * @return RandomAccessRO
     *
     * @throws IOException
     * @see com.imagero.uio.RandomAccessRO
     * @see com.imagero.uio.buffer.BufferManager
     * @see com.imagero.uio.buffer.DefaultBufferManager
     * @see com.imagero.uio.buffer.InputStreamBufferManager
     * @see com.imagero.uio.buffer.Buffer
     */
    public static RandomAccessBufferRO createBufferedRO(BufferManager sm, int byteOrder) throws IOException {
        return new RandomAccessBufferRO(sm, byteOrder);
    }

    /**
     * create RandomAccessRO from BufferManager
     *
     * @param sm BufferManager
     *
     * @return RandomAccessRO
     *
     * @throws IOException
     * @see com.imagero.uio.RandomAccessRO
     * @see com.imagero.uio.buffer.BufferManager
     * @see com.imagero.uio.buffer.DefaultBufferManager
     * @see com.imagero.uio.buffer.InputStreamBufferManager
     * @see com.imagero.uio.buffer.Buffer
     */
    public static RandomAccessBufferRO createBufferedRO(BufferManager sm) throws IOException {
        int byteOrder = BIG_ENDIAN;
        if (sm instanceof RABufferManager) {
            RABufferManager ram = (RABufferManager) sm;
            byteOrder = ram.getByteOrder();
        }
        return new RandomAccessBufferRO(sm, byteOrder);
    }

    /**
     * create RandomAccess from Buffer array.
     * Same as <code>create(new DefaultBufferManager(buffer));</code>
     *
     * @param ds        array of Buffers
     * @param byteOrder if AUTO_ENDIAN then byte order is detected. If detection failed then byteOrder is set to BIG_ENDIAN
     *
     * @return RandomAccess
     *
     * @throws IOException
     * @see com.imagero.uio.RandomAccess
     * @see com.imagero.uio.buffer.BufferManager
     * @see com.imagero.uio.buffer.DefaultBufferManager
     * @see com.imagero.uio.buffer.InputStreamBufferManager
     * @see com.imagero.uio.buffer.Buffer
     */
    public static RandomAccessBuffer createBuffered(Buffer[] ds, int byteOrder) throws IOException {
        return createBuffered(new DefaultBufferManager(ds), byteOrder);
    }

    /**
     * create RandomAccess from Buffer array with default java byte order (BIG_ENDIAN)
     * Same as <code>create(new DefaultBufferManager(buffer));</code>
     *
     * @param ds Buffer array
     *
     * @return RandomAccess
     *
     * @throws IOException
     * @see com.imagero.uio.RandomAccess
     * @see com.imagero.uio.buffer.BufferManager
     * @see com.imagero.uio.buffer.DefaultBufferManager
     * @see com.imagero.uio.buffer.InputStreamBufferManager
     * @see com.imagero.uio.buffer.Buffer
     */
    public static RandomAccessBuffer createBuffered(Buffer[] ds) throws IOException {
        return createBuffered(new DefaultBufferManager(ds), BIG_ENDIAN);
    }

    /**
     * create RandomAccessRO from Buffer array.
     * Same as <code>createRO(new DefaultBufferManager(buffer));</code>
     *
     * @param ds        Buffer array
     * @param byteOrder if AUTO_ENDIAN then byte order is detected. If detection failed then byteOrder is set to BIG_ENDIAN
     *
     * @return RandomAccessRO
     *
     * @throws IOException
     * @see com.imagero.uio.RandomAccessRO
     * @see com.imagero.uio.buffer.BufferManager
     * @see com.imagero.uio.buffer.DefaultBufferManager
     * @see com.imagero.uio.buffer.InputStreamBufferManager
     * @see com.imagero.uio.buffer.Buffer
     */
    public static RandomAccessBufferRO createBufferedRO(Buffer[] ds, int byteOrder) throws IOException {
        return createBufferedRO(new DefaultBufferManager(ds), byteOrder);
    }

    /**
     * create RandomAccessRO from Buffer array with #BIG_ENDIAN byte order.
     * Same as <code>createRO(new DefaultBufferManager(buffer));</code>
     *
     * @param ds Buffer array
     *
     * @return RandomAccessRO
     *
     * @throws IOException
     * @see com.imagero.uio.RandomAccessRO
     * @see com.imagero.uio.buffer.BufferManager
     * @see com.imagero.uio.buffer.DefaultBufferManager
     * @see com.imagero.uio.buffer.InputStreamBufferManager
     * @see com.imagero.uio.buffer.Buffer
     */
    public static RandomAccessRO createBufferedRO(Buffer[] ds) throws IOException {
        return createBufferedRO(new DefaultBufferManager(ds), BIG_ENDIAN);
    }

    /**
     * create RandomAccessRO from InputStream.
     *
     * @param in        InputStream
     * @param byteOrder if AUTO_ENDIAN then byte order is detected. If detection failed then byteOrder is set to BIG_ENDIAN
     *
     * @return RandomAccessRO
     *
     * @throws IOException
     * @see com.imagero.uio.RandomAccessRO
     * @see com.imagero.uio.buffer.BufferManager
     * @see com.imagero.uio.buffer.DefaultBufferManager
     * @see com.imagero.uio.buffer.InputStreamBufferManager
     * @see com.imagero.uio.buffer.Buffer
     */
    public static RandomAccessBufferRO createBufferedRO(InputStream in, int byteOrder) throws IOException {
        return new RandomAccessBufferRO(new InputStreamBufferManager(in), byteOrder);
    }

    /**
     * create RandomAccessRO from InputStream with default for java byte order (BIG_ENDIAN).
     *
     * @param in InputStream
     *
     * @return RandomAccessRO
     *
     * @throws IOException
     * @see com.imagero.uio.RandomAccessRO
     * @see com.imagero.uio.buffer.BufferManager
     * @see com.imagero.uio.buffer.DefaultBufferManager
     * @see com.imagero.uio.buffer.InputStreamBufferManager
     * @see com.imagero.uio.buffer.Buffer
     */
    public static RandomAccessBufferRO createBufferedRO(InputStream in) throws IOException {
        return createBufferedRO(in, BIG_ENDIAN);
    }


    /**
     * create RandomAccess to read from and write to specified file.
     *
     * @param f         File to read/write
     * @param byteOrder if AUTO_ENDIAN then byte order is detected. If detection failed then byteOrder is set to BIG_ENDIAN
     *
     * @return RandomAccess
     *
     * @throws IOException
     * @see com.imagero.uio.RandomAccess
     */
    public static RandomAccess create(File f, int byteOrder) throws IOException {
        return create(new RandomAccessFile(f, "rw"), byteOrder);
    }

    /**
     * create RandomAccess (with BIG_ENDIAN byte order) to read from and write to specified file.
     *
     * @param f File to read/write
     *
     * @return RandomAccess
     *
     * @throws IOException
     * @see com.imagero.uio.RandomAccess
     */
    public static RandomAccess create(File f) throws IOException {
        return create(new RandomAccessFile(f, "rw"), BIG_ENDIAN);
    }

    /**
     * create RandomAccessRO to read from to specified file.
     *
     * @param f         File to read
     * @param byteOrder if AUTO_ENDIAN then byte order is detected. If detection failed then byteOrder is set to BIG_ENDIAN
     *
     * @return RandomAccess
     *
     * @throws IOException
     * @see com.imagero.uio.RandomAccessRO
     */
    public static RandomAccessRO createRO(File f, int byteOrder) throws IOException {
        return createRO(new RandomAccessFile(f, "r"), byteOrder);
    }

    /**
     * create RandomAccessRO with BIG-ENDIAN byte order to read from to specified file.
     *
     * @param f File to read
     *
     * @return RandomAccess
     *
     * @throws IOException
     * @see com.imagero.uio.RandomAccessRO
     */
    public static RandomAccessRO createRO(File f) throws IOException {
        return createRO(new RandomAccessFile(f, "r"), BIG_ENDIAN);
    }

    /**
     * create RandomAccess to read from and write to file with specified name.
     *
     * @param name      file name
     * @param byteOrder if AUTO_ENDIAN then byte order is detected. If detection failed then byteOrder is set to BIG_ENDIAN
     *
     * @return RandomAccess
     *
     * @throws IOException
     */
    public static RandomAccess create(String name, int byteOrder) throws IOException {
        return create(new RandomAccessFile(name, "rw"), byteOrder);
    }

    /**
     * create RandomAccess with BIG-ENDIAN byte order to read from and write to file with specified name.
     *
     * @param name file name
     *
     * @return RandomAccess
     *
     * @throws IOException
     */
    public static RandomAccess create(String name) throws IOException {
        return create(new RandomAccessFile(name, "rw"), BIG_ENDIAN);
    }

    /**
     * create RandomAccessRO to read data from file with specified name.
     *
     * @param name      file name
     * @param byteOrder if AUTO_ENDIAN then byte order is detected. If detection failed then byteOrder is set to BIG_ENDIAN
     *
     * @return RandomAccessRO
     *
     * @throws IOException
     */
    public static RandomAccessRO createRO(String name, int byteOrder) throws IOException {
        return createRO(new RandomAccessFile(name, "r"), byteOrder);
    }

    /**
     * create RandomAccessRO with BIG-ENDIAN byte order to read data from file with specified name.
     *
     * @param name file name
     *
     * @return RandomAccessRO
     *
     * @throws IOException
     */
    public static RandomAccessRO createRO(String name) throws IOException {
        return createRO(new RandomAccessFile(name, "r"), BIG_ENDIAN);
    }

    /**
     * create RandomAccess to read from and write to specified file starting from specified offset in file.
     *
     * @param file      File to read/write
     * @param offset    start offset in file
     * @param byteOrder if AUTO_ENDIAN then byte order is detected. If detection failed then byteOrder is set to BIG_ENDIAN
     *
     * @return RandomAccess
     *
     * @throws IOException
     */
    public static RandomAccess create(File file, long offset, int byteOrder) throws IOException {
        return create(new OffsetRandomAccessFile(file, "rw", offset), byteOrder);
    }

    /**
     * create RandomAccess with BIG-ENDIAN byte order to read from and write to specified file starting from specified offset in file.
     *
     * @param file   File to read/write
     * @param offset start offset in file
     *
     * @return RandomAccess
     *
     * @throws IOException
     */
    public static RandomAccess create(File file, long offset) throws IOException {
        return create(new OffsetRandomAccessFile(file, "rw", offset), BIG_ENDIAN);
    }

    /**
     * create RandomAccessRO to read from specified file starting from specified offset in file.
     * this is the OLD unbuffered method to read file (slow for DataInput methods)
     *
     * @param file      File to read
     * @param byteOrder if AUTO_ENDIAN then byte order is detected. If detection failed then byteOrder is set to BIG_ENDIAN
     * @param offset    start offset in file
     *
     * @return RandomAccessRO
     *
     * @throws IOException
     */
    public static RandomAccessRO createRO(File file, long offset, int byteOrder) throws IOException {
        return createRO(new OffsetRandomAccessFile(file, "r", offset), byteOrder);
    }

    /**
     * create RandomAccessRO with BIG-ENDIAN byte order to read from specified file starting from specified offset in file.
     *
     * @param file   File to read
     * @param offset start offset in file
     *
     * @return RandomAccessRO
     *
     * @throws IOException
     */
    public static RandomAccessRO createRO(File file, long offset) throws IOException {
        return createRO(new OffsetRandomAccessFile(file, "r", offset), BIG_ENDIAN);
    }

    /**
     * create RandomAccess to read from/write to specified segment of the file.
     *
     * @param file      file to read/write
     * @param offset    start of segment
     * @param byteOrder if AUTO_ENDIAN then byte order is detected. If detection failed then byteOrder is set to BIG_ENDIAN
     * @param length    segment length
     *
     * @return RandomAccess
     *
     * @throws IOException
     */
    public static RandomAccess create(File file, long offset, long length, int byteOrder) throws IOException {
        return create(new OffsetRandomAccessFile(file, "rw", offset, length), byteOrder);
    }

    /**
     * create bufferer RandomAccess to read from/write to specified segment of the file.
     *
     * @param file      file to read/write
     * @param offset    start of segment
     * @param byteOrder if AUTO_ENDIAN then byte order is detected. If detection failed then byteOrder is set to BIG_ENDIAN
     * @param length    segment length
     *
     * @return RandomAccess
     *
     * @throws IOException
     */
    public static RandomAccessBuffer createBuffered(File file, long offset, long length, int byteOrder) throws IOException {
        MutableBufferManager mbm = new MutableRAFBufferManager(new RandomAccessFile(file, "rw"), offset, length);
        return createBuffered(mbm, byteOrder);
    }

    /**
     * create RandomAccess with BIG-ENDIAN byte order to read from/write to specified segment of the file.
     *
     * @param file   file to read/write
     * @param offset start of segment
     * @param length segment length
     *
     * @return RandomAccess
     *
     * @throws IOException
     */
    public static RandomAccess create(File file, long offset, long length) throws IOException {
        return create(new OffsetRandomAccessFile(file, "rw", offset, length), BIG_ENDIAN);
    }

    /**
     * create buffered RandomAccess with BIG-ENDIAN byte order to read from/write to specified segment of the file.
     *
     * @param file   file to read/write
     * @param offset start of segment
     * @param length segment length
     *
     * @return RandomAccess
     *
     * @throws IOException
     */
    public static RandomAccessBuffer createBuffered(File file, long offset, long length) throws IOException {
        return createBuffered(file, offset, length, BIG_ENDIAN);
    }

    /**
     * create RandomAccessRO to read from specified segment of the file.
     *
     * @param file      file to read
     * @param offset    start of segment
     * @param length    segment length
     * @param byteOrder if AUTO_ENDIAN then byte order is detected. If detection failed then byteOrder is set to BIG_ENDIAN
     *
     * @return RandomAccessRO
     *
     * @throws IOException
     */
    public static RandomAccessRO createRO(File file, long offset, long length, int byteOrder) throws IOException {
        return createRO(new OffsetRandomAccessFile(file, "r", offset, length), byteOrder);
    }

    /**
     * create buffered RandomAccessRO to read from specified segment of the file.
     *
     * @param file      file to read
     * @param offset    start of segment
     * @param length    segment length
     * @param byteOrder if AUTO_ENDIAN then byte order is detected. If detection failed then byteOrder is set to BIG_ENDIAN
     *
     * @return RandomAccessRO
     *
     * @throws IOException
     */
    public static RandomAccessRO createBufferedRO(File file, long offset, long length, int byteOrder) throws IOException {
        BufferManager mbm = new RAFBufferManager(new RandomAccessFile(file, "r"), offset, (int) length);
        return createBufferedRO(mbm, byteOrder);
    }

    /**
     * create RandomAccessRO with BIG-ENDIAN byte order to read from specified segment of the file.
     *
     * @param file   file to read
     * @param offset start of segment
     * @param length segment length
     *
     * @return RandomAccessRO
     *
     * @throws IOException
     */
    public static RandomAccessRO createRO(File file, long offset, long length) throws IOException {
        return createRO(new OffsetRandomAccessFile(file, "r", offset, length), BIG_ENDIAN);
    }

    /**
     * create buffered RandomAccessRO with BIG-ENDIAN byte order to read from specified segment of the file.
     *
     * @param file   file to read
     * @param offset start of segment
     * @param length segment length
     *
     * @return RandomAccessRO
     *
     * @throws IOException
     */
    public static RandomAccessRO createBufferedRO(File file, long offset, long length) throws IOException {
        BufferManager mbm = new RAFBufferManager(new RandomAccessFile(file, "r"), offset, (int) length);
        return createBufferedRO(mbm, BIG_ENDIAN);
    }

    /**
     * create RandomAccess to read from/write to segment of the file with specified name.
     *
     * @param name      file name
     * @param offset    start of segment
     * @param length    segment length
     * @param byteOrder if AUTO_ENDIAN then byte order is detected. If detection failed then byteOrder is set to BIG_ENDIAN
     *
     * @return RandomAccess
     *
     * @throws IOException
     */
    public static RandomAccess create(String name, long offset, long length, int byteOrder) throws IOException {
        return create(new OffsetRandomAccessFile(name, "rw", offset, length), byteOrder);
    }

    /**
     * create buffered RandomAccess to read from/write to segment of the file with specified name.
     *
     * @param name      file name
     * @param offset    start of segment
     * @param length    segment length
     * @param byteOrder if AUTO_ENDIAN then byte order is detected. If detection failed then byteOrder is set to BIG_ENDIAN
     *
     * @return RandomAccess
     *
     * @throws IOException
     */
    public static RandomAccessBuffer createBuffered(String name, long offset, long length, int byteOrder) throws IOException {
        MutableBufferManager mbm = new MutableRAFBufferManager(new RandomAccessFile(name, "rw"), offset, (int) length);
        return createBuffered(mbm, byteOrder);
    }

    /**
     * create RandomAccess with BIG-ENDIAN byte order to read from/write to segment of the file with specified name.
     *
     * @param name   file name
     * @param offset start of segment
     * @param length segment length
     *
     * @return RandomAccess
     *
     * @throws IOException
     */
    public static RandomAccess create(String name, long offset, long length) throws IOException {
        return create(new OffsetRandomAccessFile(name, "rw", offset, length), BIG_ENDIAN);
    }

    /**
     * create buffered RandomAccess with BIG-ENDIAN byte order to read from/write to segment of the file with specified name.
     *
     * @param name   file name
     * @param offset start of segment
     * @param length segment length
     *
     * @return RandomAccess
     *
     * @throws IOException
     */
    public static RandomAccessBuffer createBuffered(String name, long offset, long length) throws IOException {
        MutableBufferManager mbm = new MutableRAFBufferManager(new RandomAccessFile(name, "rw"), offset, (int) length);
        return createBuffered(mbm, BIG_ENDIAN);
    }

    /**
     * create RandomAccessRO to read from segment of the file with specified name.
     *
     * @param name      file name
     * @param offset    start of segment
     * @param length    segment length
     * @param byteOrder if AUTO_ENDIAN then byte order is detected. If detection failed then byteOrder is set to BIG_ENDIAN
     *
     * @return RandomAccessRO
     *
     * @throws IOException
     */
    public static RandomAccessRO createRO(String name, long offset, long length, int byteOrder) throws IOException {
        return createRO(new OffsetRandomAccessFile(name, "r", offset, length), byteOrder);
    }

    /**
     * create buffered RandomAccessRO to read from segment of the file with specified name.
     *
     * @param name      file name
     * @param offset    start of segment
     * @param length    segment length
     * @param byteOrder if AUTO_ENDIAN then byte order is detected. If detection failed then byteOrder is set to BIG_ENDIAN
     *
     * @return RandomAccessRO
     *
     * @throws IOException
     */
    public static RandomAccessRO createBufferedRO(String name, long offset, long length, int byteOrder) throws IOException {
        BufferManager mbm = new RAFBufferManager(new RandomAccessFile(name, "r"), offset, (int) length);
        return createBufferedRO(mbm, byteOrder);
    }

    /**
     * create RandomAccessRO with BIG-ENDIAN byte order to read from segment of the file with specified name.
     *
     * @param name   file name
     * @param offset start of segment
     * @param length segment length
     *
     * @return RandomAccessRO
     *
     * @throws IOException
     */
    public static RandomAccessRO createRO(String name, long offset, long length) throws IOException {
        return createRO(new OffsetRandomAccessFile(name, "r", offset, length), BIG_ENDIAN);
    }

    /**
     * create RandomAccessRO with BIG-ENDIAN byte order to read from segment of the file with specified name.
     *
     * @param name   file name
     * @param offset start of segment
     * @param length segment length
     *
     * @return RandomAccessRO
     *
     * @throws IOException
     */
    public static RandomAccessRO createBufferedRO(String name, long offset, long length) throws IOException {
        BufferManager mbm = new RAFBufferManager(new RandomAccessFile(name, "r"), offset, (int) length);
        return createBufferedRO(mbm, BIG_ENDIAN);
    }

    /**
     * create RandomAccess from specified RandomAccessFile
     *
     * @param raf       RandomAccessFile
     * @param byteOrder if AUTO_ENDIAN then byte order is detected. If detection failed then byteOrder is set to BIG_ENDIAN
     *
     * @return RandomAccess
     */
    public static RandomAccess create(RandomAccessFile raf, int byteOrder) throws IOException {
        return new RandomAccessFileWrapper(raf, byteOrder);
    }

    /**
     * create buffered RandomAccess from specified RandomAccessFile
     *
     * @param raf       RandomAccessFile
     * @param byteOrder if AUTO_ENDIAN then byte order is detected. If detection failed then byteOrder is set to BIG_ENDIAN
     *
     * @return RandomAccess
     */
    public static RandomAccessBuffer createBuffered(RandomAccessFile raf, int byteOrder) throws IOException {
        MutableBufferManager mbm = new MutableRAFBufferManager(raf, 0, (int) raf.length());
        return createBuffered(mbm, byteOrder);
    }

    /**
     * create RandomAccess with BIG-ENDIAN byte order from specified RandomAccessFile
     *
     * @param raf RandomAccessFile
     *
     * @return RandomAccess
     */
    public static RandomAccess create(RandomAccessFile raf) throws IOException {
        return new RandomAccessFileWrapper(raf, BIG_ENDIAN);
    }

    /**
     * create RandomAccess with BIG-ENDIAN byte order from specified RandomAccessFile
     *
     * @param raf RandomAccessFile
     *
     * @return RandomAccess
     */
    public static RandomAccessBuffer createBuffered(RandomAccessFile raf) throws IOException {
        MutableBufferManager mbm = new MutableRAFBufferManager(raf, 0, (int) raf.length());
        return createBuffered(mbm, BIG_ENDIAN);
    }

    /**
     * create RandomAccess with BIG-ENDIAN byte order from specified RandomAccessFile
     *
     * @param out OutputStream
     *
     * @return RandomAccess
     */
    public static RandomAccessBuffer createBuffered(OutputStream out) throws IOException {
        MutableBufferManager mbm = new OutputStreamBufferManager(out);
        return createBuffered(mbm, BIG_ENDIAN);
    }

    /**
     * create RandomAccessRO from specified RandomAccessFile
     *
     * @param raf       RandomAccessFile
     * @param byteOrder if AUTO_ENDIAN then byte order is detected. If detection failed then byteOrder is set to BIG_ENDIAN
     *
     * @return RandomAccessRO
     */
    public static RandomAccessRO createRO(RandomAccessFile raf, int byteOrder) throws IOException {
        return new RandomAccessFileWrapperRO(raf, byteOrder);
    }

    /**
     * create buffered RandomAccessRO from specified RandomAccessFile
     *
     * @param raf       RandomAccessFile
     * @return RandomAccessRO
     */
    public static RandomAccessBufferRO createBufferedRO(RandomAccessFile raf, int byteOrder) throws IOException {
        BufferManager mbm = new RAFBufferManager(raf, 0, (int) raf.length());
        return createBufferedRO(mbm, byteOrder);
    }

    /**
     * create RandomAccessRO with BIG-ENDIAN byte order from specified RandomAccessFile
     *
     * @param raf RandomAccessFile
     *
     * @return RandomAccessRO
     */
    public static RandomAccessRO createRO(RandomAccessFile raf) throws IOException {
        return new RandomAccessFileWrapperRO(raf, BIG_ENDIAN);
    }

    /**
     * create buffered RandomAccessRO with BIG-ENDIAN byte order from specified RandomAccessFile
     *
     * @param raf RandomAccessFile
     *
     * @return RandomAccessRO
     */
    public static RandomAccessBufferRO createBufferedRO(RandomAccessFile raf) throws IOException {
        BufferManager mbm = new RAFBufferManager(raf, 0, (int) raf.length());
        return createBufferedRO(mbm, BIG_ENDIAN);
    }

    /**
     * create RandomAccess from specified byte array
     *
     * @param data      byte array
     * @param byteOrder if AUTO_ENDIAN then byte order is detected. If detection failed then byteOrder is set to BIG_ENDIAN
     *
     * @return RandomAccess
     *
     * @throws IOException
     */
    public static RandomAccessByteArray create(byte[] data, int byteOrder) throws IOException {
        return new RandomAccessByteArray(data, byteOrder);
    }

    /**
     * create RandomAccess with BIG-ENDIAN byte order from specified byte array
     *
     * @param data byte array
     *
     * @return RandomAccess
     *
     * @throws IOException
     */
    public static RandomAccessByteArray create(byte[] data) throws IOException {
        return new RandomAccessByteArray(data, BIG_ENDIAN);
    }

    /**
     * create RandomAccessRO from specified byte array
     *
     * @param data      byte array
     * @param byteOrder if AUTO_ENDIAN then byte order is detected. If detection failed then byteOrder is set to BIG_ENDIAN
     *
     * @return RandomAccessRO
     *
     * @throws IOException
     */
    public static RandomAccessByteArrayRO createRO(byte[] data, int byteOrder) throws IOException {
        return new RandomAccessByteArrayRO(data, byteOrder);
    }

    /**
     * create RandomAccessRO with BIG-ENDIAN byte order from specified byte array
     *
     * @param data byte array
     *
     * @return RandomAccessRO
     *
     * @throws IOException
     */
    public static RandomAccessByteArrayRO createRO(byte[] data) throws IOException {
        return new RandomAccessByteArrayRO(data, BIG_ENDIAN);
    }

    /**
     * create RandomAccess from specified segment of byte array
     *
     * @param data      byte array
     * @param off       start of segment
     * @param length    segment length
     * @param byteOrder if AUTO_ENDIAN then byte order is detected. If detection failed then byteOrder is set to BIG_ENDIAN
     *
     * @return RandomAccess
     *
     * @throws IOException
     */
    public static RandomAccessByteArray create(byte[] data, int off, int length, int byteOrder) throws IOException {
        return new RandomAccessByteArray(data, off, length, byteOrder);
    }

    /**
     * create RandomAccess with BIG-ENDIAN byte order from specified segment of byte array
     *
     * @param data   byte array
     * @param off    start of segment
     * @param length segment length
     *
     * @return RandomAccess
     *
     * @throws IOException
     */
    public static RandomAccessByteArray create(byte[] data, int off, int length) throws IOException {
        return new RandomAccessByteArray(data, off, length, BIG_ENDIAN);
    }

    /**
     * create RandomAccessRO from specified segment of byte array
     *
     * @param data      byte array
     * @param off       start of segment
     * @param length    segment length
     * @param byteOrder if AUTO_ENDIAN then byte order is detected. If detection failed then byteOrder is set to BIG_ENDIAN
     *
     * @return RandomAccessRO
     *
     * @throws IOException
     */
    public static RandomAccessByteArrayRO createRO(byte[] data, int off, int length, int byteOrder) throws IOException {
        return new RandomAccessByteArrayRO(data, off, length, byteOrder);
    }

    /**
     * create RandomAccessRO with BIG-ENDIAN byte order from specified segment of byte array
     *
     * @param data   byte array
     * @param off    start of segment
     * @param length segment length
     *
     * @return RandomAccessRO
     *
     * @throws IOException
     */
    public static RandomAccessByteArrayRO createRO(byte[] data, int off, int length) throws IOException {
        return new RandomAccessByteArrayRO(data, off, length, BIG_ENDIAN);
    }


    /**
     * create RandomAccessRO (buffered) from specified URL.
     *
     * @param url URL
     *
     * @return RandomAccessRO
     *
     * @throws IOException
     */
    public static RandomAccessBufferRO createBufferedRO(URL url) throws IOException {
        return createBufferedRO(new HTTPBufferManager(url));
    }

    /**
     * create RandomAccessRO from specified RandomAccessRO.
     *
     * @param ro     RandomAccessRO
     * @param offset offset in ro
     * @param length length of created RandomAccessRO
     *
     * @return RandomAccessRO
     *
     * @throws IOException
     */
    public static RandomAccessBufferRO createBufferedRO(RandomAccessRO ro, long offset, int length) throws IOException {
        return createBufferedRO(new RABufferManager(ro, offset, length));
    }

    /**
     * create RandomAccess from specified RandomAccess.
     *
     * @param ro     RandomAccess
     * @param offset offset in ro
     * @param length length of created RandomAccess
     *
     * @return RandomAccessRO
     *
     * @throws IOException
     */
    public static RandomAccessBuffer createBuffered(RandomAccess ro, long offset, int length) throws IOException {
        return createBuffered(new MutableRABufferManager(ro, offset, length));
    }

    /**
     * Create RandomAccessRO from short array
     *
     * @param data short array
     *
     * @return RandomAccessRO
     *
     * @throws IOException
     */
    public static RandomAccessBufferRO createBufferedRO(short[] data) throws IOException {
        return createBufferedRO(new ShortArrayBufferManager(data));
    }

    /**
     * Create RandomAccess from short array
     *
     * @param data short array
     *
     * @return RandomAccess
     *
     * @throws IOException
     */
    public static RandomAccessBuffer createBuffered(short[] data) throws IOException {
        return createBuffered(new ShortArrayBufferManager(data));
    }

    /**
     * Create RandomAccessRO from short array
     *
     * @param data   short array
     * @param off    start offset
     * @param length required length
     *
     * @return RandomAccess
     *
     * @throws IOException
     */
    public static RandomAccessBufferRO createBufferedRO(short[] data, int off, int length) throws IOException {
        return createBufferedRO(new ShortArrayBufferManager(data, off, length));
    }

    /**
     * Create RandomAccess from short array
     *
     * @param data   short array
     * @param off    start offset
     * @param length required length
     *
     * @return RandomAccess
     *
     * @throws IOException
     */
    public static RandomAccessBuffer createBuffered(short[] data, int off, int length) throws IOException {
        return createBuffered(new ShortArrayBufferManager(data, off, length));
    }

    /**
     * Create RandomAccessRO from char array
     *
     * @param data char array
     *
     * @return RandomAccessRO
     *
     * @throws IOException
     */
    public static RandomAccessBufferRO createBufferedRO(char[] data) throws IOException {
        return createBufferedRO(new CharArrayBufferManager(data));
    }

    /**
     * Create RandomAccess from char array
     *
     * @param data char array
     *
     * @return RandomAccess
     *
     * @throws IOException
     */
    public static RandomAccessBuffer createBuffered(char[] data) throws IOException {
        return createBuffered(new CharArrayBufferManager(data));
    }

    /**
     * Create RandomAccessRO from char array
     *
     * @param data   char array
     * @param off    start offset
     * @param length required length
     *
     * @return RandomAccess
     *
     * @throws IOException
     */
    public static RandomAccessBufferRO createBufferedRO(char[] data, int off, int length) throws IOException {
        return createBufferedRO(new CharArrayBufferManager(data, off, length));
    }

    /**
     * Create RandomAccess from char array
     *
     * @param data   char array
     * @param off    start offset
     * @param length required length
     *
     * @return RandomAccess
     *
     * @throws IOException
     */
    public static RandomAccessBuffer createBuffered(char[] data, int off, int length) throws IOException {
        return createBuffered(new CharArrayBufferManager(data, off, length));
    }

    /**
     * Create RandomAccessRO from int array
     *
     * @param data int array
     *
     * @return RandomAccess
     *
     * @throws IOException
     */
    public static RandomAccessBufferRO createBufferedRO(int[] data) throws IOException {
        return createBufferedRO(new IntArrayBufferManager(data));
    }

    /**
     * Create RandomAccess from int array
     *
     * @param data int array
     *
     * @return RandomAccess
     *
     * @throws IOException
     */
    public static RandomAccessBuffer createBuffered(int[] data) throws IOException {
        return createBuffered(new IntArrayBufferManager(data));
    }

    /**
     * Create RandomAccessRO from int array
     *
     * @param data   int array
     * @param off    start offset
     * @param length required length
     *
     * @return RandomAccess
     *
     * @throws IOException
     */
    public static RandomAccessBufferRO createBufferedRO(int[] data, int off, int length) throws IOException {
        return createBufferedRO(new IntArrayBufferManager(data, off, length));
    }

    /**
     * Create RandomAccess from int array
     *
     * @param data   int array
     * @param off    start offset
     * @param length required length
     *
     * @return RandomAccess
     *
     * @throws IOException
     */
    public static RandomAccessBuffer createBuffered(int[] data, int off, int length) throws IOException {
        return createBuffered(new IntArrayBufferManager(data, off, length));
    }

    /**
     * Create RandomAccessRO from int array
     *
     * @param data int array
     *
     * @return RandomAccess
     *
     * @throws IOException
     */
    public static RandomAccessBufferRO createBufferedRO(float[] data) throws IOException {
        return createBufferedRO(new FloatArrayBufferManager(data));
    }

    /**
     * Create RandomAccess from int array
     *
     * @param data int array
     *
     * @return RandomAccess
     *
     * @throws IOException
     */
    public static RandomAccessBuffer createBuffered(float[] data) throws IOException {
        return createBuffered(new FloatArrayBufferManager(data));
    }

    /**
     * Create RandomAccessRO from int array
     *
     * @param data   int array
     * @param off    start offset
     * @param length required length
     *
     * @return RandomAccess
     *
     * @throws IOException
     */
    public static RandomAccessBufferRO createBufferedRO(float[] data, int off, int length) throws IOException {
        return createBufferedRO(new FloatArrayBufferManager(data, off, length));
    }

    /**
     * Create RandomAccess from int array
     *
     * @param data   int array
     * @param off    start offset
     * @param length required length
     *
     * @return RandomAccess
     *
     * @throws IOException
     */
    public static RandomAccessBuffer createBuffered(float[] data, int off, int length) throws IOException {
        return createBuffered(new FloatArrayBufferManager(data, off, length));
    }

    /**
     * Create RandomAccessRO from long array
     *
     * @param data long array
     *
     * @return RandomAccess
     *
     * @throws IOException
     */
    public static RandomAccessBufferRO createBufferedRO(long[] data) throws IOException {
        return createBufferedRO(new LongArrayBufferManager(data));
    }

    /**
     * Create RandomAccess from long array
     *
     * @param data long array
     *
     * @return RandomAccess
     *
     * @throws IOException
     */
    public static RandomAccessBuffer createBuffered(long[] data) throws IOException {
        return createBuffered(new LongArrayBufferManager(data));
    }

    /**
     * Create RandomAccessRO from long array
     *
     * @param data   long array
     * @param off    start offset
     * @param length required length
     *
     * @return RandomAccess
     *
     * @throws IOException
     */
    public static RandomAccessBufferRO createBufferedRO(long[] data, int off, int length) throws IOException {
        return createBufferedRO(new LongArrayBufferManager(data, off, length));
    }

    /**
     * Create RandomAccess from long array
     *
     * @param data   long array
     * @param off    start offset
     * @param length required length
     *
     * @return RandomAccess
     *
     * @throws IOException
     */
    public static RandomAccessBuffer createBuffered(long[] data, int off, int length) throws IOException {
        return createBuffered(new LongArrayBufferManager(data, off, length));
    }

    /**
     * Create RandomAccessRO from double array
     *
     * @param data double array
     *
     * @return RandomAccess
     *
     * @throws IOException
     */
    public static RandomAccessBufferRO createBufferedRO(double[] data) throws IOException {
        return createBufferedRO(new DoubleArrayBufferManager(data));
    }

    /**
     * Create RandomAccess from double array
     *
     * @param data double array
     *
     * @return RandomAccess
     *
     * @throws IOException
     */
    public static RandomAccessBuffer createBuffered(double[] data) throws IOException {
        return createBuffered(new DoubleArrayBufferManager(data));
    }

    /**
     * Create RandomAccessRO from double array
     *
     * @param data   double array
     * @param off    start offset
     * @param length required length
     *
     * @return RandomAccess
     *
     * @throws IOException
     */
    public static RandomAccessBufferRO createBufferedRO(double[] data, int off, int length) throws IOException {
        return createBufferedRO(new DoubleArrayBufferManager(data, off, length));
    }

    /**
     * Create RandomAccess from double array
     *
     * @param data   double array
     * @param off    start offset
     * @param length required length
     *
     * @return RandomAccess
     *
     * @throws IOException
     */
    public static RandomAccessBuffer createBuffered(double[] data, int off, int length) throws IOException {
        return createBuffered(new DoubleArrayBufferManager(data, off, length));
    }
}
