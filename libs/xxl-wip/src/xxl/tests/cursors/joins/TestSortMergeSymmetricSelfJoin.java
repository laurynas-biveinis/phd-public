package xxl.tests.cursors.joins;

import xxl.core.cursors.joins.SortMergeSymmetricSelfJoin;
import xxl.core.cursors.sources.Enumerator;
import xxl.core.functions.Identity;
import xxl.core.predicates.AbstractPredicate;
import xxl.core.predicates.Predicates;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class SortMergeSymmetricSelfJoin.
 */
public class TestSortMergeSymmetricSelfJoin {
	
	/**
	 * The main method contains some examples to demonstrate the usage and the
	 * functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to submit
	 *        parameters when the main method is called.
	 * @throws Exception some <tt>main</tt> methods of classes extending this
	 *         class throw exceptions.
	 */
	public static void main(String[] args) throws Exception {

		/*********************************************************************/
		/*                            Example 1                              */
		/*********************************************************************/

		// Input iterator : 0, ... , 9
		// Join Predicate : (a,b) <- Join  <=>  a=b
		
		SortMergeSymmetricSelfJoin join = new SortMergeSymmetricSelfJoin(
			new Enumerator(0,10),
			new xxl.core.collections.sweepAreas.SortMergeEquiJoinSA(
				new xxl.core.collections.sweepAreas.ListSAImplementor(),
				0,
				2
			),
			Predicates.TRUE,
			Identity.DEFAULT_INSTANCE
		);

		join.open();	
		while (join.hasNext())
			System.out.println(join.next());
		join.close();
		
		System.out.println("---");
		
		/*********************************************************************/
		/*                            Example 2                              */
		/*********************************************************************/
		
		// Input iterator : 0, ... , 3
		// Join Predicate : (a,b) <- Join  <=>  |a-b| <= 2
		
		join = new SortMergeSymmetricSelfJoin(
			new Enumerator(0,4),
			new xxl.core.collections.sweepAreas.ImplementorBasedSweepArea(
				new xxl.core.collections.sweepAreas.ListSAImplementor(),
				1,
				true,
				xxl.core.predicates.Predicates.TRUE,
				new AbstractPredicate() {
					public boolean invoke(Object o1, Object o2) {
						return ((Integer)o2).intValue()-((Integer)o1).intValue()>2;						
					}					
				},
				2
			),
			Predicates.TRUE,
			Identity.DEFAULT_INSTANCE
		);

		join.open();		
		while (join.hasNext())
			System.out.println(join.next());
		join.close();
	}

}
