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

import com.imagero.uio.RandomAccessRO;

import java.io.IOException;
import java.io.InputStream;

/**
 * same as FilterInputStream but with RandomAccess
 * @author Kouznetsov Andrei
 */
public class RandomAccessInputStream extends InputStream {
	protected static long MARK_UNDEFINED = -1L;

	protected RandomAccessRO ro;
	protected long pos;
	protected long mark = MARK_UNDEFINED;
	protected long startPos;


	public RandomAccessInputStream(RandomAccessRO ro) {
		this(ro, 0L);
	}

	public RandomAccessInputStream(RandomAccessRO ro, long startPos) {
		this.ro = ro;
		this.pos = startPos;
		this.startPos = startPos;
	}

	synchronized public int read() throws IOException {
		checkPos();
		int a = ro.read();
		pos++;
		return a;
	}

	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	synchronized public int read(byte[] b, int off, int len) throws IOException {
		checkPos();
		int r = ro.read(b, off, len);
		pos += r;
		return r;
	}

	protected void checkPos() throws IOException {
		if(ro.getFilePointer() != pos) {
			ro.seek(pos);
		}
	}

    /**
     * releases reference to RandomAccessRO, but does not closes it
     */
	public void close() throws IOException {
		ro = null;
	}

	public int available() throws IOException {
		return (int)(ro.length() - pos);
	}

	public boolean markSupported() {
		return true;
	}

	public void mark(int i) {
		mark = pos;
	}

	public void reset() throws IOException {
		if(mark == MARK_UNDEFINED) {
			throw new IOException("mark undefined");
		}
		pos = mark;
	}

	public long skip(long l) throws IOException {
		long skip = Math.min(ro.length() - pos, l);
		pos += skip;
		return skip;
	}
}
