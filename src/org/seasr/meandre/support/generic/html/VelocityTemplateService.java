/**
*
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
*
*/

package org.seasr.meandre.support.generic.html;

import java.io.StringWriter;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

/*
 *
 * template loading notes:
 *
 *  templates are searched in
 *     1 local file system on the server: published_resources/templates (under the server install)
 *     2 local file system on the server: ./templates  where . is user.path
 *     3 on the classpath
 *     4 in any jars on the classpath
 *
 *     NOTES:  if we want don't want to use the Singleton, we can use VelocityEngine
 *     see http://velocity.apache.org/engine/releases/velocity-1.5/developer-guide.html#to_singleton_or_not_to_singleton...
 *
 */


public class VelocityTemplateService {


	static private VelocityTemplateService instance = null;

	protected VelocityTemplateService()
	   throws Exception
	{

		Properties p = new Properties();
		p.setProperty("resource.loader", "url,file,class" );
		p.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader" );
		p.setProperty("url.resource.loader.class", "org.apache.velocity.runtime.resource.loader.URLResourceLoader" );
	    p.setProperty("file.resource.loader.path", "published_resources/templates, WEB-INF/templates, ./templates");

	    // p.setProperty("url.resource.loader.root", "http://3gne.com/mikeh/");
	    // you need an empty string for the root property to work
	    p.setProperty("url.resource.loader.root", "");
	    Velocity.init( p );

	    //Velocity.init("WEB-INF/velocity.properties");
	}


	public static synchronized VelocityTemplateService getInstance()
	{
		if (instance == null) {

			try {
				instance = new VelocityTemplateService();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return instance;
	}

	public VelocityContext getNewContext()
	{
		return new VelocityContext();
	}

	public String generateOutput(VelocityContext context, String templateName)
	   throws Exception
	{
		Template template = null;
		try {
			template = Velocity.getTemplate(templateName);
		}
		catch (ResourceNotFoundException rnf) {
			throw new RuntimeException("Unable to find the template " + templateName);
		}
		catch (ParseErrorException pee) {
			throw new RuntimeException("Unable to parse the template " + templateName);
		}

        StringWriter sw = new StringWriter();
    	template.merge(context,sw);
    	return sw.toString();
	}
}


/*
formInputName = ccp.getProperty(DATA_PROPERTY_FORM);

Reader reader =
    new InputStreamReader(getClass().getClassLoader().
                               getResourceAsStream("history.vm"));
  VelocityContext context = new VelocityContext();
  context.put("location", location );
  context.put("weathers", weathers );
  StringWriter writer = new StringWriter();
  Velocity.evaluate(context, writer, "", reader);

  */
