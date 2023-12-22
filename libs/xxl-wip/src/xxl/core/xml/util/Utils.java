/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.xml.util;

import java.io.PrintStream;

/**
 * Some static methods which helps while working with XML. 
 */
public class Utils {
	/**
	 * No instance allowed - only static methods.
	 */
	private Utils() {
	}
	
	/**
	 * Writes characters (given as a byte array) to a DataOuptut
	 * changing special XML characters to XML & expressions.
	 * <br>
	 * Entity  	&lt;  	&gt;  	&apos; 	&quot;  &amp;  	&#93;<br>
	 * Java Value 	'<' 	'>' 	'\'' 	'\"' 	'&' 	']'<br>
	 * @param b Characters
	 * @param out DataOutput to send the XML encoding of the characters.
	 */
	public static void writeCharactersXMLConform(byte[]b, PrintStream out) {
		for (int i=0; i<b.length; i++) {
			switch (b[i]) {
			case '<':  out.print("&lt;"); break;
			case '>':  out.print("&gt;"); break;
			case '\'': out.print("&apos;"); break;
			case '\"': out.print("&quot;"); break;
			case '&':  out.print("&amp;"); break;
			case ']':  out.print("&#93;"); break;
			default:   out.print((char) b[i]);
			}
		}
	}
}
