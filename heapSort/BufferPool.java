import java.io.*;
import java.util.*;

public class BufferPool implements BufferADT {
    private int numBuffers;
    private LinkedHashMap<Integer, Buffer> lruCache;
    private RandomAccessFile file;
    private int cacheHits;
    private int diskReads;
    private int diskWrites;

    public BufferPool(int numBuffers, String filename) throws IOException {
        this.numBuffers = numBuffers;
        this.lruCache = new LinkedHashMap<>(numBuffers, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<Integer, Buffer> eldest) {
                if (size() > BufferPool.this.numBuffers) {
                    try {
                        BufferPool.this.flushBlock(eldest.getValue());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return true;
                }
                return false;
            }
        };
        this.file = new RandomAccessFile(filename, "rw");
        this.cacheHits = 0;
        this.diskReads = 0;
        this.diskWrites = 0;
    }

    @Override
    public byte[] readBlock() {
        // This method will not be directly used, the specific implementation for block management will be shown below
        return null;
    }

    @Override
    public byte[] getDataPointer() {
        // This method will not be directly used, the specific implementation for block management will be shown below
        return null;
    }

    @Override
    public void markDirty() {
        // This method will not be directly used, the specific implementation for block management will be shown below
    }

    @Override
    public void releaseBuffer() {
        // This method will not be directly used, the specific implementation for block management will be shown below
    }

    public void insert(byte[] space, int sz, int pos) {
        try {
            int blockNum = pos / FileGenerator.BYTES_PER_BLOCK;
            Buffer buffer = lruCache.computeIfAbsent(blockNum, k -> {
                try {
                    return new Buffer(readBlockFromFile(k));
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            });

            int offset = pos % FileGenerator.BYTES_PER_BLOCK;
            System.arraycopy(space, 0, buffer.getDataPointer(), offset, sz);
            buffer.markDirty();
            lruCache.put(blockNum, buffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getbytes(byte[] space, int sz, int pos) {
        try {
            int blockNum = pos / FileGenerator.BYTES_PER_BLOCK;
            Buffer buffer = lruCache.computeIfAbsent(blockNum, k -> {
                try {
                    return new Buffer(readBlockFromFile(k));
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            });

            int offset = pos % FileGenerator.BYTES_PER_BLOCK;
            System.arraycopy(buffer.getDataPointer(), offset, space, 0, sz);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] readBlockFromFile(int blockNum) throws IOException {
        byte[] block = new byte[FileGenerator.BYTES_PER_BLOCK];
        file.seek(blockNum * FileGenerator.BYTES_PER_BLOCK);
        file.readFully(block);
        diskReads++;
        return block;
    }

    private void flushBlock(Buffer buffer) throws IOException {
        if (buffer.isDirty()) {
            int blockNum = buffer.getBlockNum();
            file.seek(blockNum * FileGenerator.BYTES_PER_BLOCK);
            file.write(buffer.getDataPointer());
            diskWrites++;
            buffer.clearDirty();
        }
    }

    public int getCacheHits() {
        return cacheHits;
    }

    public int getDiskReads() {
        return diskReads;
    }

    public int getDiskWrites() {
        return diskWrites;
    }

    public int getNumRecords() throws IOException {
        return (int) (file.length() / FileGenerator.BYTES_PER_RECORD);
    }

    public void close() throws IOException {
        for (Buffer buffer : lruCache.values()) {
            flushBlock(buffer);
        }
        file.close();
    }

    private class Buffer {
        private byte[] data;
        private boolean dirty;
        private int blockNum;

        public Buffer(byte[] data) {
            this.data = data;
            this.dirty = false;
        }

        public byte[] getDataPointer() {
            return data;
        }

        public void markDirty() {
            dirty = true;
        }

        public boolean isDirty() {
            return dirty;
        }

        public void clearDirty() {
            dirty = false;
        }

        public int getBlockNum() {
            return blockNum;
        }

        public void setBlockNum(int blockNum) {
            this.blockNum = blockNum;
        }
    }
}
