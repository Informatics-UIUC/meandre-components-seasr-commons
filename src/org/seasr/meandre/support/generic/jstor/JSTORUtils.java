/**
 *
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
 *
 */

package org.seasr.meandre.support.generic.jstor;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;

/**
 *
 * @author Boris Capitanu
 *
 */
public abstract class JSTORUtils {

    /** Regular expression to extract the document URL from the JSTOR intermediary page */
    public static final Pattern JSTOR_REGEX;

    static {
        // the code expects group 2 to contain the actual URL
        // do not forget to adjust the code if you change the regular expression
        JSTOR_REGEX = Pattern.compile(".*<a href=(\"|\')(.+)\\1>Proceed to PDF</a>.*");
    }

    /**
     * Get a URLConnection to a JSTOR document
     *
     * @param jstorConnection The connection object to the JSTOR permalink (respects timeout values)
     * @return The URLConnection object that can be used to read the JSTOR document
     * @throws JSTORException Thrown if the JSTOR document URL could not be extracted from the intermediary page
     * @throws IOException Thrown if an I/O error occurred
     */
    public static URLConnection getURLConnection(URLConnection jstorConnection) throws JSTORException, IOException {

        int connectTimeout = jstorConnection.getConnectTimeout();
        int readTimeout = jstorConnection.getReadTimeout();

        HttpClient client = new HttpClient();
        HttpConnectionManagerParams connectionParams = client.getHttpConnectionManager().getParams();
        connectionParams.setConnectionTimeout(connectTimeout);
        connectionParams.setSoTimeout(readTimeout);
        client.getParams().setCookiePolicy(CookiePolicy.RFC_2109);

        String response = null;

        GetMethod getMethod = new GetMethod(jstorConnection.getURL().toString());
        try {
            client.executeMethod(getMethod);
            response = getMethod.getResponseBodyAsString();
        }
        finally {
            getMethod.releaseConnection();
        }

        StringBuffer sb = new StringBuffer();
        for (Cookie cookie : client.getState().getCookies())
            sb.append("; ").append(cookie.toExternalForm());

        String cookies = sb.substring(2);

        Matcher matcher = JSTOR_REGEX.matcher(response);
        if (!matcher.find())
            throw new JSTORException("Could not extract the document URL from the JSTOR response page");

        String documentURL = "http://www.jstor.org" + matcher.group(2);
        URL url = new URL(documentURL);

        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(connectTimeout);
        connection.setReadTimeout(readTimeout);
        connection.setRequestProperty("Cookie", cookies);

        return connection;
    }

    /**
     * Get a URLConnection to a JSTOR document
     *
     * @param jstorURL The URL of the JSTOR permalink
     * @return The URLConnection object that can be used to read the JSTOR document
     * @throws JSTORException Thrown if the JSTOR document URL could not be extracted from the intermediary page
     * @throws IOException Thrown if an I/O error occurred
     */
    public static URLConnection getURLConnection(URL jstorURL) throws JSTORException, IOException {
        return getURLConnection(jstorURL.openConnection());
    }

    /**
     * Get a URLConnection to a JSTOR document
     *
     * @param jstorURL The URL of the JSTOR permalink
     * @return The URLConnection object that can be used to read the JSTOR document
     * @throws JSTORException Thrown if the JSTOR document URL could not be extracted from the intermediary page
     * @throws IOException Thrown if an I/O error occurred
     */
    public static URLConnection getURLConnection(String jstorURL) throws JSTORException, IOException {
        return getURLConnection(new URL(jstorURL));
    }
}
