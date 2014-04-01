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

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * LEDataOutputStream.java
 * <br>
 * Little-endian writing.
 * <br>
 * @author Kouznetsov Andrei
 *
 */
public class LEDataOutputStream extends FilterOutputStream implements DataOutput {

	public LEDataOutputStream(OutputStream out) {
		super(out);
	}

	public final void writeShort(int value) throws IOException {
		write(value & 0xFF);
		write((value >> 8) & 0xFF);
	}

	public final void writeChar(int value) throws IOException {
		write(value & 0xFF);
		write((value >> 8) & 0xFF);
	}

	public final void writeInt(int value) throws IOException {
		write(value & 0xFF);
		write((value >> 8) & 0xFF);
		write((value >> 16) & 0xFF);
		write((value >> 24) & 0xFF);
	}

	public final void writeLong(long value) throws IOException {
		writeInt((int)(value & 0xFFFFFFFF));
		writeInt((int)((value >> 32) & 0xFFFFFFFF));
	}

	public final void writeFloat(float value) throws IOException {
		writeInt(Float.floatToIntBits(value));
	}

	public final void writeDouble(double value) throws IOException {
		writeLong(Double.doubleToLongBits(value));
	}

	public void writeBoolean(boolean b) throws IOException {
		out.write(b ? 1 : 0);
	}

	public void writeByte(int v) throws IOException {
		write(v);
	}

	public void writeBytes(String s) throws IOException {
		int len = s.length();
		for (int i = 0 ; i < len ; i++) {
			out.write((byte)s.charAt(i));
		}
	}

	public void writeChars(String s) throws IOException {
		int len = s.length();
		byte [] b = new byte[len * 2];
		int index = 0;
		for(int i = 0; i < len; i++) {
			int v = s.charAt(i);
			b[index++] = (byte)((v >>> 0) & 0xFF);
			b[index++] = (byte)((v >>> 8) & 0xFF);
		}
		write(b);
	}

	public void writeUTF(String str) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream(str.length());
		DataOutputStream dataOut = new DataOutputStream(out);
		dataOut.writeUTF(str);
		dataOut.flush();
		dataOut.close();
		byte[] b = out.toByteArray();
		write(b);
	}
}


