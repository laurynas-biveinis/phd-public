/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.connectivity.jama;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import Jama.Matrix;

/**
 * This class provides some useful static methods for dealing with
 * matrices provided in the 
 * </a> (<a href="http://math.nist.gov/javanumerics/jama/">JAMA Package</a>.
 */

public class Jamas {

	/** Don't let anyone instanziate this class or inherit from this class. */
	private Jamas() {}

	/* ------------------------------------------------------------------- */
	/* -- toMatrix methods ----------------------------------------------- */
	/* -- data must be given as double[] !!! ----------------------------- */
	/* ------------------------------------------------------------------- */

	/** Returns data given as <tt>double[]</tt> in a {@link java.util.List List}
	 * as an Object of type Matrix from the Jama package.
	 * 
	 * @param data given data as <tt>double[]</tt> that is stored in a List
	 * @throws IllegalArgumentException if the matrix cannot be build
	 * @return Matrix representation of the data 
	 */
	public static Matrix toMatrix(List<double[]> data) throws IllegalArgumentException {
		int rows = 0;
		rows = data.get(0).length;
		int columns = data.size();
		if (columns == 0)
			throw new IllegalArgumentException("given data could not be processed!\nList has size() == 0!");
		Matrix R = new Matrix(rows, columns);
		double[] d = null;
		for (int i = 0; i < data.size(); i++) {
			d = data.get(i);
			for (int j = 0; j < d.length; j++)
				R.set(j, i, d[j]);
		}
		return R;
	}

	/** Returns data given as <tt>double[]</tt>
	 * as an Object of type Matrix from the Jama package.
	 * 
	 * @param d given <tt>double[]</tt> seen as one row in a Matrix
	 * @return Matrix representation of the data
	 * @throws IllegalArgumentException if the matrix cannot be build
	 */
	public static Matrix toMatrix(double[] d) throws IllegalArgumentException {
		Matrix T = null;
		try {
			T = new Matrix(d, 1);
		}
		catch (Exception e) {
			throw new IllegalArgumentException("given double[] could not be processed!\n" + e.getMessage());
		}
		return T;
	}

	/** Returns data given as <tt>double[]</tt> in an iterator
	 * as an Object of type Matrix from the Jama package.
	 * 
	 * @param iterator
	 * @return Matrix representation of the data
	 * @throws IllegalArgumentException
	 */
	public static Matrix toMatrix(Iterator<double[]> iterator) {
		List<double[]> tmp = new ArrayList<double[]>();
		while (iterator.hasNext()) {
			tmp.add(iterator.next());
		}
		return toMatrix(tmp);
	}

	/** Returns data given as <tt>double[]</tt> in an iterator
	 * as an Object of type Matrix from the Jama package.
	 * 
	 * @param iterator
	 * @param rows number of rows
	 * @param columns number of columns
	 * @return Matrix representation of the data
	 * @throws IllegalArgumentException
	 */
	public static Matrix toMatrix(Iterator<double[]> iterator, int rows, int columns) {
		Matrix R = new Matrix(rows, columns);
		double[] d = null;
		int i = 0;
		while (iterator.hasNext()) {
			d = iterator.next();
			for (int j = 0; j < d.length; j++)
				R.set(j, i, d[j]);
			i++;
		}
		return R;
	}

	/* ------------------------------------------------------------------- */
	/* -- toAnything methods  -------------------------------------------- */
	/* -- data must be given as double[] !!! ----------------------------- */
	/* ------------------------------------------------------------------- */

	/** Converts a m x 1 or 1 x m Matrix into <tt>double[]</tt>. 
	 * 
	 * @param A {@link Matrix Jama.Matrix Matrix} to convert
	 * @throws IllegalArgumentException if dimensions are invalid
	 * @return <tt>double[]</tt> containing the matrix values
	 */
	public static double[] toDoubleArray(Matrix A) throws IllegalArgumentException {
		if ((A.getRowDimension() > 1) && (A.getColumnDimension() > 1)) {
			throw new IllegalArgumentException("dimensions are not valid ...");
		}
		if (A.getRowDimension() == 1) {
			int dim = A.getColumnDimension();
			double[] t = new double[dim];
			for (int i = 0; i < dim; i++)
				t[i] = A.get(0, i);
			return t;
		}
		if (A.getColumnDimension() == 1) {
			int dim = A.getRowDimension();
			double[] t = new double[dim];
			for (int i = 0; i < dim; i++)
				t[i] = A.get(i, 0);
			return t;
		}
		return null;
	}

	/** Drops the columns k+1,...,n and the rows j+1,...,m if T is a m x n-matrix.
	 * 
	 * @param T Matrix to drop
	 * @param k columns to drop
	 * @param j rows to drop
	 * @throws IllegalArgumentException if k > n or j > m
	 * @return new j x k-matrix
	 */
	public static Matrix cropMatrix(Matrix T, int k, int j) throws IllegalArgumentException {
		if (k > T.getColumnDimension())
			throw new IllegalArgumentException("k not valid ...");
		if (j > T.getRowDimension())
			throw new IllegalArgumentException("j not valid ...");
		Matrix r = new Matrix(j, k);
		r.setMatrix(0, j - 1, 0, k - 1, T);
		return r;
	}

	/** Swaps two columns of a matrix.
	 *  
	 * @param A {@link Matrix Jama.Matrix}
	 * @param i index of the first column
	 * @param j index of the second column
	 */
	protected static void swapColumns(Matrix A, int i, int j) {
		if (i != j) {
			double tmp = 0;
			for (int s = 0; s < A.getRowDimension(); s++) {
				tmp = A.get(s, i);
				A.set(s, i, A.get(s, j));
				A.set(s, j, tmp);
			}
		}
	}
}
