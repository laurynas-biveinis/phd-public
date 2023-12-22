package xxl.tests.spatial.cursors;

import java.io.File;
import java.util.Iterator;

import xxl.core.cursors.Cursor;
import xxl.core.cursors.Cursors;
import xxl.core.cursors.filters.Filter;
import xxl.core.cursors.filters.Sampler;
import xxl.core.functions.AbstractFunction;
import xxl.core.functions.Function;
import xxl.core.functions.Tuplify;
import xxl.core.predicates.AbstractPredicate;
import xxl.core.predicates.Predicate;
import xxl.core.spatial.cursors.NestedLoopsJoin;
import xxl.core.spatial.cursors.PointInputCursor;
import xxl.core.spatial.points.Point;
import xxl.core.spatial.points.Points;
import xxl.core.spatial.predicates.DistanceWithinMaximum;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class NestedLoopsJoin.
 */
public class TestNestedLoopsJoin {

	/** use-case for similarity-join
	 *	@param args The arguments of the main method.
	 *
	 */
	public static void main(String[] args) {

	    if (args.length == 0) {
	        args = new String[5];
	        String dataPath = xxl.core.util.XXLSystem.getDataPath(new String[] {"geo"}); 
		    args[0] = dataPath+File.separator+"rr_small.bin";
		    args[1] = dataPath+File.separator+"st_small.bin";
		    args[2] = "2";
		    args[3] = "0.2";
		    args[4] = "0.02";
	    }
	    
        if (args.length != 5) {
			System.out.println("Similarity-join for point data. If the given file-names are equal a self-join is performed.");
			System.out.println("usage: java xxl.core.spatial.cursors.NestedLoops <file-name0> <file-name1> <dim> <epsilon-distance> <fraction of elements to be used from the input>");
			return;
		}
		boolean selfJoin = false;
		final String input0 = args[0];
		final String input1 = args[1];
		if (input0.equals(input1)) {
			//                        System.out.print("APPLYING_SELF_JOIN\t");
			selfJoin = true;
		}
		final int dim = Integer.parseInt(args[2]);
		final float epsilon = Float.parseFloat(args[3]);
		final double p = Double.parseDouble(args[4]); //fraction of elements to be used from the input data
		final boolean strict = false;
		final int seed = 42; //note: seed must be passed to the Sampler (at least for input s) otherwise the result of the join will not be correct!
		Iterator r = new Sampler(new PointInputCursor(new File(input1),
				PointInputCursor.FLOAT_POINT, dim, 1024 * 1024), p, seed);
		Function newCursor = new AbstractFunction() {
			public Object invoke() {
				return new Sampler(new PointInputCursor(new File(input0),
						PointInputCursor.FLOAT_POINT, dim, 1024 * 1024), p,
						seed);
			}
		};
		Iterator s = (Iterator) newCursor.invoke();
		Predicate predicate = new DistanceWithinMaximum(epsilon);
		if (strict) { //low-level nested-loops join based on a simple loop
			int res = 0;
			Object[] array = new Object[Cursors.count(s)];
			s = (Iterator) newCursor.invoke();
			for (int i = 0; i < array.length; i++)
				array[i] = s.next();
			while (r.hasNext()) {
				Object next = r.next();
				for (int i = 0; i < array.length; i++) {
					if (predicate.invoke(next, array[i])) {
						res++;
						System.out.println(Points.maxDistance((Point) next,
								(Point) array[i]));
					}
				}
			}
		} else { //lazy (XXL):
			long start = System.currentTimeMillis();
			Cursor nl = null;
			nl = new NestedLoopsJoin(r, s, newCursor, predicate,
					Tuplify.DEFAULT_INSTANCE);
			if (selfJoin) {
				nl = new Filter(nl, //a filter that removes trivial results form the result-cursor
						new AbstractPredicate() {
							public boolean invoke(Object object) {
								Object[] tuple = (Object[]) object;
								return !tuple[0].equals(tuple[1]);
							}
						});
			}/**/
			/*			int res = 0;
			 for(;nl.hasNext();res++){
			 Object[] tuple = (Object[]) nl.next();
			 System.out.println(Points.maxDistance( (Point) tuple[0], (Point) tuple[1] ) );
			 }/**/
			System.out.print(java.util.Arrays.toString(args) + "\t");
			if (selfJoin)
				System.out.print("RES(divided by 2):\t"
						+ (Cursors.count(nl) / 2) + "\t");
			else
				System.out.print("RES:\t" + Cursors.count(nl) + "\t");
			System.out.println("runtime(sec):\t"
					+ (System.currentTimeMillis() - start) / 1000.0);/**/
		}
	}

}
