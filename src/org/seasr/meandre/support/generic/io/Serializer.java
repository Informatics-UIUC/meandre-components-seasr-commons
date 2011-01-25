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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.seasr.meandre.support.generic.util.Tuples.Tuple2;

import com.google.protobuf.AbstractMessageLite;


public abstract class Serializer {

    public static enum SerializationFormat {
        protobuf, java
    }

    public static Tuple2<byte[], SerializationFormat> serializeObject(Object obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SerializationFormat serializer = null;

        if (obj instanceof AbstractMessageLite) {
            // Google Protocol Buffers serialization
            ((AbstractMessageLite) obj).writeTo(baos);
            serializer = SerializationFormat.protobuf;
        }

        else

        if (obj instanceof Serializable) {
            // Regular Java serialization
            ObjectOutputStream out = new ObjectOutputStream(baos);
            out.writeObject(obj);
            out.close();
            serializer = SerializationFormat.java;
        }

        return baos.size() > 0 ? new Tuple2<byte[], SerializationFormat>(baos.toByteArray(), serializer) : null;
    }

    @SuppressWarnings("unchecked")
    public static <T> T deserializeObject(InputStream objStream, Class<T> clazz, SerializationFormat format)
        throws IOException, IllegalArgumentException, IllegalAccessException, ClassNotFoundException {

        T obj = null;

        switch (format) {
            case protobuf:
                try {
                    Method parseFromMethod = clazz.getMethod("parseFrom", InputStream.class);
                    obj = (T) parseFromMethod.invoke(null, objStream);
                }
                catch (NoSuchMethodException e) {
                    throw new IllegalArgumentException("Cannot unserialize object via Google Protocol Buffers: incompatible parameter 'clazz'", e);
                }
                catch (InvocationTargetException e) {
                    throw new IOException(e.getTargetException());
                }
                break;

            case java:
                ObjectInputStream ois = new ObjectInputStream(objStream);
                obj = (T) ois.readObject();
                break;

            default:
                throw new IllegalArgumentException(String.format("Unsupported serialization format: %s", format));
        }

        return obj;
    }
}
