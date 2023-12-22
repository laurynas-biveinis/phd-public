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
import java.util.Vector;

import xxl.connectivity.jts.io.Geometry2DConverter;
import xxl.connectivity.jts.io.Geometry2DFileIO;
import xxl.connectivity.jts.visual.VisualGeometry2DCursor;
import xxl.connectivity.jts.visual.VisualOutput;
import xxl.core.cursors.Cursor;
import xxl.core.cursors.Cursors;
import xxl.core.cursors.DecoratorCursor;
import xxl.core.cursors.joins.NestedLoopsJoin;
import xxl.core.functions.AbstractFunction;
import xxl.core.functions.Function;
import xxl.core.spatial.geometries.Geometry2D;
import xxl.core.spatial.geometries.predicates.Intersects;
import xxl.core.spatial.rectangles.DoublePointRectangle;
import xxl.core.spatial.rectangles.Rectangle;
import xxl.core.spatial.rectangles.Rectangles;
import xxl.core.util.XXLSystem;

/** This class implements a simple NestedLoopsJoin-operator: for each geometry of the first geometry-relation
 *  each geometry of the other relation is tested for intersection. The join can be processed in main-memory
 *  or it can be processed by continously reading the geometries from disk. The latter is extremely slow for 
 *  huge relations, whereas the first solution isn't suitable either, because very large relations usually don't
 *  fit in memory.
 *  
 * @see NestedLoopsJoin
 */
public class NaiveNestedLoopsJoinTest {

	/** Returns a Function to initialize an iterator over an geometry-file. 
	 * 
	 * @param file the geometry-file to read 
	 * @param internal determines whether or not the geometries should be loaded in main-memory completely 
	 * @param bufferSize specifies the size of the buffer used for reading the file
	 * @return a Function to initialize an iterator over an geometry-file.
	 */
	public static Function<Object, Iterator<Geometry2D>> getInputIterator(final File file, final boolean internal, final int bufferSize){
		return new AbstractFunction<Object, Iterator<Geometry2D>>(){				
			Vector<Geometry2D> S = null;
	
			public Iterator<Geometry2D> invoke(){
					if(internal){
						if(S== null){
							S= new Vector<Geometry2D>();
							Iterator<Geometry2D> c = Geometry2DFileIO.read(Geometry2DConverter.DEFAULT_INSTANCE, file, bufferSize);
							while(c.hasNext()) S.add(c.next());
						}
						return S.iterator();
					}
					return Geometry2DFileIO.read(Geometry2DConverter.DEFAULT_INSTANCE, file, bufferSize);
				}
			};
	}
	
	
	
	/**
	 * Starts the NaiveNestedLoopsJoinTest. If no command line parameters are given the test is run with default-parameters
	 * @param args command line parameters
	 */
	public static void main(String[] args){
		
		final File input0, input1; 		// the path to the input geometry files (*.geom)
		final int bufferSize;			// determines whether to load the inputdate into main memory, or to read it from external storage continously
		final boolean external, 		// the size of the read- buffer used in the external- case
					  show;				// determines whether or not to show the inputs and join- results

		System.out.println("Naive Nested- Loops- Join\n");

		if(args.length < 4){
			System.out.println(  "usage: java "+NaiveNestedLoopsJoinTest.class.getCanonicalName()+" file1 file2 external bufferSize [show]" +
							   "\n  with  file1, file2    - the path to the input geometry files (*.geom)"+
							   "\n        external        - determines whether to load the inputdate into main memory, or to read it from external storage continously" +
							   "\n        bufferSize      - the size of the read- buffer used in the external- case" +
							   "\n        show            - determines whether or not to show the inputs and join- results" +
							   "\n"							   
							   );			
			
			System.out.println("No parameters specified ! Using default values :");

			String dataPath = XXLSystem.getDataPath(new String[]{"geo"});
			input0 		= new File( dataPath+ File.separator+ "houston_roads.geom");
			input1 		= new File( dataPath+ File.separator+ "houston_hydro.geom");
			external 	= false;
			bufferSize  = 4096; 
			show 		= true;			
		} else {			
			input0 		= new File(args[0]);
			input1 		= new File(args[1]);
			external 	= args.length > 2 ? Boolean.parseBoolean(args[2]) : true;
			bufferSize 	= args.length > 3 ? Integer.parseInt(args[3]) : 4096;
			show 		= args.length > 4 ? Boolean.parseBoolean(args[4]) : false;
			
			System.out.println("Specified parameters: ");
		}
		
		System.out.println(	"\n\tfile1        = " + input0.getAbsolutePath() +
							"\n\tfile2        = " + input1.getAbsolutePath() +
							"\n\tshow         = " + show +
							"\n\texternal     = " + external +
							"\n\tbufferSize   = " + bufferSize + 
							"\n");

		// initialize Iterators on both relations
		Function<Object, Iterator<Geometry2D>> getR = getInputIterator(input0, external, bufferSize);
		Function<Object, Iterator<Geometry2D>> getS = getInputIterator(input1, external, bufferSize);

		System.out.println("File 1 contains "+Cursors.count(getR.invoke())+" geometries");
		System.out.println("File 2 contains "+Cursors.count(getS.invoke())+" geometries");

		// if geometries should be drawn create an output-panel and draw the geometries of both relations
		final VisualOutput outputPanel;
		if(show){
			// Read the universe containing all input geometries. This is needed for 
			Rectangle uniR = new DoublePointRectangle(2);
			Rectangle uniS = new DoublePointRectangle(2);
			uniR = Rectangles.readSingletonRectangle(new File(input0.getAbsoluteFile()+".universe"),uniR);
			uniS = Rectangles.readSingletonRectangle(new File(input1.getAbsoluteFile()+".universe"),uniS);
			uniR.union(uniS);
		
			outputPanel = new VisualOutput("NaiveNestedLoopsJoin",uniR, 700);
												
			Cursors.consume( new VisualGeometry2DCursor( getR.invoke(),	outputPanel, Color.DARK_GRAY ));						
			Cursors.consume( new VisualGeometry2DCursor( getS.invoke(),	outputPanel, Color.GRAY ));						
	
			outputPanel.push();	
			outputPanel.repaint();			
		} else outputPanel = null;
							   		
		// initialize the join-operator
		
	    Cursor<Geometry2D[]> join = 
	    	new NestedLoopsJoin<Geometry2D, Geometry2D[]>(
	    			getR.invoke(), 	// first iterator
	    			getS.invoke(),	// second iterator
	    			getS,			// continously reopens the second iterator
	    			Intersects.DEFAULT_INSTANCE,	// the join-predicate	    			
	    			new AbstractFunction<Geometry2D, Geometry2D[]>(){ // a tuplify-function
	    				public Geometry2D[] invoke(Geometry2D g0, Geometry2D g1){
	    					return new Geometry2D[] {g0,g1};
	    				}
	    			},
	    			NestedLoopsJoin.Type.THETA_JOIN
	    		);
	
	    // if results should be drawn decorate the join-operator with a draw-method
	    if(show) join = new DecoratorCursor<Geometry2D[]>(join){
	    					Color[] colors = new Color[]{Color.GREEN, Color.RED};
	    					
	    					public Geometry2D[] next(){	    						
	    						outputPanel.draw(super.peek(), colors);
			    				return super.next();			    		
	    					}
	    				};					    		
			    	    
	    // take the current time to see how much time join-processing takes
		long start = System.currentTimeMillis();	    				
		// print the number of the results and the time elapsed to the console 
	    System.out.println("\nNaive Nested-Loops- Join returned " 
	    						+Cursors.count( join ) +" results in " 
	    							+(System.currentTimeMillis()-start)+"ms");			
	    join.close();
	    if(show) outputPanel.repaint();	    	   
	}
}
