package xxl.tests.collections;

import java.util.Iterator;

import xxl.core.collections.BinarySearchTreeSet;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class BinarySearchTreeSet.
 */
public class TestBinarySearchTreeSet {

	/**
	 * The main method contains some examples how to use a
	 * BinarySearchTreeSet. It can also be used to test the functionality
	 * of a BinarySearchTreeSet.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main (String [] args) {

		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////

		// create a binary search tree set that is using a binary search
		// tree and the natural ordering of the elements
		BinarySearchTreeSet set = new BinarySearchTreeSet();
		// create a new iterator with 100 random number lower than 1000
		Iterator iterator = new xxl.core.cursors.sources.DiscreteRandomNumber(new xxl.core.util.random.JavaDiscreteRandomWrapper(1000), 100);
		// insert all elements of the given iterator
		while (iterator.hasNext())
			set.insert(iterator.next());
		// create an iteration of the elements of the set
		iterator = set.iterator();
		// print all elements of the iteration (set)
		while (iterator.hasNext())
			System.out.println(iterator.next());
		System.out.println();
	}

}
