/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.xml.util;

/**
 * This class contains some static method which are helpful while
 * dealing with xPath expressions.
 */
public class XPathUtils {

	/** 
	 * No instances of this class.
	 */
	private XPathUtils() {
	}
	
	/**
	 * Return the first substring (seperated by / ) of the 
	 * given xPath expression. I.e. getFirst("/item[9]/links/...") would 
	 * return "item[9].
	 * @param xPath the xPath expression
	 */
	public static String head(String xPath) {
		int second = xPath.indexOf("/",1); //beginning at 1 ignores the first slash
		if (second==-1) return xPath.substring(1, xPath.length());
		return xPath.substring(1, second);
	}

	/**
	 * Return the everything except the first substring (seperated by / ) of the 
	 * given xPath expression.
	 * @param xPath the xPath expression
	 */
	public static String tail(String xPath) {
		int second = xPath.indexOf("/",1); //beginning at 1 ignores the first slash
		if (second==-1) return xPath.substring(1, xPath.length());
		return xPath.substring(second, xPath.length());
	}

	/**
	 * The given xPath must have the form name[x] where x is an int value.
	 * This mehod splits the given xPath in name and x.
	 * @return a string array with length 2 and the name at index 0 and the x at index 1
	 */
	public static String[] split(String xPath) {
		String[] ret = new String[2];
		int firstBracket = xPath.indexOf("[");
		ret[0] = xPath.substring(0, firstBracket);
		ret[1] = xPath.substring(firstBracket+1, xPath.length()-1);
		return ret;
	}

	/** 
	 * Returns the XPath String without the last tag.
	 * @param xPath The XPath String
	 * @return XPath String without the last tag.
	 */
	public static String ignoreLast(String xPath) {
		int last = xPath.lastIndexOf("/");
		if (last==-1) return xPath;
		return xPath.substring(0, last);
	}
}
