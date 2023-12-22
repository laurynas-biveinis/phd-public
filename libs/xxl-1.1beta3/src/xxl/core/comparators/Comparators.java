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

import java.util.Comparator;

/** 
 * This class provides some useful static methods for dealing with comparators.
 */
public class Comparators {

	/**
	 * The default constructor has private access in order to ensure
	 * non-instantiability.
	 */
	private Comparators() {}

	/**
	 * Returns a comparator that is able to compare objects based on the given
	 * comparator. Internally the returned comparator casted the objects to be
	 * compared to the type expected by the given comparator. Therefore the
	 * returned comparator will cause a <code>ClasscastException</code> when
	 * its methods are called with object that do not have the expected type.
	 * 
	 * @param <T> the type of the objects to be compared.
	 * @param comparator the comparator that is used to compare the objects
	 *        internally.
	 * @return a comparator that is able to compare objects based on the given
	 *         comparator.
	 */
	public static <T> Comparator<Object> getObjectComparator(final Comparator<T> comparator) {
		return new Comparator<Object>() {
			public int compare(Object o1, Object o2) {
				return comparator.compare((T)o1, (T)o2);
			}
			
		};
	}
	
	/** 
	 * Returns a {@link java.util.Comparator comparator} able to handle
	 * <code>null</code> values. A flag controls the position of null values
	 * concerning the induced ordering of the given comparator.
	 * If the flag is true the null values will be positioned before all other
	 * values and vice versa.
	 * 
	 * @param <T> the type of the object to be compared.
	 * @param comparator internally used
	 *        {@link java.util.Comparator comparator} for objects that are not
	 *        <code>null</code>.
	 * @param flag determines the position of null values.
	 * @return a {@link java.util.Comparator comparator} able to handle null
	 *         values.
	 */
	public static <T> Comparator<T> newNullSensitiveComparator(final Comparator<T> comparator, boolean flag) {
		return flag ?
			new Comparator<T>(){
				public int compare(T o1, T o2) {
					return o1 == null && o2 == null ?
						0 :
						o1 == null ?
							-1 :
							o2 == null ?
								1 :
								comparator.compare(o1, o2);
				}
			}
		:
			new Comparator<T>(){
				public int compare(T o1, T o2) {
					return o1 == null && o2 == null ?
						0 :
						o1 == null ?
							1 :
							o2 == null ?
								-1 :
								comparator.compare(o1, o2);
				}
			}
		;
	}

	/** 
	 * Returns a {@link java.util.Comparator comparator} able to handle
	 * <code>null</code> values. Null values will be positioned before all
	 * other values.
	 * 
	 * @param <T> the type of the object to be compared.
	 * @param c internally used {@link java.util.Comparator comparator} for
	 *        objects that are not <code>null</code>
	 * @return a {@link java.util.Comparator comparator} able to handle null
	 *         values.
	 */
	public static <T> Comparator<T> newNullSensitiveComparator(Comparator<T> c){
		return newNullSensitiveComparator(c, true);
	}

	/**
	 * The main method contains some examples to demonstrate the usage
	 * and the functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 * 		  submit parameters when the main method is called.
	 */
	public static void main(String[] args) {

		/*********************************************************************/
		/*                            Example 1                             */
		/*********************************************************************/
		// example to newNullSensitiveComparator( Comparator, boolean)
		Comparator<Integer> ci = newNullSensitiveComparator(ComparableComparator.INTEGER_COMPARATOR, false);
		
		Integer[] ia1 = new Integer[] {1, 2, 4, null};
		Integer[] ia2 = new Integer[] {3, 4, 5, null};
		
		for (Integer i1 : ia1)
			for (Integer i2 : ia2)
				System.out.println("compare(" + i1 + ", " + i2 + ")=" + ci.compare(i1, i2));
		
		Comparator<String> cs = newNullSensitiveComparator(ComparableComparator.STRING_COMPARATOR, false);
		
		String[] sa1 = new String[] {"a1", "a2", "A3", null};
		String[] sa2 = new String[] {"a3", "A2", "a1", null};
	
		for (String s1 : sa1)
			for (String s2 : sa2)
				System.out.println("compare(" + s1 + ", " + s2 + ")=" + cs.compare(s1, s2));
	}
}
