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

	http://www.mathematik.uni-marburg.de/DBS/xxl

bugs, requests for enhancements: xxl@mathematik.uni-marburg.de

If you want to be informed on new versions of XXL you can 
subscribe to our mailing-list. Send an email to 
	
	xxl-request@lists.uni-marburg.de

without subject and the word "subscribe" in the message body. 
*/
package xxl.core.pipes.operators;

public class CostModelMetaDataIdentifiers {

	private CostModelMetaDataIdentifiers() {		
	}
	
	public static final String D_ESTIMATION = "D_ESTIMATION";
	public static final String L_ESTIMATION = "L_ESTIMATION";
	public static final String G_ESTIMATION = "G_ESTIMATION";
	
	public static final String COST_MEASUREMENT = "COST_MEASUREMENT";
	public static final String COST_ESTIMATION = "COST_ESTIMATION";
	
	public static String HASH_FUNCTION_CALLS = "HASH_FUNCTION_CALLS";

	
	public static final String MEM_USAGE_ESTIMATION = "MEM_USAGE_ESTIMATION";
}
