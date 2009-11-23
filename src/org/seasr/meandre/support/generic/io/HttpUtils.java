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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Properties;
import java.util.Map.Entry;

/**
 * @author Boris Capitanu
 */

public abstract class HttpUtils {

    /**
     * Performs a GET request on a URL and returns the response
     *
     * @param sUrl The URL
     * @return The response
     * @throws MalformedURLException
     * @throws IOException
     */
    public static String doGET(String sUrl) throws MalformedURLException, IOException {
        URL url = new URL(sUrl);
        return IOUtils.getTextFromReader(new InputStreamReader(url.openStream()));
    }

    /**
     * Performs a POST request on a URL and returns the response
     *
     * @param sUrl The URL
     * @param props The payload of the POST request
     * @return The response
     * @throws MalformedURLException
     * @throws IOException
     */
    public static String doPOST(String sUrl, Properties props) throws MalformedURLException, IOException {
        URL url = new URL(sUrl);
        URLConnection connection = url.openConnection();
        connection.setDoOutput(true);

        StringBuffer sb = new StringBuffer();
        for (Entry<Object, Object> prop : props.entrySet()) {
            if (sb.length() > 0) sb.append("&");

            sb.append(URLEncoder.encode(prop.getKey().toString(), "UTF-8"));
            sb.append("=");
            sb.append(URLEncoder.encode(prop.getValue().toString(), "UTF-8"));
        }

        // Send the data
        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
        writer.write(sb.toString());
        writer.flush();

        // Get the response
        return IOUtils.getTextFromReader(new InputStreamReader(connection.getInputStream()));
    }

    /**
     * Performs a POST request on a URL and returns the response
     *
     * @param sUrl The URL
     * @param sData The payload
     * @return The response
     * @throws MalformedURLException
     * @throws IOException
     */
    public static String doPOST(String sUrl, String sData) throws MalformedURLException, IOException {
        URL url = new URL(sUrl);
        URLConnection connection = url.openConnection();
        connection.setDoOutput(true);

        // Send the data
        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
        writer.write(sData);
        writer.flush();

        // Get the response
        return IOUtils.getTextFromReader(new InputStreamReader(connection.getInputStream()));
    }

    /**
     * Performs a PUT request on a URL and returns the response
     *
     * @param sUrl The URL
     * @param sData The payload
     * @return The response
     * @throws MalformedURLException
     * @throws IOException
     */
    public static String doPUT(String sUrl, String sData) throws MalformedURLException, IOException {
        URL url = new URL(sUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("PUT");

        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(sData);
            writer.flush();

            // Get the response
            return IOUtils.getTextFromReader(new InputStreamReader(connection.getInputStream()));
        }
        finally {
            if (writer != null)
                writer.close();
        }
    }

    /**
     * Performs a DELETE request on a URL and returns the response
     *
     * @param sUrl The URL
     * @return The response
     * @throws MalformedURLException
     * @throws IOException
     */
    public static String doDELETE(String sUrl) throws MalformedURLException, IOException {
        URL url = new URL(sUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("DELETE");
        connection.connect();

        // Get the response
        return IOUtils.getTextFromReader(new InputStreamReader(connection.getInputStream()));
    }
}
