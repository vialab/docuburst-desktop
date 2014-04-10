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

import java.io.EOFException;
import java.io.IOException;

/**
 * Represent (multiple) Buffer as RandomAccessRO
 * <br>
 *
 * @author Andrei Kouzentsov
 */
public class RandomAccessBufferRO extends AbstractRandomAccessRO {

    protected BufferManager bufferManager;
    int dataIndex;
    int fp;
    byte[] buf;

    /**
     * create new RABuffer
     *
     * @throws IOException
     */
    public RandomAccessBufferRO(Buffer[] ds, int byteOrder) throws IOException {
        this(new DefaultBufferManager(ds), byteOrder);
    }

    /**
     * create new RABuffer
     *
     * @param sourceManager
     *
     * @throws IOException
     */
    public RandomAccessBufferRO(BufferManager sourceManager, int byteOrder) throws IOException {
        this.bufferManager = sourceManager;
        this.buf = sourceManager.getData(0);
        _setByteOrder(byteOrder);
    }

    public BufferManager getBufferManager() {
        return bufferManager;
    }

    /**
     * Reads a byte of data from this byte array. The byte is returned as an integer in the range 0 to 255
     * (<code>0x00-0x0ff</code>).
     *
     * @return the next byte of data, or <code>-1</code> if the end of the file has been reached.
     */
    public int read() throws IOException {
        if(fp < buf.length) {
            return buf[fp++] & 0xFF;
        }
        else {
            nextArray();
            if(buf == null || buf.length == 0) {
                return -1;
            }
            return buf[fp++] & 0xFF;
        }
    }

    protected boolean nextArray() throws IOException {
        try {
            this.buf = bufferManager.getData(++dataIndex);
            this.fp = 0;
            if(buf.length == 0) {
                return false;
            }
            return true;
        }
        catch(IOException ex) {
            this.buf = null;
            this.fp = 0;
//            return false;
            throw ex;
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

        this.dataIndex = bufferManager.getIndex(pos);
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

    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    public int read(byte[] b, int off, int length) throws IOException {
        int read = 0;
        int _off = off;
        int _length = length;

        while(read < _length) {
            int len = readBytes(b, _off, _length);
            //System.out.println("offset: "+ off + " length: " + length + " read in: " + len);
            if(len == -1) {
                boolean next = nextArray();
                if(!next) {
                    // CMC uncommented this section to allow for read ahead by more than one buffer unit
                    if(read == 0) {
                        System.out.println("x");
                        continue;
                    }
                    return read > 0 ? read : -1;
                }
                else {

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
        if(buf == null) {
            return -1;
        }
        //System.out.println("fp: " + fp + " buf.length: " + buf.length);
        int len = Math.min(length, buf.length - fp);
        if(len <= 0) {
            return -1;
        }
        System.arraycopy(buf, fp, b, off, len);
        return len;
    }

    public void close() {
        bufferManager.close();
    }

    public int skip(int n) throws IOException {
        return skipBytes(n);
    }

    protected int _read() throws IOException {
        int a = read();
        if(a < 0) {
            throw new EOFException();
        }
        return a;
    }
}
