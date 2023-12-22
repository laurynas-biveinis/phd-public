package xxl.tests.spatial.cursors;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import xxl.core.collections.queues.ListQueue;
import xxl.core.collections.queues.io.RandomAccessFileQueue;
import xxl.core.cursors.filters.Sampler;
import xxl.core.cursors.sorters.MergeSorter;
import xxl.core.functions.AbstractFunction;
import xxl.core.functions.Function;
import xxl.core.functions.Tuplify;
import xxl.core.io.IOCounter;
import xxl.core.predicates.And;
import xxl.core.predicates.FeaturePredicate;
import xxl.core.predicates.Predicate;
import xxl.core.spatial.KPEzCode;
import xxl.core.spatial.cursors.GESS;
import xxl.core.spatial.cursors.Mappers;
import xxl.core.spatial.cursors.Orenstein;
import xxl.core.spatial.cursors.PointInputCursor;
import xxl.core.spatial.cursors.Replicator;
import xxl.core.spatial.points.FloatPoint;
import xxl.core.spatial.predicates.DistanceWithinMaximum;
import xxl.core.util.BitSet;
import xxl.core.util.WrappingRuntimeException;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class GESS.
 */
public class TestGESS {

	///use-case://///////////////////////////////////////////////////////////////////////////////////////////////////////

	/** 
	 * The main method contains some examples to demonstrate the usage
	 * and the functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 * 		submit parameters when the main method is called.
	 *  Use-case: similarity-join for point data.
	 *	Please execute java xxl.core.spatial.cursors.GESS to get help on class usage.
	 */
	public static void main(String[] args){
	    
	    if (args.length == 0) {
	        args = new String[9];
	        String dataPath = xxl.core.util.XXLSystem.getDataPath(new String[] {"geo"}); 
		    args[0] = dataPath+File.separator+"rr_small.bin";
		    args[1] = dataPath+File.separator+"st_small.bin";
		    args[2] = "2";
		    args[3] = "256000";
		    args[4] = "0.2";
		    args[5] = "16";
		    args[6] = "8";
		    args[7] = "0.1";
		    args[8] = "false";
	    }
	        
		if(args.length < 8 || args.length > 10){
			System.out.println("java xxl.core.spatial.cursors.GESS <file-name0> <file-name1> <dim> <main memory> <epsilon-distance> <msl> <k> <p> <external computation=false>");
			System.out.println("    <file-name0>           : first input file");
			System.out.println("    <file-name1>           : second input file");
			System.out.println("               (Note: if both file-names are equal a self-join is performed (uses some optimizations)");
			System.out.println();
			System.out.println("    <dim>                  : dimensionality of the data");
			System.out.println("    <main memory>          : available main memory in bytes");
			System.out.println("    <epsilon-distance>     : epsilon query-distance");
			System.out.println("    <msl>                  : maximum split level");
			System.out.println("    <k>                    : maximum splits per level");
			System.out.println("    <p>                    : fraction of elements to be used from the input>");
			System.out.println("    <external>             : if true: perform external memory algorithm");
			System.out.println();
			return;
		}

		boolean selfJoin = false;
		final String input0 = args[0];
		final String input1 = args[1];
		if(input0.equals(input1)){
			System.out.print("APPLYING_SELF_JOIN_OPTIMIZATION\t");
			selfJoin = true;
		}
		final int dim = Integer.parseInt(args[2]);
		final int mem = Integer.parseInt(args[3]);
		final float epsilon = Float.parseFloat(args[4]);
		final float epsilonDiv2 = epsilon/2;
		final int msl = Integer.parseInt(args[5]);
		final int k = Integer.parseInt(args[6]);
		final double p = Double.parseDouble(args[7]);   //fraction of elements to be used from the input data
		final boolean external = args.length == 8 ? false : (args[8].equals("true"));
		final String path = args.length <10  ? null: args[9];
		if(external)
			System.out.print("EXTERNAL_ALG\t"+path+"\t");

		final int objectSize;
		try {
			objectSize = xxl.core.util.XXLSystem.getObjectSize(new KPEzCode(new FloatPoint(dim), new BitSet(32)));
		}
		catch (Exception e) {
			throw new WrappingRuntimeException(e);
		}
		
		final int initialCapacity = 30000;

		final int seed = 42; //note: same seed as in NestedLoopsJoin use-case!!

		Iterator r = new Sampler(new PointInputCursor(new File(input0), PointInputCursor.FLOAT_POINT, dim, 1024*1024), p, seed);


		Iterator s = null;

		if(!selfJoin)
			s = new Sampler(new PointInputCursor(new File(input1), PointInputCursor.FLOAT_POINT, dim, 1024*1024), p, seed);

		final IOCounter counter = new IOCounter();


		final Function newQueue = new AbstractFunction() {
			protected int no = 0;
			
			public Object invoke(Object inputBufferSize, Object outputBufferSize) {
				if (external) {
					File file = null;
					try {
						file = File.createTempFile("RAF", ".queue", new File(path));
					}
					catch (IOException ioe) {
						System.out.println(ioe);
					}
					return new RandomAccessFileQueue(
						file,
						xxl.core.io.converters.ConvertableConverter.DEFAULT_INSTANCE,
						new AbstractFunction() {
							public Object invoke() {
								return new KPEzCode(new FloatPoint(dim));
							}
						},
						(Function)inputBufferSize,
						(Function)outputBufferSize
					) {
						public void enqueueObject(Object object) {
							counter.incWrite();
							super.enqueueObject(object);
						}

						public Object dequeueObject(){
							counter.incRead();
							return super.dequeueObject();
						}
					};
				}
				else
					return new ListQueue();
			}
		};

		Function newSorter = new AbstractFunction(){
		                public Object invoke(Object object){
		                       return new MergeSorter((Iterator)object, new xxl.core.comparators.ComparableComparator(), objectSize, mem, (int)(mem*0.4),newQueue, false);
		                }
		};

		Predicate joinPredicate =
			new And(
				new FeaturePredicate(
					new DistanceWithinMaximum(epsilon),
					new AbstractFunction(){
						public Object invoke(Object object){
							return ((KPEzCode)object).getData();
						}
					}
				),
				new GESS.ReferencePointMethod(epsilonDiv2)	//duplicate removal (modify GESS to work with reference point method)
			);

		//default strategy:
//		maxLevel = Math.max(0, (- (int)(Math.log(epsilon)/Math.log(2.0))) -3 );
		Predicate splitAllowed = new And(new Replicator.MaxSplitsPerLevel(k), new Replicator.MaxSplitLevel(msl));

		final int minBitIndex = 63- (64/dim) ;

		long start = System.currentTimeMillis();
		GESS gess = null;
		Function inputMapping = Mappers.pointToFixedPointRectangleMappingFunction(epsilonDiv2);

/*
			if(selfJoin)	//note: self-join excludes trivial results, i.e. tuples like (ID42, ID42)
				gess = new GESS(r, inputMapping, joinPredicate, splitAllowed, minBitIndex, newSorter, Tuplify.DEFAULT_INSTANCE, dim, initialCapacity);
			else
*/			gess = new GESS(r, s, inputMapping, joinPredicate, splitAllowed, minBitIndex, newSorter, Tuplify.DEFAULT_INSTANCE, dim, initialCapacity);

		int res = 0;
		while(gess.hasNext()){
			res++;
			/* Object[] tuple= (Object[]) */ gess.next();
			//System.out.print( ((KPE)tuple[0]).getID() +"\t"+ ((KPE)tuple[1]).getID() +"\t");
//			System.out.println(Points.maxDistance( (Point) ((KPEzCode)tuple[0]).getData(), (Point) ((KPEzCode)tuple[1]).getData() ) );
		}
		System.out.print(java.util.Arrays.toString(args) +"\t");
		System.out.print("RES:\t"+res+"\t");
		System.out.print("runtime(sec):\t"+(System.currentTimeMillis()-start)/1000.0+"\t");
		System.out.print("element-comparisons:\t"+Orenstein.comparisons.counter+"\t");
		if(external)
			System.out.println("IOs(object-count)\tRead:\t"+counter.getReadIO()+"\tWrite:\t"+counter.getWriteIO());
		else
			System.out.println();
	}

}
