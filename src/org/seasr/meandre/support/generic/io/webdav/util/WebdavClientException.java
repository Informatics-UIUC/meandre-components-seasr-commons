package org.seasr.meandre.support.generic.io.webdav.util;

import java.io.IOException;


public class WebdavClientException extends IOException {
    private static final long serialVersionUID = 5635203788305340444L;
    
    private final int statusCode;
    private final String responsePhrase;
    private final String url;

    public WebdavClientException(Exception ex) {
        initCause(ex);
        statusCode = -1;
        responsePhrase = null;
        url = null;
    }

    public WebdavClientException(String msg, String url) {
        this(msg, url, -1, null, null);
    }

    public WebdavClientException(String msg, String url, Exception initCause) {
        this(msg, url, -1, null, initCause);
    }

    public WebdavClientException(String msg, String url, int statusCode, String responsePhrase) {
        this(msg, url, statusCode, responsePhrase, null);
    }

    public WebdavClientException(String url, int statusCode, String responsePhrase) {
        this("The server has returned an HTTP error", url, statusCode, responsePhrase, null);
    }

    public WebdavClientException(String msg, String url, int statusCode, String responsePhrase, Exception initCause) {
        super(String.format("%s [Url: %s, Status: %d, Reason: %s]", msg, url, statusCode, responsePhrase));
        this.url = url;
        this.statusCode = statusCode;
        this.responsePhrase = responsePhrase;
        if (initCause != null) initCause(initCause);
    }

    /**
     * The url that caused the failure.
     */
    public String getUrl() {
        return url;
    }

    /**
     * The http client status code. A status code of -1 means that there isn't
     * one and probably isn't a response phrase either.
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * The http client response phrase.
     */
    public String getResponsePhrase() {
        return responsePhrase;
    }
}
