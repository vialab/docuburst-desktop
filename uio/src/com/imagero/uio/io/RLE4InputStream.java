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

import java.io.IOException;

/**
 * PackBits decoder
 *
 * @author Andrei Kouznetsov
 */
public class RLE4InputStream extends RLEInputStream {

    byte[] value = new byte[2];

    BitInputStream bin;

    byte[] buffer = new byte[128];
    int bufferStart;
    int bufferLength;

    ByteArrayOutputStreamExt bout;
    BitOutputStream bitOut;

    public RLE4InputStream(BitInputStream in) {
        super(in);
        bin = in;
        bin.setBitsToRead(4);
        bout = new ByteArrayOutputStreamExt();
        bitOut = new BitOutputStream(bout);
    }

    public int read() throws IOException {
        if (bufferStart >= bufferLength) {
            fillBuffer();
        }
        if (bufferStart >= bufferLength) {
            return -1;
        }
        return buffer[bufferStart++];
    }

    private void fillBuffer() throws IOException {
        if (bout.size() == 0) {
            fillBufferImpl();
        }
        bufferStart = 0;
        bufferLength = bout.drain(buffer);
    }

    private void fillBufferImpl() throws IOException {
        int len = bin.read(8);
        if (len == 0) {
            int value = bin.read(8);
            switch (value) {
                case 0:
                    throw new EndOfLineException();
                case 1:
                    finished = true;
                    throw new EndOfBitmapException();
                case 2:
                    int x = bin.read(8);
                    int y = bin.read(8);
                    throw new DeltaRecordException(Integer.toHexString(x) + Integer.toHexString(y));
                default:
                    int skipCount = 0;
                    if ((value & 3) != 0) {
                        skipCount = (value + 3) / 4 * 4 - value;
                    }
                    for (int i = 0; i < value; i++) {
                        bitOut.write(bin.read(4), 4);
                    }
                    for (int i = 0; i < skipCount; i++) {
                        /*int ignored = */bin.read(4);
                    }
            }
        }
        else {
            value[0] = (byte) bin.read(4);
            value[1] = (byte) bin.read(4);

            for (int i = 0; i < len; i++) {
                bitOut.write(value[i & 1], 4);
            }
        }
    }
}
