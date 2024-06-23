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
    private int bufferNum;
    private RandomAccessFile f; // for I/O operations
    private List<Buffer> buffers; // to manage buffers
    private List<Integer> cacheList; // to manage LRU

    private static int cacheHits = 0;
    private static int diskReads = 0;
    private static int diskWrites = 0;

    // ~ Constructors ..........................................................
    /**
     * Create a new BufferPool object.
     * 
     * @param bufferNum
     *            max number of buffers that can be used
     * @param file
     */
    public BufferPool(int bufferNum, File file) {

        this.bufferNum = bufferNum;
        this.buffers = new ArrayList<>();
        this.cacheList = new ArrayList<>();

        try {
            this.f = new RandomAccessFile(file, "rw");
        }
        catch (IOException e) {
            System.out.println(":Error opening file" + file);
            e.printStackTrace();
        }

    }


    // ~Public Methods ........................................................
    @Override
    public Buffer acquireBuffer(int block) {

        for (Buffer bufferFound : buffers) {
            if (bufferFound.getIndex() == block && !bufferFound.isReleased()) {
                cacheHits++;
                used(block);
                return bufferFound;
            }
        }

        // if buffer pool is full, remove LRU
        if (buffers.size() >= bufferNum) {
            removeBuffer();
        }

        Buffer bufferNotFound = new Buffer(block, f);
        bufferNotFound.readBlock();
        buffers.add(bufferNotFound);
        cacheList.add(block);
        diskReads++;

        return bufferNotFound;

    }


    /**
     * Private metjod that removes the least used buffer
     */
    private void removeBuffer() {
        int removeIndex = cacheList.get(0);
        for (Buffer b : buffers) {
            if (b.getIndex() == removeIndex) {
                if (b.isDirty()) {
                    b.writeBuffer();
                    diskWrites++;
                }
                b.releaseBuffer();
                buffers.remove(b);
                cacheList.remove(0);
                break;
            }
        }
    }


    /**
     * Private method to mark buffer as used to update the postion
     * in the cache list by removing the specified index and adding
     * it to the end of the list.
     */
    private void used(int block) {
        cacheList.remove((Integer)block);
        cacheList.add(block);

    }


    /**
     * Flush all the buffers
     */
    public void flush() {
        for (Buffer b : buffers) {
            if (b.isDirty()) {
                b.writeBuffer();
            }
        }

        buffers.clear();
        cacheList.clear();
    }


    // ----------------------------------------------------------
    /**
     * Getter method for the file sixe
     * 
     * @return file size
     * @throws IOException
     */
    public long getFileSize() throws IOException {
        return f.length();
    }

    public static int getCacheHits() {
        return cacheHits;
    }

    public static int getDiskWrites() {
        return diskWrites;
    }
    
    public static int getDiskReads() {
        return diskReads;
    }
 

}
