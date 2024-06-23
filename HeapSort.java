
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.IOException;

public class HeapSort {

    private BufferPool bufferPool;

    public HeapSort(BufferPool bufferPool) {
        this.bufferPool = bufferPool;
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.err.println("Usage: java HeapSort <data-file-name> <num-buffers> <stat-file-name>");
            System.exit(1);
        }

        String dataFileName = args[0];
        int numBuffers = Integer.parseInt(args[1]);
        String statFileName = args[2];

        File dataFile = new File(dataFileName);
        BufferPool bufferPool = new BufferPool(numBuffers, dataFile);
        HeapSort heapSort = new HeapSort(bufferPool);

        long startTime = System.currentTimeMillis();
        heapSort.sort();
        long endTime = System.currentTimeMillis();

        bufferPool.flush();
        heapSort.writeStatistics(statFileName, dataFileName, endTime - startTime);
        heapSort.printStatistics(statFileName);
    }

    public void sort() throws IOException {
        long fileSize = bufferPool.getFileSize();
        int numRecords = (int) (fileSize / 4);
        System.out.println("Number of records: " + numRecords);

        // Step 1: Build the heap
        for (int i = numRecords / 2 - 1; i >= 0; i--) {
            System.out.println("Building heap: heapifying at index " + i);
            heapify(numRecords, i);
        }

        // Step 2: Extract elements from the heap one by one
        for (int i = numRecords - 1; i >= 0; i--) {
            System.out.println("Extracting element: swapping index 0 and " + i);
            swap(0, i);
            heapify(i, 0);
        }
    }

    private void heapify(int n, int i) throws IOException {
        System.out.println("Heapifying: size=" + n + ", index=" + i);
        int largest = i;
        int left = 2 * i + 1;
        int right = 2 * i + 2;

        if (left < n && getKey(left) > getKey(largest)) {
            largest = left;
        }

        if (right < n && getKey(right) > getKey(largest)) {
            largest = right;
        }

        if (largest != i) {
            System.out.println("Swapping elements at index " + i + " and " + largest);
            swap(i, largest);
            heapify(n, largest);
        }
    }

    private int getKey(int index) throws IOException {
        int bufferIndex = index / 1024;
        int offset = (index % 1024) * 4;

        System.out.println("Getting key: index=" + index + ", bufferIndex=" + bufferIndex + ", offset=" + offset);

        Buffer buffer = bufferPool.acquireBuffer(bufferIndex);
        byte[] data = buffer.readBlock();

        if (offset + 1 >= data.length) {
            throw new IOException("Offset out of bounds: " + offset);
        }

        int key = ((data[offset] & 0xFF) << 8) | (data[offset + 1] & 0xFF);
        return key;
    }

    private void swap(int i, int j) throws IOException {
        int bufferIndexI = i / 1024;
        int bufferIndexJ = j / 1024;
        int offsetI = (i % 1024) * 4;
        int offsetJ = (j % 1024) * 4;

        System.out.println("Swapping: indexI=" + i + ", indexJ=" + j + ", bufferIndexI=" + bufferIndexI + ", bufferIndexJ=" + bufferIndexJ + ", offsetI=" + offsetI + ", offsetJ=" + offsetJ);

        Buffer bufferI = bufferPool.acquireBuffer(bufferIndexI);
        Buffer bufferJ = bufferPool.acquireBuffer(bufferIndexJ);
        byte[] dataI = bufferI.readBlock();
        byte[] dataJ = bufferJ.readBlock();

        // Swap the 4-byte records
        for (int k = 0; k < 4; k++) {
            byte temp = dataI[offsetI + k];
            dataI[offsetI + k] = dataJ[offsetJ + k];
            dataJ[offsetJ + k] = temp;
        }

        bufferI.markDirty();
        bufferJ.markDirty();
    }

    private void writeStatistics(String statFileName, String dataFileName, long elapsedTime) {
        try (PrintStream out = new PrintStream(new FileOutputStream(statFileName, true))) {
            out.println("Data file: " + dataFileName);
            out.println("Cache hits: " + BufferPool.getCacheHits());
            out.println("Disk reads: " + BufferPool.getDiskReads());
            out.println("Disk writes: " + BufferPool.getDiskWrites());
            out.println("Execution time: " + elapsedTime + " ms");
            out.println();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void printStatistics(String statFileName) {
        try (BufferedReader reader = new BufferedReader(new FileReader(statFileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}