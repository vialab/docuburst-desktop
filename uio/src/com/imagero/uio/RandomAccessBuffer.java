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
import com.imagero.uio.buffer.DefaultBufferManager;
import com.imagero.uio.buffer.MutableBufferManager;
import com.imagero.uio.buffer.arrays.AbstractArrayBufferManager;

import java.io.EOFException;
import java.io.IOException;

/**
 * Makes possible to represent (multiple) Buffers as RandomAccess<br>
 *
 * @author Andrey Kuzentsov
 */
public class RandomAccessBuffer extends AbstractRandomAccess {

    MutableBufferManager bufferManager;
    int dataIndex;
    int fp;
    byte[] buf;
    boolean dirty;


    /**
     * create new RABuffer
     *
     * @throws java.io.IOException
     */
    public RandomAccessBuffer(Buffer[] ds, int byteOrder) throws IOException {
        this(new DefaultBufferManager(ds), byteOrder);
    }

    /**
     * create new RABuffer
     *
     * @param bufferManager
     *
     * @throws java.io.IOException
     */
    public RandomAccessBuffer(MutableBufferManager bufferManager, int byteOrder) throws IOException {
        this.bufferManager = bufferManager;
        this.buf = bufferManager.getData(0);
        _setByteOrder(byteOrder);
    }


    /**
     * Reads a byte of data from this byte array. The byte is returned as an integer in the range 0 to 255
     * (<code>0x00-0x0ff</code>).
     *
     * @return the next byte of data, or <code>-1</code> if the end of the file has been reached.
     */
    public int read() {
        if(fp < buf.length) {
            return buf[fp++] & 0xFF;
        }
        else {
            nextArray();
            if(buf == null) {
                return -1;
            }
            return buf[fp++] & 0xFF;
        }
    }

    protected boolean nextArray() {
        //Sys.out.println("nextArray: " + totalRead);
        try {
            if(dirty) {
                bufferManager.setDirty(dataIndex);
                dirty = false;
            }
            this.buf = bufferManager.getData(++dataIndex);
            this.fp = 0;
            return true;
        }
        catch(Exception ex) {
            //ex.printStackTrace();
            this.buf = null;
            this.fp = 0;
            return false;
        }
    }

    public long getFilePointer() throws IOException {
        return bufferManager.getDataStart(dataIndex) + fp;
    }

    /**
     * Sets the pointer offset, measured in bytes from the begin of the data,
     * at which the next read or write occurs.
     *
     * @param pos the offset position, measured in bytes from the begin of the data, at which
     *            to set the pointer.
     */
    public void seek(long pos) throws IOException {
        if(pos < 0) {
            throw new IOException("" + pos);
        }

        final int index = bufferManager.getIndex((int) pos);
        if(index != dataIndex && dirty) {
            bufferManager.setDirty(dataIndex);
        }
        this.dataIndex = index;
        this.buf = bufferManager.getData(dataIndex);
        this.fp = (int) (pos - bufferManager.getDataStart(dataIndex));
    }

    /**
     * Returns the data length (please note, that real length is not always known)
     *
     * @return the data length, measured in bytes.
     */
    public long length() {
        return bufferManager.getLength();
    }

    /**
     * just ignored, no exception is thrown
     */
    public void setLength(long newLength) throws IOException {
    }

    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    public int read(byte[] b, int off, int length) throws IOException {
        int read = 0;
        int _off = off;
        int _length = length;

        while(read < length) {
            int len = readBytes(b, _off, _length);
            if(length == -1) {
                if(!nextArray()) {
                    return read;
                }
            }
            else {
                read += len;
                fp += len;
                _off += len;
                _length -= len;
            }
        }

        return read;
    }

    private int readBytes(byte[] b, int off, int length) {
        int len = Math.min(length, buf.length - fp);
        if(len <= 0) {
            return -1;
        }
        System.arraycopy(buf, fp, b, off, len);
        return len;
    }

    /**
     * Writes max <code>b.length</code> bytes from the specified byte array
     * to this array, starting at the current array pointer.
     * <p/>
     * This method doesn't write beyond array bounds,
     * but <code>off</code> and <code>length</code> are not checked.
     *
     * @param b the data.
     */
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array
     * starting at offset <code>off</code> to this RandomAccess.<br>
     * <p/>
     * This method doesn't write beyond array bounds,
     * but <code>off</code> and <code>length</code> are not checked.
     *
     * @param b      the data.
     * @param off    the start offset in the data.
     * @param length the number of bytes to write
     *
     * @throws java.io.IOException if end of last array reached
     */
    public void write(byte[] b, int off, int length) throws IOException {
        int write = 0;
        int _off = off;
        int _length = length;

        while(write < length) {
            int len = writeBytes(b, _off, _length);
            dirty = true;
            if(len == -1) {
                if(!nextArray()) {
                    throw new EOFException();
                }
            }
            else {
                write += len;
                fp += len;
                _off += len;
                _length -= len;
                if(_length == 0) {
                    break;
                }
            }
        }
    }

    public void flush() throws IOException {
        if(dirty) {
            bufferManager.setDirty(dataIndex);
            dirty = false;
        }
        bufferManager.flush();
    }

    public void setByteOrder(int byteOrder) throws IOException {
        super.setByteOrder(byteOrder);
        if(bufferManager instanceof AbstractArrayBufferManager) {
            ((AbstractArrayBufferManager) bufferManager).setByteOrder(getByteOrder());
        }
    }

    protected void _setByteOrder(int byteOrder) throws IOException {
        super._setByteOrder(byteOrder);
        if(bufferManager instanceof AbstractArrayBufferManager) {
            ((AbstractArrayBufferManager) bufferManager).setByteOrder(getByteOrder());
        }
    }

    private int writeBytes(byte[] b, int off, int length) {
        int len = Math.min(length, buf.length - fp);
        if(len <= 0) {
            return -1;
        }
        System.arraycopy(b, off, buf, fp, len);
        return len;
    }

    /**
     * Writes the specified byte to this array. The write starts at the current array pointer.
     *
     * @param b the <code>byte</code> to be written.
     *
     * @throws java.io.IOException if end of last array reached
     */
    public void write(int b) throws IOException {
        if(fp < buf.length) {
            buf[fp++] = (byte) b;
        }
        else {
            nextArray();
            if(buf == null) {
                throw new EOFException();
            }
            buf[fp++] = (byte) b;
        }
        dirty = true;
    }

    /**
     * sets dirty flag in bufferManager and closes it
     */
    public void close() {
        if(dirty) {
            bufferManager.setDirty(dataIndex);
            dirty = false;
        }
        bufferManager.close();
    }

    public int skip(int n) throws IOException {
        return skipBytes(n);
    }

    protected int _read() throws EOFException {
        int a = read();
        if(a < 0) {
            throw new EOFException();
        }
        return a;
    }
}
