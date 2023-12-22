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
import xxl.core.spatial.cursors.IndexedNestedLoopsJoin;
import xxl.core.spatial.geometries.Geometry2D;
import xxl.core.spatial.geometries.cursors.SpatialJoin;
import xxl.core.spatial.geometries.predicates.Intersects;
import xxl.core.spatial.geometries.predicates.TuplePredicate;
import xxl.core.spatial.rectangles.DoublePointRectangle;
import xxl.core.spatial.rectangles.Rectangle;
import xxl.core.spatial.rectangles.Rectangles;
import xxl.core.util.XXLSystem;

/** A simple usage example for performing an Spatial-Join based on an {@IndexedNestedLoopsJoin} as filter-step.
 *  The geometries to be indexed are stored in a {@link MultiBlockContainer} since there size is variable and a
 *  <code>MultiBlockContainer</code> reserves as many blocks for a geometry-object as is needed.
 *  <br>
 *  The index-entries, so called {@link KPE}-objects, are stored in a {@link BlockFileContainer} since they have fixed size.
 *  This container also is backed by a LRU-Buffer to reduce data-transfer from secondary to primary-memory.<br>
 *  A <code>KPE</code>-objects contains some spatial information, i.e. the minimal bounding rectangle of the geometrie it represents
 *  and a reference to the geometrie in the geometrie-container.
 *  <br><br>The spatial-join is processed in two steps: first a filter-step on the bounding rectangles is performed to
 *  already reject a huge number of candidates by simple computations. In this particular case the filter-step is performed
 *  by wrapping the query-objects into <code>KPE</code>-objects, too, and by running the <code>IndexedNestedLoopsJoin</code>-operator.
 *  The results of this operator are <code>KPE</code>-tuples of which the rectangles contained overlap. For these candidates the according
 *  geometries are loaded in the refinement-step and are tested for intersection. Only those tuples of which the exact geometries
 *  overlap are reported as join-results.  
 *  <br><br>
 *  Since the functions needed to store geometries in containers, to build indexes on geometry-relations, etc. are the same
 *  for most of the examples in this package, these functions are implemented in the class {@link Common}.
 *   
 * @see KPE 
 * @see SpatialJoin
 * @see IndexedNestedLoopsJoin
 * @see MultiBlockContainer
 * @see BlockFileContainer
 * @see LRUBuffer
 */
public class IndexedNestedLoopsJoinTest {					
		
	
	/**
	 * Starts the IndexedNestedLoopsJoinTest. If no command line parameters are given the test is run with default-parameters
	 * @param args command line parameters
	 */
	public static void main(String[] args){
				
		final File 	input0, 	 // the *.geom-file containing the geometries used to query the R-Tree
					input1;		 // the *.geom-file	containing the geometries to build the R-Tree upon
		final int 	minCapacity, // the minimal capacity of a tree-node 
					maxCapacity, // the maximum capacity of a tree-node
					bufferSize,  // the capacity of the lru-buffer, i.e. the number of nodes to keep in the buffer
					geoBlockSize;// the block-size (in Byte) of the geometry container. Since the size of Geometry-objects is variable, we
								 // need to store them in a MultiBlockContainer, so that a Geometry is serialized in as many blocks as needed
		
		final boolean bulk,		 // determines whether or not to bulk-load the R-Tree 
					  show;		 // determines whether ot not to show the input-geometries and the join-results
		final Comparator<KPE> bulkLoadComparator;	// the comparator used to sort the inputs of the bulk-load-operator
		
		System.out.println("IndexedNestedLoopsJoinTest\n");
		
		if(args.length < 9){
			System.out.println("   usage: java "+ IndexedNestedLoopsJoin.class.getCanonicalName()+" file1 file2 minCapacity maxCapacity bufferSize geoBlockSize bulk show" +
							   "\n  with  file1           - the *.geom-file containing the geometries used to query the R-Tree"+
							   "\n        file2           - the *.geom-file	containing the geometries to build the R-Tree upon"+							   
							   "\n        minCapacity     - the minimal capacity of a tree-node "+
							   "\n        maxCapacity     - the maximum capacity of a tree-node"+
							   "\n        buffersize      - the capacity of the lru-buffer, i.e. the number of nodes to keep in the buffer " +
							   "\n        geoBlockSize    - the block-size (in Byte) of the geometry container" +
							   "\n        bulk            - determines whether or not to bulk-load the R-Tree " +
							   "\n        show            - determines whether or not to show the inputs and join- results" +
							   "\n"							   
							   );
						
			System.out.println("No parameters specified ! Using default values :");
	
			String dataPath = XXLSystem.getDataPath(new String[]{"geo"});
			input0 		= new File( dataPath+ File.separator+ "houston_roads.geom");
			input1 		= new File( dataPath+ File.separator+ "houston_hydro.geom");
			minCapacity = 20;
			maxCapacity = 50;
			bulk        = false;			
			bufferSize 	= 4096;
			geoBlockSize= 64;
			show 		= true;
		} else {			
			input0 		= new File(args[0]);
			input1 		= new File(args[1]);			
			minCapacity = Integer.parseInt(args[3]);
			maxCapacity = Integer.parseInt(args[4]);
			bufferSize 	= Integer.parseInt(args[5]);
			geoBlockSize= Integer.parseInt(args[6]);
			bulk        = Boolean.parseBoolean(args[7]);
			show 		= Boolean.parseBoolean(args[8]);
			
			System.out.println("Specified parameters: ");
		}
		
		System.out.println(	"\n\tfile1        = " + input0.getAbsolutePath() +
							"\n\tfile2        = " + input1.getAbsolutePath() +							
							"\n\tminCapacity  = " + minCapacity +
							"\n\tmaxCapacity  = " + maxCapacity+
							"\n\tbufferSize   = " + bufferSize +
							"\n\tgeoBlockSize = " + geoBlockSize +
							"\n\tbulkloading  = " + bulk+
							"\n\tshow         = " + show +								
							"\n");	

		// the path where the the containers for the tree's nodes and the geometries are created temporarily
		final String outPath = XXLSystem.getOutPath();					
	
		// the size of a index-entry: 1 DoublePointRectangle (2x 2d-DoublePoints = 2x2x8 B) + Container-Id (1Long = 8 B) id + ?
		final int objectSize = 4*8 + 8 + 6;  
		
		// the size of a block corresponds to the maximal size of tree-node
		final int blockSize = maxCapacity * objectSize;			
		
		// the MultiBlockContainer that stores the geometries 
		MultiBlockContainer fileContainer = new MultiBlockContainer(outPath+"/IndexedNestedLoopsTest", geoBlockSize);					
		final Container geometryContainer =	new ConverterContainer( fileContainer, Geometry2DConverter.DEFAULT_INSTANCE);
					
		// the BlockFileContainer that stores the tree's nodes
		BlockFileContainer rTreeFileContainer = new BlockFileContainer(outPath + "/INL_RTree", blockSize);																		
					 
		// Initialize an Iterator over both inputs				 
		Iterator<Geometry2D> relationR = Geometry2DFileIO.read(Geometry2DConverter.DEFAULT_INSTANCE, input0);
		Iterator<Geometry2D> relationS = Geometry2DFileIO.read(Geometry2DConverter.DEFAULT_INSTANCE, input1);
													
		// Read the minimal bounding rectangles of both inputs and compute the union to get the mbr of all geometries to process
		Rectangle uniR = new DoublePointRectangle(2);
		Rectangle uniS = new DoublePointRectangle(2);
		uniR = Rectangles.readSingletonRectangle(new File(input0.getAbsoluteFile()+".universe"),uniR);
		uniS = Rectangles.readSingletonRectangle(new File(input1.getAbsoluteFile()+".universe"),uniS);
		uniR.union(uniS);		
		
		// Create an output-panel if neccessary
		final VisualOutput outputPanel = show ? new VisualOutput("IndexedNestedLoopsJoin", uniR, 700) : null;				
		// and let the input-geometries be drawn to that panel
		if(show){
			relationR = new VisualGeometry2DCursor(relationR, outputPanel, Color.DARK_GRAY);
			relationS = new VisualGeometry2DCursor(relationS, outputPanel, Color.GRAY);
		}												
	
		// Create the RTree
		final RTree indexS= new RTree();
		
		// add a LRU-Buffer to the tree-node container 
		Container rTreeContainer = new BufferedContainer(
							new ConverterContainer(
									rTreeFileContainer, 			
									// specifies how to convert leafs and internal nodes
									indexS.nodeConverter(
										new ConvertableConverter<KPE>(Common.LEAFENTRY_FACTORY),
										indexS.indexEntryConverter(	new ConvertableConverter<Rectangle>( Common.DESCRIPTOR_FACTORY ) )
									)
								)
							,
							new LRUBuffer(bufferSize)									
						);
			
		// initialize the R-Tree
		indexS.initialize(
				Common.GET_DESCRIPTOR,
				rTreeContainer,
				minCapacity,
				maxCapacity				
			);

		// this function adds a geometrie to the geometry-container and returns a KPE-object
		// with the geometry's rectangle as a key and its container-id as a pointer to the geometry
		Function saveGeometry = Common.saveGeometry(geometryContainer);
		
		// insert the geometries of the second input into the tree by bulkoading
		// the tree...
		if(bulk){
			bulkLoadComparator = Common.COMPARE_HILBERT(uniR, 32);
			Iterator<KPE> it = new MergeSorter( 
									new Mapper( saveGeometry, relationS ),
									bulkLoadComparator,
									12,
									4*4096,
									4*4096
								);
			
			new SortBasedBulkLoading(indexS, it, indexS.determineContainer);
		// ...or by inserting one geometry after the other
		} else {
			while(relationS.hasNext())									
				indexS.insert( saveGeometry.invoke(relationS.next()) );
		}
				
		
		// this function simply maps a geomtry of the first input (the "query-objects") to a KPE-object
		// with the geometries mbr as a key and the geometry itself as a pointer.
		// We don't need to serialize these query-objects, since each element is read only ones from the
		// input, is processed directly by than isn't needed any longer
		final Function<Geometry2D, KPE> mapGeometryToKPE = new AbstractFunction<Geometry2D, KPE>(){
			public KPE invoke(Geometry2D g){
				return new KPE(g.getMBR(), g, null);
			}
		};
	
		// The join-operator:
		//  We perform a Indexed-Nested-Loops-Join on the MBRs of the input-relations and refine 
		//	the join-result by testing candidate-tuples for intersection of their exact geometries
		//
		Cursor<Geometry2D[]> join = new SpatialJoin<KPE[], Geometry2D[]>(
				// Filter-Step
				new IndexedNestedLoopsJoin(
						//
						new Mapper( mapGeometryToKPE, relationR),		    			
						indexS),
				// map intermediate tuples to candidate-tuples
				new AbstractFunction<KPE[], Geometry2D[]>(){
					Function<KPE, Geometry2D> getGeometry = Common.getGeometry(geometryContainer);
					
					public Geometry2D[] invoke(KPE[] k){
						return new Geometry2D[] {
									(Geometry2D) k[0].getID(), 
									getGeometry.invoke(k[1])
								};
					}								
				},
				// Refinement- Predicate
	    		new TuplePredicate(Intersects.DEFAULT_INSTANCE)		    		    		
	    		);
		
		// if results should be draw to the outputpanel decorate the join with an additional drawing-routine
		if(show) join = new DecoratorCursor<Geometry2D[]>(join){
		  	Color[] colors = new Color[]{Color.GREEN, Color.RED};
			
		  	public Geometry2D[] next(){	    						
			  	outputPanel.draw(super.peek(), colors);
			  	return super.next();			    		
		  	}
	  	};					    		
    
		// let's take the current time to see how much time join-processing takes 
		long start = System.currentTimeMillis();		
	
		// and count the number of results returned by the join
	  	System.out.println("\nIndexed- Nested- Loops Join returned " +Cursors.count( join ) +" results in " +(System.currentTimeMillis()-start)+"ms");	
	  	join.close();

	  	if(show) outputPanel.repaint();		 
	  	
	  	// release all resources
	  	fileContainer.delete();
	  	rTreeFileContainer.delete();
	}
}
