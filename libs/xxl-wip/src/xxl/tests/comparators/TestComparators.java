package xxl.tests.comparators;

import java.util.Comparator;

import xxl.core.comparators.ComparableComparator;
import xxl.core.comparators.Comparators;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class Comparators.
 */
public class TestComparators {

	/**
	 * The main method contains some examples to demonstrate the usage
	 * and the functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 * 		  submit parameters when the main method is called.
	 */
	public static void main(String[] args) {

		/*********************************************************************/
		/*                            Example 1                             */
		/*********************************************************************/
		// example to newNullSensitiveComparator( Comparator, boolean)
		Comparator<Integer> ci = Comparators.newNullSensitiveComparator(ComparableComparator.INTEGER_COMPARATOR, false);
		
		Integer[] ia1 = new Integer[] {1, 2, 4, null};
		Integer[] ia2 = new Integer[] {3, 4, 5, null};
		
		for (Integer i1 : ia1)
			for (Integer i2 : ia2)
				System.out.println("compare(" + i1 + ", " + i2 + ")=" + ci.compare(i1, i2));
		
		Comparator<String> cs = Comparators.newNullSensitiveComparator(ComparableComparator.STRING_COMPARATOR, false);
		
		String[] sa1 = new String[] {"a1", "a2", "A3", null};
		String[] sa2 = new String[] {"a3", "A2", "a1", null};
	
		for (String s1 : sa1)
			for (String s2 : sa2)
				System.out.println("compare(" + s1 + ", " + s2 + ")=" + cs.compare(s1, s2));
	}

}
