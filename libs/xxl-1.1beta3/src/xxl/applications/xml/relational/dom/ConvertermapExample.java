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

package xxl.applications.xml.relational.dom;

import java.math.BigDecimal;
import java.util.Iterator;

import xxl.core.xml.relational.StringToSQLTypeConverterMap;

/**
 * This example shows the principles of type conversions in xxl.core.xml.relational.
 */
public class ConvertermapExample {
	/**
	 * The main method starts the application.
	 * @param args The command line options are ignored here.
	 */
	public static void main(String[] args) {
		System.out.println("This example shows the principles of type conversions in connectivity.xml");
		System.out.println();
		StringToSQLTypeConverterMap cm = new StringToSQLTypeConverterMap();
	
		String SQLType = "NUMERIC";
		String Wert = "1234567";
		
		/* convert this string into BigDecimal */	
		BigDecimal BD = (BigDecimal) cm.getConverter(SQLType).convert(Wert);
		
		System.out.println("Converting the 'NUMERIC'-String '1234567' to BigDecimal: "+BD);
		
		Iterator it = cm.getMap().keySet().iterator();
		
		System.out.println("\n\nsupported SQL Types:\n");			
		while (it.hasNext())
			System.out.println((String) it.next());			
	}
}	
