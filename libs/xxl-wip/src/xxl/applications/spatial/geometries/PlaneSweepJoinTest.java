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
import java.util.Iterator;

import xxl.connectivity.jts.io.Geometry2DConverter;
import xxl.connectivity.jts.io.Geometry2DFileIO;
import xxl.connectivity.jts.visual.VisualGeometry2DCursor;
import xxl.connectivity.jts.visual.VisualOutput;
import xxl.core.collections.containers.Container;
import xxl.core.collections.containers.io.BlockFileContainer;
import xxl.core.collections.containers.io.ConverterContainer;
import xxl.core.collections.containers.io.MultiBlockContainer;
import xxl.core.cursors.Cursor;
import xxl.core.cursors.Cursors;
import xxl.core.cursors.DecoratorCursor;
import xxl.core.cursors.mappers.Mapper;
import xxl.core.functions.AbstractFunction;
import xxl.core.functions.Function;
import xxl.core.io.converters.ConvertableConverter;
import xxl.core.spatial.KPE;
import xxl.core.spatial.cursors.PlaneSweep;
import xxl.core.spatial.cursors.PlaneSweep.KPEPlaneSweepComparator;
import xxl.core.spatial.cursors.PlaneSweep.PlaneSA;
import xxl.core.spatial.geometries.Geometry2D;
import xxl.core.spatial.geometries.cursors.SpatialJoin;
import xxl.core.spatial.geometries.predicates.Intersects;
import xxl.core.spatial.geometries.predicates.TuplePredicate;
import xxl.core.spatial.rectangles.DoublePointRectangle;
import xxl.core.spatial.rectangles.Rectangle;
import xxl.core.spatial.rectangles.Rectangles;
import xxl.core.util.XXLSystem;

/** An implementation of the spatial-join based on the {@link PlaneSweep}-operator in the filter-step.
 *  <br><br> 
 *  The <code>PlaneSweep</code>-operator sorts the spatial approximations of the geometries, which are wrapped
 *  in {@link KPE}-objects, according to the x-coordinate of the mbr's lower left corner. Then both sequences
 *  of <code>KPE</code>-objects are scanned from left to right and rectangles which intersect in the x-dimension
 *  are tested for intersection in the y-dimension. Intersecting KPE-tuples are returned. <br>
 *  For a detailed explanation see Lars Arge et.al. VLDB 1998.
 *  <br><br>Subsequently the refinement-step accepts those tuples of which the exact geometries also intersect.
 *
 * @see PlaneSweep
 * @see KPE 
 * @see SpatialJoin 
 * @see MultiBlockContainer
 * @see BlockFileContainer
 */
public class PlaneSweepJoinTest {
	
	
	/**
	 * Starts the PlaneSweepJoinTest. If no command line parameters are given the test is run with default-parameters
	 * @param args command line parameters
	 */
	public static void main(String[] args){

		final File input0, input1; 	// the path to the input geometry files (*.geom)
		final int 	buckets, 		// the number of buckets used by the SweepArea
					bucketSize,		// the initial size of a bucket (Integer)
					bufferSize, 	// buffer size used by the external Queue of the sorter
					memSize, 		// the memory available to the used merge-sorter during the open-phase
					finalMemSize,	// the memory available to the merge-sorter during the next-phase
					blockSize;		// the block-size (in Byte) of the geometry container
	
		final boolean 	external, 	// determines whether ot not to use external sorting	
						show; 		// determines whether ot not to show the input-geometries and the join-results

		
		System.out.println("PlaneSweep-Join Test\n");
		
		// process command line arguements
		if(args.length < 8){
			System.out.println("   usage: java "+PlaneSweepJoinTest.class.getCanonicalName()+"  file1 file2 memSize finalMemSize blockSize show external bufferSize" +
							   "\n  with  file1, file2    - the path to the input geometry files (*.geom)"+
							   "\n        numberOfBuckets - the number of buckets used by the SweepArea "+
							   "\n        bucketSize      - the initial size of a bucket (Integer)"+
							   "\n        memSize         - the initial memory available for the MergeSorter (Integer)"+
							   "\n        finalMemSize    - the final memory available for the MergeSorter (Integer)"+
							   "\n        blocksize       - the block-size (in Byte) of the geometry container "+
							   "\n        show            - determines wether or not to show the inputs and join- results " +
							   "\n        external        - determines wether to process the join completely in main memory, or to use external Sorting" +
							   "\n        buffersize      - the buffersize used by the FileQueues of the external MergeSorter" +							   
							   "\n"							   
							   );
						
			System.out.println("No parameters specified ! Using default values :");
	
			String dataPath = XXLSystem.getDataPath(new String[]{"geo"});
			input0 		= new File( dataPath+ File.separator+ "houston_roads.geom");
			input1 		= new File( dataPath+ File.separator+ "houston_hydro.geom");
			buckets 	= 100;
			bucketSize 	= 512;
			memSize 	= 40960;
			finalMemSize= 81920;
			blockSize 	= 64;
			show 		= true;			
			external 	= true;
			bufferSize 	= 4096;
		} else {			
			input0 		= new File(args[0]);
			input1 		= new File(args[1]);
			buckets 	= Integer.parseInt(args[2]);
			bucketSize 	= Integer.parseInt(args[3]);
			memSize 	= Integer.parseInt(args[4]);
			finalMemSize= Integer.parseInt(args[5]);
			blockSize	= Integer.parseInt(args[6]);
			show 		= Boolean.parseBoolean(args[7]);
			external 	= args.length > 8 ? Boolean.parseBoolean(args[8]) : true;
			bufferSize 	= args.length > 9 ? Integer.parseInt(args[9]) : 4096;
			System.out.println("Specified parameters: ");
		}
		
		System.out.println(	"\n\tfile1        = " + input0.getAbsolutePath() +
							"\n\tfile2        = " + input1.getAbsolutePath() +
							"\n\tbuckets      = " + buckets +
							"\n\tbucketSize   = " + bucketSize+
							"\n\tmemsize      = " + memSize +
							"\n\tfinalMemSize = " + finalMemSize +
							"\n\tblockSize    = " + blockSize +	
							"\n\tshow         = " + show +
							"\n\texternal     = " + external +
							"\n\tbufferSize   = " + bufferSize + 
							"\n");
	
		
		final String outPath = XXLSystem.getOutPath();
		
		// the size of a kpe- object in main memory
		final int objectSize = 4*8 + 8; // 2 2d-DoublePoints + Long id
	
		// a container to store geometries in
		MultiBlockContainer container = new MultiBlockContainer(outPath+"/PlaneSweepTest", blockSize);		
		final Container geometryContainer = new ConverterContainer( container, Geometry2DConverter.DEFAULT_INSTANCE);						
														
		// initialize iterators over the two input-files
		Iterator<Geometry2D> relationR = Geometry2DFileIO.read(Geometry2DConverter.DEFAULT_INSTANCE, input0);
		Iterator<Geometry2D> relationS = Geometry2DFileIO.read(Geometry2DConverter.DEFAULT_INSTANCE, input1);
														
		// load the mbr of each input-file and compute the universe, that contains all geometries
		Rectangle uniR = new DoublePointRectangle(2);
		Rectangle uniS = new DoublePointRectangle(2);
		uniR = Rectangles.readSingletonRectangle(new File(input0.getAbsoluteFile()+".universe"),uniR);
		uniS = Rectangles.readSingletonRectangle(new File(input1.getAbsoluteFile()+".universe"),uniS);
		uniR.union(uniS);		
		
		// if the geometries should be shown, create an output-panel
		final VisualOutput outputPanel = show ? new VisualOutput("PlaneSweep", uniR, 700) : null;				
		// and wrap the input-iterators to draw the geometries to the output-panel					
		if(show){
			relationR = new VisualGeometry2DCursor(relationR, outputPanel, Color.DARK_GRAY);
			relationS = new VisualGeometry2DCursor(relationS, outputPanel, Color.GRAY);
		}												
					
		outputPanel.setRepaintImmediately(external);
		
		// create a Sorter to sort the KPE-objects either externally or in main memory
		Function newSorter = external ? Common.getExternalMergeSorter(
											new ConvertableConverter<KPE>(Common.LEAFENTRY_FACTORY), 
											KPEPlaneSweepComparator.DEFAULT_INSTANCE, 
											objectSize, bufferSize, memSize, finalMemSize, outPath) 
									  : Common.getInternalMergeSorter(KPEPlaneSweepComparator.DEFAULT_INSTANCE, memSize, finalMemSize);
		
	
		// the result-tuple of PlaneSweep is a tripel of the
		// form [x,[y,x]] or [[x,y],y] (RPM- method for Duplicate elimination						
		Function<Object, KPE[]> PLANESWEEP_TUPLIFY = new AbstractFunction<Object,KPE[]>(){
			public KPE[] invoke(Object o1, Object o2){
				KPE[] k = null;
				try{ k = (KPE[]) o1;			
				} catch(ClassCastException e){	
					k= (KPE[]) o2;
					KPE tmp = k[1]; k[1] = k[0]; k[0] = tmp;								
					}					              
				return k;
		    }					        					    				       
		};
		
		// initialize the filterstep
		Cursor planeSweep = new PlaneSweep(
								// save geometries in the geometry-container and return the according KPE-objects
								new Mapper( Common.saveGeometry(geometryContainer), relationR),
								new Mapper( Common.saveGeometry(geometryContainer), relationS),
								// provide sorters to sort the KPE-objects
								newSorter, newSorter,
								new PlaneSA(buckets,bucketSize, uniR),
								new PlaneSA(buckets,bucketSize, uniR),
								KPEPlaneSweepComparator.DEFAULT_INSTANCE,
								PLANESWEEP_TUPLIFY // the tuplify- function specified above
							);
 
		// initialize the filterstep
		Cursor<Geometry2D[]> join = 
			new SpatialJoin<KPE[], Geometry2D[]>(				
				planeSweep, // filter-step
				// load the geometries of a given intermediate result
				new AbstractFunction<KPE[], Geometry2D[]>(){
					Function<KPE, Geometry2D > getGeometry = Common.getGeometry(geometryContainer);
					public Geometry2D[] invoke(KPE[] o){						
						return new Geometry2D[]{
								getGeometry.invoke(o[0]),
								getGeometry.invoke(o[1])									
						};
					}
				},
				// refinement-predicate
				new TuplePredicate(Intersects.DEFAULT_INSTANCE)
			);			
				
//		if results should be draw to the outputpanel decorate the join with an additional drawing-routine
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
		System.out.println("\nPlaneSweep- Join returned " +Cursors.count( join ) +" results in " +(System.currentTimeMillis()-start)+"ms");	
		join.close();
	  
		if(show) outputPanel.repaint();	
		
		// release all resources
		container.delete(); 
	}
}
