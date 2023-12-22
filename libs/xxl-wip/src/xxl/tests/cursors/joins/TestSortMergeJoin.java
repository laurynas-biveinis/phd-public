package xxl.tests.cursors.joins;

import xxl.core.cursors.joins.SortMergeJoin;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class SortMergeJoin.
 */
public class TestSortMergeJoin {
	
	/**
	 * The main method contains some examples to demonstrate the usage and the
	 * functionality of this class.
	 *
	 * @param args array of <code>String</code> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main(String[] args) {

		/*********************************************************************/
		/*                            Example 1                              */
		/*********************************************************************/
		
		SortMergeJoin<Integer, Object[]> join = new SortMergeJoin<Integer, Object[]>(
			java.util.Arrays.asList(0, 1, 3, 4, 5, 7, 8, 9).iterator(),
			java.util.Arrays.asList(0, 2, 4, 6, 8, 10).iterator(),
			new xxl.core.collections.sweepAreas.SortMergeEquiJoinSA<Integer>(
				new xxl.core.collections.sweepAreas.ListSAImplementor<Integer>(),
				0,
				2
			),
			new xxl.core.collections.sweepAreas.SortMergeEquiJoinSA<Integer>(
				new xxl.core.collections.sweepAreas.ListSAImplementor<Integer>(),
				1,
				2
			),
			xxl.core.comparators.ComparableComparator.INTEGER_COMPARATOR,
			new xxl.core.functions.Tuplify()
		);

		join.open();
		
		while (join.hasNext())
			System.out.println(java.util.Arrays.toString(join.next()));
		
		join.close();
	}

}
