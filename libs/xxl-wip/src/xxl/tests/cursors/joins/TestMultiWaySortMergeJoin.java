package xxl.tests.cursors.joins;

import java.util.Comparator;
import java.util.Iterator;

import xxl.core.collections.sweepAreas.SweepArea;
import xxl.core.comparators.ComparableComparator;
import xxl.core.cursors.joins.MultiWaySortMergeJoin;
import xxl.core.functions.Identity;
import xxl.core.util.Arrays;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class MultiWaySortMergeJoin.
 */
public class TestMultiWaySortMergeJoin {
	
	/**
	 * The main method contains some examples to demonstrate the usage
	 * and the functionality of this class. The number of inputs to use can
	 * be specified by using the command line parameter. If none are used,
	 * the default of three is used.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 * 		submit parameters when the main method is called.
	 */
	public static void main(String[] args) {

		/*********************************************************************/
		/*                            Example 1                              */
		/*********************************************************************/
		
		int dim = 3;
		final int[] primes = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29};
		
		if (args.length > 0) {
			try {
				dim = Integer.parseInt(args[0]);
			}
			catch(NumberFormatException e) {}
		}
		
		java.util.LinkedList[] l = new java.util.LinkedList[dim];
		int inputLength = 1;
		
		for (int i = 0; i < dim; i++) {
			l[i] = new java.util.LinkedList();
			inputLength *= 10;
		}	
		for (int j = 0; j <= inputLength; j++)
			for (int i = 0; i < dim; i++)
				if (j%primes[i] == 0)
					l[i].add(new Integer(j));
		
		Iterator[] input = new Iterator[dim];
		SweepArea[] sweepAreas = new SweepArea[dim];
		Comparator[][] comparators = new Comparator[dim][];				
		for (int i = 0; i < dim; i++) {
			input[i] = l[i].listIterator();
			sweepAreas[i] = new xxl.core.collections.sweepAreas.SortMergeEquiJoinSA(
				new xxl.core.collections.sweepAreas.ListSAImplementor(),
				i,
				dim
			);
			comparators[i] = new Comparator[dim];
			java.util.Arrays.fill(comparators[i], new ComparableComparator());
		}	
				
		MultiWaySortMergeJoin join = new MultiWaySortMergeJoin(
			input, 
			sweepAreas, 
			comparators, 
			Identity.DEFAULT_INSTANCE
		);

		join.open();
		while (join.hasNext()) {
			Arrays.println((Object[])join.next(),System.out);
		}		
		join.close();
	}

}
