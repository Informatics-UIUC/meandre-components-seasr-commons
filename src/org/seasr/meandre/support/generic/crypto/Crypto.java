/*
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2008, NCSA.  All rights reserved.
 *
 * Developed by:
 * The Automated Learning Group
 * University of Illinois at Urbana-Champaign
 * http://www.seasr.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject
 * to the following conditions:
 *
 * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimers.
 *
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimers in
 * the documentation and/or other materials provided with the distribution.
 *
 * Neither the names of The Automated Learning Group, University of
 * Illinois at Urbana-Champaign, nor the names of its contributors may
 * be used to endorse or promote products derived from this Software
 * without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 */

package org.seasr.meandre.support.generic.crypto;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public abstract class Crypto {

    private static final byte[] HEX_CHAR_TABLE = {
        (byte)'0', (byte)'1', (byte)'2', (byte)'3',
        (byte)'4', (byte)'5', (byte)'6', (byte)'7',
        (byte)'8', (byte)'9', (byte)'a', (byte)'b',
        (byte)'c', (byte)'d', (byte)'e', (byte)'f'
    };

    /**
     * Returns a HEX string for the specified byte array
     *
     * @param bytes The byte array
     * @return The HEX string in ASCII encoding, or null if a problem occurred
     */
    public static String getHexString(byte[] bytes) {
        byte[] hex = new byte[2 * bytes.length];
        int index = 0;

        for (byte b : bytes) {
            int v = b & 0xFF;
            hex[index++] = HEX_CHAR_TABLE[v >>> 4];
            hex[index++] = HEX_CHAR_TABLE[v & 0xF];
        }

        try {
            return new String(hex, "ASCII");
        }
        catch (UnsupportedEncodingException e) {
            // Should never happen
            return null;
        }
    }

    /**
     * Creates an MD5 hash for an array of bytes
     *
     * @param bytes The byte array
     * @return The MD5 hash, or null if the hash cannot be computed
     */
    public static byte[] createMD5Hash(byte[] bytes) {
        try {
            return createHash(bytes, "MD5");
        }
        catch (NoSuchAlgorithmException e) {
            // Should not happen
            return null;
        }
    }

    /**
     * Creates an MD5 hash for a file
     *
     * @param file The file
     * @return The MD5 hash, or null if the hash cannot be computed
     * @throws IOException Thrown if an I/O error occurs
     */
    public static byte[] createMD5Hash(File file) throws IOException {
        try {
            return createHash(file, "MD5");
        }
        catch (NoSuchAlgorithmException e) {
            // Should never happen
            return null;
        }
    }

    /**
     * Creates a SHA1 hash for an array of bytes
     *
     * @param bytes The byte array
     * @return The SHA1 hash, or null if the hash cannot be computed
     */
    public static byte[] createSHA1Hash(byte[] bytes) {
        try {
            return createHash(bytes, "SHA-1");
        }
        catch (NoSuchAlgorithmException e) {
            // Should not happen
            return null;
        }
    }

    /**
     * Creates a SHA1 hash for a file
     *
     * @param file The file
     * @return The SHA1 hash, or null if the hash cannot be computed
     * @throws IOException Thrown if an I/O error occurs
     */
    public static byte[] createSHA1Hash(File file) throws IOException {
        try {
            return createHash(file, "SHA-1");
        }
        catch (NoSuchAlgorithmException e) {
            // Should never happen
            return null;
        }
    }

    /**
     * Creates a hash for a byte array based on a specified algorithm
     *
     * @param bytes The byte array
     * @param algorithm The algorithm to use
     * @return The hash
     * @throws NoSuchAlgorithmException Thrown if no such algorithm is available
     */
    public static byte[] createHash(byte[] bytes, String algorithm) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance(algorithm).digest(bytes);
    }

    /**
     * Creates a hash for a file based on a specified algorithm
     *
     * @param file The file
     * @param algorithm The algorithm to use
     * @return The hash
     * @throws NoSuchAlgorithmException Thrown if no such algorithm is available
     */
    public static byte[] createHash(File file, String algorithm) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        FileInputStream fis = new FileInputStream(file);

        try {
            byte[] buffer = new byte[4096];

            int numRead;
            do {
                if ((numRead = fis.read(buffer)) > 0)
                    md.update(buffer, 0, numRead);
            } while (numRead > 0);
        }
        finally {
            fis.close();
        }

        return md.digest();
    }
}
