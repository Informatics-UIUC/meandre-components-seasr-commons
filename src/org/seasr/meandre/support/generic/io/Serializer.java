/**
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * All rights reserved.
 *
 * Developed by:
 *
 * Automated Learning Group
 * National Center for Supercomputing Applications
 * http://www.seasr.org
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal with the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimers.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimers in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the names of Automated Learning Group, The National Center for
 *    Supercomputing Applications, or University of Illinois, nor the names of
 *    its contributors may be used to endorse or promote products derived from
 *    this Software without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * WITH THE SOFTWARE.
 */

package org.seasr.meandre.support.generic.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.google.protobuf.AbstractMessageLite;


public abstract class Serializer {

    public static final String SIGNATURE = "MPF";   // Meandre Persistence File
    public static final int VERSION = 1;

    public static enum SerializationFormat {
        protobuf, java
    }

    public static void serializeObject(Object obj, OutputStream outputStream, boolean useCompression) throws IOException {
        DataOutputStream dataStream = new DataOutputStream(outputStream);
        try {
            dataStream.writeBytes(SIGNATURE);
            dataStream.writeShort(VERSION);
            dataStream.writeUTF(obj.getClass().getName());
            dataStream.writeBoolean(useCompression);

            if (obj instanceof AbstractMessageLite) {
                dataStream.writeUTF(SerializationFormat.protobuf.name());
                OutputStream objStream = useCompression ? new ZipOutputStream(dataStream) : dataStream;
                ((AbstractMessageLite) obj).writeTo(objStream);
                objStream.close();
            }

            else

            if (obj instanceof Serializable) {
                dataStream.writeUTF(SerializationFormat.java.name());
                OutputStream objStream = useCompression ? new ZipOutputStream(dataStream) : dataStream;
                ObjectOutputStream out = new ObjectOutputStream(objStream);
                out.writeObject(obj);
                out.close();
                objStream.close();
            }
        }
        finally {
            dataStream.close();
        }
    }

    public static Object deserializeObject(InputStream inputStream) throws IOException, SerializationException {
        DataInputStream dataStream = new DataInputStream(inputStream);
        Object obj = null;

        try {
            // check signature
            byte[] signature = new byte[3];
            dataStream.read(signature);
            if (! new String(signature).equals(SIGNATURE))
                throw new SerializationException("Incorrect file header.");

            int version = dataStream.readShort();
            if (version > VERSION)
                throw new SerializationException("Incompatible version numbers (persisted file was created with newer serializer)");

            String className = dataStream.readUTF();
            boolean useCompression = dataStream.readBoolean();
            String format = dataStream.readUTF();

            switch (SerializationFormat.valueOf(format)) {
                case protobuf:
                    try {
                        Method parseFromMethod = Class.forName(className).getMethod("parseFrom", InputStream.class);
                        InputStream objStream = useCompression ? new ZipInputStream(dataStream) : dataStream;
                        obj = parseFromMethod.invoke(null, objStream);
                    }
                    catch (NoSuchMethodException e) {
                        throw new IllegalArgumentException("Cannot unserialize object via Google Protocol Buffers: incompatible parameter 'clazz'", e);
                    }
                    catch (InvocationTargetException e) {
                        throw new IOException(e.getTargetException());
                    }
                    catch (IllegalArgumentException e) {
                        throw new SerializationException(e);
                    }
                    catch (IllegalAccessException e) {
                        throw new SerializationException(e);
                    }
                    catch (SecurityException e) {
                        throw new SerializationException(e);
                    }
                    break;

                case java:
                    InputStream objStream = useCompression ? new ZipInputStream(dataStream) : dataStream;
                    ObjectInputStream ois = new ObjectInputStream(objStream);
                    obj = ois.readObject();
                    break;

                default:
                    throw new IllegalArgumentException(String.format("Unsupported serialization format: %s", format));
            }
        }
        catch (ClassNotFoundException e) {
            throw new SerializationException(e);
        }
        finally {
            dataStream.close();
        }

        return obj;
    }

    @SuppressWarnings("serial")
    public static class SerializationException extends Exception {
        public SerializationException(String message) {
            super(message);
        }

        public SerializationException(Throwable t) {
            super(t);
        }
    }
}
