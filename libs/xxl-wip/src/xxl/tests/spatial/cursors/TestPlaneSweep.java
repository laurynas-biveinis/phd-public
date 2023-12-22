package xxl.tests.spatial.cursors;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;

import xxl.core.collections.queues.ListQueue;
import xxl.core.collections.queues.io.RandomAccessFileQueue;
import xxl.core.cursors.Cursor;
import xxl.core.cursors.joins.SortMergeJoin;
import xxl.core.cursors.sorters.MergeSorter;
import xxl.core.functions.AbstractFunction;
import xxl.core.functions.Function;
import xxl.core.spatial.KPE;
import xxl.core.spatial.cursors.KPEInputCursor;
import xxl.core.spatial.cursors.PlaneSweep;
import xxl.core.spatial.rectangles.DoublePointRectangle;
import xxl.core.spatial.rectangles.Rectangle;
import xxl.core.spatial.rectangles.Rectangles;
import xxl.core.util.WrappingRuntimeException;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class PlaneSweep.
 */
public class TestPlaneSweep {


	/** Use-case: spatial-join for rectangles.
	 *
	 *	Please execute java xxl.core.spatial.cursors.PlaneSweep to get help on class usage.
	 *	@param args the input arguments of the main method
	 */
	public static void main(String[] args) {

	    if (args.length == 0) {
	        args = new String[5];
	        String dataPath = xxl.core.util.XXLSystem.getDataPath(new String[] {"geo"}); 
		    args[0] = dataPath+File.separator+"rr_small.bin";
		    args[1] = dataPath+File.separator+"st_small.bin";
		    args[2] = "2";
		    args[3] = "256000";
		    args[4] = "512";
	    }
	    
		if(args.length < 5 || args.length >7){
			System.out.println();
			System.out.println("PlaneSweep: A computational geometry plane sweep join-algorithm.");
			System.out.println("This implementation corresponds to the version proposed by Lars Arge et.al. on VLDB 1998. See also: Preparata and Shamos: Computational Geometry.");
			System.out.println();
			System.out.println("usage: java xxl.core.spatial.cursors.PlaneSweep <file-name0> <file-name1> <dim> <main memory> <number of hash-buckets> <external computation path>");
			return;
		}

		boolean selfJoin = false;
		final String input0 = args[0];
		final String input1 = args[1];
		if(input0.equals(input1)){
			System.out.print("APPLYING_SELF_JOIN_OPTIMZATION\t");
			selfJoin = true;
		}

		final int dim = Integer.parseInt(args[2]);
		final int mem = Integer.parseInt(args[3]);
		final int hashBuckets = Integer.parseInt(args[4]);
		final boolean external = args.length != 5;
		final String path = args.length>5 ? args[5] : null;
		if(external)
			System.out.print("EXTERNAL_ALG\t"+path+"\t");

		final Function newObject = new AbstractFunction(){
			public Object invoke(){
				return new KPE(new DoublePointRectangle(dim));
			}
		};
		final int objectSize;
		try {
			objectSize = xxl.core.util.XXLSystem.getObjectSize(newObject.invoke());
		}
		catch (Exception e) {
			throw new WrappingRuntimeException(e);
		}
		

		Cursor[] input = {	new KPEInputCursor(new File(input0),4096*100,dim),
					new KPEInputCursor(new File(input1),4096*100,dim)};

		Rectangle uniR = Rectangles.readSingletonRectangle(new File(input0+".universe"), new DoublePointRectangle(dim));
		Rectangle uniS = null;
		Rectangle universe = uniR;
		if(!selfJoin){
			uniS = Rectangles.readSingletonRectangle(new File(input1+".universe"), new DoublePointRectangle(dim));
			uniR.union(uniS);
			universe = uniR;
		}

		final Function newQueue = new AbstractFunction(){			
			public Object invoke (Object inputBufferSize, Object outputBufferSize){
				if(external){
					File file = null;
					try{
						file = File.createTempFile("RAF",".queue",new File(path));
					}catch(IOException e){System.out.println(e);}
					return new RandomAccessFileQueue(file, xxl.core.io.converters.ConvertableConverter.DEFAULT_INSTANCE, newObject,
						(Function) inputBufferSize, (Function) outputBufferSize);
				}
				else
					return new ListQueue();
			}
		};/**/

		final Comparator comparator = PlaneSweep.KPEPlaneSweepComparator.DEFAULT_INSTANCE;

		Function newSorter = new AbstractFunction(){
			public Object invoke(Object o){
				return new MergeSorter((Iterator)o, comparator, objectSize, mem, (int)(mem*2.0/7), newQueue, false);
			}
		};

		long start = System.currentTimeMillis();
		SortMergeJoin sortMerge = 
				new PlaneSweep(input[0], input[1], newSorter, newSorter, comparator, universe, hashBuckets, 5000);

		int count=0;
		while(sortMerge.hasNext()){
			sortMerge.next();
			count++;
			if( (count%50) == 0)
				System.out.println( "Time:\t"+(System.currentTimeMillis()-start)+"\t\tRES:\t"+count);
		}
		System.out.println("#Total time:\t"+(System.currentTimeMillis()-start)+"\tRES:\t"+count);
	}

}
