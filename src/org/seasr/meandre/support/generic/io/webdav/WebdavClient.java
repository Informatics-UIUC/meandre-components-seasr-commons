package org.seasr.meandre.support.generic.io.webdav;

import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.List;

import org.seasr.meandre.support.generic.io.webdav.util.WebdavClientException;

public interface WebdavClient {

    public abstract DavResource getResourceInfo(String url) throws WebdavClientException;

    public abstract List<DavResource> listContents(String url) throws WebdavClientException;

    public abstract List<DavResource> listContents(String url, boolean recurse) throws WebdavClientException;

    public abstract List<DavResource> listContents(String url, boolean recurse, FilenameFilter filter) throws WebdavClientException;

    public abstract List<DavResource> listFiles(String url) throws WebdavClientException;

    public abstract List<DavResource> listFiles(String url, boolean recurse) throws WebdavClientException;

    public abstract InputStream getResourceAsStream(String url) throws WebdavClientException;

    public abstract String getResourceAsString(String url) throws WebdavClientException;

    public abstract byte[] getResourceAsByteArray(String url) throws WebdavClientException;

    public abstract void put(String url, byte[] data) throws WebdavClientException;

    public abstract void put(String url, InputStream dataStream) throws WebdavClientException;

    public abstract void put(String url, File file, String contentType) throws WebdavClientException;

    public abstract void put(String url, String content, String charset) throws WebdavClientException;

    public abstract void delete(String url) throws WebdavClientException;

    public abstract void move(String sourceUrl, String destinationUrl) throws WebdavClientException;

    public abstract void copy(String sourceUrl, String destinationUrl) throws WebdavClientException;

    public abstract boolean mkdir(String url) throws WebdavClientException;

    public abstract boolean mkdirs(String url) throws WebdavClientException;

    public abstract boolean exists(String url) throws WebdavClientException;

    public abstract void close();

}