/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.predicates;

/**
 * This abstract class provides a wrapper for a given predicate that provides
 * additional meta data. The meta data of this predicate can be accessed by
 * calling the <code>getMetaData</code> method. Concrete implementations of
 * this predicate must implement the <code>getMetaData</code> for meta data
 * access.
 *
 * @param <P> the type of the predicate's parameters.
 * @param <M> the type of the meta data provided by this predicate.
 * @see DecoratorPredicate
 * @see MetaDataPredicate
 * @see Predicate
 */
public abstract class PredicateMetaDataPredicate<P, M> extends DecoratorPredicate<P> implements MetaDataPredicate<P, M> {

	/**
	 * Creates a new meta data predicate that adds meta data to the specified
	 * predicate.
	 *
	 * @param predicate the predicate that should provide meta data.
	 */
	public PredicateMetaDataPredicate(Predicate<? super P> predicate) {
		super(predicate);
	}

	@Override
	public abstract M getMetaData();
	
}
