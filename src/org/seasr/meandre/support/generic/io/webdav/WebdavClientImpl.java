package org.seasr.meandre.support.generic.io.webdav;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.seasr.meandre.support.generic.io.webdav.model.Multistatus;
import org.seasr.meandre.support.generic.io.webdav.model.Response;
import org.seasr.meandre.support.generic.io.webdav.util.WebdavClientException;
import org.seasr.meandre.support.generic.io.webdav.util.WebdavUtil;
import org.seasr.meandre.support.generic.io.webdav.util.WebdavUtil.HttpCopy;
import org.seasr.meandre.support.generic.io.webdav.util.WebdavUtil.HttpMkCol;
import org.seasr.meandre.support.generic.io.webdav.util.WebdavUtil.HttpMove;
import org.seasr.meandre.support.generic.io.webdav.util.WebdavUtil.HttpPropFind;


public class WebdavClientImpl implements WebdavClient {

    private final Factory factory;
    private final DefaultHttpClient client;
    private final HttpHost host;
    
    
    public WebdavClientImpl(Factory factory) throws WebdavClientException {
        this(factory, null, null, null, null, null);
    }

    public WebdavClientImpl(Factory factory, HttpHost host) throws WebdavClientException {
        this(factory, host, null, null, null, null);
    }

    public WebdavClientImpl(Factory factory, HttpHost host, String username, String password) throws WebdavClientException {
        this(factory, host, username, password, null, null);
    }

    /**
     * Main constructor.
     */
    public WebdavClientImpl(Factory factory, HttpHost host, String username, String password, SSLSocketFactory sslSocketFactory, HttpRoutePlanner routePlanner)
            throws WebdavClientException {
        this.factory = factory;
        this.host = host;

        HttpParams params = new BasicHttpParams();
        ConnPerRouteBean connPerRoute = new ConnPerRouteBean(20);
        ConnManagerParams.setMaxConnectionsPerRoute(params, connPerRoute);
        ConnManagerParams.setMaxTotalConnections(params, 100);
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setUserAgent(params, "Sardine/" + Version.getSpecification());

        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80)); 
        if (sslSocketFactory != null)
            schemeRegistry.register(new Scheme("https", sslSocketFactory, 443));
        else
            schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

        ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
        this.client = new DefaultHttpClient(cm, params);

        // for proxy configurations
        if (routePlanner != null) this.client.setRoutePlanner(routePlanner);

        if ((username != null) && (password != null)) {
            Credentials credentials = new UsernamePasswordCredentials(username, password);

            AuthScope authScope = (host != null) ? new AuthScope(host.getHostName(), host.getPort()) : new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT);
            this.client.getCredentialsProvider().setCredentials(authScope, credentials);
        }
    }

    /* (non-Javadoc)
     * @see org.seasr.meandre.support.generic.io.webdav.WebdavClient#getResourceInfo(java.lang.String)
     */
    public DavResource getResourceInfo(String url) throws WebdavClientException {
        HttpPropFind propFind = new HttpPropFind(url);
        propFind.setEntity(WebdavUtil.getResourcesEntity());
        propFind.setDepth(0);
        
        HttpResponse response = executeRequest(propFind);
 
        Multistatus multistatus = WebdavUtil.getMulitstatus(this.factory.getUnmarshaller(), response, url);
        List<Response> responses = multistatus.getResponse();

        if (responses.size() != 1)
            throw new WebdavClientException("Unexpected response length: " + responses.size(), url);
        
        return new DavResource(host, responses.get(0));
    }

    /* (non-Javadoc)
     * @see org.seasr.meandre.support.generic.io.webdav.WebdavClient#listContents(java.lang.String)
     */
    public List<DavResource> listContents(String url) throws WebdavClientException {
        if (!url.endsWith("/")) url += "/";
        
        HttpPropFind propFind = new HttpPropFind(url);
        propFind.setEntity(WebdavUtil.getResourcesEntity());

        HttpResponse response = executeRequest(propFind);
      
        Multistatus multistatus = WebdavUtil.getMulitstatus(this.factory.getUnmarshaller(), response, url);
        List<Response> responses = multistatus.getResponse();

        List<DavResource> resources = new ArrayList<DavResource>(responses.size() - 1);
        
        for (Response resp : responses) {        
            if (url.startsWith(resp.getHref().get(0))) continue;
            resources.add(new DavResource(host, resp));
        }
 
        return resources;
    }
    
    /* (non-Javadoc)
     * @see org.seasr.meandre.support.generic.io.webdav.WebdavClient#listContents(java.lang.String, boolean)
     */
    public List<DavResource> listContents(String url, boolean recurse) throws WebdavClientException {
        return listContents(url, recurse, new FilenameFilter() {
            public boolean accept(File dir, String name) {
                // accept everything
                return true;
            }
        });
    }
 
    /* (non-Javadoc)
     * @see org.seasr.meandre.support.generic.io.webdav.WebdavClient#listContents(java.lang.String, boolean, java.io.FilenameFilter)
     */
    public List<DavResource> listContents(String url, boolean recurse, FilenameFilter filter) throws WebdavClientException {
        // make sure folder references end with "/"
        if (!url.endsWith("/")) url += "/";

        List<DavResource> resources = new Vector<DavResource>();
        listResourcesInternal(url, recurse, filter, resources);
        
        return resources;
    }
    
    /* (non-Javadoc)
     * @see org.seasr.meandre.support.generic.io.webdav.WebdavClient#listFiles(java.lang.String)
     */
    public List<DavResource> listFiles(String url) throws WebdavClientException {
        return listFiles(url, false);
    }
    
    /* (non-Javadoc)
     * @see org.seasr.meandre.support.generic.io.webdav.WebdavClient#listFiles(java.lang.String, boolean)
     */
    public List<DavResource> listFiles(String url, boolean recurse) throws WebdavClientException {
        return listContents(url, recurse, new FilenameFilter() {
            public boolean accept(File dir, String name) {
                // accept only files
                return name != null;
            }
        });
    }

    /* (non-Javadoc)
     * @see org.seasr.meandre.support.generic.io.webdav.WebdavClient#getResourceAsStream(java.lang.String)
     */
    public InputStream getResourceAsStream(String url) throws WebdavClientException {
        HttpGet get = new HttpGet(url);

        HttpResponse response = executeRequest(get);

        try {
            return response.getEntity().getContent();
        }
        catch (IOException ex) {
            get.abort();
            throw new WebdavClientException(ex);
        }
    }

    /* (non-Javadoc)
     * @see org.seasr.meandre.support.generic.io.webdav.WebdavClient#getResourceAsString(java.lang.String)
     */
    public String getResourceAsString(String url) throws WebdavClientException {
        HttpGet get = new HttpGet(url);

        HttpResponse response = executeRequest(get);

        try {
            return EntityUtils.toString(response.getEntity());
        }
        catch (Exception e) {
            throw new WebdavClientException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.seasr.meandre.support.generic.io.webdav.WebdavClient#getResourceAsByteArray(java.lang.String)
     */
    public byte[] getResourceAsByteArray(String url) throws WebdavClientException {
        HttpGet get = new HttpGet(url);

        HttpResponse response = executeRequest(get);

        try {
            return EntityUtils.toByteArray(response.getEntity());
        }
        catch (Exception e) {
            throw new WebdavClientException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.seasr.meandre.support.generic.io.webdav.WebdavClient#put(java.lang.String, byte[])
     */
    public void put(String url, byte[] data) throws WebdavClientException {
        HttpPut put = new HttpPut(url);
        put.setEntity(new ByteArrayEntity(data));

        executeRequest(put);
    }

    /* (non-Javadoc)
     * @see org.seasr.meandre.support.generic.io.webdav.WebdavClient#put(java.lang.String, java.io.InputStream)
     */
    public void put(String url, InputStream dataStream) throws WebdavClientException {
        HttpPut put = new HttpPut(url);
        put.setEntity(new InputStreamEntity(dataStream, -1));

        executeRequest(put);
    }

    /* (non-Javadoc)
     * @see org.seasr.meandre.support.generic.io.webdav.WebdavClient#put(java.lang.String, java.io.File, java.lang.String)
     */
    public void put(String url, File file, String contentType) throws WebdavClientException {
        HttpPut put = new HttpPut(url);
        put.setEntity(new FileEntity(file, contentType));

        executeRequest(put);
    }

    /* (non-Javadoc)
     * @see org.seasr.meandre.support.generic.io.webdav.WebdavClient#put(java.lang.String, java.lang.String, java.lang.String)
     */
    public void put(String url, String content, String charset) throws WebdavClientException {
        HttpPut put = new HttpPut(url);
        try {
            put.setEntity(new StringEntity(content, charset));
        }
        catch (UnsupportedEncodingException e) {
            throw new WebdavClientException(e);
        }

        executeRequest(put);
    }

    /* (non-Javadoc)
     * @see org.seasr.meandre.support.generic.io.webdav.WebdavClient#delete(java.lang.String)
     */
    public void delete(String url) throws WebdavClientException {
        executeRequest(new HttpDelete(url));
    }

    /* (non-Javadoc)
     * @see org.seasr.meandre.support.generic.io.webdav.WebdavClient#move(java.lang.String, java.lang.String)
     */
    public void move(String sourceUrl, String destinationUrl) throws WebdavClientException {
        executeRequest(new HttpMove(sourceUrl, destinationUrl));
    }

    /* (non-Javadoc)
     * @see org.seasr.meandre.support.generic.io.webdav.WebdavClient#copy(java.lang.String, java.lang.String)
     */
    public void copy(String sourceUrl, String destinationUrl) throws WebdavClientException {
        executeRequest(new HttpCopy(sourceUrl, destinationUrl));
    }

    /* (non-Javadoc)
     * @see org.seasr.meandre.support.generic.io.webdav.WebdavClient#mkdir(java.lang.String)
     */
    public boolean mkdir(String url) throws WebdavClientException {
        try {
            executeRequest(new HttpMkCol(url));
            return true;
        }
        catch (WebdavClientException e) {
            if (e.getCause() != null) throw e;
            return false;
        }
    }

    /* (non-Javadoc)
     * @see org.seasr.meandre.support.generic.io.webdav.WebdavClient#mkdirs(java.lang.String)
     */
    public boolean mkdirs(String url) throws WebdavClientException {
        // normalize the URI
        if (url.endsWith("/")) url = url.substring(0, url.length() - 1);

        return exists(url + "/") || (mkdirs(url.substring(0, url.lastIndexOf('/'))) && mkdir(url));
    }

    /* (non-Javadoc)
     * @see org.seasr.meandre.support.generic.io.webdav.WebdavClient#exists(java.lang.String)
     */
    public boolean exists(String url) throws WebdavClientException {
        try {
            executeRequest(new HttpHead(url));
            return true;
        }
        catch (WebdavClientException e) {
            if (e.getCause() != null) throw e;
            return false;
        }
    }

    private HttpResponse executeRequest(HttpRequestBase request) throws WebdavClientException {
        HttpResponse response;

        try {
            response = (host != null) ? this.client.execute(host, request) : this.client.execute(request);
        }
        catch (Exception e) {
            request.abort();
            throw new WebdavClientException(e);
        }

        StatusLine statusLine = response.getStatusLine();

        if (!WebdavUtil.isGoodResponse(statusLine.getStatusCode())) {
            request.abort();
            throw new WebdavClientException(request.getMethod() + ": " + request.getURI().toString(),  
                    request.getURI().toString(), statusLine.getStatusCode(), statusLine.getReasonPhrase());
        }

        return response;
    }
    
    private void listResourcesInternal(String uri, boolean recurse, FilenameFilter filter, List<DavResource> resources) throws WebdavClientException {
        for (DavResource res : listContents(uri)) {
            if (!res.isCollection()) {
                if (filter.accept(new File(res.getParentPath()), res.getNameDecoded())) 
                    resources.add(res);
            } else {
                if (filter.accept(new File(res.getPath()), null))
                    resources.add(res);
                if (recurse) listResourcesInternal(res.getPath(), recurse, filter, resources);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.seasr.meandre.support.generic.io.webdav.WebdavClient#close()
     */
    public void close() {
        client.getConnectionManager().shutdown();
    }
}
