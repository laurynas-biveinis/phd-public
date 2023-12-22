/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/
package xxl.applications.spatial.geometries;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;

import xxl.core.collections.containers.Container;
import xxl.core.collections.queues.ListQueue;
import xxl.core.collections.queues.Queue;
import xxl.core.collections.queues.io.RandomAccessFileQueue;
import xxl.core.cursors.sorters.MergeSorter;
import xxl.core.functions.AbstractFunction;
import xxl.core.functions.Constant;
import xxl.core.functions.Function;
import xxl.core.io.converters.Converter;
import xxl.core.io.converters.LongConverter;
import xxl.core.spatial.KPE;
import xxl.core.spatial.KPEzCode;
import xxl.core.spatial.SpaceFillingCurves;
import xxl.core.spatial.geometries.Geometry2D;
import xxl.core.spatial.rectangles.DoublePointRectangle;
import xxl.core.spatial.rectangles.Rectangle;
import xxl.core.util.BitSet;
import xxl.core.util.WrappingRuntimeException;


/** 
 * Contains the most common functions needed by the examples inside this package. 
 */
public class Common {

	/** Don't let anyone instantiate this class */
	private Common(){};
	
	
	/** Factory Function to get a leaf entry. */
	public static Function<Object, KPE> LEAFENTRY_FACTORY = new AbstractFunction<Object, KPE>() {
		public KPE invoke() {
			return new KPE(new DoublePointRectangle(2),0L, LongConverter.DEFAULT_INSTANCE);
		}
	};
	
	/** Factory Function to get a KPEzCode- object */
	public static Function<Object, KPEzCode> KPEzCode_FACTORY = new AbstractFunction<Object, KPEzCode>() {
		public KPEzCode invoke() {
			return new KPEzCode(new DoublePointRectangle(2), 0L, new BitSet(32));
		}
	};
	
	/** Factory Function to get a descriptor. */
	public static Function<Object, Rectangle> DESCRIPTOR_FACTORY = new AbstractFunction<Object, Rectangle>(){
		public Rectangle invoke(){
			return new DoublePointRectangle(2);
		}
	};
			
	/** Function returning the descriptor of a given KPE object. */
	public static Function<KPE, Rectangle> GET_DESCRIPTOR = new AbstractFunction<KPE, Rectangle> () {
		public Rectangle invoke (KPE k) {
			return (Rectangle)k.getData(); 
		}
	};	
	
	/** Returns a Function to insert a geometry into the given container. The function itself 
	 *  returns a {@link KPE}-object pointing to the inserted geometry. 
	 * @param geometryContainer the container to store the geometry in 
	 * @return a Function to insert a geometry into the given container. The function itself
	 */
	public static Function<Geometry2D, KPE> saveGeometry(final Container geometryContainer){
		return new AbstractFunction<Geometry2D, KPE>(){	
			public KPE invoke(Geometry2D g){
				return new KPE(g.getMBR(), geometryContainer.insert(g), LongConverter.DEFAULT_INSTANCE);
			}
		};
	}
	
	/** Returns a Function to retrieve a geometry from the given container. The function itself
	 *  extracts the container-id from a {@link KPE}-object and returns the according geometry.
	 * 
	 * @param geometryContainer the container to retrieve the geometry from
	 * @return a Function to retrieve a geometry from the given container
	 */
	public static Function<KPE, Geometry2D> getGeometry(final Container geometryContainer){
		return new AbstractFunction<KPE, Geometry2D>(){
			public Geometry2D invoke(KPE k){
				return (Geometry2D) geometryContainer.get(k.getID());
			}
		};
	}
		
	
	/** A Function to map a {@link KPE}-object to a {@link KPEzCode}-object. This {@link KPEzCode}-object
	 *  contains spatial information in unit-space and the z-level of the rectangle, that is
	 *  the lowest level of an assumed quadtree, that completely contains the rectangle.
	 *   
	 * @param universe the universe of the KPE-object which is needed to transform the spatial information into unit-space
	 * @param maxLevel the maximal level of the assumed quadtree used for calculating the z-level
	 * @return a Function to map a {@link KPE}-object to a {@link KPEzCode}-object
	 */
	public static Function<KPE, KPEzCode> mapKPEToKPEzCode(final Rectangle universe, final int maxLevel){
		return new AbstractFunction<KPE, KPEzCode>(){	
			public KPEzCode invoke(KPE k){
				DoublePointRectangle r = (DoublePointRectangle) k.getData();
				return new KPEzCode(k, SpaceFillingCurves.zCode(r.normalize(universe),maxLevel));
			}
		};
	}
	
  
	/** 
	 * A function that sorts the rectangles according to their lower left border.
	 * This is needed for sort-based bulk insertion.
	 */
	public static Comparator<KPE> COMPARE = new Comparator<KPE>() {
		public int compare(KPE o1, KPE o2) {
			return ((Rectangle)o1.getData()).compareTo(o2.getData()); 
		}
	};

	/**
	 * A function that sorts the rectangles according to their hilbert value.
	 * This is needed for sort-based bulk insertion.
	 * @param universe the universe which contains all rectangle
	 * @return a function that sorts the rectangles according to their hilbert value
	 */
	public static Comparator COMPARE_HILBERT(final Rectangle universe, final int precision){
		return new Comparator() {
			protected double uni[];
			protected double uniDeltas[];	
			
			public int compare(Object o1, Object o2) {
				if (uni == null) {
					uni = (double[])universe.getCorner(false).getPoint();
					uniDeltas = universe.deltas();	
				}
				double leftBorders1[] = (double[])((Rectangle)((KPE)o1).getData()).getCorner(false).getPoint();
				double leftBorders2[] = (double[])((Rectangle)((KPE)o2).getData()).getCorner(false).getPoint();
					
				double x1 = (leftBorders1[0]-uni[0])/uniDeltas[0];
				double y1 = (leftBorders1[1]-uni[1])/uniDeltas[1];
				double x2 = (leftBorders2[0]-uni[0])/uniDeltas[0];
				double y2 = (leftBorders2[1]-uni[1])/uniDeltas[1];
				
				long h1 = SpaceFillingCurves.hilbert2d((int) (x1*precision),(int) (y1*precision));
				long h2 = SpaceFillingCurves.hilbert2d((int) (x2*precision),(int) (y2*precision));
				return (h1<h2)?-1: ((h1==h2)?0:+1);
			}
		};
	}

	/**
	 * A function that sorts the rectangles according to their peano value.
	 * This is needed for sort-based bulk insertion.
	 * @param universe the universe which contains all rectangle
	 * @return a function that sorts the rectangles according to their peano value
	 */
	public static Comparator COMPARE_PEANO(final Rectangle universe, final int precision){
		return new Comparator() {	
			protected double uni[];
			protected double uniDeltas[];	
			
			public int compare(Object o1, Object o2) {
				if (uni == null) {
					uni = (double[])universe.getCorner(false).getPoint();
					uniDeltas = universe.deltas();	
				}
				double leftBorders1[] = (double[])((Rectangle)((KPE)o1).getData()).getCorner(false).getPoint();
				double leftBorders2[] = (double[])((Rectangle)((KPE)o2).getData()).getCorner(false).getPoint();
				
				double x1 = (leftBorders1[0]-uni[0])/uniDeltas[0];
				double y1 = (leftBorders1[1]-uni[1])/uniDeltas[1];
				double x2 = (leftBorders2[0]-uni[0])/uniDeltas[0];
				double y2 = (leftBorders2[1]-uni[1])/uniDeltas[1];
				
				long h1 = SpaceFillingCurves.peano2d((int) (x1*precision),(int) (y1*precision));
				long h2 = SpaceFillingCurves.peano2d((int) (x2*precision),(int) (y2*precision));
				return (h1<h2)?-1: ((h1==h2)?0:+1);
			}
		};
	}	
	
	/** This Function provides a suitable Sorter for <code>KPE</code> objects which operates in external memory.
	 *  The sorter is Based on a {@link RandomAccessFileQueue}.
	 * @param <K> the type of the elements to sort
	 * @param kpeConverter the converter used to serialize the KPEs into blocks
	 * @param comparator the comparator used to compare two KPE-objects
	 * @param objectSize the size of an object in main memory
	 * @param bufferSize the buffer size used by the external Queue 
	 * @param memSize the memory available to the used merge-sorter during the open-phase.
	 * @param finalMemSize the memory available to the merge-sorter during the next-phase.
	 * @param tempPath the directory where the sorter can write its temporal results
	 * @return a Function to return a new external KPE sorter
	 * @see MergeSorter
	 * @see RandomAccessFileQueue
	 */	
	public static <K extends KPE> Function<Iterator<K>, MergeSorter<K>> getExternalMergeSorter(final Converter<K> kpeConverter, final Comparator<K> comparator,  final int objectSize, final int bufferSize, final int memSize, final int finalMemSize, final String tempPath){
		return new AbstractFunction<Iterator<K>, MergeSorter<K>>(){
			
			public MergeSorter<K> invoke(Iterator<K> k){
				Function newQueue = new AbstractFunction<Function<?, Integer>, Queue<KPE>>(){
				    public Queue<KPE> invoke (Function<?, Integer> inputBufferSize, Function<?, Integer> outputBufferSize){
				    	String fileName = "";
						try{
							fileName = File.createTempFile("RAF",".queue",new File(tempPath)).getAbsolutePath();
						}catch(IOException e){ throw new WrappingRuntimeException(e); }
						
						return new RandomAccessFileQueue(fileName, kpeConverter, new Constant(bufferSize), new Constant (bufferSize));
											
				    }
				};						    
		        return new MergeSorter<K>(k, comparator, objectSize, memSize, finalMemSize, newQueue, false);
		    };
		};
	}
	
		
	/** This Function provides a suitable Sorter for <code>KPE</code> objects which operates in main memory.
	 *  The sorter is Based on a {@link ListQueue}.
	 * @param <K> the type of the elements to sort
	 * @param comparator the comparator used to compare two KPE-objects
	 * @param memSize the memory available to the used merge-sorter during the open-phase.
	 * @param finalMemSize the memory available to the merge-sorter during the next-phase.
	 * @return a Function to return a new external KPE sorter
	 * @see MergeSorter
	 * @see ListQueue
	 */
	public static <K extends KPE> Function<Iterator<K>, MergeSorter<K>> getInternalMergeSorter(final Comparator<K> comparator, final int memSize, final int finalMemSize){
		return new AbstractFunction<Iterator<K>, MergeSorter<K>>(){			
			public MergeSorter<K> invoke(Iterator<K> k){				
				
				Function newQueue = new AbstractFunction(){
					public Object invoke (Object inputBufferSize, Object outputBufferSize){
				            return new ListQueue<K>();
				    }
				};			   
				
		        return new MergeSorter<K>(k, comparator, 1, memSize, finalMemSize, newQueue, false);
		    };
		};
	}
}
