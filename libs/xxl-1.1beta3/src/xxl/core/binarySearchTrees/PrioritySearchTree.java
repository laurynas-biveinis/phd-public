package xxl.core.binarySearchTrees;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import xxl.core.collections.Lists;
import xxl.core.comparators.ComparableComparator;
import xxl.core.util.Interval1D;

/**
 * A simple static Priority Serach Tree.
 * 
 *  - all event point have to be known on construction time of the tree.
 *  - no event point may occur twice as right border of an interval in the tree.
 */
public class PrioritySearchTree <T> {
	
	/**
	 * A node of the Priority Search Tree.
	 *
	 */
	protected class Node {
		
		/**
		 * The left son of the node.
		 */
		protected Node left;
		
		/**
		 * The right son of the node.
		 */
		protected Node right;
		
		/**
		 * The split value of the node.
		 */
		protected T split;
		
		/**
		 * The interval stored in the node.
		 */
		protected Interval1D interval;
		
		/**
		 * Creates a new node.
		 * 
		 * @param left the left son of the new node.
		 * @param right the right son of the new node.
		 * @param split the split value of the new node.
		 * @param interval the interval stored in the node.
		 */
		public Node (Node left, Node right, T split, Interval1D interval) {
			this.left = left;
			this.right = right;
			this.split = split;
			this.interval = interval;
		}
		
		/**
		 * Creates a new node without interval.
		 * 
		 * @param left the left son of the new node.
		 * @param right the right son of the new node.
		 * @param split the split value of the new node.
		 */
		public Node (Node left, Node right, T split) {
			this(left, right, split, null);
		}

		/**
		 * Creates a new leaf node without interval.
		 * 
		 * @param split the split value of the new node.
		 */
		public Node (T split) {
			this(null, null, split, null);
		}
		
		/**
		 * Moves the left split values for all nodes in the subtree up one level. 
		 */
		protected void leftup() {
			if (left==null) 
				return;
			split = left.split;
			left.leftup();
			if (right!=null) 
				right.leftup();
		}
	}
	
	/**
	 * The comparator used to compare elements of the underlying type T.
	 */
	public final Comparator<T> comparator;
	
	/**
	 * The root node of the tree.
	 */
	private Node root;
	
	/**
	 * Creates a new Priority Search Tree for a set of event points.
	 * 
	 * @param events a list of the event points to use in the tree.
	 * @param isSorted a flag indicating if the event points are sorted.
	 * @param comparator a comparator for elements of the underlying type T.
	 */
	public PrioritySearchTree (ArrayList<T> events, boolean isSorted, Comparator<T> comparator) {
		this.comparator = comparator;
		init(events, isSorted);
	}
	
	/**
	 * Creates a new Priority Search Tree for a set of intervals.
	 * 
	 * @param intervals a list of the intervals to use in the tree. The intervals are NOT initially stored in the tree.
	 * @param comparator a comparator for elements of the underlying type T.
	 */
	public PrioritySearchTree (ArrayList<Interval1D> intervals, Comparator<T> comparator) {
		this.comparator = comparator;
		ArrayList<T> obj = new ArrayList<T>();
		for (Interval1D interval : intervals) {
			obj.add((T)(interval.border(false)));
			obj.add((T)(interval.border(true)));
		}
		init(obj, false);
	}
	
	/**
	 * Initializes the sceleton of the priority search tree.
	 * 
	 * @param events the event points which may occur in the priority search tree. 
	 * @param isSorted flag indicating if <i>events</i> is sorted.
	 */
	private void init(ArrayList<T> events, boolean isSorted) {
		if (!isSorted)
			Lists.quickSort(events, comparator);
		// build leaf level
		ArrayList<Node> leafs = new ArrayList<Node>();		
		T last = events.get(0);
		leafs.add(new Node (events.get(0)));
		for (T event : events) 
			if (!event.equals(last)) 
				leafs.add(new Node (last = event));
		// build tree
		root = buildup(leafs);
		// the split values are now the maximums of the subtrees
		// move them one level up for correct split decisions
		root.leftup();		
	}
	
	/**
	 * Builds up a level of the sceleton.
	 * 
	 * @param nodes the nodes to store on the level.
	 * @return root of the tree.
	 */
	private Node buildup(ArrayList<Node> nodes) {
		if (nodes.size()==1) 
			return nodes.get(0);
		ArrayList<Node> uppernodes = new ArrayList<Node>();
		for (int i=0; i<nodes.size(); i+=2) 
			uppernodes.add( 
				(i+1<nodes.size()) ?
				(new Node (nodes.get(i),nodes.get(i+1),nodes.get(i+1).split)):
				(nodes.get(i))   
			);
		return buildup(uppernodes);
	}
	
	/**
	 * Inserts an Interval into the tree.
	 * 
	 * @param interval the interval to insert.
	 */
	public void insert (Interval1D interval) {
		insert(root, interval);
	}
	
	/**
	 * Recursively inserts an interval inside the tree.
	 * 
	 * @param node the node to traverse.
	 * @param interval the interval to insert.
	 */
	private void insert (Node node, Interval1D interval) {
		if (node.interval==null)  
			node.interval = interval;
		else {
			if (comparator.compare((T)(node.interval.border(false)), (T)(interval.border(false))) <= 0) { 
				if (comparator.compare(node.split, (T)(interval.border(true))) >= 0) 
					insert(node.left, interval);
				else 
					insert(node.right, interval);
			}
			else {
				Interval1D tmp = node.interval;
				node.interval = interval;
				insert(node, tmp);
			}
		}
	}
	
	/**
	 * Removes an interval from the tree.
	 * 
	 * @param interval the interval to remove.
	 */
	public void remove (Interval1D interval) {
		remove(root, interval);
	}
	
	/**
	 * Recursively tries to remove an interval from the tree.
	 * 
	 * @param node the node to traverse.
	 * @param interval the interval to remove
	 * @return <i>true</i>, if the interval was succesfully removed, <i>false</i> otherwise.
	 */
	private boolean remove (Node node, Interval1D interval) {
		if (node == null || node.interval == null) 
			return false;
		if ((comparator.compare((T)(node.interval.border(false)),(T)(interval.border(false)))==0) && (comparator.compare((T)(node.interval.border(true)),(T)(interval.border(true)))==0)) {
			if (node.right==null) {     				
				if (node.left==null)
					node.interval = null;				
				else {
					node.interval = node.left.interval;
					remove(node.left, node.left.interval);
				}
			}
			else if (node.right.interval==null || (node.left.interval!=null && comparator.compare((T)(node.left.interval.border(false)),(T)(node.right.interval.border(false)))<0)) {	
				node.interval = node.left.interval;
				remove(node.left, node.left.interval);
			}
			else {
				node.interval = node.right.interval;
				remove(node.right, node.right.interval);
			}
			return true;
		}
		else {
			if (comparator.compare(node.split,(T)(interval.border(true)))>=0) 
				return remove(node.left, interval);
			else 
				return remove(node.right, interval);
		}
	}	

	/**
	 * Checks if an interval is stored in the tree.
	 * 
	 * @param interval the interval to find.
	 * @return <i>true</i>, if interval is contained in the tree, <i>false</i> otherwise
	 */
	public boolean contains (Interval1D interval) {
		return contains(root, interval);
	}
	
	/**
	 * Recursively tries to find an interval in the tree.
	 * 
	 * @param node the node to traverse.
	 * @param interval the interval to find
	 * @return <i>true</i>, if the interval was succesfully removed, <i>false</i> otherwise.
	 */
	private boolean contains (Node node, Interval1D interval) {
		if (node == null || node.interval == null) 
			return false;
		if ((comparator.compare((T)(node.interval.border(false)),(T)(interval.border(false)))==0) && (comparator.compare((T)(node.interval.border(true)),(T)(interval.border(true)))==0)) {
			return true;
		}
		else {
			if (comparator.compare(node.split,(T)(interval.border(true)))>=0) 
				return remove(node.left,interval);
			else  
				return remove(node.right,interval);
		}
	}	
	
	/**
	 * Finds all intervals [x,y] for which y lies in the interval yRange and x is less than xMax.
	 * 
	 * @param xMax the upper bound for x values (not inclusive)
	 * @param yRange the range for y values (inclusive)
	 * @return all intervals [x,y] with x < xMax and y in yRange 
	 */
	public ArrayList<Interval1D> query (T xMax, Interval1D yRange) {
		ArrayList<Interval1D> results = new ArrayList<Interval1D>();
		query(root, xMax, yRange, results);
		return results;
	}

	/**
	 * Recursivly finds all intervals [x,y] in the subtree of node for which y lies in the interval yRange and x is less than xMax.
	 * 
	 * @param node the root of the subtree to traverse
	 * @param xMax the upper bound for x values (not inclusive)
        * @param yRange the range for y values (inclusive) 
	 * @param results an Arraylist for collecting the results
	 */
	private void query (Node node, T xMax, Interval1D yRange, List<Interval1D> results) {
		if (node==null || node.interval==null)  
			return;  			
		if (comparator.compare((T)(node.interval.border(false)), xMax) > 0)  
			return; 	
		int cr = yRange.contains(node.split);
		if (cr==0) 
			results.add(node.interval);
		if (cr>=0) 
			query (node.right, xMax, yRange, results);
		if (cr<=0) 
			query (node.left, xMax, yRange, results);		
	}
	
	/**
	 * Main method for tests.
	 * 
	 * @param args unused
	 */
	public static void main (String [] args) {
		// test interval creation
		ArrayList<Interval1D> points = new ArrayList<Interval1D>();
		points.add(new Interval1D(new Double(1),new Double(3)));
		points.add(new Interval1D(new Double(2),new Double(4)));
		points.add(new Interval1D(new Double(3),new Double(7)));
		points.add(new Interval1D(new Double(2),new Double(4)));
		points.add(new Interval1D(new Double(1),new Double(5)));
		points.add(new Interval1D(new Double(6),new Double(6)));
		points.add(new Interval1D(new Double(4),new Double(7)));
		points.add(new Interval1D(new Double(4),new Double(8)));
		
		// tree construction
		PrioritySearchTree<Double> pst = new PrioritySearchTree<Double>(points, new ComparableComparator<Double>());
		
		// insertion
		for (int i=0; i<points.size(); i++) 
			pst.insert(points.get(i));
		
		// deletion
		pst.remove(points.get(3));
		pst.remove(points.get(7));
		
		// contains query
		System.out.println(pst.contains(new Interval1D(new Double(1),new Double(5))));
		System.out.println(pst.contains(new Interval1D(new Double(4),new Double(8)))+"\n");
		
		// range query
		ArrayList<Interval1D> results = pst.query(new Double(5),new Interval1D(new Double(0),new Double(8),new ComparableComparator<Double>()));
		for (Interval1D result : results) 
			System.out.println(result);
	}

}
