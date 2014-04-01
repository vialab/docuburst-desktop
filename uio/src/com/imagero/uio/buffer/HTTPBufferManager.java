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
package com.imagero.uio.buffer;

import com.imagero.uio.RandomAccessFactory;
import com.imagero.uio.io.IOutils;

import java.io.IOException;
import java.net.URL;

/**
 * currently supported protocols: file and http
 *
 * @author Andrei Kouznetsov
 * @see HTTPBuffer
 *      <br>
 *      Date: 12.11.2003
 *      Time: 12:45:21
 */
public class HTTPBufferManager extends AbstractBufferManager {

    public static final int URL_TYPE_UNKNOWN = 0;
    public static final int URL_TYPE_HTTP = 1;
    public static final int URL_TYPE_FILE = 2;

    URL url;

    int lastBufferSize;

    Integer maxKey = new Integer(0);

    int urlType;

    /**
     * create BufferManager for InputStream (with standard length of 50k)
     *
     * @param url URL
     *
     * @see HTTPBuffer
     */
    public HTTPBufferManager(URL url) throws IOException {
        this(url, defaultBufferSize);
    }

    /**
     * create BufferManager for InputStream
     *
     * @param dsLength standard length of one data block of Buffer
     * @param url      URL
     *
     * @see HTTPBuffer
     */
    public HTTPBufferManager(URL url, int dsLength) throws IOException {
        this.bufferSize = dsLength;
        this.url = url;
        if("http".equals(url.getProtocol())) {
            urlType = URL_TYPE_HTTP;
        }
        else if("file".equals(url.getProtocol())) {
            urlType = URL_TYPE_FILE;
            ro = RandomAccessFactory.createRO(url.getFile());
        }
        else {
            throw new IOException("unsupported protocol: " + url.getProtocol());
        }

        Buffer uds = createBuffer(url, 0, dsLength);
        accessManager.put(new Integer(0), uds);
    }

    protected Buffer createBuffer(URL url, int offset, int dsLength) {
        switch(urlType) {
            case URL_TYPE_HTTP:
                return new HTTPBuffer(url, offset, dsLength);
            case URL_TYPE_FILE:
                return new RABufferRO(ro, offset, dsLength);
            default:
                return null;
        }
    }

    /**
     * get data (as byte array) from i'th Buffer
     *
     * @param i Buffer index
     *
     * @return byte array
     *
     * @throws IOException if i'th Buffer not exists and couldn't be read from InputStream
     */
    public byte[] getData(int i) throws IOException {
        Integer key = new Integer(i);
        Buffer b = (accessManager.get(key));
        if(b == null) {
            b = createBuffer(url, bufferSize * i, bufferSize);
            accessManager.put(key, b);
            if(key.intValue() > maxKey.intValue()) {
                maxKey = key;
            }
        }
        return b.getData();
    }

    /**
     * get length of i'th Buffer
     *
     * @param i Buffer index
     *
     * @return dsLength
     */
    public int getDataLength(int i) {
        return bufferSize;
    }

    /**
     * get index of Buffer which contains <code>pos</code>
     *
     * @param pos
     *
     * @return index of Buffer or -1
     */
    public int getIndex(long pos) {
        if(pos < 0) {
            return -1;
        }
        return (int) (pos / bufferSize);
    }

    /**
     * get length of data of all already read Buffer together (may change)
     *
     */
    public long getLength() {
        return (maxKey.intValue() + 1) * bufferSize;
    }

    /**
     * get start of i'th Buffer in byte<br>
     * I assume here that length of each Buffer (except last one) equals to <code>dsLength</code>
     *
     * @param i
     *
     */
    public long getDataStart(int i) {
        return bufferSize * i;
    }

    public void close() {
        IOutils.closeStream(ro);
    }
}
