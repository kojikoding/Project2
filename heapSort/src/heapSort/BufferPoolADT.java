// BufferPoolADT.java

// ADT for buffer pools using the message-passing style
public interface BufferPoolADT {
    // Copy "sz" bytes from "space" to position "pos" in the buffered storage
    void insert(byte[] space, int sz, int pos);

    // Copy "sz" bytes from position "pos" of the buffered storage to "space"
    void getbytes(byte[] space, int sz, int pos);
}
