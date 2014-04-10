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

import com.imagero.uio.RandomAccess;
import com.imagero.uio.RandomAccessFactory;
import com.imagero.uio.RandomAccessRO;
import com.imagero.uio.buffer.BufferManager;
import com.imagero.uio.buffer.MutableBufferManager;
import com.imagero.uio.io.IOutils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

/**
 * OpenFileManager manages open files.
 * If too much files are opened, FileManager closes some of them and opens them later if needed.
 * @author Andrey Kuznetsov
 */
public class OpenFileManager {

    static final String GET = "get";
    static final String OPEN = "open";
    static final String CLOSE = "close";

    static boolean debug;

    public static boolean isDebug() {
        return debug;
    }

    public static void setDebug(boolean debug) {
        OpenFileManager.debug = debug;
    }

    Vector v = new Vector();
    Hashtable ros = new Hashtable();

    int maxOpenCount = 100;

    ActionListener handler = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            String actionCommand = e.getActionCommand();
            Object manager = e.getSource();
            if (actionCommand == GET || actionCommand == OPEN) {
                v.removeElement(manager);
                v.addElement(manager);
            }
            else if(actionCommand == CLOSE) {
                v.removeElement(manager);
            }
        }
    };

    /**
     * create RandomAccessRO which will be managed by this FileManager
     * @param f File
     * @return created RandomAccessRO
     * @throws IOException
     */
    public RandomAccessRO createRO(File f) throws IOException {
        RandomAccessRO ro = openFileRO(f);
        return ro;
    }

    /**
     * create RandomAccess which will be managed by this FileManager
     * @param f File
     * @return created RandomAccess
     * @throws IOException
     */
    public RandomAccess create(File f) throws IOException {
        RandomAccess ra = openFile(f);
        return ra;
    }

    private RandomAccessRO openFileRO(File f) throws IOException {
        BufferManager manager = createBufferManager(f, false);
        RandomAccessRO ro = RandomAccessFactory.createBufferedRO(manager);
        ros.put(ro, manager);
        v.addElement(manager);
        checkSize();

        return ro;
    }

    private RandomAccess openFile(File f) throws IOException {
        BufferManager manager = createBufferManager(f, true);
        RandomAccess ra = RandomAccessFactory.createBuffered((MutableBufferManager) manager);
        ros.put(ra, manager);
        v.addElement(manager);
        checkSize();

        return ra;
    }

    private void checkSize() {
        if (v.size() > maxOpenCount) {
            int closeCount = v.size() - maxOpenCount;
            for (int i = 0; i < v.size(); i++) {
                BufferManager manager = (BufferManager) v.elementAt(i);
                if (manager instanceof FMBufferManager) {
                    if (((FMBufferManager) manager).canClose()) {
                        manager.close();
                        v.removeElementAt(0);
                        if (--closeCount == 0) {
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * finally close RandomAccessRO and remove it from this FileManager
     */
    public void close(RandomAccessRO ro) {
        Object mgr = ros.remove(ro);
        if (mgr == null) {
            throw new NullPointerException();
        }
        IOutils.closeStream(ro);
        v.removeElement(mgr);
        if (mgr instanceof FMBufferManagerRO) {
            ((FMBufferManagerRO) mgr)._close();
        }
        else if (mgr instanceof FMBufferManager) {
            ((FMBufferManager) mgr)._close();
        }
    }

    /**
     * get max count of simultaneously open files.
     */
    public int getMaxOpenCount() {
        return maxOpenCount;
    }

    /**
     * set max count of simultaneously open files.
     */
    public void setMaxOpenCount(int maxOpenCount) {
        this.maxOpenCount = maxOpenCount;
    }

    private BufferManager createBufferManager(File f, boolean writeable) throws IOException {
        if (writeable) {
            return new FMBufferManager(f, handler);
        }
        else {
            return new FMBufferManagerRO(f, handler);
        }
    }
}
