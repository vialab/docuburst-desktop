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
package com.imagero.uio.buffer.fm;

import com.imagero.uio.buffer.MutableRAFBufferManager;
import com.imagero.uio.buffer.Buffer;
import com.imagero.uio.buffer.RAFBuffer;
import com.imagero.uio.io.IOutils;
import com.imagero.uio.Sys;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Special version of MutableBufferManager for work with FileManger
 * @author Andrey Kuznetsov
 */
class FMBufferManager extends MutableRAFBufferManager {

    File file;
    ActionListener listener;
    volatile boolean reading, writing;

    public FMBufferManager(File file, ActionListener listener) throws IOException {
        super(new RandomAccessFile(file, "rw"));
        this.listener = listener;
        this.file = file;
    }

    public FMBufferManager(RandomAccessFile raf) throws IOException {
        super(raf);
    }

    protected Buffer createBuffer(RandomAccessFile raf, long offset, int dsLength) {
        long maxLength = 0;
        try {
            maxLength = raf.length() - offset;
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        if (maxLength < 0) {
            return new FMBuffer(raf, offset, 0);
        }
        return new FMBuffer(raf, offset, (int) Math.min(maxLength, dsLength));
    }

    boolean canClose() {
        return !reading && !writing;
    }

    void _close() {
        super.close();
        listener = null;
        file = null;
        raf = null;
    }

    public void close() {
        IOutils.closeStream(raf);
        raf = null;
        listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, OpenFileManager.CLOSE));
        if(OpenFileManager.debug && file != null) {
            Sys.out.print("\nclose: ");
            Sys.out.println(file.getAbsolutePath());
        }
    }

    private RandomAccessFile get() throws IOException {
        if (file == null) {
            throw new NullPointerException("This stream cannot be reopened!");
        }
        boolean opened = false;
        if (raf == null) {
            raf = new RandomAccessFile(file, "rw");
            listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, OpenFileManager.OPEN));
            opened = true;
            if(OpenFileManager.debug && file != null) {
                Sys.out.print("\nopen: ");
                Sys.out.println(file.getAbsolutePath());
            }
        }
        if(!opened) {
            listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, OpenFileManager.GET));
        }
        return raf;
    }

    class FMBuffer extends RAFBuffer {

        public FMBuffer(RandomAccessFile ra, long offset, int length) {
            super(ra, offset, length);
        }

        protected void readData() throws IOException {
            reading = true;
            try {
                raf = get();
                super.readData();
            }
            finally {
                reading = false;
            }
        }

        protected void writeData() throws IOException {
            writing = true;
            try {
                raf = get();
                super.writeData();
            }
            finally {
                writing = false;
            }
        }
    }
}
