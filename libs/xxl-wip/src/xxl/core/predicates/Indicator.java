/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.predicates;

import java.util.Collection;

/**
 * Implements an indicator for a given collection. An indicator is an unary
 * predicate that returns <code>true</code> exactly if the argument is
 * contained in the collection associated with the predicate.
 * 
 * @param <P> the type of the predicate's parameters.
 */
public class Indicator<P> extends AbstractPredicate<P> {

	/**
	 * The collection for which the indicator should be created.
	 */
	protected Collection<? super P> collection;
	
	/**
	 * Creates a new Indicator predicate.
	 * 
	 * @param collection the collection for which the indicator should be
	 *        created.
	 */
	public Indicator(Collection<? super P> collection){
		this.collection = collection;
	}

	/** Returns true iff the collection contains the object.
	 * 
	 * @param o object to be checked.
	 * @return returns true iff the collection contains the object.
	 */
	@Override
	public boolean invoke(P o) {
		return collection.contains(o);
	}
}
