/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

/**
 * 
 */
package xxl.core.predicates;

import java.util.List;

import xxl.core.util.metaData.MetaDataProvider;

/**
 * A marker interface for predicates providing metadata.
 *
 * @param <P> the type of the predicate's parameters.
 * @param <M> the type of the meta data provided by this predicate.
 */
public interface MetaDataPredicate<P, M> extends Predicate<P>, MetaDataProvider<M> {
	
	@Override
	public abstract boolean invoke(List<? extends P> arguments);

	@Override
	public abstract boolean invoke();

	@Override
	public abstract boolean invoke(P argument);

	@Override
	public abstract boolean invoke(P argument0, P argument1);

	@Override
	public abstract M getMetaData();

}
