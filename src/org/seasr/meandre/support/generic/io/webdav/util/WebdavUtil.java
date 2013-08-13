package org.seasr.meandre.support.generic.io.webdav.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.seasr.meandre.support.generic.io.webdav.model.Multistatus;


public class WebdavUtil {

    /** cached version of getResources() webdav xml GET request */
    private static StringEntity GET_RESOURCES = null;

    /**
     * Date formats using for Date parsing.
     */
    static final SimpleDateFormat formats[] = { new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US),
            new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US), new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US), new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US),
            new SimpleDateFormat("EEEEEE, dd-MMM-yy HH:mm:ss zzz", Locale.US), new SimpleDateFormat("EEE MMMM d HH:mm:ss yyyy", Locale.US) };

    /**
     * GMT timezone.
     */
    final static TimeZone gmtZone = TimeZone.getTimeZone("GMT");

    static {
        for (SimpleDateFormat format : formats) {
            format.setTimeZone(gmtZone);
        }
    }

    /**
     * Hides the irritating declared exception.
     */
    public static String encode(String value) {
        try {
            return URLEncoder.encode(value, "utf-8");
        }
        catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Hides the irritating declared exception.
     *
     * @return null if there is an IllegalArgumentException
     * @throws RuntimeException
     *             if there is an UnsupportedEncodingException
     */
    public static String decode(String value) {
        try {
            return URLDecoder.decode(value, "utf-8");
        }
        catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
        catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /**
     * Loops over all the possible date formats and tries to find the right one.
     *
     * @param dateValue
     */
    public static Date parseDate(String dateValue) {
        if (dateValue == null) return null;

        Date date = null;
        for (int i = 0; (date == null) && (i < formats.length); i++) {
            try {
                synchronized (formats[i]) {
                    date = formats[i].parse(dateValue);
                }
            }
            catch (ParseException e) {
            }
        }

        return date;
    }

    /**
     * Simple class for making propfind a bit easier to deal with.
     */
    public static class HttpPropFind extends HttpEntityEnclosingRequestBase {

        public HttpPropFind(String url) {
            super();
            this.setDepth(1);
            this.setURI(URI.create(url));
            this.setHeader(HttpHeaders.CONTENT_TYPE, "text/xml");
        }

        @Override
        public String getMethod() {
            return "PROPFIND";
        }

        public void setDepth(int val) {
            this.setHeader("Depth", String.valueOf(val));
        }
    }

    /**
     * Simple class for making move a bit easier to deal with.
     */
    public static class HttpMove extends HttpEntityEnclosingRequestBase {

        public HttpMove(String sourceUrl, String destinationUrl) throws WebdavClientException {
            super();
            this.setHeader("Destination", destinationUrl);
            this.setHeader("Overwrite", "T");
            this.setURI(URI.create(sourceUrl));

            if (sourceUrl.endsWith("/") && !destinationUrl.endsWith("/"))
                throw new WebdavClientException("Destinationurl must end with a /", destinationUrl);
        }

        @Override
        public String getMethod() {
            return "MOVE";
        }
    }

    /**
     * Simple class for making copy a bit easier to deal with. Assumes Overwrite
     * = T.
     */
    public static class HttpCopy extends HttpEntityEnclosingRequestBase {

        public HttpCopy(String sourceUrl, String destinationUrl) throws WebdavClientException {
            super();
            this.setHeader("Destination", destinationUrl);
            this.setHeader("Overwrite", "T");
            this.setURI(URI.create(sourceUrl));

            if (sourceUrl.endsWith("/") && !destinationUrl.endsWith("/"))
                throw new WebdavClientException("Destinationurl must end with a /", destinationUrl);
        }

        @Override
        public String getMethod() {
            return "COPY";
        }
    }

    /**
     * Simple class for making mkcol a bit easier to deal with.
     */
    public static class HttpMkCol extends HttpEntityEnclosingRequestBase {

        public HttpMkCol(String url) {
            super();
            this.setURI(URI.create(url));
        }

        @Override
        public String getMethod() {
            return "MKCOL";
        }
    }

    /**
     * Is the status code 2xx
     */
    public static boolean isGoodResponse(int statusCode) {
        return statusCode / 100 == 2; // 2xx codes
    }

    /**
     * Stupid wrapper cause it needs to be in a try/catch
     */
    public static StringEntity getResourcesEntity() {
        if (GET_RESOURCES == null) {
            try {
                GET_RESOURCES = new StringEntity("<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" + "<propfind xmlns=\"DAV:\">\n" + "	<allprop/>\n"
                        + "</propfind>", "UTF-8");
                GET_RESOURCES.setContentType("text/xmlmc; charset=utf-8");
            }
            catch (UnsupportedEncodingException e) {
                // Ignored
            }
        }

        return GET_RESOURCES;
    }

    /**
     * Helper method for getting the Multistatus response processor.
     */
    public static Multistatus getMulitstatus(Unmarshaller unmarshaller, HttpResponse response, String url) throws WebdavClientException {
        try {
            HttpEntity entity = response.getEntity();
            Multistatus multiStatus = (Multistatus) unmarshaller.unmarshal(entity.getContent());
            EntityUtils.consumeQuietly(entity);

            return multiStatus;
        }
        catch (JAXBException ex) {
            throw new WebdavClientException("Problem unmarshalling the data", url, ex);
        }
        catch (IOException ex) {
            throw new WebdavClientException(ex);
        }
    }
}
