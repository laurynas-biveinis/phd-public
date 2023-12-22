package xxl.tests.cursors.joins;

import xxl.core.cursors.joins.SortMergeSelfJoin;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class SortMergeSelfJoin.
 */
public class TestSortMergeSelfJoin {
	
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
		
		// Input iterator : 0, ... , 3
		// Join Predicate : (a,b) <- Join  <=>  b <=a <= b+2
		
		SortMergeSelfJoin<Integer, Object[]> join = new SortMergeSelfJoin<Integer, Object[]>(
			new xxl.core.cursors.sources.Enumerator(0, 4),
			new xxl.core.collections.sweepAreas.SortMergeEquiJoinSA<Integer>(
				new xxl.core.collections.sweepAreas.ListSAImplementor<Integer>(),
				0,
				2
			),
			new xxl.core.collections.sweepAreas.ImplementorBasedSweepArea<Integer>(
				new xxl.core.collections.sweepAreas.ListSAImplementor<Integer>(),
				1,
				true,
				xxl.core.predicates.Predicates.TRUE,
				new xxl.core.predicates.AbstractPredicate<Integer>() {
					public boolean invoke(Integer i1, Integer i2) {
						return i2 - i1 > 2;
					}
				},
				2
			),
			xxl.core.functions.Tuplify.DEFAULT_INSTANCE
		);

		join.open();		
		while (join.hasNext())
			System.out.println(java.util.Arrays.toString(join.next()));
		join.close();
	}

}
