package xxl.tests.spatial.cursors;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import xxl.core.collections.queues.ListQueue;
import xxl.core.collections.queues.io.RandomAccessFileQueue;
import xxl.core.cursors.filters.Sampler;
import xxl.core.functions.AbstractFunction;
import xxl.core.functions.Function;
import xxl.core.functions.Tuplify;
import xxl.core.io.IOCounter;
import xxl.core.predicates.FeaturePredicate;
import xxl.core.predicates.Predicate;
import xxl.core.spatial.KPEzCode;
import xxl.core.spatial.cursors.MSJ;
import xxl.core.spatial.cursors.Mappers;
import xxl.core.spatial.cursors.Orenstein;
import xxl.core.spatial.cursors.PointInputCursor;
import xxl.core.spatial.points.FloatPoint;
import xxl.core.spatial.predicates.DistanceWithinMaximum;
import xxl.core.util.BitSet;
import xxl.core.util.WrappingRuntimeException;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class MSJ.
 */
public class TestMSJ {

	///use-case://///////////////////////////////////////////////////////////////////////////////////////////////////////

	/** use-case: similarity-join for point data.
	 * @param args the input arguments of the main method
	*/
	public static void main(String[] args) {

	    if (args.length == 0) {
	        args = new String[8];
	        String dataPath = xxl.core.util.XXLSystem.getDataPath(new String[] {"geo"}); 
		    args[0] = dataPath+File.separator+"rr_small.bin";
		    args[1] = dataPath+File.separator+"st_small.bin";
		    args[2] = "2";
		    args[3] = "256000";
		    args[4] = "0.2";
		    args[5] = "16";
		    args[6] = "0.1";
		    args[7] = "false";
	    }
	    
		if (args.length < 7 || args.length > 9) {
			System.out.println("usage: java xxl.core.spatial.cursors.MSJ <file-name0> <file-name1> <dim> <main memory> <epsilon-distance> <maximum level of the partitioning> <fraction of elements to be used from the input> <external computation=false>");
			return;
		}
		boolean selfJoin = false;
		final String input0 = args[0];
		final String input1 = args[1];
		if (input0.equals(input1)) {
			System.out.print("APPLYING_SELF_JOIN_OPTIMIZATION\t");
			selfJoin = true;
		}
		final int dim = Integer.parseInt(args[2]);
		final int mem = Integer.parseInt(args[3]);
		final float epsilon = Float.parseFloat(args[4]);
		final int maxLevel = Integer.parseInt(args[5]);
		final double p = Double.parseDouble(args[6]); //fraction of
													  // elements to be used
													  // from the input data
		final boolean external = args.length == 7 ? false : (args[7]
				.equals("true"));
		final String path = args.length < 9 ? null : args[8];
		if (external)
			System.out.print("EXTERNAL_ALG\t" + path + "\t");
		try {
			xxl.core.util.XXLSystem.getObjectSize(new KPEzCode(new FloatPoint(dim), new BitSet(32)));
		}
		catch (Exception e) {
			throw new WrappingRuntimeException(e);
		}
		
		final int initialCapacity = 30000;
		final int seed = 42; //note: same seed as in NestedLoopsJoin
							 // use-case!!
		Iterator r = Mappers.mapPointToKPEzCode(new Sampler(
				new PointInputCursor(new File(input0),
						PointInputCursor.FLOAT_POINT, dim, 1024 * 1024), p,
				seed), epsilon, maxLevel);
		Iterator s = null;
		if (!selfJoin) {
			s = Mappers.mapPointToKPEzCode(
					new Sampler(
							new PointInputCursor(new File(input1),
									PointInputCursor.FLOAT_POINT, dim,
									1024 * 1024), p, seed), epsilon,
					maxLevel);
		}
		final IOCounter counter = new IOCounter();
		final Function newQueue = new AbstractFunction() {
			protected int no = 0;
			public Object invoke(Object inputBufferSize,
					Object outputBufferSize) {
				if (external) {
					File file = null;
					try {
						file = File.createTempFile("RAF", ".queue",
								new File(path));
					} catch (IOException ioe) {
						System.out.println(ioe);
					}
					return new RandomAccessFileQueue(file, xxl.core.io.converters.ConvertableConverter.DEFAULT_INSTANCE, new AbstractFunction() {
						public Object invoke() {
							return new KPEzCode(new FloatPoint(dim),
									new BitSet());
						}
					}, (Function) inputBufferSize,
							(Function) outputBufferSize) {
						public void enqueueObject(Object object) {
							counter.incWrite();
							super.enqueueObject(object);
						}
						public Object dequeueObject() {
							counter.incRead();
							return super.dequeueObject();
						}
					};
				} else
					return new ListQueue();
			}
		};
		Predicate joinPredicate = new FeaturePredicate(
				new DistanceWithinMaximum(epsilon), new AbstractFunction() {
					public Object invoke(Object object) {
						return ((KPEzCode) object).getData();
					}
				});
		long start = System.currentTimeMillis();
		MSJ msj = null;
		/*
		 * if(selfJoin) //note: self-join excludes trivial results, i.e.
		 * tuples like (ID42, ID42) msj = new MSJ(r, joinPredicate,
		 * Tuplify.DEFAULT_INSTANCE, initialCapacity, maxLevel, dim,
		 * newQueue, mem); else
		 */
		msj = new MSJ(r, s, joinPredicate, Tuplify.DEFAULT_INSTANCE,
				initialCapacity, maxLevel, dim, newQueue, mem);
		int res = 0;
		while (msj.hasNext()) {
			res++;
			/* Object[] tuple = (Object[]) */ msj.next();
			//System.out.print( ((KPE)tuple[0]).getID() +"\t"+
			// ((KPE)tuple[1]).getID() +"\t");
			//			System.out.println(Points.maxDistance( (Point)
			// ((KPEzCode)tuple[0]).getData(), (Point)
			// ((KPEzCode)tuple[1]).getData() ) );
		}
		System.out.print(java.util.Arrays.toString(args) + "\t");
		System.out.print("RES:\t" + res + "\t");
		System.out.print("runtime(sec):\t"
				+ (System.currentTimeMillis() - start) / 1000.0 + "\t");
		System.out.print("element-comparisons:\t" + Orenstein.comparisons.counter
				+ "\t");
		if (external)
			System.out.println("IOs(object-count)\tRead:\t"
					+ counter.getReadIO() + "\tWrite:\t"
					+ counter.getWriteIO());
		else
			System.out.println();
	}

}
