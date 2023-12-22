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

/**
 * This class represent an object with free variables which can bind with
 * values (arguments). This object can be a function, a predicate or a whole
 * subquery.
 * 
 * @param <T> the type of the values to be bound.
 */
public interface Binding<T> {
	
	/**
	 * Set the constant values to which a part of the free arguments 
	 * should be bound.
	 *
	 * @param constArguments the constant values to which a part of the
	 *        free arguments should be bound.
	 */
	public void setBinds(List<? extends T> constArguments);
	
	/**
	 * Set the constant values to which a part of the free arguments
	 * should be bound.
	 *
	 * @param constIndices the indices of the arguments which
	 *        should be bound to given arguments. The important is
	 *		  that it should always be sorted.
	 * @param constArguments the constant values to which a part of the
	 *        free arguments should be bound.
	 */
	public void setBinds(List<Integer> constIndices, List<? extends T> constArguments);
	
	/**
	 * Set a constant value to which a free argument should be bound.
	 *
	 * @param constIndex the index of the arguments which should be bound to
	 *        the given argument.
	 * @param constArgument the constant value to which a free argument
	 *        should be bound.
	 */
	public void setBind(int constIndex, T constArgument);
	
	/**
	 * Set free all bound arguments.
	 */
	public void restoreBinds();
	
}
