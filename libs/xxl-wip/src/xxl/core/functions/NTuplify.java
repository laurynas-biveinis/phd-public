/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.functions;

/**
 * A binary function that creates a tuple represented as an Object[]. The
 * arguments can be a simple Object or an Object[]. The results is always an
 * Object[]. This class is typically used for creating the resulting tuples in
 * join-trees. 
 *
 */
public class NTuplify extends AbstractFunction<Object, Object[]> {

	/**
	 * This instance can be used for getting a default instance of NTuplify. It
	 * is similar to the <i>Singleton Design Pattern</i> (for further details
	 * see Creational Patterns, Prototype in <i>Design Patterns: Elements of
	 * Reusable Object-Oriented Software</i> by Erich Gamma, Richard Helm,
	 * Ralph Johnson, and John Vlissides) except that there are no mechanisms
	 * to avoid the creation of other instances of NTuplify.
	 */
	public static final NTuplify DEFAULT_INSTANCE = new NTuplify();

	/**
	 * A binary method that creates a tuple represented as an Object[]. The
	 * arguments can be a simple Object or an Object[]. The results is always
	 * an Object[].
	 *
	 * @param object1 the first Object/Object[].
	 * @param object2 the second Object/Object[].
	 * @return the tuple (Object[]) containing the elements of object1 and
	 *         object2. 
	 */
	@Override
	public Object[] invoke(Object object1, Object object2) {
		Object[] oa1 = object1 instanceof Object[] ? (Object[])object1 : new Object[] {object1};
		Object[] oa2 = object2 instanceof Object[] ? (Object[])object2 : new Object[] {object2};
		Object[] result = new Object[oa1.length + oa2.length];
		System.arraycopy(oa1, 0, result, 0, oa1.length);
		System.arraycopy(oa2, 0, result, oa1.length, oa2.length);
		return result;
	}

}
