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

import java.io.IOException;

/**
 * BufferManager.java
 *
 * @author Andrei Kouznetsov
 */
public interface BufferManager {

    final byte[] empty = new byte[0];

    /**
     * Get data (as byte array) from i'th Buffer
     *
     * @param i Buffer index
     *
     * @return byte array
     */
    byte[] getData(int i) throws IOException;

    /**
     * Get count of Buffer objects in this BufferManager
     *
     * @return int
     */
    int getCount();

    /**
     * Get length of i'th Buffer
     *
     * @param i Buffer index
     *
     * @return int
     */
    int getDataLength(int i);

    /**
     * Get index of Buffer which contains <code>pos</code>
     *
     * @param pos
     *
     */
    int getIndex(long pos);

    /**
     * Get length of data of all Buffer together
     *
     */
    long getLength();

    /**
     * Allows to free all possibly used resources
     */
    void close();

    /**
     * get start of i'th Buffer in byte<br>
     *
     * @param i Buffer index
     *
     * @return int
     *         <TABLE ALIGN="left" BORDER=0 CELLSPACING=0 CELLPADDING=0>
     *         <TR ALIGN="left" VALIGN="middle">
     *         <TD valign="middle" colspan="3">
     *         &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<FONT SIZE="1">n-1</FONT><br>
     *         </TD>
     *         </tr>
     *         <TR ALIGN="left" VALIGN="middle">
     *         <TD valign="middle">
     *         dataStart(n) =
     *         </TD>
     *         <TD valign="middle">
     *         <FONT SIZE="7">&#x03A3;</FONT>
     *         </TD>
     *         <TD valign="middle">
     *         getDataLength(i)
     *         </TD>
     *         </tr>
     *         <TR ALIGN="left" VALIGN="middle">
     *         <TD valign="middle" colspan="3">
     *         &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<FONT SIZE="1">i=0</FONT><br>
     *         </TD>
     *         </tr>
     *         </TABLE>
     */
    long getDataStart(int i);

    /**
     * clear all buffered data without closing
     */
    void clear();

    /**
     * get max buffer count
     * @return max buffer count
     */
    int getMaxCache();

    /**
     * set max buffer count
     */
    void setMaxCache(int max);

    /**
     * all buffered data in stream between start and end will be cleared
     */
    void clear(long start, long end) throws IOException;
}
