package xxl.tests.cursors.groupers;

import java.util.Iterator;

import xxl.core.cursors.Cursor;
import xxl.core.cursors.Cursors;
import xxl.core.cursors.groupers.DBScan;
import xxl.core.functions.AbstractFunction;
import xxl.core.indexStructures.Sphere;
import xxl.core.predicates.AbstractPredicate;
import xxl.core.spatial.points.DoublePoint;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class DBScan.
 */
public class TestDBScan {
	
	/**
	 * The main method contains some examples to demonstrate the usage and the
	 * functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to submit
	 *        parameters when the main method is called.
	 */
	public static void main(String[] args) {

		// defining some points in 2D Euclidean space
		
		final xxl.core.collections.bags.ListBag data = new xxl.core.collections.bags.ListBag();
		
		data.insert(new DBScan.ClassifiableObject(new DoublePoint(new double[]{1.0, 1.0})));
		data.insert(new DBScan.ClassifiableObject(new DoublePoint(new double[]{2.0, 1.0})));
		data.insert(new DBScan.ClassifiableObject(new DoublePoint(new double[]{3.0, 1.0})));

		data.insert(new DBScan.ClassifiableObject(new DoublePoint(new double[]{1.0, 2.0})));
		data.insert(new DBScan.ClassifiableObject(new DoublePoint(new double[]{2.0, 2.0})));
		data.insert(new DBScan.ClassifiableObject(new DoublePoint(new double[]{2.0, 2.0})));
		data.insert(new DBScan.ClassifiableObject(new DoublePoint(new double[]{2.0, 2.0})));
		data.insert(new DBScan.ClassifiableObject(new DoublePoint(new double[]{2.0, 2.0})));
		data.insert(new DBScan.ClassifiableObject(new DoublePoint(new double[]{3.0, 2.0})));
		data.insert(new DBScan.ClassifiableObject(new DoublePoint(new double[]{3.0, 2.0})));

		data.insert(new DBScan.ClassifiableObject(new DoublePoint(new double[]{2.0, 8.0})));

		data.insert(new DBScan.ClassifiableObject(new DoublePoint(new double[]{1.0, 3.0})));
		data.insert(new DBScan.ClassifiableObject(new DoublePoint(new double[]{2.0, 3.0})));
		data.insert(new DBScan.ClassifiableObject(new DoublePoint(new double[]{3.0, 3.0})));

		data.insert(new DBScan.ClassifiableObject(new DoublePoint(new double[]{4.0, 4.0})));
		data.insert(new DBScan.ClassifiableObject(new DoublePoint(new double[]{5.0, 5.0})));
		data.insert(new DBScan.ClassifiableObject(new DoublePoint(new double[]{6.0, 6.0})));
		data.insert(new DBScan.ClassifiableObject(new DoublePoint(new double[]{7.0, 7.0})));
		data.insert(new DBScan.ClassifiableObject(new DoublePoint(new double[]{8.0, 8.0})));
		data.insert(new DBScan.ClassifiableObject(new DoublePoint(new double[]{9.0, 9.0})));

		data.insert(new DBScan.ClassifiableObject(new DoublePoint(new double[]{1.0, 6.0})));
		data.insert(new DBScan.ClassifiableObject(new DoublePoint(new double[]{1.0, 7.0})));
		data.insert(new DBScan.ClassifiableObject(new DoublePoint(new double[]{1.0, 8.0})));
		data.insert(new DBScan.ClassifiableObject(new DoublePoint(new double[]{1.0, 9.0})));

		data.insert(new DBScan.ClassifiableObject(new DoublePoint(new double[]{2.0, 7.0})));
		data.insert(new DBScan.ClassifiableObject(new DoublePoint(new double[]{2.0, 7.0})));
		data.insert(new DBScan.ClassifiableObject(new DoublePoint(new double[]{2.0, 7.0})));
		data.insert(new DBScan.ClassifiableObject(new DoublePoint(new double[]{2.0, 7.0})));
		data.insert(new DBScan.ClassifiableObject(new DoublePoint(new double[]{2.0, 7.0})));

		data.insert(new DBScan.ClassifiableObject(new DoublePoint(new double[]{2.0, 8.0})));
		data.insert(new DBScan.ClassifiableObject(new DoublePoint(new double[]{2.0, 9.0})));

		data.insert(new DBScan.ClassifiableObject(new DoublePoint(new double[]{3.0, 9.0})));

		data.insert(new DBScan.ClassifiableObject(new DoublePoint(new double[]{5.0, 9.0})));
		data.insert(new DBScan.ClassifiableObject(new DoublePoint(new double[]{9.0, 4.0})));
		data.insert(new DBScan.ClassifiableObject(new DoublePoint(new double[]{8.0, 1.0})));

		/*
		data.insert(new DBScan.ClassifiableObject(new DoublePoint(new double[]{4.0, 4.0})));
		data.insert(new DBScan.ClassifiableObject(new DoublePoint(new double[]{0.0, 0.0})));
		data.insert(new DBScan.ClassifiableObject(new DoublePoint(new double[]{1.0, 1.0})));
		data.insert(new DBScan.ClassifiableObject(new DoublePoint(new double[]{1.0, 1.0})));

		data.insert(new DBScan.ClassifiableObject(new DoublePoint(new double[]{1.0, 1.0})));
		data.insert(new DBScan.ClassifiableObject(new DoublePoint(new double[]{2.0, 2.0})));

		data.insert(new DBScan.ClassifiableObject(new DoublePoint(new double[]{5.0, 5.0})));
		data.insert(new DBScan.ClassifiableObject(new DoublePoint(new double[]{5.0, 5.0})));
		data.insert(new DBScan.ClassifiableObject(new DoublePoint(new double[]{5.0, 5.0})));
		data.insert(new DBScan.ClassifiableObject(new DoublePoint(new double[]{6.0, 6.0})));
		data.insert(new DBScan.ClassifiableObject(new DoublePoint(new double[]{3.0, 3.0})));
		*/

		// get the data
		Iterator input = data.cursor();

		// using an Euclidean metric as distance function
		// eps = 1.6
		// minPts = 3
		DBScan clusterCursor = new DBScan(
			input,
			1.6,
			3,
			new AbstractFunction() {
				public Object invoke(Object descriptor) {
					final Sphere sphere = (Sphere)descriptor;
					return data.query(  // define a simple range query: iterating over all elements
						new AbstractPredicate() { // and checking if the given point is contained in the search sphere
							public boolean invoke(Object o) {
								return sphere.contains(
								        new Sphere(((DBScan.ClassifiableObject)o).getObject(), 0d, null)
								);
							}
						}
					);
				}
			},
			new AbstractFunction() {
				public Object invoke(Object object, Object eps) {
					return new Sphere(((DBScan.ClassifiableObject)object).getObject(), ((Double)eps).doubleValue(), null);
				}
			}
		);
		
		clusterCursor.open();

		for (int i = 0; clusterCursor.hasNext(); i++) {
			Cursor next = (Cursor)clusterCursor.next(); // each element of the clusterCursor is a new cursor, namely a new cluster
			System.out.println("cluster " + i + ": ");
			Cursors.println(next);
		}
		
		clusterCursor.close();
	}

}
