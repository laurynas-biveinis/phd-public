package xxl.tests.collections;

import xxl.core.collections.MapEntry;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class MapEntry.
 */
public class TestMapEntry {

	/**
	 * The main method contains some examples how to use a MapEntry. It
	 * can also be used to test the functionality of a MapEntry.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main(String[] args) {

		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////

		// create two MapEntry
		MapEntry<Integer, String> entry1 = new MapEntry<Integer, String>(5, "five"),
		                          entry2 = new MapEntry<Integer, String>(2, "two");
		// check if both entries are equal
		if (entry1.equals(entry2))
			System.out.println("the entries are equal");
		else
			System.out.println("the entries are unqual");
		// change the second entry
		entry2.setKey(5);
		// check if both entries are equal
		if (entry1.equals(entry2))
			System.out.println("the entries are equal");
		else
			System.out.println("the entries are unqual");
		// change the second entry
		entry2.setKey(2);
		entry2.setValue("five");
		// check if both entries are equal
		if (entry1.equals(entry2))
			System.out.println("the entries are equal");
		else
			System.out.println("the entries are unqual");
		// change the second entry
		entry2.setKey(5);
		// check if both entries are equal
		if (entry1.equals(entry2))
			System.out.println("the entries are equal");
		else
			System.out.println("the entries are unqual");
		System.out.println();
	}

}
