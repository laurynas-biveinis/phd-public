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

package xxl.core.predicates;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import xxl.core.collections.Lists;
import xxl.core.comparators.ComparableComparator;
import xxl.core.functions.Binding;

/**
 * This class provides a predicate whose arguments are partially bound to some
 * constant objects.
 * 
 * <p>Example:<br />
 * Consider the predicate {@link Less} that returns true if the first argument
 * is less than the second. By creating a <code>BindingPredicate</code>
 * instance
 * <code><pre>
 *   Predicate&lt;Integer&gt; p = new BindingPredicate&lt;Integer&gt;(
 *       new Less&lt;Integer&gt;(
 *           new ComparableComparator&lt;Integer&gt;()
 *       ),
 *       Arrays.asList(0),
 *       Arrays.asList(42)
 *   );
 * </pre></code>
 * <code>p</code> can be evaluated by calling
 * <code><pre>
 *   p.invoke(new 2);               //predicate: 42 < 2
 * </pre></code>
 * which corresponds to the call
 * <code><pre>
 *   p.invoke(42, 2);               //predicate: 42 < 2
 * </pre></code>
 *
 * @param <P> the type of the predicate's parameters.
 * @see LeftBind
 * @see RightBind
 */
public class BindingPredicate<P> extends Predicate<P> implements Binding<P> {

	/**
	 * These objects are used as constant objects of this predicate's
	 * <code>invoke</code> methods. The important is that the attribute
	 * constIndices should always be sorted(!).
	 */
	protected Predicate<? super P> predicate;
	
	/**
	 * Arguments for binding.
	 */
	protected List<P> constArguments;
	
	/**
	 * Indices for binding.
	 */
	protected List<Integer> constIndices;

	/**
	 * Creates a new predicate which binds a part of the arguments of the
	 * specified predicate to the given constant objects.
	 *
	 * @param predicate the predicate whose arguments should be partially
	 *        bound.
	 * @param constIndices the indices of the arguments which should be bound.
	 * @param constArguments the constant arguments to be used in the
	 *        predicate.
	 */
	public BindingPredicate(Predicate<? super P> predicate, List<Integer> constIndices, List<? extends P> constArguments) {
		this.predicate = predicate;
		this.constIndices = null;
		this.constArguments = null;
		setBinds(constIndices, constArguments);
	}

	/**
	 * Creates a new predicate which binds part of the arguments of the
	 * specified predicate to <code>null</code>.
	 *
	 * @param predicate the predicate whose arguments should be bound to
	 *        <code>null</code>.
	 */
	public BindingPredicate(Predicate<? super P> predicate) {
		this(predicate, null, null);
	}

	/**
	 * Creates a new predicate which binds part of the arguments of the
	 * specified predicate to <code>null</code>.
	 *
	 * @param predicate the predicate whose arguments should be partially
	 *        bound to <code>null</code>.
	 * @param constIndices the indices of the arguments which should be bound
	 *        to <code>null</code>. 
	 */
	public BindingPredicate(Predicate<? super P> predicate, List<Integer> constIndices) {
		this.predicate = predicate;

		int i = constIndices.size();
		setBinds(constIndices, Lists.initializedList((P)null, i));
	}
	
	/**
	 * Set the constant values to which a part of the arguments of the wrapped
	 * predicate should be bound.
	 *
	 * @param constArguments the constant values to which a part of the
	 *        arguments of the wrapped predicate should be bound.
	 */
	public void setBinds(List<? extends P> constArguments) {
		if (this.constArguments.size() == constArguments.size())
			Collections.copy(this.constArguments, constArguments);
	}
	
	/**
	 * Set the constant values to which a part of the arguments of the wrapped
	 * predicate should be bound and returns the changed predicate.
	 *
	 * @param constIndices the indices of the arguments which should be bound.
	 * @param constArguments the constant values to which a part of the
	 *        arguments of the wrapped predicate should be bound.
	 */
	public void setBinds(List<Integer> constIndices, List<? extends P> constArguments) {
		if (constIndices != null) {
			int len = constIndices.size();
			for (int i = 0; i < len; i++)
				if (constIndices.get(i) != -1)
					setBind(constIndices.get(i), constArguments.get(i));
		}
	}

	/**
	 * Set free all bound arguments of the wrapped predicate.
	 */
	public void restoreBinds() {
		this.constIndices = null;
		this.constArguments = null;
	} 

	/**
	 * Set a constant value to which an arguments of the wrapped predicate
	 * should be bound and returns the changed predicate.
	 *
	 * @param constIndex the index of the arguments which should be bound.
	 * @param constArgument the constant value to which an argument of the
	 *        wrapped predicate should be bound.
	 */
	public void setBind(int constIndex, P constArgument) {
		if (constIndex == -1)
			return;
		int len;
		if (constIndices != null){
			len = constIndices.size();
			for (int i = 0; i < len; i++){
				if (constIndex == constIndices.get(i)) {
					constArguments.set(i, constArgument);
					return;
				}
			}
			List<Integer> tempConstIndices = new ArrayList<Integer>(len+1);
			List<P> tempConstArguments = new ArrayList<P>(len+1);
			int pos = 0;
			while (pos < len && constIndices.get(pos) < constIndex) {
				tempConstIndices.add(constIndices.get(pos));
				tempConstArguments.add(constArguments.get(pos));
				pos++;
			}
			tempConstIndices.add(constIndex);
			tempConstArguments.add(constArgument);
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
			constIndices = Arrays.asList(constIndex);
			constArguments = Arrays.asList(constArgument);
		}
		
	} 

	/**
	 * Returns the result of the underlying predicate's <code>invoke</code>
	 * method that is called with the partially bound arguments.
	 *
	 * @param arguments the arguments to the underlying predicate.
	 * @return the result of the underlying predicate's <code>invoke</code>
	 *         method that is called with the partially bound arguments.
	 */
	public boolean invoke(List<? extends P> arguments) {
		if (arguments == null)
			arguments = new ArrayList<P>(0);
		if (constArguments == null)
			constArguments = new ArrayList<P>(0);
			
		int totalArgumentsLength = constArguments.size() + arguments.size();
		List<P> newArguments = new ArrayList<P>(totalArgumentsLength);
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

		switch (newArguments.size()) {
			case 0:
				return predicate.invoke();
			case 1:
				return predicate.invoke(newArguments.get(0));
			case 2:
				return predicate.invoke(newArguments.get(0), newArguments.get(1));
			default:
				return predicate.invoke(newArguments);
		}
	}

	/**
	 * Returns the result of the underlying predicate's <code>invoke</code>
	 * method that is called with the partially bound arguments.
	 *
	 * @param argument the free argument to the underlying predicate.
	 * @return the result of the underlying predicate's <code>invoke</code>
	 *         method that is called with the partially bound arguments.
	 */
	public boolean invoke(P argument) {
		return invoke(Arrays.asList(argument));
	}

	/**
	 * Returns the result of the underlying predicate's <code>invoke</code>
	 * method that is called with the partially bound arguments.
	 *
	 * @param argument0 the frist free argument to the underlying predicate.
	 * @param argument1 the second free argument to the underlying predicate.
	 * @return the result of the underlying predicate's <code>invoke</code>
	 *         method that is called with the partially bound argument.
	 */
	public boolean invoke(P argument0, P argument1) {
		return invoke(Arrays.asList(argument0, argument1));

	}

	/**
	 * The main method contains some examples of how to use a BindingPredicate.
	 * It can also be used to test the functionality of a BindingPredicate.
	 *
	 * @param args array of <code>String</code> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main(String[] args){
		// create a BindingPredicate that implements the Less-Predicate
		// 42 < x
		BindingPredicate<Integer> p = new BindingPredicate<Integer>(
			new Less<Integer>(
				ComparableComparator.INTEGER_COMPARATOR
			),
			Arrays.asList(0),
			Arrays.asList(42)
		);
		
		System.out.println(p.invoke(2));
		System.out.println(p.invoke(Arrays.asList(44)));

		p.restoreBinds();
		// x < 42
		p.setBind(1, 42);
		
		System.out.println(p.invoke(2));

		// 44 < 42
		p.setBind(0, 44);

		System.out.println(p.invoke());

		System.out.println();
	}
}