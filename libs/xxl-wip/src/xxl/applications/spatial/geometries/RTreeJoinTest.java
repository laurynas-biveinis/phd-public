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
import xxl.core.collections.containers.CounterContainer;
import xxl.core.collections.containers.io.BlockFileContainer;
import xxl.core.collections.containers.io.BufferedContainer;
import xxl.core.collections.containers.io.ConverterContainer;
import xxl.core.collections.containers.io.MultiBlockContainer;
import xxl.core.cursors.Cursor;
import xxl.core.cursors.Cursors;
import xxl.core.cursors.DecoratorCursor;
import xxl.core.cursors.mappers.Mapper;
import xxl.core.cursors.sorters.MergeSorter;
import xxl.core.functions.AbstractFunction;
import xxl.core.functions.Function;
import xxl.core.indexStructures.RTree;
import xxl.core.indexStructures.SortBasedBulkLoading;
import xxl.core.io.LRUBuffer;
import xxl.core.io.converters.ConvertableConverter;
import xxl.core.spatial.KPE;
import xxl.core.spatial.cursors.RTreeJoin;
import xxl.core.spatial.geometries.Geometry2D;
import xxl.core.spatial.geometries.cursors.SpatialJoin;
import xxl.core.spatial.geometries.predicates.Intersects;
import xxl.core.spatial.geometries.predicates.TuplePredicate;
import xxl.core.spatial.rectangles.DoublePointRectangle;
import xxl.core.spatial.rectangles.Rectangle;
import xxl.core.spatial.rectangles.Rectangles;
import xxl.core.util.XXLSystem;

/** A simple usage example for performing an Spatial-Join based on an {@RTreeJoin} as filter-step.
 *  The geometries of both input-relations are stored in a {@link MultiBlockContainer} and an index is created over each
 *  relation.
 *  <br><br>
 *  The spatial-join is processed in two steps: first a filter-step on the bounding rectangles is performed to
 *  already reject a huge number of candidates by simple computations. In this particular case the filter-step is performed
 *  by simultanouesly traversing both R-Tree-indexes by means of the {@link RTreeJoin}-operator.
 *  The results of this operator are <code>KPE</code>-tuples of which the rectangles contained overlap. For these candidates the according
 *  geometries are loaded in the refinement-step and are tested for intersection. Only those tuples of which the exact geometries
 *  overlap are reported as join-results.  
 *   
 * @see KPE 
 * @see SpatialJoin
 * @see RTreeJoin
 * @see MultiBlockContainer
 * @see BlockFileContainer
 * @see LRUBuffer
 */
public class RTreeJoinTest {
		
		
	/**
	 * Starts the RTreeJoinTest. If no command line parameters are given the test is run with default-parameters
	 * @param args command line parameters
	 */
	public static void main(String[] args){
		final File input0, input1;	 // the path to the input geometry-files (*.geom)	
		
		final int 	minCapacity1, minCapacity2, maxCapacity1, maxCapacity2, // the minimal and maximal node-capacities of each tree 
					bufferSize;		 // the number of nodes to keep in the LRUBuffer of each tree
		final boolean 	bulk,		 // determines whether or not to bulk-load the R-Tree 
		  				show;		 // determines whether ot not to show the input-geometries and the join-results
		final Comparator<KPE> bulkLoadComparator;	// the comparator used to sort the inputs of the bulk-load-operator

		System.out.println("RTree-Join Test\n");
		
		if(args.length < 9){
			System.out.println("   usage: java "+ RTreeJoin.class.getCanonicalName()+" file1 file2 minCapacity1 maxCapacity1 minCapacity2 maxCapacity2 bufferSize bulk show" +
							   "\n  with  file1, file2    - the path to the input geometry-files (*.geom)"+							   
							   "\n        minCapacity1,2  - the minimal node-capacity of each tree"+
							   "\n        maxCapacity1,2  - the maximal node-capacity of each tree"+
							   "\n		  bufferSize      - the number of nodes to keep in the LRUBuffer of each tree"+
							   "\n        bulk            - determines wether or not to bulkload the RTree" +							   
							   "\n        show            - determines wether or not to show the inputs and join- results" +
							   "\n"							   
							   );
						
			System.out.println("No parameters specified ! Using default values :");
	
			String dataPath = XXLSystem.getDataPath(new String[]{"geo"});
			input0 		 = new File( dataPath+ File.separator+ "houston_roads.geom");
			input1 		 = new File( dataPath+ File.separator+ "houston_hydro.geom");
			minCapacity1 = 20;
			maxCapacity1 = 50;
			minCapacity2 = 60;
			maxCapacity2 = 130;
			bufferSize   = 64;
			bulk         = true;			
			show 		 = true;
		} else {			
			input0 			= new File(args[0]);
			input1 			= new File(args[1]);			
			minCapacity1 	= Integer.parseInt(args[2]);
			maxCapacity1 	= Integer.parseInt(args[3]);
			minCapacity2 	= Integer.parseInt(args[4]);
			maxCapacity2	= Integer.parseInt(args[5]); 
			bufferSize      = Integer.parseInt(args[6]);
			bulk        	= Boolean.parseBoolean(args[7]);
			show 			= Boolean.parseBoolean(args[8]);
			System.out.println("Specified parameters: ");
		}
		
		System.out.println(	"\n\tfile1         = " + input0.getAbsolutePath() +
							"\n\tfile2         = " + input1.getAbsolutePath() +							
							"\n\tminCapacity1  = " + minCapacity1 +
							"\n\tmaxCapacity1  = " + maxCapacity1 +
							"\n\tminCapacity2  = " + minCapacity2 +
							"\n\tmaxCapacity2  = " + maxCapacity2 +
							"\n\tbufferSize    = " + bufferSize +
							"\n\tbulkloading   = " + bulk+							
							"\n\tshow          = " + show +								
							"\n");
	
		
		final String outPath = XXLSystem.getOutPath();					
					
		// count accesses to the geometry- and tree-node- containers
		final CounterContainer geometryContainer, rTreeContainer1, rTreeContainer2;		
				
		// Build an index on both relations
		final RTree indexR = new RTree();
		final RTree indexS = new RTree();				
		
		System.out.print("Building RTrees... ");
		
		// let's see how long it takes
		long start = System.currentTimeMillis();
		
		final int blockSize1 = maxCapacity1*(40+4+2);
		final int blockSize2 = maxCapacity2*(40+4+2);		
		
		// create a container for each tree 
		final BlockFileContainer kpeFileContainer1 = new BlockFileContainer(outPath+"/KPEs1_" , blockSize1);						
		final CounterContainer kpeCounter1 = new CounterContainer(kpeFileContainer1);
		rTreeContainer1 = new CounterContainer(
				 new ConverterContainer(
						 // add a LRU-Buffer to the tree-node container
						 new BufferedContainer(
								kpeCounter1,
								new LRUBuffer(bufferSize, bufferSize * blockSize1)
						),
						indexR.nodeConverter(
							new ConvertableConverter<KPE>(Common.LEAFENTRY_FACTORY), 
							indexR.indexEntryConverter(new ConvertableConverter<Rectangle>(Common.DESCRIPTOR_FACTORY))
						)
					)							 
				);

		final BlockFileContainer kpeFileContainer2 = new BlockFileContainer(outPath+"/KPEs2_" , blockSize2);									
		final CounterContainer kpeCounter2 = new CounterContainer(kpeFileContainer2);
		rTreeContainer2 = new CounterContainer(
				new ConverterContainer(
						// add a LRU-Buffer to the tree-node container 
						new BufferedContainer(
							kpeCounter2,
							new LRUBuffer(bufferSize, bufferSize * blockSize2)
													),
					indexS.nodeConverter(
						new ConvertableConverter<KPE>(Common.LEAFENTRY_FACTORY), 
						indexS.indexEntryConverter(new ConvertableConverter<Rectangle>(Common.DESCRIPTOR_FACTORY)))
						)
				);

		// create a container to store the geometries
		final MultiBlockContainer geometryFileContainer = new MultiBlockContainer(outPath+"/Geometries_", 256); 						
		geometryContainer = new CounterContainer( 
									new ConverterContainer(
										geometryFileContainer,
										Geometry2DConverter.DEFAULT_INSTANCE
									)
								);
			
		// initialize both r-trees
		indexR.initialize( Common.GET_DESCRIPTOR, rTreeContainer1, minCapacity1, maxCapacity1);		
		indexS.initialize( Common.GET_DESCRIPTOR, rTreeContainer2, minCapacity2, maxCapacity2);
	
		// initialize interators over both input-files
		Iterator<Geometry2D> relationR = Geometry2DFileIO.read(Geometry2DConverter.DEFAULT_INSTANCE, input0);
		Iterator<Geometry2D> relationS = Geometry2DFileIO.read(Geometry2DConverter.DEFAULT_INSTANCE, input1);
		
		
		// load the mbr of each input-file and compute the universe, that contains all geometries
		Rectangle uniR = new DoublePointRectangle(2);
		Rectangle uniS = new DoublePointRectangle(2);
		uniR = Rectangles.readSingletonRectangle(new File(input0.getAbsoluteFile()+".universe"),uniR);
		uniS = Rectangles.readSingletonRectangle(new File(input1.getAbsoluteFile()+".universe"),uniS);
		uniR.union(uniS);		
		
		// if the geometries should be shown, create an output-panel
		final VisualOutput outputPanel = show ? new VisualOutput("RTreeJoin", uniR, 700) : null;				
		// and wrap the input-iterators to draw the geometries to the output-panel
		outputPanel.setRepaintImmediately(true);
		if(show){
			relationR = new VisualGeometry2DCursor(relationR, outputPanel, Color.DARK_GRAY);
			relationS = new VisualGeometry2DCursor(relationS, outputPanel, Color.GRAY);
		}	
						
		// insert the geometries into the trees by bulkoading
		// the trees...
		if(bulk){
			bulkLoadComparator = Common.COMPARE_HILBERT(uniR, 32);
			Cursor<KPE> c0 = new MergeSorter<KPE>(
						new Mapper<Geometry2D, KPE>(
								Common.saveGeometry(geometryContainer),
								relationR
								),bulkLoadComparator,46,4*4096,4*4096);
			new SortBasedBulkLoading(indexR, c0, indexR.determineContainer);
	
			Cursor<KPE> c1 = new MergeSorter<KPE>(
					new Mapper<Geometry2D, KPE>(
							Common.saveGeometry(geometryContainer),
							relationS
							),Common.COMPARE_PEANO(uniR,32),46,4*4096,4*4096);
			new SortBasedBulkLoading(indexS, c1, indexS.determineContainer);			
		} else {
			// ...or by inserting one geometry after the other
			Function saveGeometry = Common.saveGeometry(geometryContainer);
			
			while(relationR.hasNext()) indexR.insert( saveGeometry.invoke(relationR.next()) );
			while(relationS.hasNext()) indexS.insert( saveGeometry.invoke(relationS.next()) );
			
			outputPanel.repaint();
		}
					
		// show some information on the number of accesses to the containers
		System.out.println("done in "+(System.currentTimeMillis()-start)+"ms\n");
		System.out.println("R: number of data- entries="+Cursors.count(indexR.query())+" depth="+indexR.height());
		System.out.println("S: number of data- entries="+Cursors.count(indexS.query())+" depth="+indexS.height());
		System.out.println();
		System.out.println("Access to rTree container1: "+rTreeContainer1);		
		System.out.println("Access to rTree container2: "+rTreeContainer2);		
		System.out.println("Access to geometry container: "+geometryContainer);
		System.out.println("Access to kpe1 container: "+kpeCounter1);
		System.out.println("Access to kpe2 container: "+kpeCounter2);
		System.out.println();
		
		// reset all counters
		rTreeContainer1.reset();
		rTreeContainer2.reset();
		geometryContainer.reset();
		kpeCounter1.reset();
		kpeCounter2.reset();
			    		
		// initialize the spatial-join-operator
		Cursor<Geometry2D[]> join = new SpatialJoin<KPE[], Geometry2D[]>(
											new RTreeJoin(indexR, indexS),
											new AbstractFunction<KPE[],Geometry2D[]>(){
												Function<KPE,Geometry2D> getGeometry = Common.getGeometry(geometryContainer);
												public Geometry2D[] invoke(KPE[] k){
													return new Geometry2D[]{
															getGeometry.invoke(k[0]),
															getGeometry.invoke(k[1])
														};
												}												
											},											
								    		new TuplePredicate(Intersects.DEFAULT_INSTANCE)		    		    										    	
									); 				

//		 if results should be draw to the outputpanel decorate the join with an additional drawing-routine
		if(show) join = new DecoratorCursor<Geometry2D[]>(join){
		  	Color[] colors = new Color[]{Color.GREEN, Color.RED};
			
		  	public Geometry2D[] next(){	    						
			  	outputPanel.draw(super.peek(), colors);
			  	return super.next();			    		
		  	}
	  	};					    		
    
		// let's take the current time to see how much time join-processing takes 
		start = System.currentTimeMillis();		
	
		// and count the number of results returned by the join
	  	System.out.println("\nIndexed- Nested- Loops Join returned " +Cursors.count( join ) +" results in " +(System.currentTimeMillis()-start)+"ms");	
	  	join.close();

	  	if(show) outputPanel.repaint();	
				
	  	// once again show the number of accesses to the containers
		System.out.println("Access to rTree container1: "+rTreeContainer1);		
		System.out.println("Access to rTree container2: "+rTreeContainer2);		
		System.out.println("Access to geometry container: "+geometryContainer);
		System.out.println("Access to kpe1 container: "+kpeCounter1);
		System.out.println("Access to kpe2 container: "+kpeCounter2);
					
		// release all resources
		kpeFileContainer1.delete();
		kpeFileContainer2.delete();			
		geometryFileContainer.delete();			   
	}
}
