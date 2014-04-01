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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * PackBits decoder
 *
 * @author Andrei Kouznetsov
 */
public class PackBitsInputStream extends FilterInputStream {

    boolean finished;

    int numSamples, value;
    boolean copyLiter;

    public PackBitsInputStream(InputStream in) {
        super(in);
    }

    public int available() throws IOException {
        if(finished) {
            return 0;
        }
        return 1;
    }

    public void close() throws IOException {
    }

    public boolean markSupported() {
        return false;
    }

    public synchronized void mark(int readlimit) {
    }

    public synchronized void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }

    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    public int read(byte b[], int off, int len) throws IOException {
        int i = off;

        try {
            for(; i < off + len; i++) {
                int a = read();
                if(a == -1) {
                    i--;
                    break;
                }
                b[i] = (byte) a;
            }
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }
        return i - off;
    }

    public int read() throws IOException {
        if(numSamples == 0) {
            byte l = read128();
            if(l < 0) {
                numSamples = -l + 1;
                copyLiter = false;
                value = in.read();
            }
            else {
                numSamples = l + 1;
                copyLiter = true;
            }
        }
        numSamples--;
        if(copyLiter) {
            return in.read();
        }
        else {
            return value;
        }
    }

    byte read128() throws IOException {
        do {
            byte a = (byte) in.read();
            if(a != -128) {
                return a;
            }
        }
        while(true);
    }
}
