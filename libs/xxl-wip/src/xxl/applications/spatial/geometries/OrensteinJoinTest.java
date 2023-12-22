/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/
package xxl.applications.spatial.geometries;

import java.awt.Color;
import java.io.File;
import java.util.Comparator;
import java.util.Iterator;

import xxl.connectivity.jts.io.Geometry2DConverter;
import xxl.connectivity.jts.io.Geometry2DFileIO;
import xxl.connectivity.jts.visual.VisualGeometry2DCursor;
import xxl.connectivity.jts.visual.VisualOutput;
import xxl.core.collections.containers.Container;
import xxl.core.collections.containers.io.ConverterContainer;
import xxl.core.collections.containers.io.MultiBlockContainer;
import xxl.core.comparators.ComparableComparator;
import xxl.core.cursors.Cursor;
import xxl.core.cursors.Cursors;
import xxl.core.cursors.DecoratorCursor;
import xxl.core.cursors.mappers.Mapper;
import xxl.core.functions.AbstractFunction;
import xxl.core.functions.Function;
import xxl.core.functions.Tuplify;
import xxl.core.io.converters.ConvertableConverter;
import xxl.core.predicates.FeaturePredicate;
import xxl.core.predicates.Predicate;
import xxl.core.spatial.KPE;
import xxl.core.spatial.KPEzCode;
import xxl.core.spatial.cursors.Orenstein;
import xxl.core.spatial.geometries.Geometry2D;
import xxl.core.spatial.geometries.cursors.SpatialJoin;
import xxl.core.spatial.geometries.predicates.Intersects;
import xxl.core.spatial.geometries.predicates.TuplePredicate;
import xxl.core.spatial.predicates.OverlapsPredicate;
import xxl.core.spatial.rectangles.DoublePointRectangle;
import xxl.core.spatial.rectangles.Rectangle;
import xxl.core.spatial.rectangles.Rectangles;
import xxl.core.util.WrappingRuntimeException;
import xxl.core.util.XXLSystem;

/** This class runs the Spatial-Join with the {@link Orenstein}-operator in the filter-step.
 *  For a detailed explanation of this operator see {@link Orenstein}. 
 *  <br><br>
 *  In the first part of the join geometries are stored in {@link MultiBlockContainer}s and the filter-step
 *  is run on the geometries approximations, i.e. their minimal bounding rectangles wrapped in so-called 
 *  {@link KPEzCode}-objects. These objects contain the spatial information of the mbr (in unit-space) and
 *  the z-value of the rectangle, that is the highest level of an imaginary quadtree, that completely contains 
 *  the mbr. This z-value defines an order on the rectangles and the join-operator only tests those rectangles
 *  for overlap, of which the z-codes are similar, i.e. they are the same, or one is a prefix of the other.
 *  <br>For each resulting candidate-tuple the exact geometries are loaded and tested for intersection in the
 *  refinement-step.
 *   
 * @see KPEzCode 
 * @see SpatialJoin
 * @see Orenstein
 * @see MultiBlockContainer
 */
public class OrensteinJoinTest{
		
	/**
	 * Starts the OrensteinJoinTest. If no command line parameters are given the test is run with default-parameters
	 * @param args command line parameters
	 */
	public static void main(String[] args){				
			
		final File input0, input1;	// the path to the input geometry-files (*.geom)
		final int 	bufferSize,		// the buffersize used by the FileQueues of the external MergeSorter 
					memSize, 		// the initial memory available for the MergeSorter (Integer)
					finalMemSize, 	// the final memory available for the MergeSorter (Integer)
					maxLevel, 		// the maximal level of the assumed quadtree used for calculating the z-level					
					initialCapacity,// the initial capacity of the ArrayBag that is used for organizing the SweepAreas
					blockSize;		// the block-size (in Byte) of the geometry container
		
		final boolean 	external, 	// determines whether ot not to use external sorting	
						show; 		// determines whether ot not to show the input-geometries and the join-results
		
		System.out.println("Orenstein-Join Test\n");
		
		// process command line arguements
		if(args.length < 8){
			System.out.println("   usage: java "+OrensteinJoinTest.class.getCanonicalName()+"  file1 file2 memSize finalMemSize maxLevel initialCapacity show external bufferSize " +
							   "\n  with  file1, file2    - the path to the input geometry-files (*.geom)"+
							   "\n        memSize         - the initial memory available for the MergeSorter (Integer)"+
							   "\n        finalMemSize    - the final memory available for the MergeSorter (Integer)"+
							   "\n		  maxLevel        - the maximal level of the assumed quadtree used for calculating the z-level"+
							   "\n        initialCapacity - the initial capacity of the ArrayBag that is used for organizing the SweepAreas"+
							   "\n        blockSize       - the block-size (in Byte) of the geometry container"+
							   "\n        show            - determines wether or not to show the inputs and join- results " +
							   "\n        external        - determines wether to process the join completely in main memory, or to use external sorting" +
							   "\n        buffersize      - the buffersize used by the FileQueues of the external MergeSorter" +
							   "\n"							   
							   );
						
			System.out.println("No parameters specified ! Using default values :");
	
			String dataPath = XXLSystem.getDataPath(new String[]{"geo"});
			input0 		= new File( dataPath+ File.separator+ "houston_roads.geom");
			input1 		= new File( dataPath+ File.separator+ "houston_hydro.geom");
			memSize 	= 40960; // 4kb
			finalMemSize= 81920; // 8kb
			maxLevel = 32;
			initialCapacity = 30000;
			blockSize = 64;
			show 		= true;			
			external 	= true;
			bufferSize 	= 4096;
		} else {			
			input0 		= new File(args[0]);
			input1 		= new File(args[1]);
			memSize 	= Integer.parseInt(args[2]);
			finalMemSize= Integer.parseInt(args[3]);
			maxLevel    = Integer.parseInt(args[4]);;
			initialCapacity = Integer.parseInt(args[5]);
			blockSize 	= Integer.parseInt(args[6]);
			show 		= Boolean.parseBoolean(args[7]);
			external 	= args.length > 8 ? Boolean.parseBoolean(args[8]) : true;
			bufferSize 	= args.length > 9 ? Integer.parseInt(args[9]) : 4096;
			System.out.println("Specified parameters: ");
		}
		
		System.out.println(	"\n\tfile1        = " + input0.getAbsolutePath() +
							"\n\tfile2        = " + input1.getAbsolutePath() +
							"\n\tmemsize      = " + memSize +
							"\n\tfinalMemSize = " + finalMemSize +
							"\n\tmaxLevel	  = " + maxLevel +
							"\n\tinitialCapac.= " + initialCapacity +
							"\n\tblockSie     = " + blockSize +
							"\n\tshow         = " + show +
							"\n\texternal     = " + external +
							"\n\tbufferSize   = " + bufferSize + 
							"\n");
	
		
		final String outPath = XXLSystem.getOutPath();		
	 		
		// the container to store the geometries
		MultiBlockContainer container = new MultiBlockContainer(outPath+"/OrensteinTest", blockSize);		
		final Container geometryContainer = new ConverterContainer( container, Geometry2DConverter.DEFAULT_INSTANCE);						
														
		// initialize an iterator on each input-file
		Iterator<Geometry2D> relationR = Geometry2DFileIO.read(Geometry2DConverter.DEFAULT_INSTANCE, input0);
		Iterator<Geometry2D> relationS = Geometry2DFileIO.read(Geometry2DConverter.DEFAULT_INSTANCE, input1);
													
		// load the mbr of each input-file and compute the universe, that contains all geometries
		Rectangle uniR = new DoublePointRectangle(2);
		Rectangle uniS = new DoublePointRectangle(2);
		uniR = Rectangles.readSingletonRectangle(new File(input0.getAbsoluteFile()+".universe"),uniR);
		uniS = Rectangles.readSingletonRectangle(new File(input1.getAbsoluteFile()+".universe"),uniS);
		uniR.union(uniS);		
		
		// if the geometries should be shown, create an output-panel
		final VisualOutput outputPanel = show ? new VisualOutput("Orenstein", uniR, 700) : null;				
		// and wrap the input-iterators to draw the geometries to the output-panel
		outputPanel.setRepaintImmediately(true);
		if(show){
			relationR = new VisualGeometry2DCursor(relationR, outputPanel, Color.DARK_GRAY);
			relationS = new VisualGeometry2DCursor(relationS, outputPanel, Color.GRAY);
		}												
					
		// a comparator to compare KPEzCode-objects according to their z-code
		Comparator<KPEzCode> comparator = new ComparableComparator();
//		Comparator<KPEzCode>(){
//			public int compare(KPEzCode o1, KPEzCode o2) {
//				return o1.compareTo(o2);
//			}			
//		};

		// get the size of an KPEzCode-object in main memory
		int objectSize;
		try {
			objectSize = xxl.core.util.XXLSystem.getObjectSize(Common.KPEzCode_FACTORY.invoke());
		}
		catch (Exception e) {
			throw new WrappingRuntimeException(e);
		}
		
		// create a Sorter to sort the KPEzCode-objects either externally or in main memory
		Function newSorter = external ? Common.getExternalMergeSorter(
											new ConvertableConverter<KPEzCode>(Common.KPEzCode_FACTORY), 
											comparator,
											objectSize, bufferSize, memSize, finalMemSize, outPath) 
									  : Common.getInternalMergeSorter(comparator, memSize, finalMemSize);
						
		// the join-predicate
		Predicate<KPE> joinPredicate = new FeaturePredicate<KPE, Rectangle>(
				OverlapsPredicate.DEFAULT_INSTANCE,
				Common.GET_DESCRIPTOR
				);			
		
		// wrap the input-geometry-iterators to save the geometries into the geometry-container 
		// and to return the according KPE pointing to the geometry
		Iterator<KPE> kpeInput1 = 
			new Mapper<Geometry2D, KPE>(
				Common.saveGeometry(geometryContainer),
				relationR
			);
		
		Iterator<KPE> kpeInput2 = 
			new Mapper<Geometry2D, KPE>(
				Common.saveGeometry(geometryContainer),
				relationS
			);
		
		// initialize the orenstein-operator (filter-step)
		Cursor orenstein = new xxl.core.spatial.cursors.Orenstein(
				// map the KPE-objects to KPEzCode-objects
				new Mapper<KPE, KPEzCode>(Common.mapKPEToKPEzCode(uniR, maxLevel),kpeInput1), 
				new Mapper<KPE, KPEzCode>(Common.mapKPEToKPEzCode(uniR, maxLevel),kpeInput2),
				// the join-predicate for the filter-step
				joinPredicate, 				
				newSorter, // the sorter 
				Tuplify.DEFAULT_INSTANCE, 
				initialCapacity 
			);
							
		// initialize the spatial-join-operator
		Cursor<Geometry2D[]> join = new SpatialJoin<Object,Geometry2D[]>(
				// filter-step
	    		orenstein,
	    		// map intermediate-tuples to candidate-tuples
	    		new AbstractFunction<Object, Geometry2D[]>(){
	    			Function<KPE, Geometry2D> getGeometry = Common.getGeometry(geometryContainer);
	    			public Geometry2D[] invoke(Object o){
						Object[] k = (Object[]) o;
	    				return new Geometry2D[]{
	    						getGeometry.invoke((KPEzCode)k[0]),
	    						getGeometry.invoke((KPEzCode)k[1])
	    					};	    					
	    			}			    				    			
	    		},
	    		// refinement-predicate
	    		new TuplePredicate<Geometry2D>(Intersects.DEFAULT_INSTANCE)	    			    		
	    	);

		// if results should be draw to the outputpanel decorate the join with an additional drawing-routine
		if(show) join = new DecoratorCursor<Geometry2D[]>(join){
		  Color[] colors = new Color[]{Color.GREEN, Color.RED};
			
		  public Geometry2D[] next(){	    						
			  outputPanel.draw(super.peek(), colors);
			  return super.next();			    		
		  }
		};					    		

		// let's take the current time to see how much time join-processing takes ...
		long start = System.currentTimeMillis();				   
		// and count the number of results returned by the join
		System.out.println("\nOrenstein- Join returned " +Cursors.count( join ) +" results in " +(System.currentTimeMillis()-start)+"ms");	
		join.close();
	  
		if(show) outputPanel.repaint();	
		
		// release all resources
		container.delete(); 
	}
}
