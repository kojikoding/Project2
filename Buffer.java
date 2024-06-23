import java.io.IOException;
import java.io.RandomAccessFile;

public class Buffer implements BufferADT {

    // ~ Fields ................................................................
    private int index;
    private byte[] data;
    private boolean dirty;
    private boolean released;
    private RandomAccessFile file;

    // ----------------------------------------------------------
    /**
     * Create a new Buffer object and initializes the fields
     * 
     * @param index
     *            is the index of the block
     * @param file
     */
    // ~ Constructors ..........................................................
    public Buffer(int index, RandomAccessFile file) {
        this.index = index;
        this.data = new byte[4096];
        this.dirty = false;
        this.released = false;
        this.file = file;
        readBlock();

    }

    // ~Public Methods ........................................................


    @Override
    public byte[] readBlock() {
        if (data == null) {
            data = new byte[4096];
            try {
                file.seek(index * 4096);
                file.readFully(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return data;
    }


    @Override
    public byte[] getDataPointer() {
        return data;
    }


    @Override
    public void markDirty() {
        this.dirty = true;

    }


    /**
     * Checks if buffer content is modified
     * 
     * @return true/false abt the buffer state
     */
    public boolean isDirty() {
        return dirty;
    }


    @Override
    public void releaseBuffer() {
        this.released = true;

    }


    /**
     * Checks if the block's access is released
     * 
     * @return true/false abt the blocks access status.
     */
    public boolean isReleased() {
        return released;
    }


    // ----------------------------------------------------------
    /**
     * Returns the index of the blocks
     * 
     * @return the index
     */
    public int getIndex() {
        return index;
    }


    /**
     * 
     */
    public void writeBuffer() {
        if (dirty && data != null) {
            try {
                file.seek(index * 4096);
                file.write(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
            dirty = false;
        }
    }

}
