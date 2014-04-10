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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * remove all App blocks from jpeg file
 * <br>
 * how to use:
 * <br>
 * <pre>
 * 		//read data from file and save filtered data to another file
 * 		File fs = new File("source.jpg");
 * 		File fd = new File("dest.jpg");
 * 		JpegFilterInputStream.filter(fs, fd);
 *
 *		or
 *
 * 		//filter data in byte array
 * 		byte [] source = ...
 * 		byte [] dest = JpegFilterInputStream.filter(source);
 *
 * 		or
 *
 * 		//filter data from InputStream
 * 		InputStream in;
 * 		OutputStream out;
 * 		JpegFilterInputStream.filter(in, out);
 *
 *      or
 *
 *      //just wrap InputStream
 *      JpegFilterInputStream jfis = new JpegFilterInputStream(in);
 *
 * </pre>
 * @author Andrey Kuznetsov
 */
public class JpegFilterInputStream extends FilterInputStream {

    public static final int APP_0 = 0xE0;
    public static final int APP_1 = 0xE1;
    public static final int APP_2 = 0xE2;
    public static final int APP_3 = 0xE3;
    public static final int APP_4 = 0xE4;
    public static final int APP_5 = 0xE5;
    public static final int APP_6 = 0xE6;
    public static final int APP_7 = 0xE7;
    public static final int APP_8 = 0xE8;
    public static final int APP_9 = 0xE9;
    public static final int APP_10 = 0xEA;
    public static final int APP_11 = 0xEB;
    public static final int APP_12 = 0xEC;
    public static final int APP_13 = 0xED;
    public static final int APP_14 = 0xEE;
    public static final int APP_15 = 0xEF;

    public static final int[] allMarkers = new int[]{
        APP_0, APP_1, APP_2, APP_3, APP_4, APP_5, APP_6, APP_7,
        APP_8, APP_9, APP_10, APP_11, APP_12, APP_13, APP_14, APP_15
    };

    public static final int[] defaultMarkers = new int[]{
        APP_1, APP_2, APP_3, APP_4, APP_5, APP_6, APP_7,
        APP_8, APP_9, APP_10, APP_11, APP_12, APP_13, APP_14, APP_15
    };

    public static void filterFile(File src, File dest) throws IOException {
        FileInputStream in = new FileInputStream(src);
        FileOutputStream out = new FileOutputStream(dest);

        filter(in, out);
        IOutils.closeStream(out);
        IOutils.closeStream(in);
    }

    /**
     * filter out all markers (except App0)
     * @param in InputStream
     * @param out
     * @throws java.io.IOException
     */
    public static void filter(InputStream in, OutputStream out) throws IOException {
        filter(in, out, defaultMarkers);
    }

    public static void filter(InputStream in0, OutputStream out, int[] markers) throws IOException {
        JpegFilterInputStream in = new JpegFilterInputStream(in0, markers);
        int a = 0;
        while (a >= 0) {
            a = in.read();
            out.write(a);
        }
    }

    /**
     * filter out all markers (except App0)
     * @param data
     * @return byte array
     * @throws IOException
     */
    public static byte[] filter(byte[] data) throws IOException {
        return filter(data, defaultMarkers);
    }

    public static byte[] filter(byte[] data, int[] markers) throws IOException {
        final ByteArrayInputStream in0 = new ByteArrayInputStream(data);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        filter(in0, bout, markers);
        return bout.toByteArray();
    }

    int[] markers;

    /**
     * create JpegFilterInputStream (filter out all markers except App0)
     * @param in InputStream (with valid JPEG stream)
     */
    public JpegFilterInputStream(InputStream in) {
        this(in, JpegFilterInputStream.defaultMarkers);
    }

    /**
     * create JpegFilterInputStream
     * @param in InputStream (with valid JPEG stream)
     * @param markers markers to filter out
     */
    public JpegFilterInputStream(InputStream in, int[] markers) {
        super(in);
        this.markers = markers;
    }

    private boolean markerOn;

    public int read() throws IOException {
        int a = in.read();
        if (!markerOn) {
            if (a == 0xFF) {
                markerOn = true;
            }
            return a;
        }
        else {
            if (isAppMarker(a)) {
                int length = (in.read() << 8) + in.read() - 2;
//					Sys.out.println("length:" + length);
                in.skip(length);
                int b = in.read();
                if (b != 0xFF) {
                    throw new IOException("marker???");
                }
                return read();
            }
            else {
                markerOn = false;
                return a;
            }
        }
    }

    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    public int read(byte b[], int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        }
        if (off + len > b.length || off < 0) {
            throw new ArrayIndexOutOfBoundsException();
        }
        int read = 1;
        int a = read();
        if (a == -1) {
            return -1;
        }
        b[off] = (byte) a;
        for (int i = off + 1; i < len; i++) {
            a = read();
            if (a == -1) {
                break;
            }
            read++;
            b[i] = (byte) a;
        }
        return read;
    }

    public boolean isAppMarker(int a) {
        for (int i = 0; i < markers.length; i++) {
            if (a == markers[i]) {
                return true;
            }
        }
        return false;
    }
}
