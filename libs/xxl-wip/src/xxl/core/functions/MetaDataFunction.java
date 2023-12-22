/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.functions;

import java.util.List;

import xxl.core.util.metaData.MetaDataProvider;

/**
 * A marker interface for functions providing metadata.
 *
 * @param <P> the type of the function's parameters.
 * @param <R> the return type of the function.
 * @param <M> the type of the meta data provided by this function.
 */
public interface MetaDataFunction<P, R, M> extends Function<P, R>, MetaDataProvider<M> {
	
	@Override
	public abstract R invoke(List<? extends P> arguments);

	@Override
	public abstract R invoke();

	@Override
	public abstract R invoke(P argument);

	@Override
	public abstract R invoke(P argument0, P argument1);

	@Override
	public abstract M getMetaData();

}
