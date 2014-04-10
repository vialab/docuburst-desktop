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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * OffsetRandomAccessFile.java
 * <br>
 * Usefull for reading EXIF files<br>
 * However this class has bad performance, bacause the data is unbuffered.
 *
 * @author Andrei Kouznetsov
 */
public class OffsetRandomAccessFile extends RandomAccessFile {
	protected long offset;
	protected long length;

	public OffsetRandomAccessFile(File file, String mode, long offset) throws IOException {
		this(file, mode, offset, file.length() - offset);
	}

	public OffsetRandomAccessFile(File file, String mode, long offset, long length) throws IOException {
		super(file, mode);
		this.offset = offset;
		this.length = length;
		seek(0);
	}

	public OffsetRandomAccessFile(String name, String mode, long offset) throws IOException {
		this(new File(name), mode, offset);
	}

	public OffsetRandomAccessFile(String name, String mode, long offset, long length) throws IOException {
		this(new File(name), mode, offset, length);
	}

	public void seek(long pos) throws IOException {
		if(pos < 0) {
			throw new IOException();
		}
		super.seek(pos + offset);
	}

	public int read() throws IOException {
		if(getFilePointer() >= length) {
			return -1;
		}
		return super.read();
	}

	public long length() throws IOException {
		return length;
	}

	public long getFilePointer() throws IOException {
		return super.getFilePointer() - offset;
	}

	public int skip(int n) throws IOException {
		return skipBytes(n);
	}
}
