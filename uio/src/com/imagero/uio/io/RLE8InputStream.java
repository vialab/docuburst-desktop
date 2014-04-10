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
import java.io.InputStream;

/**
 * PackBits decoder
 *
 * @author Andrei Kouznetsov
 */
public class RLE8InputStream extends RLEInputStream {

    int numSamples, value;
    boolean copyLiter;
    boolean ignoreByte;

    public RLE8InputStream(InputStream in) {
        super(in);
    }

    public int read() throws IOException {
        if (numSamples == 0) {
            if (ignoreByte) {
                ignoreByte = false;
                /*int ignored = */in.read();
            }
            int l = in.read();
            if (l == 0) {
                value = in.read();
                switch (value) {
                    case 0:
                        throw new EndOfLineException();
                    case 1:
                        finished = true;
                        throw new EndOfBitmapException();
                    case 2:
                        int x = in.read();
                        int y = in.read();
                        throw new DeltaRecordException(Integer.toHexString(x) + Integer.toHexString(y));
                    default:
                        copyLiter = true;
                        numSamples = value;
                        if ((numSamples & 1) != 0) {
                            ignoreByte = true;
                        }
                }
            }
            else {
                numSamples = l;
                copyLiter = false;
                value = in.read();
            }
        }
        numSamples--;
        if (copyLiter) {
            return in.read();
        }
        else {
            return value;
        }
    }
}
