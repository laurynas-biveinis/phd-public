package xxl.tests.collections;

import java.util.List;

import xxl.core.collections.MappedList;
import xxl.core.functions.AbstractFunction;
import xxl.core.functions.Function;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class MappedList.
 */
public class TestMappedList {

	/**
	 * The main method contains some examples how to use a MappedList. It can
	 * also be used to test the functionality of a MappedList.
	 *
	 * @param args array of <code>String</code> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main(String[] args) {

		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////

		// create a new list
		List<Integer> list = new java.util.ArrayList<Integer>();
		// insert the Integers between 0 and 19 into the list
		for (int i = 0; i < 20; i++)
			list.add(i);
		// create a function that multplies every odd Integer with 10 and
		// divides every even Integer by 2
		Function<Integer, Integer> function = new AbstractFunction<Integer, Integer>() {
			public Integer invoke(Integer o) {
				return o%2 != 0 ? o*10 : o/2;
			}
		};
		// create a new mapped list that mapped the given list with the
		// given function
		MappedList<Integer, Integer> mappedList = new MappedList<Integer, Integer>(list, function);
		// print all elements of the mapped list
		for (int i = 0; i < mappedList.size(); i++)
			System.out.println(mappedList.get(i));
		System.out.println();
	}

}
