package org.seasr.meandre.support.generic.io.webdav;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpHost;
import org.seasr.meandre.support.generic.io.webdav.model.Creationdate;
import org.seasr.meandre.support.generic.io.webdav.model.Getcontentlength;
import org.seasr.meandre.support.generic.io.webdav.model.Getcontenttype;
import org.seasr.meandre.support.generic.io.webdav.model.Getlastmodified;
import org.seasr.meandre.support.generic.io.webdav.model.Prop;
import org.seasr.meandre.support.generic.io.webdav.model.Response;
import org.seasr.meandre.support.generic.io.webdav.util.WebdavUtil;


/**
 * Describes a resource on a remote server. 
 * This could be a directory or an actual file.
 */
public class DavResource {

    private static final Pattern PAT_RESNAME = Pattern.compile("/([^/]+)/?$");

    private final String _href;
    private final String _name;
    private final Date _creationDate;
    private final Date _modifiedDate;
    private final String _contentType;
    private final long _contentLength;
    private final boolean _isCollection;
    private final HttpHost _host;

    
    public DavResource(HttpHost host, Response resp) {
        _host = host;

        String href = resp.getHref().get(0);
        Prop prop = resp.getPropstat().get(0).getProp();

        _isCollection = resp.getPropstat().get(0).getProp().getResourcetype().getCollection() != null;

        if (_isCollection && !href.endsWith("/")) href += "/";

        _href = href;

        Matcher matcher = PAT_RESNAME.matcher(_href);
        _name = matcher.find() ? matcher.group(1) : null;

        String sCreationDate = null;
        Creationdate gcd = prop.getCreationdate();
        if ((gcd != null) && (gcd.getContent().size() == 1)) sCreationDate = gcd.getContent().get(0);
        _creationDate = WebdavUtil.parseDate(sCreationDate);

        // modifieddate is sometimes not set
        // if that's the case, use creationdate
        String sModifiedDate = null;
        Getlastmodified glm = prop.getGetlastmodified();
        sModifiedDate = ((glm != null) && (glm.getContent().size() == 1)) ? glm.getContent().get(0) : sCreationDate;
        _modifiedDate = WebdavUtil.parseDate(sModifiedDate);

        String contentType = null;
        Getcontenttype gct = prop.getGetcontenttype();
        if ((gct != null) && (gct.getContent().size() == 1)) contentType = gct.getContent().get(0);
        // Make sure that directories have the correct content type.
        if (_isCollection && (contentType == null))
        // Need to correct the contentType to identify as a directory.
            contentType = "httpd/unix-directory";
        _contentType = contentType;

        String contentLength = "0";
        Getcontentlength gcl = prop.getGetcontentlength();
        if ((gcl != null) && (gcl.getContent().size() == 1)) contentLength = gcl.getContent().get(0);
        _contentLength = Long.valueOf(contentLength);
    }

    public String getPath() {
        return getUrl().getPath();
    }
    
    public String getParentPath() {
        URL url = getParentUrl();
        return (url != null) ? url.getPath() : null;
    }

    /**
     * A URLEncoded version of the name as returned by the server.
     */
    public String getName() {
        return _name;
    }

    /**
     * A URLDecoded version of the name.
     */
    public String getNameDecoded() {
        return WebdavUtil.decode(_name);
    }

    /** */
    public Date getCreationDate() {
        return _creationDate;
    }

    /** */
    public Date getModifiedDate() {
        return _modifiedDate;
    }

    /** */
    public String getContentType() {
        return _contentType;
    }

    /** */
    public long getContentLength() {
        return _contentLength;
    }

    /**
     * Absolute url to the resource.
     */
    public URL getUrl() {
        try {
            return new URL(_href);
        }
        catch (MalformedURLException e) {
            try {
                return new URL(new URL(_host.toURI()), _href);
            }
            catch (MalformedURLException e1) {
                throw new RuntimeException(e1);
            }
        }
    }
    
    public URL getParentUrl() {
        URL url = getUrl();
        String path = url.getPath();
        Matcher matcher = PAT_RESNAME.matcher(path);
        if (matcher.find()) 
            path = path.substring(0, matcher.start(1));
        else
            return null;
        
        try {
            return new URL(url, path);
        }
        catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isCollection() {
        return _isCollection;
    }

    @Override
    public String toString() {
        return "DavResource [url=" + _href + ", name=" + _name + ", nameDecoded=" + getNameDecoded() + ", contentLength="
                + _contentLength + ", contentType=" + _contentType + ", created=" + _creationDate + ", modified=" + _modifiedDate
                + ", absoluteUrl=" + getUrl() + ", isCollection=" + isCollection() + "]";
    }
}
