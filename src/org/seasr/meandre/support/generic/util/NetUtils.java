/*
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
 */

package org.seasr.meandre.support.generic.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Provides network-related utilities
 * 
 * @author Boris Capitanu
 */

public abstract class NetUtils {
    
    /** 
     * Returns the best guess for the host name
     * 
     * @return The host name
     */
    public static String getLocalHostName() {
        try {
            return InetAddress.getLocalHost().getCanonicalHostName();
        } 
        catch (UnknownHostException e) {
            return "localhost";
        }
    }
    
    /** 
     * Returns the best guess for the host IP
     * 
     * @return The host IP
     */
    public static String getLocalHostIP() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } 
        catch (UnknownHostException e) {
            return "127.0.0.1";
        }
    }
    
    /** 
     * Returns the hex version of the IP for localhost
     * 
     * @return The hex IP value
     */
    public static String getLocalHostIPHexValue() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            
            String sRes = "";
            for (byte b : ip.getAddress()) {
                String sTmp = Integer.toHexString(b);
                sRes += (sTmp.length() < 2) ? "0" + sTmp : sTmp.substring(sTmp.length() - 2);
            }
            
            return sRes.toUpperCase();
        } 
        catch (UnknownHostException e) {
            return "UNKNOWN";
        }
    }
}
