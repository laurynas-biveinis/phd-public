package xxl.tests.collections;

import java.util.List;

import xxl.core.collections.ReversedList;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class ReversedList.
 */
public class TestReversedList {
	
	/**
	 * The main method contains some examples how to use a ReversedList.
	 * It can also be used to test the functionality of a ReversedList.
	 * 
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main (String [] args) {
		
		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////
		
		// create a new list
		List<Integer> list = new java.util.ArrayList<Integer>();
		// insert the Integers between 0 and 19 into the list
		for (int i = 0; i < 20; i++)
			list.add(new Integer(i));
		// create a new reversed list with the given list
		ReversedList<Integer> reversedList = new ReversedList<Integer>(list);
		// print all elements of the reversed list
		for (int i = 0; i < reversedList.size(); i++)
			System.out.println(reversedList.get(i));
		System.out.println();
	}

}
