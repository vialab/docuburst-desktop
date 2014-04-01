package com.imagero.uio.buffer;

import java.util.Hashtable;
import java.util.Enumeration;

/**
 * @author Andrey Kuznetsov
 */
public class IHashtable {

    private int max;

    private Hashtable ht = new Hashtable();

    public Object put(int key, Object value) {
        max = Math.max(max, key) + 1;
        return ht.put(new Integer(key), value);
    }

    public Object get(int key) {
        return ht.get(new Integer(key));
    }

    public Object add(Object value) {
        int key = nextKey();
        return put(key, value);
    }

    public int nextKey() {
        return max;
    }

    public Object remove(int key) {
        return ht.remove(new Integer(key));
    }

    public int size() {
        return ht.size();
    }

    public void clear() {
        ht.clear();
    }

    public Enumeration keys() {
        return ht.keys();
    }
}
