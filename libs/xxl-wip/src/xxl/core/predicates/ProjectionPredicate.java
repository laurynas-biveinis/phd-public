/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.predicates;

import xxl.core.functions.Projection;

/**
 * This class provides a decorator predicate that applies a projection to the
 * arguments of the underlying predicate. Everytime arguments are passed to the
 * <code>invoke</code> method of this class, the arguments are first projected
 * using a given projection and afterwards passed to the <code>invoke</code>
 * method of the underlying predicate.
 *
 * @param <P> the type of the predicate's parameters.
 * @see Projection
 */
public class ProjectionPredicate<P> extends FeaturePredicate<P[], P[]> {

	/**
	 * Creates a new projection predicate that applies the specified projection
	 * to the arguments of it's <code>invoke</code> methods before the
	 * <code>invoke</code> method of the given predicate is called.
	 *
	 * @param predicate the predicate which input arguments should be mapped.
	 * @param projection the projection that is applied to the arguments of the
	 *        wrapped predicate.
	 */
	public ProjectionPredicate(Predicate<P[]> predicate, Projection<P> projection) {
		super(predicate, projection);
	}
}
