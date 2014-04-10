package com.imagero.uio.io;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Andrey Kuznetsov
 */
public class TargaRLEInputStream extends RLEInputStream {

    int numSamples;
    byte [] value;
    boolean rawPacket;
    int pixelSize;
    int vindex;

    public TargaRLEInputStream(InputStream in, int pixelSize) {
        super(in);
        this.pixelSize = pixelSize;
        value = new byte[pixelSize];
    }

    public int read() throws IOException {
        if (numSamples == 0) {
            int v = in.read();
            if(v == -1) {
                return -1;
            }
            if ((v >> 7) == 1) {
                for (int i = 0; i < value.length; i++) {
                    value[i] = (byte) in.read();
                }
                numSamples = ((v & 0x7F) + 1) * pixelSize;
                rawPacket = false;
            }
            else {
                numSamples = (v + 1) * pixelSize;
                rawPacket = true;
            }
        }
        numSamples--;
        if (rawPacket) {
            return in.read();
        }
        else {
            int b = value[vindex++] & 0xFF;
            if(vindex == pixelSize) {
                vindex = 0;
            }
            return b;
        }
    }
}
