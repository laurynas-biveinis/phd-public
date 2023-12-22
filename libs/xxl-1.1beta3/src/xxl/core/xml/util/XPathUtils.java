/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2006 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307,
USA

	http://www.xxl-library.de

bugs, requests for enhancements: request@xxl-library.de

If you want to be informed on new versions of XXL you can 
subscribe to our mailing-list. Send an email to 
	
	xxl-request@lists.uni-marburg.de

without subject and the word "subscribe" in the message body. 
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

	/**
	 * Simple sample application of the static mehods.
	 */
	public static void main(String[] args) {
		
		System.out.println("head: "+head("/item[9]/link[42]"));
		System.out.println("tail: "+tail("/item[9]/link[42]"));
		String[] sa = split("item[9]");
		System.out.println(sa[0]);
		System.out.println(sa[1]);
		System.out.println(tail("/item[1]"));
	}
}
