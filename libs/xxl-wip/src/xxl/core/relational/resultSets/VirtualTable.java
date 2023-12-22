/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.relational.resultSets;

import java.sql.ResultSet;

import xxl.core.functions.Function;

/**
 * This class is a wrapper for result sets with the intention to receive an
 * instance of the VTI (Virtual Table Interface) in order to use the wrapped
 * result set in a FROM clause of an SQL-J statement.
 * 
 * <p><b>IMPORTANT:</b> the function SET_RESULTSET has to be defined before any
 * call to a constructor, i.e. before using NEW VirtualTable() in a
 * FROM-clause.</p>
 */
public class VirtualTable extends DecoratorResultSet {

	/**
	 * A function taht is used to initialize the wrapped result set. This
	 * function has to be defined before any call to a constructor.
	 */
	public static Function<?, ? extends ResultSet> SET_RESULTSET = null;

	/**
	 * Creates a Virtual Table when the function SET_RESULTSET has been set
	 * before calling this constructor. Otherwise a
	 * <code>NullPointerException</code> is thrown.
	 */
	public VirtualTable() {
		super(SET_RESULTSET.invoke());
	}
}
