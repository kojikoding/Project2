import java.io.*;
import java.util.ArrayList;
import java.util.List;

// -------------------------------------------------------------------------
/**
 * Write a one-sentence summary of your class here.
 * Follow it with additional details about its purpose, what abstraction
 * it represents, and how to use it.
 * 
 * @author labibasajjad
 * @version Jun 23, 2024
 */
public class BufferPool implements BufferPoolADT {

   

    // ~ Fields ................................................................
    private RandomAccessFile rF;
    private Buffer buffer;
    private int cacheHits = 0;
    private int dReads = 0;
    private int dWrites = 0;

    public BufferPool(File file)
    {
        try {
            this.rF = new RandomAccessFile(file, "rw");
            this.buffer = new Buffer(rF);
        }
        catch(IOException e)
        {
            System.out.println(":Error opening file" + file);
            e.printStackTrace();
        }

    }
    @Override
    public Buffer acquireBuffer() {
        if(buffer == null)
        {
            buffer = new Buffer(rF);
            buffer.readBlock();
            dReads++;
        }
        return buffer;
    }
    
    public void flush()
    {
        if(buffer.isDirty())
        {
            buffer.writeBuffer();
            dWrites++;
        }
    }
    public int getCacheHits() {
        return cacheHits;
    }

    public int getDiskWrites() {
        return dWrites;
    }

    public int getDiskReads() {
        return dReads;
    }
}
