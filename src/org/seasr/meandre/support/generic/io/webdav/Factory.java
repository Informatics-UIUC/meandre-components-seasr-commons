package org.seasr.meandre.support.generic.io.webdav;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.http.HttpHost;
import org.apache.http.auth.Credentials;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.seasr.meandre.support.generic.io.webdav.model.ObjectFactory;
import org.seasr.meandre.support.generic.io.webdav.util.WebdavClientException;

/**
 * The factory class is responsible for instantiating the JAXB stuff as well as
 * the instance to WebdavClientImpl.
 */
public class Factory {

    protected static Factory instance = new Factory();

    protected static Factory instance() {
        return instance;
    }

    private JAXBContext context = null;

    public Factory() {
        try {
            if (this.context == null) this.context = JAXBContext.newInstance(ObjectFactory.class);
        }
        catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return the JAXBContext
     */
    public JAXBContext getContext() {
        return this.context;
    }

    /**
     * Note: the unmarshaller is not thread safe, so it must be created for
     * every request.
     * 
     * @return the JAXB Unmarshaller
     */
    public Unmarshaller getUnmarshaller() throws WebdavClientException {
        try {
            return this.context.createUnmarshaller();
        }
        catch (JAXBException e) {
            throw new WebdavClientException(e);
        }
    }

    public WebdavClient begin() throws WebdavClientException {
        return this.begin(null, null, null);
    }

    public WebdavClient begin(HttpHost host) throws WebdavClientException {
        return this.begin(host, null, null);
    }

    public WebdavClient begin(HttpHost host, SSLSocketFactory sslSocketFactory) throws WebdavClientException {
        return this.begin(host, null, sslSocketFactory);
    }

    public WebdavClient begin(HttpHost host, Credentials creds) throws WebdavClientException {
        return this.begin(host, creds, null);
    }

    public WebdavClient begin(HttpHost host, Credentials creds, SSLSocketFactory sslSocketFactory) throws WebdavClientException {
        return this.begin(host, creds, sslSocketFactory, null);
    }

    public WebdavClient begin(HttpHost host, Credentials creds, SSLSocketFactory sslSocketFactory, HttpRoutePlanner routePlanner)
            throws WebdavClientException {
        return new WebdavClientImpl(this, host, creds, sslSocketFactory, routePlanner);
    }
}
