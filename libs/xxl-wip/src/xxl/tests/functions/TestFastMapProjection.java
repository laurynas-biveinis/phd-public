package xxl.tests.functions;

import java.util.Iterator;

import xxl.core.functions.FastMapProjection;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class FastMapProjection.
 */
public class TestFastMapProjection {

	/**
	 * The main method contains some examples to demonstrate the usage and the
	 * functionality of this class.
	 *
	 * @param args array of <code>String</code> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main(String[] args) {
		java.util.List<xxl.core.spatial.points.DoublePoint> l = new java.util.ArrayList<xxl.core.spatial.points.DoublePoint>();
		
		l.add(new xxl.core.spatial.points.DoublePoint(new double[] {0.9, 0.3}));
		l.add(new xxl.core.spatial.points.DoublePoint(new double[] {0.8, 0.4}));
		l.add(new xxl.core.spatial.points.DoublePoint(new double[] {0.7, 0.2}));
		l.add(new xxl.core.spatial.points.DoublePoint(new double[] {0.6, 0.8}));
		l.add(new xxl.core.spatial.points.DoublePoint(new double[] {0.5, 0.9}));
		
		FastMapProjection<xxl.core.spatial.points.DoublePoint> fm = new FastMapProjection<xxl.core.spatial.points.DoublePoint>(
			l.iterator(),
			1,				// map everything to one double value
			xxl.core.spatial.LpMetric.EUCLIDEAN	
		);
		
		System.out.println("All points are projected to the line between point 3 {0.7,0.2} and point 5 {0.5,0.9}");
		System.out.println("Transformed Coordinates");
		Iterator<xxl.core.spatial.points.DoublePoint> it = l.iterator(); 
		while (it.hasNext()) {
			xxl.core.spatial.points.DoublePoint point = it.next();
			double res[] = fm.invoke(point);
			for (int i = 0; i < res.length-1; i++)
				System.out.print(res[i]);
			System.out.print(res[res.length-1]);
			System.out.println();
		}
	}

}
