package utils.base64;

import java.io.BufferedInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Filtered input stream for reading base64 encoded data in decoded form. 
 *
 */
public class Base64InputStream extends FilterInputStream {
    /**
     * Default read ahead bytes
     */
    private static final int DEFAULT_READ_COUNT = 4096;
    
    /**
     * Base64 char to byte convertor
     */
    private Base64Convertor.Convertor convertor;
    
    /**
     * Next byte position in decodedBytes
     */
    private int nextPosition;
    
    /**
     * Decoded bytes for read
     */
    private byte[] decodedBytes;
    
    /**
     * Flag indicating end of stream
     */
    private boolean endReached;
    
    /**
     * Construct a Base64InputStream with standard convertor
     * 
     * @param in Source inuput stream containing base64 encoded data 
     * @throws IOException
     */
    public Base64InputStream(InputStream in) throws IOException {
        this(in, Base64Convertor.Convertor.STANDARD);
    }
    
    /**
     * Construct a Base64InputStream with provided convertor
     * 
     * @param in Source input stream containing base64 encoded data
     * @param convertor target convertor
     * @throws IOException
     */
    public Base64InputStream(InputStream in, Base64Convertor.Convertor convertor) throws IOException {
        super(new BufferedInputStream(in));
        if (convertor != null) {
            this.convertor = convertor;
        } else {
            this.convertor = Base64Convertor.Convertor.STANDARD;
        }
        
        buildValues();
    }
    
    /**
     * Build default values
     * 
     * @throws IOException
     */
    private void buildValues() throws IOException {
        this.nextPosition = 0;
        this.endReached = false;
        
        try {
            decodeNextBytes();
        } catch (IOException iox) {
            throw new IOException();
        }
    }
    
    /**
     * Decode ahead for next read
     * 
     * @throws IOException
     */
    private void decodeNextBytes() throws IOException {
        if (endReached) {
            return;
        }
        
        byte[] bytes = new byte[DEFAULT_READ_COUNT];
        
        int iby = in.read();
        int c = 0;
        while (iby != -1) {
            byte byt = convertor.decode(iby);
            if (byt >= 0) {
                bytes[c++] = byt;
                
                if (c == bytes.length) {
                    this.decodedBytes = Base64Convertor.shiftDecode(bytes, c);
                    return;
                }
            }
            iby = in.read();
        }
        
        this.decodedBytes = Base64Convertor.shiftDecode(bytes, c);
        
        endReached = true;
    }
    
    /**
     * Not implemented, call underlying input stream only.
     * 
     * @return an estimate of the number of bytes that can be read (or skipped over) 
     *         from this input stream without blocking.
     */
    @Override
    public int available() throws IOException {
        return getIn().available();
    }
    
    /**
     * Close the underlying input stream.
     */
    @Override
    public void close() throws IOException {
        if (in != null) {
            in.close();
        }
        in = null;
    }
    
    /**
     * Not implemented, always false.
     */
    @Override
    public boolean markSupported() {
        // Not supported!
        return false;
    }
    
    /**
     * Read next byte
     * 
     * @return next decoded byte, -1 is returned when end of stream reached.
     */
    @Override
    public int read() throws IOException {
        if (nextPosition == decodedBytes.length) {
            if (endReached) {
                return -1;
            } else {
                nextPosition = 0;
                decodeNextBytes();
            }
        }
        if (nextPosition < decodedBytes.length) {
            return decodedBytes[nextPosition++] & 0xFF;
        } else {
            return -1;
        }
    }
    
    /**
     * Read bytes that fill up the input buffer
     * 
     * @param bytes buffer for which data is read
     * @return length of data read, -1 is returned when end of stream reached 
     */
    @Override
    public int read(byte[] bytes) throws IOException {
        return read(bytes, 0, bytes.length);
    }
    
    /**
     * Read bytes that fill up the input buffer with specified offset and length
     * 
     * @param bytes buffer for which data is read
     * @param offset start offset of the buffer
     * @param length maximum length of data to be read
     * 
     * @return length of data read, -1 is returned when end of stream reached
     */
    @Override
    public int read(byte[] bytes, int offset, int length) throws IOException {
        if (offset < 0 || length < 0 || offset > bytes.length || offset + length > bytes.length) {
            throw new IndexOutOfBoundsException();
        } else if (length == 0) {
            return 0;
        } else {
            if (endReached && nextPosition >= decodedBytes.length) {
                return -1;
            } else {
                if (length <= decodedBytes.length - nextPosition) {
                    System.arraycopy(decodedBytes, nextPosition, bytes, offset, length);
                    nextPosition += length;
                    
                    if (nextPosition == decodedBytes.length && !endReached) {
                        decodeNextBytes();
                        nextPosition = 0;
                    }
                    
                    return length;
                } else {
                    if (endReached) {
                        if (nextPosition == decodedBytes.length) {
                            return -1;
                        } else {
                            int l = decodedBytes.length - nextPosition;
                            
                            System.arraycopy(decodedBytes, nextPosition, bytes, offset, l);
                            
                            nextPosition = decodedBytes.length;
                            
                            return l;
                        }
                    } else {
                        int l = decodedBytes.length - nextPosition;
                        
                        System.arraycopy(decodedBytes, nextPosition, bytes, offset, l);
                        
                        decodeNextBytes();
                        nextPosition = 0;
                        
                        int r = read(bytes, offset + l, length - l);
                        
                        if (r == -1) {
                            return l;
                        } else {
                            return l + r;
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Get the underlying input stream
     * @return underlying input stream, null is returned for closed stream
     * @throws IOException
     */
    private InputStream getIn() throws IOException {
        if (in != null) {
            return in;
        } else {
            throw new IOException("InputStream closed!");
        }
    }
    
    /**
     * Not implemented
     */
    @Override
    public void reset() throws IOException {
        throw new IOException("reset not supported!!");
    }
    
    /**
     * Skip bytes without reading
     * Actually bytes are still reading in and decoded but offsetted making them unreadable
     * 
     * @param n number of byte to be skipped
     */
    @Override
    public long skip(long n) throws IOException {
        if (endReached) {
            int l = decodedBytes.length - nextPosition;
            
            if (n <= l) {
                nextPosition += n;
                return n;
            } else {
                nextPosition = decodedBytes.length;
                return l;
            }
        } else if (nextPosition + n < decodedBytes.length) {
            nextPosition += n;
            
            return n;
        } else {
            int skipped = 0;
            while (nextPosition + n >= decodedBytes.length) {
                skipped += decodedBytes.length - nextPosition;
                
                decodeNextBytes();
                nextPosition = 0;
                n -= decodedBytes.length;
            }
            
            skipped += n;
            
            return skipped;
        }
    }
}
