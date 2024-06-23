public interface BufferPoolADT {
    
 
    // ----------------------------------------------------------
    /**
     * Relate a block to a buffer, returning a pointer to a buffer object
     * @param block
     * @return
     */
    Buffer acquireBuffer(int block);

}
