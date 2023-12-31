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

package org.seasr.meandre.support.generic.logging.formatter;

import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * A generic log formatter that includes useful information
 *
 * @author Boris Capitanu
 */
public class GenericLogFormatter extends Formatter {

    @Override
    public String format(LogRecord record) {
        String msg = record.getMessage();
        if (msg == null || msg.length() == 0)
            msg = null;

        String extra = "";
        Throwable thrown = record.getThrown();
        if (thrown != null) {
            if (msg == null)
                msg = thrown.toString();
            else
                msg += "  (" + thrown.toString() + ")";

            extra = ExceptionFormatter.formatException(thrown) + System.getProperty("line.separator");
        }

        String srcClassName = record.getSourceClassName();
        String srcMethodName = record.getSourceMethodName();

        srcClassName = srcClassName.substring(srcClassName.lastIndexOf(".") + 1);

        return String.format("%6$tm/%6$td/%6$ty %6$tH:%6$tM:%6$tS [%s]: %s\t[%s.%s]%n%s",
                record.getLevel(), msg, srcClassName, srcMethodName, extra, new Date(record.getMillis()));
    }

}
