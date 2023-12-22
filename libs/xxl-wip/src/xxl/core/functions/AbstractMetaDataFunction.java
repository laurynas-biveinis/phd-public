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
 * An abstract implementation of the interface MetaDataFunction that extends
 * the class AbstractFunction.
 *
 * @param <P> the type of the function's parameters.
 * @param <R> the return type of the function.
 * @param <M> the type of the meta data provided by this function.
 */
public abstract class AbstractMetaDataFunction<P, R, M> extends AbstractFunction<P, R> implements MetaDataFunction<P, R, M> {
	
	@Override
	public abstract M getMetaData();
	
}
