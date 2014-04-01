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

import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;

/**
 * MemoryAccessManager.
 * Implements 5 possible strategies to free memory:
 * - DROP_NEVER - Buffer(s) are never dropped
 * - DROP_IMMEDIATELY - only 1 Buffer is held in memory
 * - DROP_RANDOM - if buffer count exceeds maxBufferCount then randomly choosed Buffer is dropped
 * - DROP_LRU - if buffer count exceeds maxBufferCount then Least Recently Used Buffer is dropped
 * - DROP_FIFO - if buffer count exceeds maxBufferCount then Least Recently Added Buffer is dropped
 * @author Andrey Kuznetsov
 */
public abstract class MemoryAccessManager {

    public static final int DROP_NEVER = 0;
    public static final int DROP_IMMEDIATELY = -1;
    public static final int DROP_RANDOM = 1;
    public static final int DROP_LRU = 2;
    public static final int DROP_FIFO = 3;

    private static int defaultStrategy = DROP_NEVER; // CHANGED by CMC as workaround to bug 20 for Applets, Apri. 15, 2006

    public static int getDefaultStrategy() {
        return defaultStrategy;
    }

    public static void setDefaultStrategy(int defaultStrategy) {
        MemoryAccessManager.defaultStrategy = defaultStrategy;
    }

    public static MemoryAccessManager createMemoryAccessManager() {
        return createMemoryAccessManager(getDefaultStrategy());
    }

    public static MemoryAccessManager createMemoryAccessManager(int strategy) {
        switch (strategy) {
            case DROP_NEVER:
                return new DNMemoryAccessManager();
            case DROP_IMMEDIATELY:
                return new DIMemoryAccessManager();
            case DROP_RANDOM:
                return new DRMemoryAccessManager();
            case DROP_LRU:
                return new LRUMemoryAccessManager();
            case DROP_FIFO:
                return new FIFOMemoryAccessManager();
            default:
                throw new IllegalArgumentException("unknown strategy:" + strategy);
        }
    }

    int maxBufferCount = 10; 
    IHashtable ht = new IHashtable();

    public abstract Buffer get(Integer key);

    public abstract void put(Integer key, Buffer b);

    public void add(Buffer b) {
        put(new Integer(ht.nextKey()), b);
    }

    public Buffer get(int i) {
        return get(new Integer(i));
    }

    public int getCount() {
        return ht.size();
    }

    public int getBufferLength(int i) {
        return getBufferLength(new Integer(i));
    }

    public int getBufferLength(Integer key) {
        Buffer b = get(key);
        if (b != null) {
            return b.length();
        }
        return 0;
    }

    public Buffer drop(Integer key) {
        return (Buffer) ht.remove(key.intValue());
    }

    public int getMaxBufferCount() {
        return maxBufferCount;
    }

    public void setMaxBufferCount(int maxBufferCount) {
        this.maxBufferCount = maxBufferCount;
    }

    public void clear() {
        ht.clear();
    }

    public boolean isDirty(int index) {
        Buffer b = (Buffer) ht.get(index);
        if (b != null) {
            return b.isDirty();
        }
        return false;
    }

    public boolean isDirty(Integer key) {
        Buffer b = (Buffer) ht.get(key.intValue());
        if (b != null) {
            return b.isDirty();
        }
        return false;
    }

    public Enumeration keys() {
        return ht.keys();
    }

    static class DNMemoryAccessManager extends MemoryAccessManager {

        public Buffer get(Integer key) {
            return (Buffer) ht.get(key.intValue());
        }

        public void put(Integer key, Buffer b) {
            ht.put(key.intValue(), b);
        }
    }

    static class DIMemoryAccessManager extends MemoryAccessManager {
        public Buffer get(Integer key) {
            return (Buffer) ht.get(key.intValue());
        }

        public void put(Integer key, Buffer b) {
            ht.clear();
            ht.put(key.intValue(), b);
        }
    }

    static class DRMemoryAccessManager extends MemoryAccessManager {
        Random random = new Random(System.currentTimeMillis());

        public Buffer get(Integer key) {
            return (Buffer) ht.get(key.intValue());
        }

        public void put(Integer key, Buffer b) {
            int size = ht.size();
            if (size >= maxBufferCount) {
                int r = Math.abs(random.nextInt()) % size;
                Enumeration keys = ht.keys();
                for (int i = 0; i < r; i++) {
                    if (keys.hasMoreElements()) {
                        keys.nextElement();
                    }
                }
                boolean dropped = false;
                //drop after random position
                while (keys.hasMoreElements()) {
                    Integer k = (Integer) keys.nextElement();
                    Buffer b0 = get(k);
                    if (!b0.isDirty()) {
                        this.drop(k);
                        dropped = true;
                        break;
                    }
                }
                if (!dropped) {
                    keys = ht.keys();
                    //drop first not dirty
                    while (keys.hasMoreElements()) {
                        Integer k = (Integer) keys.nextElement();
                        Buffer b0 = get(k);
                        if (!b0.isDirty()) {
                            this.drop(k);
                            break;
                        }
                    }
                }
            }
            ht.put(key.intValue(), b);
        }
    }

    static class FIFOMemoryAccessManager extends MemoryAccessManager {
        Vector fifo = new Vector();

        public Buffer get(Integer key) {
            //System.out.println("get:" + key);
            return (Buffer) ht.get(key.intValue());
        }

        public void put(Integer key, Buffer b) {
            int size = ht.size();
            if (size >= maxBufferCount) {
                for (int i = 0; i < fifo.size(); i++) {
                    Integer k = (Integer) fifo.elementAt(i);
                    Buffer b0 = get(k);
                    if (!b0.isDirty()) {
                        drop(k);
                        break;
                    }
                }
            }
            fifo.addElement(key);
            ht.put(key.intValue(), b);
        }

        public Buffer drop(Integer key) {
            fifo.removeElement(key);
            return super.drop(key);
        }

        public void clear() {
            ht.clear();
            fifo.removeAllElements();
        }
    }

    static class LRUMemoryAccessManager extends MemoryAccessManager {
        Vector lru = new Vector();

        public Buffer get(Integer key) {
            lru.removeElement(key);
            lru.addElement(key);
            return (Buffer) ht.get(key.intValue());
        }

        public void put(Integer key, Buffer b) {
            lru.removeElement(key);
            lru.addElement(key);
            if (lru.size() >= maxBufferCount) {
                for (int i = 0; i < lru.size(); i++) {
                    Integer k = (Integer) lru.elementAt(i);
                    Buffer b0 = get(k);
                    if (!b0.isDirty()) {
                        drop(k);
                        break;
                    }
                }
            }
            ht.put(key.intValue(), b);
        }

        public Buffer drop(Integer key) {
            lru.removeElement(key);
            return super.drop(key);
        }

        public void clear() {
            ht.clear();
            lru.removeAllElements();
        }
    }
}
