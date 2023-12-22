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

package xxl.core.functions;

import java.util.Iterator;
import java.util.List;

import xxl.core.cursors.sources.EmptyCursor;

/** 
 * This class provides a factory method for typed arrays. The ArrayFactory
 * creates an array and fills it with objects. An iterator of arguments can be
 * passed to the invoke method of this class to create the objects which are
 * stored in the array.
 * 
 * @param <T> the component type of the array to be returned.
 */
public class ArrayFactory<T> extends Function<Object, T[]> {

	/**
	 * A factory method that gets one parameter and returns an array.
	 */
	protected Function<Object, T[]> newArray;

	/**
	 * A factory method that gets one parameter and returns an object used for
	 * initializing the array.
	 */
	protected Function<Object, ? extends T> newObject;

	/**
	 * Creates a new ArrayFactory.
	 * 
	 * @param newArray factory method that returns an array.
	 * @param newObject factory method that returns the elements of the array.
	 */
	public ArrayFactory(Function<Object, T[]> newArray, Function<Object, ? extends T> newObject) {
		this.newArray = newArray;
		this.newObject = newObject;
	}

    /**
     * Returns the result of the ArrayFactory as a typed array. This method
     * calls the invoke method of the newArray function which returns an array
     * of typed objects. After this, the invoke method of the newObject
     * function is called, so many times as the length of the array. As
     * parameter to the function an element of the iterator is given that is
     * specified as second argument.
     * 
     * @param arguments the arguments to this function. The first arguments
     *        must be an object used as argument to the newArray function.
     *        The second argument must be an iterator holding the arguments to
     *        the newObject function.
     * @return the initialized array.
     */
	public T[] invoke(List<? extends Object> arguments) {
		if (arguments.size() != 2)
			throw new IllegalArgumentException("the function must be invoked with an object and an iterator.");
		T[] array = newArray.invoke(arguments.get(0));
		Iterator<?> newObjectArguments = (Iterator<?>)arguments.get(1);
		for (int i = 0; i < array.length; i++)
			array[i] = newObject.invoke(newObjectArguments.hasNext() ? newObjectArguments.next() : null);	//call invoke on newObject with argument-Object from arguments-Iterator
		return array;
	}
    
    /**
     * Returns the result of the ArrayFactory as a typed array. This method
     * calls the invoke method of the newArray function using the given
     * argument which returns an array of typed objects. After this, the invoke
     * method of the newObject function is called without any arguments, so
     * many times as the length of the array.
     * 
     * @param argument the argument to the newArray function.
     * @return the initialized array.
     */
	public T[] invoke(Object argument) {
		return invoke(argument, EmptyCursor.DEFAULT_INSTANCE);
	}

    /**
     * Returns the result of the ArrayFactory as a typed array. This method
     * calls the invoke method of the newArray function using the argument
     * <code>null</code> which returns an array of typed objects. After this,
     * the invoke method of the newObject function is called without any 
     * rguments, so many times as the length of the array.
     * 
     * @return the initialized array.
     */
	public T[] invoke() {
		return invoke((Object)null);
	}

	/**
	 * The main method contains some examples to demonstrate the usage and the
	 * functionality of this class.
	 *
	 * @param args array of <code>String</code> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main(String[] args){
		ArrayFactory<List> f = new ArrayFactory<List>(
			new Function<Object, List[]>() {
				public List[] invoke(List<? extends Object> arguments){
					return new List[(Integer)arguments.get(0)];
				}
			},
			new Function<Object, List>() {
				public List invoke(List<? extends Object> arguments){
					java.util.ArrayList<Integer> list = new java.util.ArrayList<Integer>(1);
					list.add((Integer)arguments.get(0));
					return list;
				}
			}
		);

		List[] array = f.invoke(10, new xxl.core.cursors.sources.Enumerator());
		int i = 0;
		for (java.util.List list : array)
			System.out.println(i++ + "\t" + list);
	}
}