package org.seasr.meandre.support.generic.text.stemming;

public class StringUtilities {
	
	protected StringUtilities()
	{
		
	}
	
	public static boolean isAllLetters(String text) 
	{
		//
		// TODO: time if Pattern/Match is faster
		//
		char[] ch = text.toCharArray();
		for (char c : ch) {
			if (!Character.isLetter(c)) {
				return false;
			}
		}
		return true;
	}

}
