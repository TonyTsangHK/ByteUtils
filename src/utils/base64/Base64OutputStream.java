package utils.base64;

import java.io.BufferedOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Filtered output stream for writing data in encoded form
 *
 */
public class Base64OutputStream extends FilterOutputStream {
    /**
     * Default buffer size for unencoded data
     */
    private static final int DEFAULT_UNENCODED_BYTE_COUNT = 3072;
    
    /**
     * Base64 char to byte convertor
     */
    private Base64Convertor.Convertor convertor;
    
    /**
     * Padding byte
     */
    private byte paddingByte;
    
    /**
     * Flags indicating data should chunked / padded
     * 
     * P.S. padding byte will not written unless the stream is closing
     */
    private boolean chunked, padding;
    
    /**
     * Buffer for unencoded data before writing to the underlying output stream.
     */
    private byte[] unencodedBytes;
    
    /**
     * Next position of input byte (unencoded)
     */
    private int nextPosition = 0;
    
    /**
     * Counters recording wrote byte & encoded byte
     */
    private int byteWrote = 0, encodedByteWrote = 0;
    
    /**
     * bytes used for line break
     */
    private byte[] lineBreakBytes = {'\r', '\n'};
    
    /**
     * Construct a Base64OutputStream with default settings: No line break & paddings, Standard convertor
     * 
     * @param os underlying output stream
     */
    public Base64OutputStream(OutputStream os) {
        this(os, Base64Convertor.Convertor.STANDARD, false, false);
    }
    
    /**
     * Construct a Base64OutputStream with standard convertor
     * 
     * @param os underlying output stream
     * @param chunked flag indicating data should be chunked
     * @param padding flag indication data should be padded
     */
    public Base64OutputStream(OutputStream os, boolean chunked, boolean padding) {
        this(os, Base64Convertor.Convertor.STANDARD, chunked, padding);
    }
    
    /**
     * Construct a Base64OutputStream
     * 
     * @param os underlying output stream
     * @param convertor base64 char to byte convertor
     * @param chunked flag indicating data should be chunked
     * @param padding flag indication data should be padded
     */
    public Base64OutputStream(OutputStream os, Base64Convertor.Convertor convertor, boolean chunked, boolean padding) {
        super(new BufferedOutputStream(os));
        
        this.convertor = convertor;
        
        this.chunked = chunked;
        this.padding = padding;
        
        this.paddingByte = Base64Convertor.PADDING_BYTE;
        
        buildValues();
    }
    
    /**
     * Construct a Base64OutputStream
     * 
     * @param os underlying output stream
     * @param convertor base64 char to byte convertor
     * @param chunked flag indicating data should be chunked
     * @param padding flag indicating data should be padded
     * @param paddingByte padding byte 
     * @param lineBreakStr string used for line breaks
     */
    public Base64OutputStream(
        OutputStream os, Base64Convertor.Convertor convertor, boolean chunked, boolean padding,
        byte paddingByte, String lineBreakStr
    ) {
        this(os, convertor, chunked, padding, paddingByte, lineBreakStr.getBytes());
    }
    
    /**
     * Construct a Base64OutputStream
     * 
     * @param os underlying output stream
     * @param convertor base64 char to byte convertor
     * @param chunked flag indicating data should be chunked
     * @param padding flag indicating data should be padded
     * @param paddingByte padding byte
     * @param lineBreakBytes byte used for line breaks
     */
    public Base64OutputStream(
        OutputStream os, Base64Convertor.Convertor convertor, boolean chunked, boolean padding,
        byte paddingByte, byte[] lineBreakBytes
    ) {
        super(new BufferedOutputStream(os));
        
        this.convertor = convertor;
        
        this.chunked = chunked;
        this.padding = padding;
        
        this.paddingByte = paddingByte;
        
        this.lineBreakBytes = lineBreakBytes;
        
        buildValues();
    }
    
    /**
     * build default values
     */
    private void buildValues() {
        unencodedBytes = new byte[DEFAULT_UNENCODED_BYTE_COUNT];
        
        nextPosition = 0;
    }
    
    /**
     * Close the underlying output stream.
     * Padding bytes is written during this process.
     */
    @Override
    public void close() throws IOException {
        flushAll();
        if (padding) {
            int r = byteWrote % 3;
            if (r > 0) {
                out.write(paddingByte);
                if (r == 1) {
                    out.write(paddingByte);
                }
            }
        }
        out.close();
    }
    
    /**
     * Flushes out unwritten(unencoded) bytes<br>
     * if the unencoded bytes does makes up blocks of 3, the final incomplete block will keep unencoded.
     */
    @Override
    public void flush() throws IOException {
        writeUnencoded(true);
        out.flush();
    }
    
    /**
     * Flush everything!
     * 
     * @throws IOException
     */
    public void flushAll() throws IOException {
        writeUnencoded(false);
        out.flush();
    }
    
    /**
     * Encode the unencoded data and write it to the underlying output stream.
     * 
     * @param keepIncompleteBlock indicating incomplete block (block of 3) should be keep or not
     * 
     * @throws IOException
     */
    private void writeUnencoded(boolean keepIncompleteBlock) throws IOException {
        if (nextPosition == 0) {
            return;
        }
        
        int length = nextPosition;
        
        if (keepIncompleteBlock) {
            length = nextPosition - (nextPosition % 3);
        }
        
        byte[] encodedBytes = null;
        if (nextPosition == unencodedBytes.length) {
            encodedBytes = Base64Convertor.encode(unencodedBytes, convertor, false, false);
        } else {
            encodedBytes = Base64Convertor.encode(unencodedBytes, convertor, 0, length, false, false);
        }
        
        if (length != nextPosition) {
            System.arraycopy(unencodedBytes, length, unencodedBytes, 0, nextPosition-length);
        }
        
        if (chunked) {
            int l = encodedBytes.length, offset = 0;
            int preceeding = encodedByteWrote % Base64Convertor.CHUNK_SIZE;
            
            if (preceeding == 0 && encodedByteWrote > 0) {
                out.write(lineBreakBytes);
            }
            
            while (l > 0 && preceeding + l >= Base64Convertor.CHUNK_SIZE) {
                int bl = Base64Convertor.CHUNK_SIZE - preceeding;
                
                if (bl > 0) {
                    out.write(encodedBytes, offset, bl);
                    
                    if (l > bl) {
                        out.write(lineBreakBytes);
                    }
                }
                
                preceeding = 0;
                l -= bl;
                offset += bl;
            }
            
            out.write(encodedBytes, offset, l);
        } else {
            out.write(encodedBytes);
        }
        
        encodedByteWrote += encodedBytes.length;
        byteWrote += length;
        nextPosition = nextPosition - length;
    }
    
    /**
     * Write a byte, it will be stored in the unencoded buffer directly. 
     */
    @Override
    public void write(int b) throws IOException {
        // if the buffer is full write it to the underlying output stream.
        if (nextPosition == unencodedBytes.length) {
            writeUnencoded(false);
        }
        unencodedBytes[nextPosition++] = (byte)b;
    }
    
    /**
     * Write bytes
     * 
     * @param bytes bytes to be written(encoded)
     * 
     * @throws IOException
     */
    @Override
    public void write(byte[] bytes) throws IOException {
        write(bytes, 0, bytes.length);
    }
    
    /**
     * Write bytes with provided offset and length
     * 
     * @param bytes bytes to be written(encoded)
     * @param offset offsetted bytes length
     * @param length maximum bytes length to be written(encoded)
     * 
     * @throws IOException
     */
    @Override
    public void write(byte[] bytes, int offset, int length) throws IOException {
        if (nextPosition == unencodedBytes.length) {
            writeUnencoded(false);
        }
        
        while (nextPosition + length >= unencodedBytes.length) {
            int l = unencodedBytes.length - nextPosition;
            
            System.arraycopy(bytes, offset, unencodedBytes, nextPosition, l);
            nextPosition = unencodedBytes.length;
            writeUnencoded(false);
            
            offset += l;
            length -= l;
        }
        
        if (length > 0) {
            System.arraycopy(bytes, offset, unencodedBytes, nextPosition, length);
            
            nextPosition += length;
        }
    }
}
