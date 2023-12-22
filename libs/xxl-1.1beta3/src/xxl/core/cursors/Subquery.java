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

package xxl.core.cursors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import xxl.core.functions.Binding;

/**
 * This class is a prototypical implementation for subqueries which may be used
 * in {@link xxl.core.predicates.ExistPredicate exist},
 * {@link xxl.core.predicates.AllPredicate all},
 * {@link xxl.core.predicates.AnyPredicate any} predicates, etc. It allows to
 * treat a part of the parameters of a subquery as free variables and delivers
 * the result when they are bound.
 * 
 * <p>The cursor of the subquery has to support the reset-method. After one
 * reset the result has to be recomputed and must not be buffered.</p>
 *
 * @param <E> the type of the elements returned by this iteration.
 * @see xxl.core.predicates.AllPredicate
 * @see xxl.core.predicates.AnyPredicate
 * @see xxl.core.predicates.ExistPredicate
 */
public class Subquery<E> extends SecureDecoratorCursor<E> implements Binding<E> {

	/**
	 * The subquery, whose result will be delivered when its free variables are
	 * bound.
	 */
	protected Cursor<E> outCursor;

	/**
	 * This two-dimensional array saves information of the free variables.
	 * Two-dimensional index: (index of the bindings, index of the parameter)
	 */
	protected int[][] mConstIndices;

	/**
	 * The bindings used in this subquery.
	 */
	protected List<? extends Binding<E>> bindings;

	/**
	 * The arguments for prebinding.
	 */
	protected List<E> constArguments;

	/**
	 * The indices for prebinding.
	 */
	protected List<Integer> constIndices;

	/**
	 * Creates a new instance of a subquery. It allow to treat a part of the
	 * parameters of a subquery as free variables and delivers the result when
	 * they are bound.
	 *
	 * @param outCursor the subquery, whose result will be delivered when its
	 *        free variables are bound.
	 * @param bindings the bindings used in this subquery.
	 * @param mConstIndices the two-dimensional array saving information of the
	 *        free variables.<br />
	 *		  <i>Example:</i><br />
	 *        <code>mConstIndices[i][j]=k</code>: the <code>j</code>-th free
	 *        variable is the <code>k</code>-th parameter of the
	 *        <code>i</code>-th binding.<br />
	 *        <code>k=-1</code>: the free variable is not used in the binding.
	 */
	public Subquery(Cursor<E> outCursor, List<? extends Binding<E>> bindings, int[][] mConstIndices) {
		super(outCursor);
		this.outCursor = outCursor;
		this.bindings = bindings;
		this.mConstIndices = mConstIndices;
		int i = 0;
		if (bindings != null) {
			while ((i < mConstIndices.length) && (i < bindings.size())) {
				for (int j = 0; j < mConstIndices[i].length; j++)
					if (mConstIndices[i][j] != -1)
						bindings.get(i).setBind(mConstIndices[i][j], null);
				i++;
			}
		}
		constArguments = new ArrayList<E>();
		constIndices = new ArrayList<Integer>();
	}

	/**
	 * Set the values of the free variables of the subquery.
	 *
	 * @param arguments the objects to which the free variables of the wrapped
	 *        subquery should be bound.
	 */
	public void bind(List<? extends E> arguments) {
		if (arguments == null)
			arguments = new ArrayList<E>(0);
		int totalArgumentsLength = constArguments.size() + arguments.size();
		List<E> newArguments = new ArrayList<E>(totalArgumentsLength);
		for (int pos = 0, indConst = 0, ind = 0; pos < totalArgumentsLength; pos++)
			if ((indConst < constArguments.size()) && (pos == constIndices.get(indConst))) {
				newArguments.add(constArguments.get(indConst));
				indConst++;
			}
			else
				if (ind < arguments.size()) {
					newArguments.add(arguments.get(ind));
					ind++;
				}
				else
					newArguments.add(null);
		

		if (bindings == null)
			return;
		for (int i = 0; i < mConstIndices.length && i < bindings.size(); i++)
			for (int j = 0; j < mConstIndices[i].length; j++)
				if (mConstIndices[i][j] != -1 && j < newArguments.size())
					bindings.get(i).setBind(mConstIndices[i][j], newArguments.get(j));
	}

	/**
	 * Set the values of the free variables of the subquery.
	 *
	 * @param arguments the objects to which the free variables of the wrapped
	 *        subquery should be bound.
	 */
	public void setBinds(List<? extends E> arguments) {
		if (constArguments.size() == arguments.size())
			Collections.copy(constArguments, arguments);
	}

	/**
	 * Set the values of given parameters of the subquery.
	 *
	 * @param indices the indices of the free variables which should be bound
	 *        to the given value.
	 * @param arguments the objects to which the given predicate-parameter of
	 *        the wrapped subquery should be bound.
	 */
	public void setBinds(List<Integer> indices, List<? extends E> arguments) {
		if (indices == null)
			return;
		else {
			int len = indices.size();
			for (int i = 0; i < len; i++) {
				if (indices.get(i) != -1)
					setBind(indices.get(i), arguments.get(i));
			}
		}
	}

	/**
	 * Set the value of a given parameter of the subquery.
	 *
	 * @param index the index of the free variable which should be bound to the
	 *        given value.
	 * @param argument the object to which the given predicate-parameter of the
	 *        wrapped subquery should be bound.
	 */
	public void setBind(int index, E argument) {
		if (index == -1)
			return;
		int len;
		if (constIndices != null) {
			len = constIndices.size();
			for (int i = 0; i < len; i++)
				if (index == constIndices.get(i)) {
					constArguments.set(i, argument);
					return;
				}
			List<Integer> tempConstIndices = new ArrayList<Integer>(len+1);
			List<E> tempConstArguments = new ArrayList<E>(len+1);
			int pos = 0;
			while (pos < len && constIndices.get(pos) < index) {
				tempConstIndices.add(constIndices.get(pos));
				tempConstArguments.add(constArguments.get(pos));
				pos++;
			}
			tempConstIndices.add(index);
			tempConstArguments.add(argument);
			pos++;
			while (pos <= len) {
				tempConstIndices.add(constIndices.get(pos-1));
				tempConstArguments.add(constArguments.get(pos-1));
				pos++;
			}
			constIndices = tempConstIndices;
			constArguments = tempConstArguments;
		}
		else {
			constIndices = Arrays.asList(index);
			constArguments = Arrays.asList(argument);
		}
	}

	/**
	 * Remove all bindings from the free variables of the subquery, i.e.,
	 * restore the initial state of the subquery without any bindings.
	 */
	public void restoreBinds() {
		constArguments.clear();
		constIndices.clear();
	}

	/**
	 * The main method contains some examples of how to use an
	 * {@link xxl.core.predicates.ExistPredicate exist} predicate. It can also
	 * be used to test the functionality of the exist predicate.
	 *
	 * @param args array of <code>String</code> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main(String[] args) {

		/*********************************************************************/
		/*                            Example 1                              */
		/*********************************************************************/

		System.out.println("Cursor 1: integers 8 to 15");
		xxl.core.cursors.sources.Enumerator cursor1 = new xxl.core.cursors.sources.Enumerator(8, 16);

		System.out.println("Cursor 2: integers 9 to 19");
		xxl.core.cursors.sources.Enumerator cursor2 = new xxl.core.cursors.sources.Enumerator(9, 20);

		System.out.println("Cursor 3: integers 1 to 11");
		xxl.core.cursors.sources.Enumerator cursor3 = new xxl.core.cursors.sources.Enumerator(1, 12);
		
		// SELECT Integer3
		// FROM Cursor3
		// WHERE EXIST(
		//     SELECT Integer2
		//     FROM Cursor 2
		//     WHERE ANY Integer3 = (
		//         SELECT Integer1
		//         FROM Cursor 1
		//         WHERE Integer2=Integer1 AND Integer1>=Integer3
		//     )
		// )

		xxl.core.predicates.Predicate<Integer> equal = new xxl.core.predicates.Equal<Integer>();
		xxl.core.predicates.Predicate<Integer> greaterEqual = new xxl.core.predicates.GreaterEqual<Integer>(new xxl.core.comparators.ComparableComparator<Integer>());
		// bind Integer2 in Equal
		xxl.core.predicates.BindingPredicate<Integer> bindEqual = new xxl.core.predicates.BindingPredicate<Integer>(equal, Arrays.asList(0));
		// bind Integer3 in GreaterEqual
		xxl.core.predicates.BindingPredicate<Integer> bindGrEqual = new xxl.core.predicates.BindingPredicate<Integer>(greaterEqual, Arrays.asList(1));
		// and bindings
		xxl.core.predicates.Predicate<Integer> and = new xxl.core.predicates.And<Integer>(bindEqual, bindGrEqual);
		xxl.core.cursors.filters.Filter<Integer> sel = new xxl.core.cursors.filters.Filter<Integer>(cursor1, and);
		// inner subquery with two bindings
		Subquery<Integer> sub = new Subquery<Integer>(
			sel,
			Arrays.asList(
				bindEqual,
				bindGrEqual
			),
			new int[][] {
				new int[] {0, -1},
				new int[] {-1, 1}
			}
		);

		// any condition
		xxl.core.predicates.Predicate<Integer> equal2 = new xxl.core.predicates.Equal<Integer>();
		// integer3 has to bind in any condition
		xxl.core.predicates.BindingPredicate<Integer> bindEqual2 = new xxl.core.predicates.BindingPredicate<Integer>(equal2, Arrays.asList(0));
		// any predicate will called as invoke(Integer2), so nothing has to bind in the any condition (value -1)
		xxl.core.predicates.Predicate<Integer> anyPred = new xxl.core.predicates.AnyPredicate<Integer>(sub, bindEqual2, Arrays.asList(-1));
		// filter in subquery of cursor2
		xxl.core.cursors.filters.Filter<Integer> sel2 = new xxl.core.cursors.filters.Filter<Integer>(cursor2, anyPred);

		// subquery where Integer3 has to bind in inner subquery and any condition
		Subquery<Integer> sub2 = new Subquery<Integer>(
			sel2,
			Arrays.asList(
				(Binding<Integer>)sub,
				bindEqual2
			),
			new int[][] {
				new int[] {1},
				new int[] {0}
			}
		);

		// exist predicate of the outer subquery
		xxl.core.predicates.Predicate<Integer> exist0 = new xxl.core.predicates.ExistPredicate<Integer>(sub2);
		// filter of cursor3
		xxl.core.cursors.filters.Filter<Integer> cursor = new xxl.core.cursors.filters.Filter<Integer>(cursor3, exist0);

		// cursor output
		System.out.println("Cursor: result");
		xxl.core.cursors.Cursors.println(cursor);
	}

}
