import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

class BufferPool implements BufferPoolADT {
    private int numBuffers;
    private LinkedHashMap<Integer, byte[]> lruCache;
    private RandomAccessFile file;
    private int cacheHits;
    private int diskReads;
    private int diskWrites;

    public BufferPool(int numBuffers, String filename) throws IOException {
        this.numBuffers = numBuffers;
        this.lruCache = new LinkedHashMap<>(numBuffers, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<Integer, byte[]> eldest) {
                if (size() > BufferPool.this.numBuffers) {
                    try {
                        BufferPool.this.flushBlock(eldest.getKey());
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
    public void insert(byte[] space, int sz, int pos) {
        try {
            int blockNum = pos / FileGenerator.BYTES_PER_BLOCK;
            byte[] block = lruCache.computeIfAbsent(blockNum, k -> {
                try {
                    return readBlock(k);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            });

            int offset = pos % FileGenerator.BYTES_PER_BLOCK;
            System.arraycopy(space, 0, block, offset, sz);
            lruCache.put(blockNum, block);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getbytes(byte[] space, int sz, int pos) {
        try {
            int blockNum = pos / FileGenerator.BYTES_PER_BLOCK;
            byte[] block = lruCache.computeIfAbsent(blockNum, k -> {
                try {
                    return readBlock(k);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            });

            int offset = pos % FileGenerator.BYTES_PER_BLOCK;
            System.arraycopy(block, offset, space, 0, sz);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] readBlock(int blockNum) throws IOException {
        byte[] block = new byte[FileGenerator.BYTES_PER_BLOCK];
        file.seek(blockNum * FileGenerator.BYTES_PER_BLOCK);
        file.readFully(block);
        diskReads++;
        return block;
    }

    private void flushBlock(int blockNum) throws IOException {
        if (lruCache.containsKey(blockNum)) {
            byte[] block = lruCache.get(blockNum);
            file.seek(blockNum * FileGenerator.BYTES_PER_BLOCK);
            file.write(block);
            diskWrites++;
            lruCache.remove(blockNum);
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
        for (Integer blockNum : lruCache.keySet()) {
            flushBlock(blockNum);
        }
        file.close();
    }
}
