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
package com.imagero.uio;

import java.io.EOFException;
import java.io.IOException;

/**
 * Makes possible to access byte array as RandomAccess<br>
 *
 * @author Andrei Kouzentsov
 */
public class RandomAccessByteArray extends AbstractRandomAccess {

	int fp;

	byte[] buf;
	int length;
	int offset;

	/**
	 * create new RandomAccessByteArray
	 * @param data byte array
	 * @throws java.io.IOException
	 */
	public RandomAccessByteArray(byte[] data, int byteOrder) throws IOException {
		this(data, 0, data.length, byteOrder);
	}

	/**
	 * create new RandomAccessByteArray
	 * @param data byte array
	 * @param off index of the first byte
	 * @param length number of bytes
	 * @throws java.io.IOException
	 */
	public RandomAccessByteArray(byte[] data, int off, int length, int byteOrder) throws IOException {
		this.buf = data;
		this.length = length;
		this.offset = off;
		_setByteOrder(byteOrder);
	}

	/**
	 * Reads a byte of data from this byte array. The byte is returned as an integer in the range 0 to 255
	 * (<code>0x00-0x0ff</code>).
	 *
	 * @return the next byte of data, or <code>-1</code> if the end of the file has been reached.
	 */
	public int read() {
		if(fp < length + offset) {
			return buf[offset + fp++] & 0xFF;
		}
		else {
			return -1;
		}
	}

	protected int _read() throws EOFException {
		if(fp < length + offset) {
			return buf[offset + fp++] & 0xFF;
		}
		else {
			throw new EOFException();
		}
	}

	/**
	 * Sets the length of byte array.
	 * no array copying is performed.
	 * @param      newLength    The desired length of the file
	 * @exception  java.io.IOException  if <code>length</code> is more then length of array minus <code>offset</code>
	 */
	public void setLength(long newLength) throws IOException {
		if(newLength < 0) {
			throw new ArrayIndexOutOfBoundsException("" + newLength);
		}
		if(newLength == length) {
			return;
		}
		else if(newLength < buf.length - offset) {
			length = (int)newLength;
			return;
		}
		else {
//			byte[] tmp = new byte[(int)newLength];
//			int length = Math.min(tmp.length, length);
//			System.arraycopy(buf, offset, tmp, 0, length);
			throw new IOException("length too big");
		}
	}


	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	public int read(byte[] b, int off, int length) throws IOException {
		int len = Math.min(length, this.length - (fp + offset));
		if(len <= 0) {
			return -1;
		}
		System.arraycopy(buf, fp + offset, b, off, len);
		fp += len;
		return len;
	}


	/**
	 * Writes max <code>b.length</code> bytes from the specified byte array
	 * to this array, starting at the current array pointer.
	 *
	 * This method doesn't write beyond array bounds,
	 * but <code>off</code> and <code>length</code> are not checked.
	 *
	 * @param      b   the data.
	 */
	public void write(byte[] b) throws IOException {
		write(b, 0, b.length);
	}

	/**
	 * Writes <code>len</code> bytes from the specified byte array
	 * starting at offset <code>off</code> to this byte array.<br>
	 *
	 * This method doesn't write beyond array bounds,
	 * but <code>off</code> and <code>length</code> are not checked
	 * and no exception is thrown.
	 *
	 * @param      b     the data.
	 * @param      off   the start offset in the data.
	 * @param      length   the number of bytes to write
	 */
	public void write(byte[] b, int off, int length) {
		int len = Math.min(length, this.length - (fp + offset));
		System.arraycopy(b, off, buf, fp + offset, len);
		fp += len;
	}

	/**
	 * Writes the specified byte to this array. The write starts at the current array pointer.
	 *
	 * @param b the <code>byte</code> to be written.
	 */
	public void write(int b) throws IOException {
		buf[offset + fp++] = (byte)b;
	}

	/**
	 * does nothing
	 */
	public void close() {
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int skip(int n) throws IOException {
		return skipBytes(n);
	}

	public long getFilePointer() throws IOException {
		return fp + offset;
	}

	/**
	 * Sets the array-pointer offset, measured from the beginning of this byte array, at which the next read or write
	 * occurs. The offset may NOT be set beyond the end of the byte array.
	 *
	 * @param     pos                 the offset position, measured in bytes from the beginning of the byte array, at which
	 *                                to set the array pointer.
	 */
	public void seek(long pos) {
		if(/*(pos > length + offset) ||*/ pos < 0) {
			throw new ArrayIndexOutOfBoundsException("" + pos);
		}
		this.fp = (int)pos;
	}

	/**
	 * Returns the length of this byte array.
	 *
	 * @return     the length of this byte array, measured in bytes.
	 */
	public long length() {
		return length;
	}
}
