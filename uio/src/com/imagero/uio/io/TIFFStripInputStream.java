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

import com.imagero.uio.RandomAccess;

import java.io.IOException;

/**
 * InputStream over all strips
 * @author Andrei Kouznetsov
 */
public class TIFFStripInputStream extends RandomAccessInputStream {

	int[] stripOffsets, stripByteCount;

	int currentStrip = -1;
	long stripLimit;
	int markStrip;

	public TIFFStripInputStream(RandomAccess ra, int[] stripByteCount, int[] stripOffsets) {
		super(ra);
		this.stripByteCount = stripByteCount;
		this.stripOffsets = stripOffsets;
	}

	public TIFFStripInputStream(RandomAccess ra, long startPos, int[] stripByteCount, int[] stripOffsets) {
		super(ra, startPos);
		this.stripByteCount = stripByteCount;
		this.stripOffsets = stripOffsets;
	}

	protected void checkPos() throws IOException {
		if(pos > stripLimit || currentStrip == -1) {
//			Sys.out.println("currentStrip:" + currentStrip);
			if(currentStrip >= stripOffsets.length) {
				throw new IOException();
			}
			currentStrip++;
			stripLimit = stripOffsets[currentStrip] + stripByteCount[currentStrip];
			pos = stripOffsets[currentStrip];

//			File ft = new File(TiffReader.workDir + "tables" + currentStrip + ".jpg");
//			FileOutputStream fout = new FileOutputStream(ft);
//			in.seek(pos);
//			byte [] b = new byte[256];
//			for(int i = 0; i < stripByteCount[currentStrip];) {
//				int r = in.read(b);
//				if(r < 0) {
//					break;
//				}
//				fout.write(b, 0, r);
//				i =+ r;
//			}
//			fout.close();
		}
		super.checkPos();
	}

	synchronized public int read(byte[] b, int off, int len) throws IOException {
		return super.read(b, off, Math.min(len, (int)(stripLimit - pos)));
	}

	public void mark(int i) {
		super.mark(i);
		markStrip = currentStrip;
	}

	public void reset() throws IOException {
		super.reset();
		currentStrip = markStrip;
	}

	public long skip(long l) throws IOException {
		int lsi = stripOffsets.length - 1;
		long limit = stripOffsets[lsi] + stripByteCount[lsi];
		long remaining = l;
		while(remaining > 0) {
			checkPos();
			long cs = Math.min(remaining, stripLimit - pos);
			if(cs > limit - pos) {
				cs = limit - pos;
				remaining -= cs;
				pos += cs;
				break;
			}
			remaining -= cs;
			pos += cs;
		}
		return l - remaining;
	}

	public int available() {
		try {
			return super.available();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public long getPos() {
		long res = 0;
		for(int i = 0; i < currentStrip; i++) {
			res += stripByteCount[i];
		}
		res += stripByteCount[currentStrip]
			- (stripByteCount[currentStrip] + stripOffsets[currentStrip] - pos);
		return res;
	}
}
