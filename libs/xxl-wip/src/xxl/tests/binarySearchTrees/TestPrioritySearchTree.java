package xxl.tests.binarySearchTrees;

import java.util.ArrayList;

import xxl.core.binarySearchTrees.PrioritySearchTree;
import xxl.core.comparators.ComparableComparator;
import xxl.core.util.Interval1D;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class PrioritySearchTree.
 */
public class TestPrioritySearchTree {
	
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
