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
 * An abstract implementation of the interface MetaDataPredicate that extends
 * the class AbstractPredicate.
 *
 * @param <P> the type of the predicate's parameters.
 * @param <M> the type of the meta data provided by this predicate.
 */
public abstract class AbstractMetaDataPredicate<P, M> extends AbstractPredicate<P> implements MetaDataPredicate<P, M> {
	
	@Override
	public abstract M getMetaData();
	
}
