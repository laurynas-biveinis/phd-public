/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.relational.tuples;

/**
 * Makes a projection of a tuple to certain columns.
 */
public class ProjectedTuple extends WrappedTuple {
	
	/**
	 * The columns of the tuple that will be projected.
	 */
	protected int[] columns;
	
	/**
	 * Creates a new projected tuple that contains the columns identified by
	 * the specified indices.
	 * 
	 * @param tuple the tuple to be projected.
	 * @param columns the indices of the columns which will be available from
	 *        the projected tuple.
	 */
	public ProjectedTuple(Tuple tuple, int... columns) {
		super(tuple);
		this.columns = columns;
	}

	/**
	 * Returns the column number of the original tuple that has been mapped to
	 * the column number value that is passed to the call.
	 *
	 * @param columnIndex the column number of the mapped tuple.
	 * @return the column number of the original tuple.
	 */
	@Override
	protected int originalColumnIndex(int columnIndex) {
		if (columnIndex < 1 || columnIndex > columns.length)
			throw new IndexOutOfBoundsException("the specified column " + columnIndex + " cannot be identified");
		return columns[columnIndex-1];
	}
	
	/**
	 * Returns the number of columns in this tuple.
	 * 
	 * @return the number of columns.
	 */
	@Override
	public int getColumnCount() {
		return columns.length;
	}
}
