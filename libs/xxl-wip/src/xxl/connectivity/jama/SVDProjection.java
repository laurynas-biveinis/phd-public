/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.connectivity.jama;

import java.util.Iterator;

import xxl.core.functions.AbstractFunction;
import Jama.Matrix;
import Jama.SingularValueDecomposition;

/**
 * This class provides a singular value decomposition in the sense
 * of dimension reduction for real-valued high-dimensional spaces.
 * This implementation also provides a back transformation
 * for objects of the reduced space to the original space.
 *
 * This class simply uses the implemented SVD decomposition of the Jama-package.<br>
 * <b></a> (<a href="http://math.nist.gov/javanumerics/jama/">JAMA: A Java Matrix Package</a>) </b>
 *
 * @see Jama.Matrix
 * @see Jama.SingularValueDecomposition
 */

public class SVDProjection extends AbstractFunction<double[], double[]> {

	/** The chosen dimension. The data is projected to a dim-dimensional space. */
	protected int dim;

	/** Indicates whether the SVD decomposition has already been computed.
	 * If initialized = true SVD has been accomplished. */
	protected boolean initialized;

	/** The starting matrix.It will be decomposed to A = V*S*U^T */
	protected Matrix A;

	/** Decomposition matrix A = V*S*U^T. */
	protected Matrix U;

	/** Decomposition matrix A = V*S*U^T. */
	protected Matrix V;

	/** Decomposition matrix A = V*S*U^T. */
	protected Matrix S;

	/** Constructs a new Object of type SVDProjection. The data must
	 * be given as Objects of type <tt>double []</tt>!
	 * 
	 * @param data n dimensional data given as <tt>double[]</tt>
	 * @param dim dimension of the subspace to project into 
	 */
	public SVDProjection(Iterator<double[]> data, int dim) {
		A = Jamas.toMatrix(data);
		this.dim = dim;
		initialized = false;
	}

	/** Initializes required matrices for the SVD decomposition.
	 * @throws IllegalArgumentException if the svd decomposition could not be performed
	 *
	 * @see Jama.Matrix
	 * @see Jama.SingularValueDecomposition
	 */
	private void initialize() throws IllegalArgumentException {
		A = A.transpose();
		SingularValueDecomposition svd = A.svd(); // now: A=V*S*U^T !!!
		U = svd.getU();
		V = svd.getV();
		S = svd.getS();
		int k = dim;
		try {
			U = Jamas.cropMatrix(U, k, U.getRowDimension());
			V = Jamas.cropMatrix(V, k, V.getRowDimension());
			S = Jamas.cropMatrix(S, k, k);
		}
		catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(
				"svd decomposition could not be performed due to a dimensional error.\n" + e);
		}
		initialized = true;
	}

	/**
	 * Returns a projection of the given <tt>double []</tt> of dimension <tt>n</tt> to a
	 * subspace of dimension <tt>k</tt>.
	 * 
	 * @param x object of type <tt>double []</tt> to project to a subspace
	 * @throws IllegalArgumentException if the given argument x is not of type <tt>double []</tt>
	 * or the svd decomposition could not be performed
	 * @return projected <tt>double []</tt> of dimension <tt>k</tt>
	 */
	@Override
	public double[] invoke(double[] x) throws IllegalArgumentException {
		if (!initialized)
			initialize();
		Matrix X = null;
		try {
			X = Jamas.toMatrix(x);
		}
		catch (Exception e) {
			throw new IllegalArgumentException("argument not of type double[]!");
		}
		return Jamas.toDoubleArray(S.inverse().times(V.transpose().times(X.transpose())));
	}

	/**
	 * Returns a back projection of the given <tt>double []</tt>
	 * of dimension <tt>k</tt> back to the space of dimension <tt>n</tt>.
	 * 
	 * @param d object of type <tt>double []</tt> to project back to the original space
	 * @throws IllegalArgumentException if the given argument v is not of type <tt>double []</tt>
	 * or the svd decomposition could not be performed
	 * @return 'back' projection from v to dimension <tt>n</tt>
	 */
	public Object inverse(Object d) throws IllegalArgumentException {
		if (!initialized)
			initialize();
		Matrix X = Jamas.toMatrix((double[]) d);
		return Jamas.toDoubleArray(V.times(S.times(X.transpose())));
	}
}
