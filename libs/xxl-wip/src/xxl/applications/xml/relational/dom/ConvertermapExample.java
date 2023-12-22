/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
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
