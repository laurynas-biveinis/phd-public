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
