package xxl.tests.binarySearchTrees;

import java.util.Comparator;
import java.util.Iterator;

import xxl.core.binarySearchTrees.AVLTree;
import xxl.core.binarySearchTrees.BinarySearchTree;
import xxl.core.functions.Function;
import xxl.core.functions.Identity;
import xxl.core.predicates.Predicate;
import xxl.core.predicates.Predicates;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class AVLTree.
 */
public class TestAVLTree extends AVLTree {
	
	/**
	 * Creates an AVLTree. 
	 * 
	 * @param fixAggregate Predicate that is called when an aggregate information in 
	 * 	the tree might be outdated.
	 * @param fixRotation Function that is called when a rotation has occured and rotation 
	 *	information in a node has to become fixed.
	 * @param maxBalance The maximum allowed balance, i.e. the maximum allowed 
	 * difference in height between the children of a node. 
	 */
	public TestAVLTree(Predicate fixAggregate, Function fixRotation, int maxBalance) {
		super(fixAggregate, fixRotation, maxBalance);
	}

	/**
	 * Creates a AVLTree with maximum balance 1.
	 * This constructor is equivalent to the call of
	 * <code>AVLTree(fixAggregate,fixRotation,1)</code>.
	 * 
	 * @param fixAggregate Predicate that is called when an aggregate information in 
	 * 	the tree might be outdated.
	 * @param fixRotation Function that is called when a rotation has occured and rotation 
	 *	information in a node has to become fixed.
	 */
	public TestAVLTree(Predicate fixAggregate, Function fixRotation) {
		super(fixAggregate, fixRotation);
	}

	public static void main(String[] args) {
		TestAVLTree tree = new TestAVLTree(Predicates.FALSE, Identity.DEFAULT_INSTANCE);
		Comparator comp = new Comparator() {
			public int compare(Object o1, Object o2) {
				double temp1 = ((Double[])((Object[]) o1)[0])[0];
				double temp2 = ((Double[])((BinarySearchTree.Node)o2).object())[0];
				if(temp1 < temp2) return -1;
				if(temp1 > temp2) return 1;
				return 0;
			}			
		};
		System.out.println(tree.insert(comp, new Object[]{new Double[]{1.0}}));
		System.out.println(tree.insert(comp, new Object[]{new Double[]{3.0}}));
		System.out.println(tree.insert(comp, new Object[]{new Double[]{5.0}}));
		System.out.println(tree.insert(comp, new Object[]{new Double[]{7.0}}));
		System.out.println(tree.insert(comp, new Object[]{new Double[]{9.0}}));
		System.out.println(tree.insert(comp, new Object[]{new Double[]{2.0}}));
		System.out.println(tree.insert(comp, new Object[]{new Double[]{4.0}}));
		
		Iterator it = tree.levelOrderIterator();
		//it = tree.iterator(true);
		Double[] in;
  		while (it.hasNext()) {
  			in = (Double[]) ((Node) it.next()).object();
  			System.out.println(in[0]);
		}
  		System.out.println();
  		System.out.println("height: ");
  		System.out.println(tree.getHeight(tree.root));
  		
  		System.out.println();
  		System.out.println("range query:");
  		Iterator rangeQuery = tree.rangeQuery(comp, 
				new Object[]{new Double[]{3.1}}, new Object[]{new Double[]{5.1}}, false);
		while(rangeQuery.hasNext()) {
			in = (Double[])(((Node)rangeQuery.next()).object());
			System.out.println(in[0]);
		}
	}

}
