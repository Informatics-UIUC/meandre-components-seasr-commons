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

        Throwable thrown = record.getThrown();
        if (thrown != null) {
            if (msg == null)
                msg = thrown.toString();
            else
                msg += "  (" + thrown.toString() + ")";
        }

        String srcClassName = record.getSourceClassName();
        String srcMethodName = record.getSourceMethodName();

        srcClassName = srcClassName.substring(srcClassName.lastIndexOf(".") + 1);

        return String.format("%5$tm/%5$td/%5$ty %5$tH:%5$tM:%5$tS [%s]: %s\t[%s.%s]%n",
                record.getLevel(), msg, srcClassName, srcMethodName, new Date(record.getMillis()));
    }

}
