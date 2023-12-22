/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2006 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307,
USA

	http://www.xxl-library.de

bugs, requests for enhancements: request@xxl-library.de

If you want to be informed on new versions of XXL you can
subscribe to our mailing-list. Send an email to

	xxl-request@lists.uni-marburg.de

without subject and the word "subscribe" in the message body.
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
	public int getColumnCount() {
		return columns.length;
	}
}
