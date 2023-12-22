package xxl.tests.binarySearchTrees;

import java.util.Comparator;
import java.util.Iterator;

import xxl.core.binarySearchTrees.BinarySearchTree;
import xxl.core.binarySearchTrees.BinarySearchTree.Node;
import xxl.core.comparators.ComparableComparator;
import xxl.core.functions.Identity;
import xxl.core.predicates.AbstractPredicate;
import xxl.core.predicates.Predicate;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class BinarySearchTree.
 */
public class TestBinarySearchTree {
	
	/** 
	 * Class needed for the use case "Storing Intervals"
	 */
	private static class Interval implements Comparable {
		int left;
		int right;
		int agg_right;
		Interval(int left,int right) {
			this.left = left;
			this.right = right;
			this.agg_right = right;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(Object o) {
			return new Integer(left).compareTo(new Integer(((Interval) o).left));
		}
	}
	
	/** 
	 * Use case: Storing Intervals<br>
	 * In this example we use the aggregate Function to compute the maximum of the 
	 * right margins of the intervals in the child nodes. The aggregate Function is 
	 * called every time the tree changes in such a manner that an aggregate has to 
	 * become updated.
	 * 
	 * @param args command line arguments
	 * @throws Exception
	 */
	public static void main(String [] args) throws Exception {
		Predicate aggregateFunction = new AbstractPredicate() {
			public boolean invoke (Object argument) {
				Node node = (Node) argument;
				Interval in = (Interval) node.object();
				int previous = in.agg_right;
				in.agg_right = in.right;
				if (node.child(false)!=null)
					in.agg_right = Math.max(in.agg_right,((Interval) node.child(false).object()).agg_right);
				if (node.child(true)!=null)
					in.agg_right = Math.max(in.agg_right,((Interval) node.child(true).object()).agg_right);
				if (in.agg_right==previous)
					return true; // Aggregation complete
				else 
					return false; // Aggregation needed in parent node
			}
		};
			
		BinarySearchTree tree = new BinarySearchTree(aggregateFunction,Identity.DEFAULT_INSTANCE);

		final Comparator comparator = new ComparableComparator();
	
		Comparator chooseSubtree = new Comparator () {
			public int compare (Object object0, Object object1) {
				return comparator.compare(((Object[]) object0)[0], ((BinarySearchTree.Node)object1).object());
			}
		};
		
		System.out.println("Insert Intervals into the tree according to their left margin.");
		tree.insert(chooseSubtree,new Object[] {new Interval(5,7)});
		tree.insert(chooseSubtree,new Object[] {new Interval(3,4)});
		tree.insert(chooseSubtree,new Object[] {new Interval(7,8)});
		tree.insert(chooseSubtree,new Object[] {new Interval(9,11)});
		tree.insert(chooseSubtree,new Object[] {new Interval(6,12)});

		Iterator it;

		System.out.println("Perform a range query forwards.");
		it = tree.rangeQuery(chooseSubtree,new Object [] {new Interval(3,0)},new Object [] {new Interval(7,0)},true);
  		while (it.hasNext()) {
  			Interval in = (Interval) ((Node) it.next()).object();
			System.out.println("["+in.left+","+in.right+"] agg="+in.agg_right);
		}

		System.out.println("Perform a range query backwards.");
		it = tree.rangeQuery(chooseSubtree,new Object [] {new Interval(3,0)},new Object [] {new Interval(7,0)},false);
  		while (it.hasNext()) {
  			Interval in = (Interval) ((Node) it.next()).object();
			System.out.println("["+in.left+","+in.right+"] agg="+in.agg_right);
		}

		System.out.println("Perform a level-order traversal through the tree.");
		it = tree.levelOrderIterator();
  		while (it.hasNext()) {
  			Interval in = (Interval) ((Node) it.next()).object();
			System.out.println("["+in.left+","+in.right+"] agg="+in.agg_right);
		}

	}

}
