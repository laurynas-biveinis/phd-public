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

package xxl.core.comparators;

import java.io.Serializable;
import java.util.Comparator;

/**
 * The LexicographicalComparator creates a lexicographical ordering.
 * 
 * @param <T> the type of the objects to be compared.
 * @see java.util.Comparator
 */
public class LexicographicalComparator<T> implements Comparator<T>, Serializable {
	
	/**
	 * The comparators to be used for the different levels of the
	 * lexicographical ordering.
	 */
	protected Comparator<? super T>[] comparators;

	/**
	 * Creates a new LexicographicalComparator.
	 * 
	 * @param comparators the comparators to be used for the different levels
	 *        of the lexicographical ordering
	*/
	public LexicographicalComparator(Comparator<? super T>... comparators) {
		this.comparators = comparators;
	}

	/** 
	 * Compares its two arguments for order.
	 * 
	 * @param object1 the first object to be compared.
	 * @param object2 the second object to be compared.
	 * @return the result of the comparison.
	 */
	public int compare(T object1, T object2) {
		int result = 0;
		for (Comparator<? super T> comparator : comparators)
			if ((result = comparator.compare(object1, object2)) != 0)
				return result;
		return result;
	}
}