package utils.base64;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Base64 Encoding & decoding
 * 
 */
public class Base64Convertor {
    /**
     * Character to byte convertor<br><br>
     * 
     */
    public enum Convertor {
        /**
         * Author's preference not defined in any specification, using this may cause conflict with standard convertor!!!
         */
        MY      ("my",       _MY_ALPHABET,       _MY_DECODE),
        
        /**
         * Standard base64 convertor
         */
        STANDARD("standard", _STANDARD_ALPHABET, _STANDARD_DECODE),

        /**
         * Ordered base64 convertor, encoding table (URL SAFE) follow its ASCII order
         */
        ORDERED ("ordered",  _ORDERED_ALPHABET,  _ORDERED_DECODE),
        
        /**
         * URL-safe base64 convertor, replacing '+' & '/' with '-' & '_'
         */
        URL_SAFE("urlSafe",  _URL_SAFE_ALPHABET, _URL_SAFE_DECODE);
        
        /**
         * Convertor description
         */
        public  final String desc;
        private final byte[] ALPHABETS;
        private final byte[] DECODABETS;
        
        Convertor(String desc, byte[] ALPHABETS, byte[] DECODABETS) {
            this.desc       = desc;
            this.ALPHABETS  = ALPHABETS;
            this.DECODABETS = DECODABETS;
        }
        
        /**
         * Check whether the character is a valid base64 character
         *  
         * @param c character to be checked
         * 
         * @return check result
         */
        public boolean isEncodedChar(char c) {
            for (byte byt : ALPHABETS) {
                if (byt == c) {
                    return true;
                }
            }
            return false;
        }
        
        /**
         * Check whether the input byte is representing a base64 character
         * 
         * @param b byte to be checked
         * 
         * @return check result
         */
        public boolean isEncodedByte(byte b) {
            for (byte byt : ALPHABETS) {
                if (byt == b) {
                    return true;
                }
            }
            return false;
        }
        
        /**
         * Mapping 6bit value to its corresponding ASCII character
         * 
         * @param byt 6bit byte
         * @return mapped character
         */
        public byte encode(int byt) {
            return ALPHABETS[byt];
        }
        
        /**
         * Mapping character to its corresponding 6bit byte value
         * 
         * @param byt ASCII character in numerical value
         * @return mapped character 6bit byte value (0 ~ 63)
         */
        public byte decode(int byt) {
            return DECODABETS[byt];
        }
        
        /**
         * Mapping 6bit value to its corresponding ASCII character
         * 
         * @param byt 6bit byte
         * @return mapped character
         */
        public byte encode(byte byt) {
            return ALPHABETS[byt];
        }
        
        /**
         * Mapping character to its corresponding 6bit byte value
         * 
         * @param byt ASCII character in numeric value
         * @return mapped character 6bit byte value (0 ~ 63)
         */
        public byte decode(byte byt) {
            return DECODABETS[byt];
        }
        
        /**
         * Retrieve a representation string for this encoder, debug only.
         * 
         * @return representation string
         */
        @Override
        public String toString() {
            return this.desc;
        }
    }
    
    /**
     * Standard chunk size, defined in RFC???
     * 
     */
    public static final int CHUNK_SIZE = 76;
    
    /**
     * Standard padding byte character
     * 
     */
    public static final byte PADDING_BYTE = (byte)'=';
    
    /**
     * Standard line break \r\n
     */
    private static final byte[] LINE_BREAK_BYTES = {'\r', '\n'};
    
    /**
     * MY encoding table
     */
    private final static byte[] _MY_ALPHABET = {
        (byte)'*', (byte)'+',
        (byte)'0', (byte)'1', (byte)'2', (byte)'3', (byte)'4', (byte)'5',
        (byte)'6', (byte)'7', (byte)'8', (byte)'9',
        (byte)'A', (byte)'B', (byte)'C', (byte)'D', (byte)'E', (byte)'F', (byte)'G',
        (byte)'H', (byte)'I', (byte)'J', (byte)'K', (byte)'L', (byte)'M', (byte)'N',
        (byte)'O', (byte)'P', (byte)'Q', (byte)'R', (byte)'S', (byte)'T', (byte)'U',
        (byte)'V', (byte)'W', (byte)'X', (byte)'Y', (byte)'Z',
        (byte)'a', (byte)'b', (byte)'c', (byte)'d', (byte)'e', (byte)'f', (byte)'g',
        (byte)'h', (byte)'i', (byte)'j', (byte)'k', (byte)'l', (byte)'m', (byte)'n',
        (byte)'o', (byte)'p', (byte)'q', (byte)'r', (byte)'s', (byte)'t', (byte)'u',
        (byte)'v', (byte)'w', (byte)'x', (byte)'y', (byte)'z'
    };
    
    /**
     * MY decoding table
     */
    private final static byte[] _MY_DECODE = {
        -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,
        -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,
        0, 1,
        -9,-9,-9,-9,
        2,3,4,5,6,7,8,9,10,11,
        -9,-9,-9,-1,-9,-9,-9,
        12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,        
        -9,-9,-9,-9,-9,-9,
        38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,
        -9,-9,-9,-9
    };
    
    /**
     * Standard encoding table
     */
    private final static byte[] _STANDARD_ALPHABET = {
        (byte)'A', (byte)'B', (byte)'C', (byte)'D', (byte)'E', (byte)'F', (byte)'G',
        (byte)'H', (byte)'I', (byte)'J', (byte)'K', (byte)'L', (byte)'M', (byte)'N',
        (byte)'O', (byte)'P', (byte)'Q', (byte)'R', (byte)'S', (byte)'T', (byte)'U',
        (byte)'V', (byte)'W', (byte)'X', (byte)'Y', (byte)'Z',
        (byte)'a', (byte)'b', (byte)'c', (byte)'d', (byte)'e', (byte)'f', (byte)'g',
        (byte)'h', (byte)'i', (byte)'j', (byte)'k', (byte)'l', (byte)'m', (byte)'n',
        (byte)'o', (byte)'p', (byte)'q', (byte)'r', (byte)'s', (byte)'t', (byte)'u',
        (byte)'v', (byte)'w', (byte)'x', (byte)'y', (byte)'z',
        (byte)'0', (byte)'1', (byte)'2', (byte)'3', (byte)'4', (byte)'5',
        (byte)'6', (byte)'7', (byte)'8', (byte)'9', (byte)'+', (byte)'/'
    };
    
    /**
     * Standard decoding table
     */
    private final static byte[] _STANDARD_DECODE = {
        -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,
        -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,
        62,
        -9,-9,-9,
        63,
        52,53,54,55,56,57,58,59,60,61,
        -9,-9,-9,-1,-9,-9,-9,
        0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,
        -9,-9,-9,-9,-9,-9,
        26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,
        -9,-9,-9,-9
    };
    
    /**
     * Url safe encoding table
     */
    private final static byte[] _URL_SAFE_ALPHABET = {
        (byte)'A', (byte)'B', (byte)'C', (byte)'D', (byte)'E', (byte)'F', (byte)'G',
        (byte)'H', (byte)'I', (byte)'J', (byte)'K', (byte)'L', (byte)'M', (byte)'N',
        (byte)'O', (byte)'P', (byte)'Q', (byte)'R', (byte)'S', (byte)'T', (byte)'U',
        (byte)'V', (byte)'W', (byte)'X', (byte)'Y', (byte)'Z',
        (byte)'a', (byte)'b', (byte)'c', (byte)'d', (byte)'e', (byte)'f', (byte)'g',
        (byte)'h', (byte)'i', (byte)'j', (byte)'k', (byte)'l', (byte)'m', (byte)'n',
        (byte)'o', (byte)'p', (byte)'q', (byte)'r', (byte)'s', (byte)'t', (byte)'u',
        (byte)'v', (byte)'w', (byte)'x', (byte)'y', (byte)'z',
        (byte)'0', (byte)'1', (byte)'2', (byte)'3', (byte)'4', (byte)'5',
        (byte)'6', (byte)'7', (byte)'8', (byte)'9', (byte)'-', (byte)'_'
    };
    
    /**
     * Url safe decoding table
     */
    private final static byte[] _URL_SAFE_DECODE = {
        -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,
        -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,
        62,
        -9,-9,
        52,53,54,55,56,57,58,59,60,61,
        -9,-9,-9,-1,-9,-9,-9,
        0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,
        -9,-9,-9,-9,
        63,
        -9,
        26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,
        -9,-9,-9,-9
    };
    
    /**
     * Ordered encoding table
     */
    private final static byte[] _ORDERED_ALPHABET = {
        (byte)'-',
        (byte)'0', (byte)'1', (byte)'2', (byte)'3', (byte)'4',
        (byte)'5', (byte)'6', (byte)'7', (byte)'8', (byte)'9',
        (byte)'A', (byte)'B', (byte)'C', (byte)'D', (byte)'E', (byte)'F', (byte)'G',
        (byte)'H', (byte)'I', (byte)'J', (byte)'K', (byte)'L', (byte)'M', (byte)'N',
        (byte)'O', (byte)'P', (byte)'Q', (byte)'R', (byte)'S', (byte)'T', (byte)'U',
        (byte)'V', (byte)'W', (byte)'X', (byte)'Y', (byte)'Z',
        (byte)'_',
        (byte)'a', (byte)'b', (byte)'c', (byte)'d', (byte)'e', (byte)'f', (byte)'g',
        (byte)'h', (byte)'i', (byte)'j', (byte)'k', (byte)'l', (byte)'m', (byte)'n',
        (byte)'o', (byte)'p', (byte)'q', (byte)'r', (byte)'s', (byte)'t', (byte)'u',
        (byte)'v', (byte)'w', (byte)'x', (byte)'y', (byte)'z'
    };
    
    /**
     * Ordered decoding table
     */
    private final static byte[] _ORDERED_DECODE = {
        -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,
        -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,
        0,
        -9,-9,
        1,2,3,4,5,6,7,8,9,10,
        -9,-9,-9,-1,-9,-9,-9,
        11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,
        -9,-9,-9,-9,
        37,
        -9,
        38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,
        -9,-9,-9,-9
    };
    
    /**
     * No instantiation is required!
     */
    private Base64Convertor() {}
    
    /**
     * Decode bytes(Base64) to its original binary bytes
     * 
     * @param srcBytes source bytes
     * @param convertor target convertor
     * @return original binary bytes
     */
    public static byte[] decode(byte[] srcBytes, Convertor convertor) {
        byte[] bytes = new byte[srcBytes.length];
        int c = 0;
        for (int i = 0; i < srcBytes.length; i++) {
            byte byt = convertor.decode(srcBytes[i]);
            if (byt >= 0) {
                bytes[c++] = byt;
            }
        }
        return shiftDecode(bytes, c);
    }
    
    /**
     * Decode encoded characters to its original binary bytes
     * 
     * @param chars encoded characters
     * @param convertor target convertor
     * @return original binary bytes
     */
    public static byte[] decode(char[] chars, Convertor convertor) {
        byte[] bytes = new byte[chars.length];
        int c = 0;
        for (int i = 0; i < chars.length; i++) {
            byte byt = convertor.decode(chars[i]);
            if (byt >= 0) {
                bytes[c++] = byt;
            }
        }
        return shiftDecode(bytes, c);
    }

    /**
     * Suggest base64 convertor by detecting special character within encoded string
     * If no distinguishable character is found, suggest standard convertor
     * 
     * MY & ORDERED convertor will be ignored, since they may cause issues
     * 
     * @param base64String encoded base64 string
     *
     * @return suggested convertor, default STANDARD
     */
    public static Convertor suggestConvertor(String base64String) {
        for (int i = 0; i < base64String.length(); i++) {
            char ch = base64String.charAt(i);
            
            // Only suggest standard and url safe convertor
            switch (ch) {
                case '+':
                case '/':
                    return Convertor.STANDARD;
                case '-':
                case '_':
                    return Convertor.URL_SAFE;
            }
        }
        
        // No distinguishable character found, suggest standard
        // Assuming input contains no invalid character, since this method is not for validation
        return Convertor.STANDARD;
    }

    /**
     * Decode base64 encoded string with suggested convertor
     * 
     * @param base64String encoded base64 string
     * 
     * @return decoded bytes
     */
    public static byte[] decode(String base64String) {
        return decode(base64String, suggestConvertor(base64String));
    }
    
    /**
     * Decode encoded string to its original binary bytes
     * 
     * @param base64String base64 encoded string
     * @param convertor target convertor
     * @return original binary bytes
     */
    public static byte[] decode(String base64String, Convertor convertor) {
        byte[] bytes = new byte[base64String.length()];
        
        int c = 0;
        for (int i = 0; i < base64String.length(); i++) {
            byte byt = convertor.decode(base64String.charAt(i));
            if (byt >= 0) {
                bytes[c++] = byt;
            }
        }
        
        return shiftDecode(bytes, c);
    }
    
    /**
     * @param src source (character mapped)
     * @param dest destination
     * @param soffset source offset
     * @param doffset destination offset
     * @param len length (1 ~ 4)
     */
    private static void shiftDecode(byte[] src, byte[] dest, int soffset, int doffset, int len) {
        int v1 = src[soffset], v2 = 0, v3 = 0, v4 = 0;
        if (len == 1) {
            if (doffset < dest.length) {
                dest[doffset] = (byte)(v1 << 2);
            }
        } else {
            v2 = src[soffset+1];
            dest[doffset] = (byte)((v1 << 2) | (v2 >> 4 & 0x3));
            
            if (len == 2) {
                if (doffset + 1 < dest.length) {
                    dest[doffset + 1] = (byte)((v2 & 0xF) << 4);
                }
            } else {
                v3 = src[soffset+2];
                dest[doffset + 1] = (byte)(((v2 & 0xF) << 4) | ((v3 >> 2) & 0xF));
                
                if (len == 3) {
                    if (doffset + 2 < dest.length) {
                        dest[doffset + 2] = (byte) ((v3 & 0x3) << 6);
                    }
                } else {
                    v4 = src[soffset+3];
                    dest[doffset + 2] = (byte)(((v3 & 0x3) << 6) | (v4 & 0x3F));
                }
            }
        }
    }
    
    /**
     * Convert 6bit encoded bytes to 8bit bytes
     * 
     * @param bytes source (character mapped)
     * @param len length of byte to decode
     * @return decoded data
     */
    public static byte[] shiftDecode(byte[] bytes, int len) {
        byte[] output = new byte[(int)Math.floor(len * 6 / 8.0)];
        
        int si = 0, di = 0;
        while (si < len) {
            if (si + 4 < len) {
                shiftDecode(bytes, output, si, di, 4);
            } else {
                shiftDecode(bytes, output, si, di, len - si);
            }
            si += 4;
            di += 3;
        }
        
        return output;
    }

    /**
     * Decode input stream data(Base64 encoded) to byte array
     * 
     * @param is input stream
     * @param convertor target convertor
     * @param closeStream close input stream after decoding
     * @return decoded bytes
     * @throws IOException
     */
    public static byte[] decode(InputStream is, Convertor convertor, boolean closeStream) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        decode(is, bos, convertor, closeStream);
        bos.close();
        return bos.toByteArray();
    }

    /**
     * Decode input stream data(Base64 encoded) and output the result directly
     * 
     * @param is input stream
     * @param os output stream
     * @param convertor target convertor
     * @param closeStream close both input & output stream after decoding
     * @throws IOException
     */
    public static void decode(InputStream is, OutputStream os, Convertor convertor, boolean closeStream) 
            throws IOException {
        byte[] bytes = new byte[4096];
        
        int iby = is.read();
        int c = 0;
        while (iby != -1) {
            byte byt = convertor.decode(iby);
            /* 
             * Ignored any unmappable character
             * If there is any unmappable character(except padding, CRLF)
             * input data may be corrupted or a wrong convertor is used!
             */
            if (byt >= 0) {
                bytes[c++] = byt;
                
                if (c == bytes.length) {
                    os.write(shiftDecode(bytes, c));
                    c = 0;
                }
            }
            iby = is.read();
        }
        
        if (c != 0) {
            os.write(shiftDecode(bytes, c));
        }
        
        os.flush();
        
        if (closeStream) {
            is.close();
            os.close();
        }
    }
    
    /**
     * Decode encoded file and output the result to a file directly
     * 
     * @param input input file
     * @param output output file
     * @param convertor target convertor
     * @throws IOException
     */
    public static void decodeFileToFile(File input, File output, Convertor convertor) throws IOException {
        decode(
                new BufferedInputStream(new FileInputStream(input)),
                new BufferedOutputStream(new FileOutputStream(output)),
                convertor, true
        );
    }
    
    /**
     * Decode encoded file and output the result to a file directly
     * 
     * @param input input file path
     * @param output output file path
     * @param convertor target convertor 
     * @throws IOException
     */
    public static void decodeFileToFile(String input, String output, Convertor convertor) throws IOException {
        decode(
                new BufferedInputStream(new FileInputStream(input)),
                new BufferedOutputStream(new FileOutputStream(output)),
                convertor, true
        );
    }

    /**
     * Encode binary bytes to base64 with defaults: Standard convertor, not chunked, with padding
     * 
     * @param bytes source binary bytes
     * 
     * @return encoded bytes
     */
    public static byte[] encode(byte[] bytes) {
        return encode(bytes, Convertor.STANDARD, false, true);
    }
    
    /**
     * Encode binary bytes to Base64
     * 
     * @param bytes source binary bytes
     * @param convertor target convertor
     * @param chunked result should be chunked
     * @param padding result should be padded (=)
     * @return encoded bytes
     */
    public static byte[] encode(byte[] bytes, Convertor convertor, boolean chunked, boolean padding) {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try {
            encode(bais, baos, convertor, chunked, padding, true);
            return baos.toByteArray();
        } catch (IOException iox) {
            // No actual disk IO, will it occur??
            return null;
        }
    }
    
    /**
     * Encode binary bytes to Base64
     * 
     * @param bytes bytes source binary bytes
     * @param convertor target convertor
     * @param offset starting offset
     * @param length number of byte to encode
     * @param chunked result should be chunked
     * @param padding result should be padded (=)
     * @return encoded bytes
     */
    public static byte[] encode(
            byte[] bytes, Convertor convertor, int offset, int length, boolean chunked, boolean padding
    ) {
        byte[] targetBytes = new byte[length];
        
        System.arraycopy(bytes, offset, targetBytes, 0, length);
        
        return encode(targetBytes, convertor, chunked, padding);
    }

    /**
     * Encode binary bytes to base64 String, with defaults: Standard convertor, not chunked, with padding
     * 
     * @param bytes source binary bytes
     * 
     * @return encoded base64 String
     */
    public static String encodeToString(byte[] bytes) {
        return encodeToString(bytes, Convertor.STANDARD, false, true);
    }
    
    /**
     * Encode binary bytes to Base64 String
     * 
     * @param bytes source binary bytes
     * @param convertor target convertor
     * @param chunked result should be chunked
     * @param padding result should be padded (=)
     * @return encoded Base64 String
     */
    public static String encodeToString(byte[] bytes, Convertor convertor, boolean chunked, boolean padding) {
        return new String(encode(bytes, convertor, chunked, padding));
    }
    
    /**
     * 
     * @param src source bytes
     * @param dest destination bytes
     * @param soffset source offset
     * @param doffset destination offset
     * @param len length (1 ~ 3)
     * @param convertor target convertor
     */
    private static void encode(byte[] src, byte[] dest, int soffset, int doffset, int len, Convertor convertor) {
        dest[doffset] = convertor.encode(src[soffset] >> 2 & 0x3F);
        
        if (len == 1) {
            dest[doffset + 1] = convertor.encode((src[soffset] & 0x3) << 4);
        } else {
            dest[doffset + 1] = convertor.encode((src[soffset] & 0x3) << 4 | (src[soffset + 1] >> 4 & 0xF));
            
            if (len == 2) {
                dest[doffset + 2] = convertor.encode(src[soffset + 1] << 2 & 0x3C);
            } else {
                dest[doffset + 2] = convertor.encode(src[soffset + 1] << 2 & 0x3C | src[soffset + 2] >> 6 & 0x03);
                dest[doffset + 3] = convertor.encode(src[soffset + 2] & 0x3F);
            }
        }
    }
    
    /**
     * 
     * @param bytes binary bytes
     * @param len number of bytes to process, bytes exceeding this length is ignored
     * @param convertor target convertor
     * @return encoded bytes
     */
    private static byte[] encode(byte[] bytes, int len, Convertor convertor) {
        byte[] output = new byte[(int)Math.ceil(len * 8 / 6.0)];
        
        int si = 0, di = 0;
        while (si < len) {
            encode(bytes, output, si, di, (si + 3 < len)? 3 : (len - si), convertor);
            
            si += 3;
            di += 4;
        }
        
        return output;
    }
    
    /**
     * 
     * @param os output stream
     * @param bytes data bytes
     * @param offset data offset
     * @param length length to write
     * @return bytes written
     * @throws IOException
     */
    private static int writeBytes(OutputStream os, byte[] bytes, int offset, int length) throws IOException {
        int end = offset + length;
        if (end < bytes.length) {
            os.write(bytes, offset, length);
            os.write(LINE_BREAK_BYTES);
            return length;
        } else {
            int l = bytes.length - offset;
            os.write(bytes, offset, l);
            return l;
        }
    }
    
    /**
     * 
     * @param os output stream
     * @param bytes data bytes
     * @param lineBreakCount character before next line break
     * @param precedingCount preceding characters on the same line before this method call 
     * @return last line character counts (without line break)
     * @throws IOException
     */
    private static int writeByteWithLineBreak(OutputStream os, byte[] bytes, int lineBreakCount, int precedingCount) 
            throws IOException {
        int offset = 0;
        
        if (precedingCount > 0) {
            offset = writeBytes(os, bytes, offset, lineBreakCount - precedingCount);
        }
        
        int l = 0;
        while (offset < bytes.length) {
            l = writeBytes(os, bytes, offset, lineBreakCount);
            offset +=l;
        }
        
        return l;
    }
    
    /**
     * Encode inputted binary stream to Base64 and output the result directly
     * 
     * @param is input binary data stream
     * @param os result output stream
     * @param convertor target convertor
     * @param chunked result should be chunked
     * @param padding result should be padded
     * @param closeStream close both input & output stream after encoding
     * @throws IOException
     */
    public static void encode(
        InputStream is, OutputStream os, Convertor convertor, 
        boolean chunked, boolean padding, boolean closeStream
    ) throws IOException {
        // Simply ignore chunk & padding when URL_SAFE convertor is used
        if (convertor == Convertor.URL_SAFE) {
            chunked = padding = false;
        }
        
        byte[] bytes = new byte[3072];
        
        int iby = is.read();
        int c = 0, pc = 0;
        while (iby != -1) {
            bytes[c++] = (byte)iby;
            
            if (c == bytes.length) {
                if (chunked) {
                    pc = writeByteWithLineBreak(os, encode(bytes, c, convertor), CHUNK_SIZE, pc);
                } else {
                    os.write(encode(bytes, c, convertor));
                }
                c = 0;
            }
            iby = is.read();
        }
        
        if (c != 0) {
            if (chunked) {
                pc = writeByteWithLineBreak(os, encode(bytes, c, convertor), CHUNK_SIZE, pc);
            } else {
                os.write(encode(bytes, c, convertor));
            }
            if (padding) {
                int r = c % 3;
                if (r > 0) {
                    os.write(PADDING_BYTE);
                    if (r == 1) {
                        os.write(PADDING_BYTE);
                    }
                }
            }
        }
        
        os.flush();
        
        if (closeStream) {
            is.close();
            os.close();
        }
    }
    
    /**
     * Encode binary file and output the result to a file directly
     * 
     * @param input input file
     * @param output result output file
     * @param convertor target convertor
     * @param chunked result should be chunked
     * @param padding result should be padded
     * @throws IOException
     */
    public static void encodeFileToFile(File input, File output, Convertor convertor, boolean chunked, boolean padding) 
            throws IOException {
        encode(
            new BufferedInputStream(new FileInputStream(input)),
            new BufferedOutputStream(new FileOutputStream(output)),
            convertor, chunked, padding, true
        );
    }
    
    /**
     * Encode binary file and output the result to a file directly
     * 
     * @param input input file path
     * @param output output file path
     * @param convertor target convertor
     * @param chunked result should be chunked
     * @param padding result should be padded
     * @throws IOException
     */
    public static void encodeFileToFile(String input, String output, Convertor convertor, boolean chunked, boolean padding) 
            throws IOException {
        encode(
            new BufferedInputStream(new FileInputStream(input)),
            new BufferedOutputStream(new FileOutputStream(output)),
            convertor, chunked, padding, true
        );
    }
}
