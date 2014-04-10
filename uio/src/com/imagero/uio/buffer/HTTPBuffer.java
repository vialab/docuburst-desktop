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

import com.imagero.uio.io.IOutils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Implementation of Buffer for http.
 * This intelligent Buffer uses "byteserving" feature
 * of HTTP 1.1 for reading only data which is needed.
 *
 * @author Andrei Kouznetsov
 *         Date: 05.01.2004
 *         Time: 20:59:01
 */
public class HTTPBuffer implements Buffer {

    URL url;

    long offset;
    int length;
    byte[] data;

    public HTTPBuffer(URL url, int offset, int length) {
        this.url = url;
        this.offset = offset;
        this.length = length;

//		Sys.out.println("HTTPBuffer");
//		Sys.out.println("offset:" + Long.toHexString(offset));
//		Sys.out.println("length:" + Integer.toHexString(length));
    }

    /**
     * read specified part of data from URL
     *
     * @return data
     *
     * @throws IOException if server don't support "byteserving"
     */
    public byte[] getData() throws IOException {
        if(data == null) {
            readData();
        }
        return data;
    }

    public byte[] getData(byte[] d) throws IOException {
        if(data == null) {
            readData();
        }
        System.arraycopy(data, 0, d, 0, Math.min(data.length, d.length));
        return d;
    }

    public int length() {
        return length;
    }

    private void readData() throws IOException {
        HttpURLConnection httpcon = (HttpURLConnection) url.openConnection();
        httpcon.setAllowUserInteraction(true);
        httpcon.setDoInput(true);
        httpcon.setDoOutput(true);
        httpcon.setRequestMethod("GET");
        httpcon.setUseCaches(false);
        httpcon.setRequestProperty("Range", "bytes=" + offset + "-" + (offset + length));
        httpcon.connect();

        String responseMessage = httpcon.getResponseMessage();
        int responseCode = httpcon.getResponseCode();
        if(responseCode != 206) {
            httpcon.disconnect();
            throw new IOException("byteserving not supported by server");
        }

//		Sys.out.println(responseCode + " " + responseMessage);
        InputStream in = httpcon.getInputStream();

        data = new byte[length];
        IOutils.readFully2(in, data);

        httpcon.disconnect();
    }

    public boolean isDirty() {
        return false;
    }
}
