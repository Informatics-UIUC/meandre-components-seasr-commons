package org.seasr.meandre.support.generic.io.webdav.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.seasr.meandre.support.generic.io.webdav.DavResource;
import org.seasr.meandre.support.generic.io.webdav.WebdavClient;
import org.seasr.meandre.support.generic.io.webdav.WebdavClientFactory;
import org.seasr.meandre.support.generic.io.webdav.util.WebdavClientException;


/**
 * Test harness for WebdavClient
 *
 * @author Boris Capitanu
 */
public class WebdavClientTest {

    // the test server
    final String server = "";   // put the correct server here

    // for private (authenticated) access
    final String username = "user";  	 // put the correct user name here
    final String password = "password";  // put the correct password here


    final String testFolder = "/webdav/_WebdavClientTest/";

    // for public access
    // final String testFolder = "/public-dav/_WebdavClientTest/";
    // final String username = null;
    // final String password = null;

    private WebdavClient client;
    private File tempFile;

    @Before
    public void setUp() throws Exception {
        assertTrue ("Test server not specified!", server != null && !server.isEmpty());

        // create a test file
        tempFile = File.createTempFile("WebdavClientTest", ".txt");
        PrintWriter writer = new PrintWriter(tempFile);
        writer.write("This is a test file" + System.getProperty("line.separator"));
        writer.flush();

        client = WebdavClientFactory.begin(new HttpHost(server), new UsernamePasswordCredentials(username, password));

        if (client.exists(testFolder)) client.delete(testFolder);

        if (!client.mkdirs(testFolder)) throw new RuntimeException("Cannot create test folder");
    }

    @Test
    public void testDavResource() {
        try {
            client.put(testFolder + "testResource.txt", tempFile, "text/plain");
            DavResource res = client.getResourceInfo(testFolder + "testResource.txt");

            assertEquals("testResource.txt", res.getName());
            assertEquals(testFolder, res.getParentPath());
            assertFalse(res.isCollection());

            String parentUrl = "http://" + server + testFolder;
            assertEquals(parentUrl, res.getParentUrl().toString());

            assertEquals(parentUrl + "testResource.txt", res.getUrl().toString());

            res = client.getResourceInfo(testFolder);
            assertTrue(res.isCollection());
            assertEquals(testFolder, res.getPath());

            try {
                client.getResourceInfo(testFolder + "/nonExistentFolder/_nonExistentResource_");
                fail("This call should fail");
            }
            catch (WebdavClientException e) {
                if (e.getStatusCode() != HttpStatus.SC_NOT_FOUND) fail(e.getResponsePhrase());
            }

            client.put(testFolder + "testResource2.txt", "This is a test file", "utf8");
            res = client.getResourceInfo("http://" + server + testFolder + "testResource2.txt");
            assertEquals("testResource2.txt", res.getName());
            assertEquals(testFolder, res.getParentPath());
            assertFalse(res.isCollection());

            parentUrl = "http://" + server + testFolder;
            assertEquals(parentUrl, res.getParentUrl().toString());

            assertEquals(parentUrl + "testResource2.txt", res.getUrl().toString());
            assertEquals(19, res.getContentLength());
        }
        catch (IOException e) {
            fail("Communication error: " + e.getMessage());
        }
    }

    @Test
    public void testGetResourceAsStream() {
        try {
            client.put(testFolder + "testResource.txt", tempFile, "text/plain");

            InputStream stream = client.getResourceAsStream(testFolder + "testResource.txt");
            assertNotNull(stream);

            // check the content to make sure it's correct
            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            String content = br.readLine();
            assertEquals(content, "This is a test file");

            try {
                client.getResourceAsStream(testFolder + "nonExistentFile.txt");
                fail("This call should fail");
            }
            catch (WebdavClientException e) {
                if (e.getStatusCode() != HttpStatus.SC_NOT_FOUND) fail(e.getResponsePhrase());
            }
        }
        catch (IOException e) {
            fail("Communication error: " + e.getMessage());
        }
    }

    @Test
    public void testGetResourceAsString() {
        try {
            client.put(testFolder + "testResource.txt", tempFile, "text/plain");

            String content = client.getResourceAsString(testFolder + "testResource.txt");
            assertNotNull(content);

            // check the content to make sure it's correct
            assertEquals(content, "This is a test file" + System.getProperty("line.separator"));

            try {
                client.getResourceAsString(testFolder + "nonExistentFile.txt");
            }
            catch (WebdavClientException e) {
                if (e.getStatusCode() != HttpStatus.SC_NOT_FOUND) fail(e.getResponsePhrase());
            }
        }
        catch (IOException e) {
            fail("Communication error: " + e.getMessage());
        }
    }

    @Test
    public void testGetResourceAsByteArray() {
        try {
            client.put(testFolder + "testResource.txt", tempFile, "text/plain");

            byte[] data = client.getResourceAsByteArray(testFolder + "testResource.txt");
            assertNotNull(data);

            // check the content to make sure it's correct
            assertEquals(new String(data), "This is a test file" + System.getProperty("line.separator"));

            try {
                client.getResourceAsByteArray(testFolder + "nonExistentFile.txt");
            }
            catch (WebdavClientException e) {
                if (e.getStatusCode() != HttpStatus.SC_NOT_FOUND) fail(e.getResponsePhrase());
            }
        }
        catch (IOException e) {
            fail("Communication error: " + e.getMessage());
        }
    }

    @Test
    public void testPutInputStream() {
        try {
            client.put(testFolder + "testPutInputStream.txt", new FileInputStream(tempFile));
            assertTrue(client.exists(testFolder + "testPutInputStream.txt"));

            client.put("http://" + server + testFolder + "testPutInputStream2.txt", new FileInputStream(tempFile));
            assertTrue(client.exists(testFolder + "testPutInputStream2.txt"));

            try {
                client.put(testFolder + "nonExistentFolder/fileInputStream.txt", new FileInputStream(tempFile));
                fail("This call should fail");
            }
            catch (WebdavClientException e) {
            }
        }
        catch (IOException e) {
            fail("Communication error: " + e.getMessage());
        }
    }

    @Test
    public void testPutByteArray() {
        byte[] data = "This is a test file".getBytes();

        try {
            client.put(testFolder + "testPutByteArray.txt", data);
            assertTrue(client.exists(testFolder + "testPutByteArray.txt"));

            client.put("http://" + server + testFolder + "testPutByteArray2.txt", data);
            assertTrue(client.exists(testFolder + "testPutByteArray2.txt"));

            try {
                client.put(testFolder + "/nonExistentFolder/fileByteArray.txt", data);
                fail("This call should fail");
            }
            catch (WebdavClientException e) {

            }
        }
        catch (IOException e) {
            fail("Communication error: " + e.getMessage());
        }
    }

    @Test
    public void testPutFile() {
        try {
            client.put(testFolder + "testPutFile.txt", tempFile, "text/plain");
            assertTrue(client.exists(testFolder + "testPutFile.txt"));

            client.put("http://" + server + testFolder + "testPutFile2.txt", tempFile, "text/plain");
            assertTrue(client.exists(testFolder + "testPutFile2.txt"));

            try {
                client.put(testFolder + "/nonExistentFolder/file.txt", tempFile, "text/plain");
                fail("This call should fail");
            }
            catch (WebdavClientException e) {
            }
        }
        catch (IOException e) {
            fail("Communication error: " + e.getMessage());
        }
    }

    @Test
    public void testPutString() {
        try {
            client.put(testFolder + "testPutString.txt", "This is a test file", "utf8");
            assertTrue(client.exists(testFolder + "testPutString.txt"));

            client.put("http://" + server + testFolder + "testPutString2.txt", "This is a test file", "utf8");
            assertTrue(client.exists(testFolder + "testPutString2.txt"));
        }
        catch (IOException e) {
            fail("Communication error: " + e.getMessage());
        }
    }

    @Test
    public void testDelete() {
        try {
            // create and delete a test folder
            assertTrue("Cannot create test folder", client.mkdir(testFolder + "testDelete/"));
            client.delete(testFolder + "testDelete/");

            // create and delete a test file

            client.put(testFolder + "testDelete.txt", "Test", "utf8");
            client.delete(testFolder + "testDelete.txt");
        }
        catch (IOException e) {
            fail("Communication error: " + e.getMessage());
        }
    }

    @Test
    public void testMkdir() {
        try {
            assertTrue(client.mkdir(testFolder + "testMkdir/"));
            assertTrue(client.exists(testFolder + "testMkdir/"));
        }
        catch (IOException e) {
            fail("Communication error: " + e.getMessage());
        }
    }

    @Test
    public void testMkdirs() {
        try {
            assertTrue(client.mkdirs(testFolder + "testMkdirs/one/two/three/four/"));
            assertTrue(client.exists(testFolder + "testMkdirs/one/two/three/four/"));
        }
        catch (IOException e) {
            fail("Communication error: " + e.getMessage());
        }
    }

    @Test
    public void testExists() {
        try {
            assertFalse(client.exists(testFolder + "nonExistentFile.txt"));
            assertFalse(client.exists(testFolder + "nonExistentFolder/"));

            assertTrue(client.exists(testFolder));
            client.put(testFolder + "testFile.txt", "Test file", "utf8");
            assertTrue(client.exists(testFolder + "testFile.txt"));
        }
        catch (IOException e) {
            fail("Communication error: " + e.getMessage());
        }
    }

    @Test
    public void testListContents() {
        try {
            assertTrue("Cannot create test folder", client.mkdir(testFolder + "testListContents/"));
            client.put(testFolder + "testListContents/testFile.txt", "Test file", "utf8");
            client.put(testFolder + "testListContents/testFile2.txt", "Test file2", "utf8");

            List<DavResource> resources = client.listContents(testFolder + "testListContents/");
            assertNotNull(resources);
            assertEquals(resources.size(), 2);
            assertFalse(resources.get(0).getNameDecoded().equals(resources.get(1).getNameDecoded()));
            assertTrue(resources.get(0).getName().startsWith("testFile"));
            assertTrue(resources.get(1).getName().startsWith("testFile"));

            for (DavResource res : resources) {
                System.out.println(res.getUrl());
            }

            assertTrue("Cannot create test folder", client.mkdir(testFolder + "testListContents2/"));
            List<DavResource> result = client.listContents(testFolder + "testListContents2");
            assertNotNull(result);
            assertEquals(result.size(), 0);

            try {
                client.listContents(testFolder + "nonExistentFolder/");
                fail("This test should fail");
            }
            catch (WebdavClientException e) {
                if (e.getStatusCode() != HttpStatus.SC_NOT_FOUND) fail(e.getResponsePhrase());
            }
        }
        catch (IOException e) {
            fail("Communication error: " + e.getMessage());
        }

    }

    @Test
    public void testListFiles() {
        try {
            assertTrue("Cannot create test folder", client.mkdirs(testFolder + "testListFiles/one/two/"));
            assertTrue("Cannot create test folder", client.mkdirs(testFolder + "testListFiles/three/"));
            client.put(testFolder + "testListFiles/one/testFileOne.txt", "Test file", "utf8");
            client.put(testFolder + "testListFiles/one/testFileOne2.txt", "Test file", "utf8");
            client.put(testFolder + "testListFiles/one/two/testFileTwo.txt", "Test file", "utf8");
            client.put(testFolder + "testListFiles/testFile.txt", "Test file", "utf8");
            client.put(testFolder + "testListFiles/testFile.tmp", "Test file", "utf8");
            client.put(testFolder + "testListFiles/one/two/testFile.tmp", "Test file", "utf8");

            List<DavResource> filesOne = client.listFiles(testFolder + "testListFiles/one/", false);

            assertNotNull(filesOne);
            assertEquals(2, filesOne.size());

            filesOne = client.listContents(testFolder + "testListFiles/one/");
            assertNotNull(filesOne);
            assertEquals(3, filesOne.size()); // includes folders

            List<DavResource> filesAll = client.listFiles(testFolder + "testListFiles/", true);

            assertNotNull(filesAll);
            assertEquals(6, filesAll.size());

            filesAll = client.listContents(testFolder + "testListFiles/", true);
            assertNotNull(filesAll);
            assertEquals(9, filesAll.size());

            List<DavResource> filesFilter = client.listContents(testFolder + "testListFiles/", true, new FilenameFilter() {

                public boolean accept(File dir, String name) {
                    return name != null && name.endsWith(".tmp");
                }
            });

            assertNotNull(filesFilter);
            assertEquals(2, filesFilter.size());
            assertFalse(filesFilter.get(0).getPath().equals(filesFilter.get(1).getPath()));
            assertTrue(filesFilter.get(0).getName().equals(filesFilter.get(1).getName()));

            List<DavResource> filesEmpty = client.listContents(testFolder + "testListFiles/three/", true, new FilenameFilter() {

                public boolean accept(File dir, String name) {
                    return true;
                }
            });

            assertNotNull(filesEmpty);
            assertEquals(filesEmpty.size(), 0);

            try {
                client.listContents(testFolder + "testListFiles/four/", true, new FilenameFilter() {

                    public boolean accept(File dir, String name) {
                        return true;
                    }
                });

                fail("This call should fail");
            }
            catch (WebdavClientException e) {
                if (e.getStatusCode() != HttpStatus.SC_NOT_FOUND) throw e;
            }
        }
        catch (IOException e) {
            fail("Communication error: " + e.getMessage());
        }
    }

    @After
    public void tearDown() throws Exception {
        client.delete(testFolder);
        client.close();
        tempFile.delete();
    }
}
