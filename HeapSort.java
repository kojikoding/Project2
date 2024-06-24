
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.IOException;

public class HeapSort {

    private BufferPool pool;

    public HeapSort(BufferPool pool) {
        this.pool = pool;
    }


    public static void main(String[] args) {

        if (args.length != 3) {
            System.err.println("Incorrect Paramaters");
            System.exit(1);
        }

        String dataFileName = args[0];
        int buffNum = Integer.parseInt(args[1]);
        String statFileName = args[2];

        File dataFile = new File(dataFileName);
        BufferPool pool = new BufferPool(dataFile);
        HeapSort heapSort = new HeapSort(pool);

        long sTime = System.currentTimeMillis();
        heapSort.sort();
        long eTime = System.currentTimeMillis();
        
        pool.flush();
        heapSort.writeStatistics(statFileName, dataFileName, eTime - sTime);
        heapSort.printStatistics(statFileName);

    }


    public void sort() {
        Buffer buffer = pool.acquireBuffer();
        byte[] data = buffer.getDataPointer();
        int numRec = data.length / 4;
        
       // System.out.println("Data before sorting:");
        printData(data);

        // Heap
        for (int i = numRec / 2 - 1; i >= 0; i--) {
            heapify(data, numRec, i);
        }

        // extract
        for (int i = numRec - 1; i >= 0; i--) {

            swap(data, 0, i);
            heapify(data, i , 0);
        }
        
        buffer.markDirty();

        //System.out.println("Data after sorting:");
        printData(data);
    }
    
    private void heapify(byte[] data, int n, int i)
    {
        while (true) {
            int largest = i;
            int left = 2 * i + 1;
            int right = 2 * i + 2;

            if (left < n && getKey(data, left) > getKey(data, largest)) {
                largest = left;
            }

            if (right < n && getKey(data, right) > getKey(data, largest)) {
                largest = right;
            }

            if (largest == i) {
                break;
            }

            swap(data, i, largest);
            i = largest;
        }
    }
    
    private int getKey(byte[] data, int index)
    {
        int offset = index * 4;
        return ((data[offset] & 0xFF) << 8) | (data[offset + 1] & 0xFF);
    }
    
    private void swap(byte[] data, int i, int j)
    {
        int offI = i * 4;
        int offJ = j * 4;
        
        for(int k = 0; k < 4; k++)
        {
            byte temp = data[offI + k];
            data[offI + k] = data[offJ + k];
            data[offJ + k] = temp;
        }
        
    }
    
    private void writeStatistics(String statFileName, String dataFileName, long elapsedTime) {
        try (PrintStream out = new PrintStream(new FileOutputStream(statFileName, true))) {
            out.println("Data file: " + dataFileName);
            out.println("Cache hits: " + pool.getCacheHits());
            out.println("Disk reads: " + pool.getDiskReads());
            out.println("Disk writes: " + pool.getDiskWrites());
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
    
    private void printData(byte[] data) {
        for (int i = 0; i < data.length; i += 4) {
            int key = ((data[i] & 0xFF) << 8) | (data[i + 1] & 0xFF);
            int value = ((data[i + 2] & 0xFF) << 8) | (data[i + 3] & 0xFF);
            System.out.printf("Key: %c, Value: %c\n", (char)key, (char)value);
        }
    }

}
