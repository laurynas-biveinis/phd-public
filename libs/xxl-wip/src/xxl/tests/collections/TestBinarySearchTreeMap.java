package xxl.tests.collections;

import xxl.core.collections.BinarySearchTreeMap;
import xxl.core.collections.MapEntry;
import xxl.core.functions.AbstractFunction;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class BinarySearchTreeMap.
 */
public class TestBinarySearchTreeMap {

	/**
	 * The main method contains some examples how to use an
	 * BinarySearchTreeMap. It can also be used to test the functionality
	 * of a BinarySearchTreeMap.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main (String [] args) {

		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////

		// create a binary search tree map that is using a binary search
		// tree and the natural ordering of the elements
		BinarySearchTreeMap map = new BinarySearchTreeMap();
		// create a new iterator with 100 random number lower than 1000
		xxl.core.cursors.Cursor cursor = new xxl.core.cursors.sources.DiscreteRandomNumber(new xxl.core.util.random.JavaDiscreteRandomWrapper(1000), 100);
		// insert all elements of the given iterator
		while (cursor.hasNext())
			map.put(cursor.peek(), cursor.next());
		// create an iteration of the elements of the set
		java.util.Iterator iterator = map.entrySet().iterator();
		// print all elements of the iteration (set)
		while (iterator.hasNext()) {
			MapEntry entry = (MapEntry)iterator.next();
			System.out.println("key \t= "+entry.getKey()+"\t & value \t= "+entry.getValue());
		}
		System.out.println();

		//////////////////////////////////////////////////////////////////
		//                      Usage example (2).                      //
		//////////////////////////////////////////////////////////////////

		// create a binary search tree map that is using a avl tree and
		// the natural ordering of the elements
		map = new BinarySearchTreeMap(new AbstractFunction() {
			public Object invoke (Object f1, Object f2) {
				return xxl.core.binarySearchTrees.AVLTree.FACTORY_METHOD.invoke(f1, f2);
			}
		});
		// create a new iterator with the numbers from 0 to 100
		cursor = new xxl.core.cursors.sources.Enumerator(101);
		// insert all elements of the given iterator
		while (cursor.hasNext())
			map.put(
				cursor.peek(),
				new Integer(100 * ((Integer)cursor.next()).intValue())
			);
		// create an iteration of the elements of the set
		iterator = map.entrySet().iterator();
		// print all elements of the iteration (set)
		while (iterator.hasNext()) {
			MapEntry entry = (MapEntry)iterator.next();
			System.out.println("key \t= "+entry.getKey()+"\t & value \t= "+entry.getValue());
		}
		System.out.println();
	}

}
