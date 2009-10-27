package org.seasr.meandre.support.generic.text.stemming;

import org.tartarus.snowball.SnowballStemmer;

/*
 * 
 * This is a simple wrapper around the libstemmer.java project
 * http://snowball.tartarus.org/dist/libstemmer_java.tgz
 * 
 * After downloading the source, compile and jar it up:
 * 
 *    1. javac org/tartarus/snowball/*.java org/tartarus/snowball/ext/*.java
 *    2. jar -cvf snowball.jar org
 *    3. copy over that jar file to the build path
 * 
 */


public class SnowballStemmingService 
{
	
	/* language has to exist in org/tartarus/snowball/ext
	 * 
	 */
	SnowballStemmer stemmer;
	
	public SnowballStemmingService() 
	{
		this("english");
	}
	
	public SnowballStemmingService(String language)
	{
		String path = language;
		try {
			path = "org.tartarus.snowball.ext." + language + "Stemmer";
			Class stemClass = Class.forName(path);
		    this.stemmer = (SnowballStemmer) stemClass.newInstance();
		}
		catch (Exception e) {
			throw new RuntimeException("unable to open path " + path);
		}
	}
	
	public String getStem(String fromThisWord) 
	{
		stemmer.setCurrent(fromThisWord.toLowerCase());
        stemmer.stem();
        return stemmer.getCurrent();
	}

}
