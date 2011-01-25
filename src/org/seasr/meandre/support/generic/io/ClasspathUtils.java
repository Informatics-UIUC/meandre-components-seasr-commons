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

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

public abstract class ClasspathUtils {

    /**
     * Finds a dependency
     *
     * @param jarName The name of the jar file (i.e. "maxent-models.jar")
     * @param clazz The class whose class loader to search first, or null to just search in the classpath
     * @return A URL to the dependency, or null if not found
     * @throws Exception Thrown if something went wrong
     */
    public static URL findJARInClasspath(String jarName, Class<?> clazz) throws Exception {
        URL depURL = null;

        if (clazz != null) {
            ClassLoader classLoader = clazz.getClassLoader();

            if (classLoader instanceof URLClassLoader) {
                URLClassLoader urlClassLoader = (URLClassLoader)classLoader;
                for (URL url : urlClassLoader.getURLs()) {
                    String cpJarName = url.toString().replaceAll("^jar:", "").replaceAll("!/$", "");
                    URL jarURL = new URL(cpJarName);
                    String fName = cpJarName.substring(cpJarName.lastIndexOf("/") + 1);
                    if (fName.equals(jarName)) {
                        depURL = jarURL;
                        break;
                    }
                }
            }
        }

        if (depURL == null){
            for (String s : System.getProperty("java.class.path").split(File.pathSeparator)) {
                File f = new File(s);
                if (f.isDirectory()) continue;

                String fName = s.substring(s.lastIndexOf(File.separator) + 1);
                if (fName.equals(jarName)) {
                    depURL = f.toURI().toURL();
                    break;
                }
            }
        }

        return depURL;
    }

}
