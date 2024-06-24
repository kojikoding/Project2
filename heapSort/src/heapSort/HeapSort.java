import java.io.*;
import java.nio.ByteBuffer;

public class HeapSort {

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java HeapSort <data-file-name> <num-buffers> <stat-file-name>");
            return;
        }

        String dataFileName = args[0];
        int numBuffers = Integer.parseInt(args[1]);
        String statFileName = args[2];

        try {
            BufferPool bufferPool = new BufferPool(numBuffers, dataFileName);
            long startTime = System.currentTimeMillis();
            heapSort(bufferPool, bufferPool.getNumRecords());
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            writeStatistics(statFileName, dataFileName, bufferPool.getCacheHits(), bufferPool.getDiskReads(), bufferPool.getDiskWrites(), duration);
            bufferPool.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void heapSort(BufferPoolADT bufferPool, int numRecords) throws IOException {
        buildHeap(bufferPool, numRecords);
        for (int i = numRecords - 1; i > 0; i--) {
            swap(bufferPool, 0, i);
            heapify(bufferPool, i, 0);
        }
    }

    private static void buildHeap(BufferPoolADT bufferPool, int numRecords) throws IOException {
        for (int i = numRecords / 2 - 1; i >= 0; i--) {
            heapify(bufferPool, numRecords, i);
        }
    }

    private static void heapify(BufferPoolADT bufferPool, int heapSize, int rootIndex) throws IOException {
        int largest = rootIndex;
        int leftChild = 2 * rootIndex + 1;
        int rightChild = 2 * rootIndex + 2;

        if (leftChild < heapSize && compare(bufferPool, leftChild, largest) > 0) {
            largest = leftChild;
        }
        if (rightChild < heapSize && compare(bufferPool, rightChild, largest) > 0) {
            largest = rightChild;
        }
        if (largest != rootIndex) {
            swap(bufferPool, rootIndex, largest);
            heapify(bufferPool, heapSize, largest);
        }
    }

    private static int compare(BufferPoolADT bufferPool, int index1, int index2) throws IOException {
        byte[] space1 = new byte[FileGenerator.BYTES_IN_KEY];
        byte[] space2 = new byte[FileGenerator.BYTES_IN_KEY];

        bufferPool.getbytes(space1, FileGenerator.BYTES_IN_KEY, index1 * FileGenerator.BYTES_PER_RECORD);
        bufferPool.getbytes(space2, FileGenerator.BYTES_IN_KEY, index2 * FileGenerator.BYTES_PER_RECORD);

        short key1 = ByteBuffer.wrap(space1).getShort();
        short key2 = ByteBuffer.wrap(space2).getShort();

        return Short.compare(key1, key2);
    }

    private static void swap(BufferPoolADT bufferPool, int index1, int index2) throws IOException {
        byte[] record1 = new byte[FileGenerator.BYTES_PER_RECORD];
        byte[] record2 = new byte[FileGenerator.BYTES_PER_RECORD];

        bufferPool.getbytes(record1, FileGenerator.BYTES_PER_RECORD, index1 * FileGenerator.BYTES_PER_RECORD);
        bufferPool.getbytes(record2, FileGenerator.BYTES_PER_RECORD, index2 * FileGenerator.BYTES_PER_RECORD);

        bufferPool.insert(record2, FileGenerator.BYTES_PER_RECORD, index1 * FileGenerator.BYTES_PER_RECORD);
        bufferPool.insert(record1, FileGenerator.BYTES_PER_RECORD, index2 * FileGenerator.BYTES_PER_RECORD);
    }

    public static void writeStatistics(String statFileName, String dataFileName, int cacheHits, int diskReads, int diskWrites, long duration) {
        try (FileWriter fw = new FileWriter(statFileName, true)) {
            fw.write("Data file: " + dataFileName + "\n");
            fw.write("Cache hits: " + cacheHits + "\n");
            fw.write("Disk reads: " + diskReads + "\n");
            fw.write("Disk writes: " + diskWrites + "\n");
            fw.write("Execution time (ms): " + duration + "\n\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
