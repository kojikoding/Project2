import java.io.IOException;
import java.io.RandomAccessFile;

public class Buffer implements BufferADT {

    // ~ Fields ................................................................
    private byte[] data;
    private boolean dirty;
    private RandomAccessFile file;

    public Buffer(RandomAccessFile file) {
        this.data = null;
        this.dirty = false;
        this.file = file;
        readBlock();
    }


    @Override
    public byte[] readBlock() {
        try {
            long fileLen = file.length();
            data = new byte[(int)fileLen];
            file.seek(0);
            file.readFully(data);
            System.out.println("Buffer: Reading entire file into buffer");
        }
        catch (IOException e) {
            e.printStackTrace();
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
        System.out.println("Buffer: Marking buffer as dirty");

    }


    public boolean isDirty() {
        return dirty;
    }


    public void writeBuffer() {
        if (dirty && data != null) {
            try {
                file.seek(0);
                file.write(data);
                System.out.println(
                    "Buffer: Writing entire buffer back to file");
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            dirty = false;
        }
    }


    @Override
    public void releaseBuffer() {
        // TODO Auto-generated method stub

    }

}
