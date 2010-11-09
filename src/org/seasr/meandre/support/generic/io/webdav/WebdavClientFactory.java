package org.seasr.meandre.support.generic.io.webdav;

import org.apache.http.HttpHost;
import org.apache.http.auth.Credentials;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.seasr.meandre.support.generic.io.webdav.util.WebdavClientException;


public class WebdavClientFactory {

    public static WebdavClient begin() throws WebdavClientException {
        return Factory.instance().begin(null, null, null);
    }

    /**
     * Default begin() for when you don't need anything but no authentication
     * and default settings for SSL.
     */
    public static WebdavClient begin(HttpHost host) throws WebdavClientException {
        return Factory.instance().begin(host, null, null);
    }

    /**
     * If you want to use custom HTTPS settings, this allows you to
     * pass in a SSLSocketFactory.
     * 
     * @see <a
     *      href="http://hc.apache.org/httpcomponents-client/httpclient/xref/org/apache/http/conn/ssl/SSLSocketFactory.html">SSLSocketFactory</a>
     */
    public static WebdavClient begin(HttpHost host, SSLSocketFactory sslSocketFactory) throws WebdavClientException {
        return Factory.instance().begin(host, null, sslSocketFactory);
    }

    /**
     * Pass in a HTTP Auth username/password for being used with all connections
     */
    public static WebdavClient begin(HttpHost host, Credentials creds) throws WebdavClientException {
        return Factory.instance().begin(host, creds);
    }

    /**
     * If you want to use custom HTTPS settings, this allows you to
     * pass in a SSLSocketFactory.
     * 
     * @see <a
     *      href="http://hc.apache.org/httpcomponents-client/httpclient/xref/org/apache/http/conn/ssl/SSLSocketFactory.html">SSLSocketFactory</a>
     */
    public static WebdavClient begin(HttpHost host, Credentials creds, SSLSocketFactory sslSocketFactory) throws WebdavClientException {
        return Factory.instance().begin(host, creds, sslSocketFactory);
    }

    /**
     * Useful for when you need to define a http proxy
     */
    public static WebdavClient begin(HttpHost host, HttpRoutePlanner routePlanner) throws WebdavClientException {
        return Factory.instance().begin(host, null, null, routePlanner);
    }

    /**
     * Useful for when you need to define a http proxy
     */
    public static WebdavClient begin(HttpHost host, HttpRoutePlanner routePlanner, SSLSocketFactory sslSocketFactory) throws WebdavClientException {
        return Factory.instance().begin(host, null, sslSocketFactory, routePlanner);
    }

    /**
     * Useful for when you need to define a http proxy
     */
    public static WebdavClient begin(HttpHost host, Credentials creds, HttpRoutePlanner routePlanner) throws WebdavClientException {
        return Factory.instance().begin(host, creds, null, routePlanner);
    }

    /**
     * Useful for when you need to define a http proxy
     */
    public static WebdavClient begin(HttpHost host, Credentials creds, SSLSocketFactory sslSocketFactory, HttpRoutePlanner routePlanner)
            throws WebdavClientException {
        return Factory.instance().begin(host, creds, sslSocketFactory);
    }
}
