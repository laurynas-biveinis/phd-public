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
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Properties;

import xxl.connectivity.jts.Geometry2DFactory;
import xxl.connectivity.jts.Point2DAdapter;
import xxl.connectivity.jts.io.Geometry2DConverter;
import xxl.connectivity.jts.io.Geometry2DFileIO;
import xxl.connectivity.jts.visual.VisualGeometry2DCursor;
import xxl.connectivity.jts.visual.VisualOutput;
import xxl.core.collections.containers.Container;
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
import xxl.core.cursors.sources.SingleObjectCursor;
import xxl.core.functions.AbstractFunction;
import xxl.core.functions.Constant;
import xxl.core.functions.Function;
import xxl.core.indexStructures.GreenesRTree;
import xxl.core.indexStructures.LinearRTree;
import xxl.core.indexStructures.QuadraticRTree;
import xxl.core.indexStructures.RTree;
import xxl.core.indexStructures.SortBasedBulkLoading;
import xxl.core.indexStructures.XTree;
import xxl.core.io.Convertable;
import xxl.core.io.FilesystemOperations;
import xxl.core.io.LRUBuffer;
import xxl.core.io.LogFilesystemOperations;
import xxl.core.io.XXLFilesystem;
import xxl.core.io.converters.ConvertableConverter;
import xxl.core.io.converters.LongConverter;
import xxl.core.relational.metaData.ColumnMetaDataResultSetMetaData;
import xxl.core.relational.metaData.StoredColumnMetaData;
import xxl.core.spatial.KPE;
import xxl.core.spatial.geometries.Geometry2D;
import xxl.core.spatial.geometries.cursors.DistanceQuery;
import xxl.core.spatial.geometries.cursors.KNearestNeighborQuery;
import xxl.core.spatial.geometries.cursors.NearestNeighborQuery;
import xxl.core.spatial.geometries.cursors.RegionQuery;
import xxl.core.spatial.rectangles.DoublePointRectangle;
import xxl.core.spatial.rectangles.Rectangle;
import xxl.core.spatial.rectangles.Rectangles;
import xxl.core.util.Arrays;
import xxl.core.util.reflect.TestFramework;

/**
 * 
 *
 */
public class RTreeQueriesTest {
	
	/** Description for {@link #filenameTree}.
	 */
	public static final String filenameTreeDescription = "name of the file used for the tree";
	
	/** Name of the file used for the tree.
	 */
	public static String filenameTree = "houston_roads.geom";
	
	/** Description for {@link #filenameQueries}.
	 */
	public static final String filenameQueriesDescription = "name of the file that contains the queries";
	
	/** Name of the file that contains the queries.
	 */
	public static String filenameQueries = "houston_hydro.geom";
		
	/** Description for {@link #blockSize}.
	 */
	public static final String blockSizeDescription = "size of a block in bytes";
	
	/** Size of a block in bytes.
	 */
	public static int blockSize = 1536;
		
	/** Description for {@link #minMaxFactor}.
	 */
	public static final String minMaxFactorDescription = "factor which the minimum capacity of nodes is smaller than the maximum capacity";
	
	/** Factor which the minimum capacity of nodes is smaller than the maximum capacity.
	 */
	public static double minMaxFactor = 1.0/3.0;
	
	/** Description for {@link #tree}.
	 */
	public static final String treeDescription = "Linear, Quadratic, Greene, R*, X";
	
	/** Linear, Quadratic, Greene, R*, X.
	 */
	public static String tree = "Greene";

	/** Description for {@link #insertType}.
	 */
	public static final String insertTypeDescription = "type of insertion: tuple, bulk, bulk_xsort, bulk_peano, bulk_hilbert";
	
	/** Type of insertion: tuple, bulk, bulk_xsort, bulk_peano, bulk_hilbert.
	 */
	public static String insertType = "tuple";
	
	/** Description for {@link #bufferSize}.
	 */
	public static final String bufferSizeDescription = "buffersize (number of node-objects)";
	
	/** Buffersize (number of node-objects).
	 */
	public static int bufferSize = 100;
	
	/** Description for {@link #targetLevel}.
	 */
	public static final String targetLevelDescription = "number of level for queries";
	
	/** Number of level for queries.
	 */
	public static int targetLevel = 0;
	
	/** Description for {@link #unbufferedWrite}.
	 */
	public static final String unbufferedWriteDescription = "unbuffered write operations. Only with sdk 1.4 and higher";
	
	/** Unbuffered write operations. Only with sdk 1.4 and higher.
	 */
	public static boolean unbufferedWrite = false;
	
	/** Description for {@link #rawDevice}.
	 */
	public static final String rawDeviceDescription = "use raw devices";
	
	/** Use raw devices.
	 */
	public static boolean rawDevice= false;

	/** Description for {@link #showData}.
	 */
	public static final String showDataDescription = "shows the input data of the first relation while inserting";
	
	/** Shows the input data of the first relation while inserting.
	 */
	public static boolean showData = true;
	
	/** Description for {@link #repaintImmediately}.
	 */
	public static final String repaintImmediatelyDescription = "Every painting causes an immediate update of the painting area";
	
	/** Every painting causes an immediate update of the painting area
	 */
	public static boolean repaintImmediately = false;


	/**
	 * Part of the @link{TestFramework} which returns the relational 
	 * metadata to the meassured values of a test run. These test values
	 * are stored inside a @link{TestFramework.list}.
	 * @return Here, the following values are meassured and the appropriate
	 * 	relational meta data is returned: TimeForInsertion, TreeHeight,
	 *  CheckDescriptor, OverAndUnderflows, RangeQueryTime, RangeQueryHits, 
	 *  NearestNeighborTime, RootRangeQueryResults, RemoveTime. 
	 */
	public static ResultSetMetaData getReturnRSMD() {
		return new ColumnMetaDataResultSetMetaData(
			new StoredColumnMetaData(false, false, true, false, ResultSetMetaData.columnNullable, true, 9, "TimeForInsertion", "TimeForInsertion", "", 9, 0, "", "", Types.INTEGER, true, false, false),
			new StoredColumnMetaData(false, false, true, false, ResultSetMetaData.columnNullable, true, 9, "TreeHeight", "TreeHeight", "", 9, 0, "", "", Types.INTEGER, true, false, false),
			new StoredColumnMetaData(false, false, true, false, ResultSetMetaData.columnNullable, false, 1, "CheckDescriptor", "CheckDescriptor", "", 1, 0, "", "", Types.BIT,  true, false, false),
			new StoredColumnMetaData(false, false, true, false, ResultSetMetaData.columnNullable, true, 9, "OverAndUnderflows", "OverAndUnderflows", "", 9, 0, "", "", Types.INTEGER, true, false, false),
			new StoredColumnMetaData(false, false, true, false, ResultSetMetaData.columnNullable, true, 9, "RangeQueryTime", "RangeQueryTime", "", 9, 0, "", "", Types.INTEGER, true, false, false),
			new StoredColumnMetaData(false, false, true, false, ResultSetMetaData.columnNullable, true, 9, "RangeQueryHits", "RangeQueryHits", "", 9, 0, "", "", Types.INTEGER, true, false, false),
			new StoredColumnMetaData(false, false, true, false, ResultSetMetaData.columnNullable, true, 9, "NearestNeighborTime", "NearestNeighborTime", "", 9, 0, "", "", Types.INTEGER, true, false, false),
			new StoredColumnMetaData(false, false, true, false, ResultSetMetaData.columnNullable, true, 9, "RootRangeQueryResults", "RootRangeQueryResults", "", 9, 0, "", "", Types.INTEGER, true, false, false),
			new StoredColumnMetaData(false, false, true, false, ResultSetMetaData.columnNullable, true, 9, "RemoveTime", "RemoveTime", "", 9, 0, "", "", Types.INTEGER, true, false, false)
		);
	}

	/**
	 * Part of the @link{TestFramework} which has to produces test values
	 * for a variable declared above in this class.
	 * @param fieldName Name of a variable from above for which
	 * 	test values are wanted.
	 * @return An Iterator containing appropriate test values.
	 */
	public static Iterator getTestValues(String fieldName) {
		if (fieldName.equals("verbose"))
			return new SingleObjectCursor(Boolean.FALSE);
		else if (fieldName.equals("showData"))
			return new SingleObjectCursor(Boolean.FALSE);
		else if (fieldName.equals("showDataDelay"))
			return new SingleObjectCursor(new Integer(1));
		else if (fieldName.equals("rawDevice"))
			return new SingleObjectCursor(Boolean.FALSE);
		else if (fieldName.equals("targetLevel"))
			return new SingleObjectCursor(new Integer(0));
		else if (fieldName.equals("bufferSize"))
			return Arrays.intArrayIterator(new int[]{0,200});
		else if (fieldName.equals("insertType"))
			return Arrays.stringArrayIterator(new String[]{"tuple", "bulk", "bulk_xsort", "bulk_peano", "bulk_hilbert"});
		else if (fieldName.equals("tree"))
			return Arrays.stringArrayIterator(new String[]{"Quadratic", "Greene", "R*", "X", "Linear"});
		else if (fieldName.equals("mincap"))
			return Arrays.intArrayIterator(new int[]{10,20,30,40});
		else if (fieldName.equals("maxcapFactor"))
			return Arrays.doubleArrayIterator(new double[]{3.0});
		else
			return null;
	}

	/**
	 * The Universe containing all used rectangles.
	 */
	protected static Rectangle universe = null;
	
	/**
	 * Precision of the space filling curve.
	 */
	protected static int FILLING_CURVE_PRECISION = 1<<30;
	
	
	/**
	 * Parses the arguments and put them into a property list.
	 * 
	 * @param defaultProps default values for properties
	 * @param args command line parameters
	 * @return property list 
	 */
	public static Properties argsToProperties(Properties defaultProps, String args[]) {
		Properties props = new Properties(defaultProps);
		
		for (int i=0; i<args.length ; i++) {
			String prop;
			String val = null;
			int indexEqual = args[i].indexOf('=');
			
			if (indexEqual>0) {
				prop = args[i].substring(0,indexEqual);
				val = args[i].substring(indexEqual+1);
			}
			else {
				prop = args[i];
				val = ""; 
			}
			prop = prop.toLowerCase();
			props.setProperty(prop,val);	
		}
		
		return props;
	}

	/**
	 * Starts the RTreeQueriesTest. Start this class with
	 * option "help" in order to get all the command-line options.
	 * 
	 * @param args command line parameters
	 * @throws Exception
	 */
	public static void main (String args[]) throws Exception {
		
		if (!TestFramework.processParameters("RTreeQueriesTest : an example using RTree and its variants\n", RTreeQueriesTest.class, args, System.out))
			return;
		
		String path = xxl.applications.indexStructures.Common.getOutPath();
		
		boolean bulk = insertType.toLowerCase().startsWith("bulk");
		Comparator compare = null;
		if (bulk) {
			if (insertType.equalsIgnoreCase("bulk_peano"))
				compare = Common.COMPARE_PEANO(universe, FILLING_CURVE_PRECISION);
			else if (insertType.equalsIgnoreCase("bulk_hilbert"))
				compare = Common.COMPARE_HILBERT(universe, FILLING_CURVE_PRECISION);
			else if (insertType.equalsIgnoreCase("bulk_xsort"))
				compare = Common.COMPARE;
		}
				
		int dataSize = 32+8; // DoublePointRectangle + Long
		int descriptorSize = 32; // DoublePointRectangle
		boolean useMultiBlockContainer = tree.equalsIgnoreCase("x");
				
		// RawDevice internal parameters
		int sizeOfRawDevice = 10000; // only interesting if it is a file
		boolean nativeRawDevice = false;
		String rawDeviceName = path+"RTree";
		
		XXLFilesystem fs;
		
		if (rawDevice) {
			path = "";
			int partitions[];
			String fileNames[];
			
			if (useMultiBlockContainer) {
				int ctrBlocks = (sizeOfRawDevice-2-2-2-10)/2;
				partitions =  new int[]{2,2,2,10,ctrBlocks,2,2,2,10};
				fileNames = MultiBlockContainer.getFilenamesUsed("RTree");
			}
			else {
				partitions = new int[]{2,2,2,10};
				fileNames = BlockFileContainer.getFilenamesUsed("RTree");
			}
			fs = new XXLFilesystem(
				nativeRawDevice, blockSize,	sizeOfRawDevice, 
				rawDeviceName, partitions, fileNames, 0, false, true, false
			);
		}
		else
			fs = new XXLFilesystem(unbufferedWrite);
		
		FilesystemOperations fso = fs.getFilesystemOperations();
		
		/*********************************************************************/
		/*                          BUILDING TREES                           */
		/*********************************************************************/
				
		RTree rtree=null;
		Container fileContainer=null;
		fso = new LogFilesystemOperations(fso,System.out,true);
		
		if (tree.equalsIgnoreCase("x")) {
			rtree = new XTree();
			fileContainer = new MultiBlockContainer(path+"RTree", blockSize, fso);
		}
		else {
			if (tree.equalsIgnoreCase("r*"))
				rtree = new RTree();
			else if (tree.equalsIgnoreCase("greene"))
				rtree = new GreenesRTree();
			else if (tree.equalsIgnoreCase("linear"))
				rtree = new LinearRTree();
			else if (tree.equalsIgnoreCase("quadratic"))
				rtree = new QuadraticRTree();
			
			fileContainer = new BlockFileContainer(path+"RTree",blockSize,fso);
		}
			
		// an unbuffered container that counts the access to the RTree		
		CounterContainer lowerCounterContainer = new CounterContainer(
			new ConverterContainer(
				fileContainer,
				rtree.nodeConverter(
					new ConvertableConverter(Common.LEAFENTRY_FACTORY),2
				)
			)
		);
		
		// a buffered container that count the access to the buffered RTree
		Container bufferedContainer;
		if (bufferSize>0)
			bufferedContainer = new BufferedContainer(lowerCounterContainer, new LRUBuffer(bufferSize), true);
		else
			bufferedContainer = lowerCounterContainer;
		
		CounterContainer upperCounterContainer = new CounterContainer(bufferedContainer);
		
		// the container that stores the content of the RTree
		Container container = upperCounterContainer;
		
		// the container that stores the geometries
		final MultiBlockContainer geometryContainer = new MultiBlockContainer(path+"Geometries", blockSize);
		
		final CounterContainer geometryCounterContainer = 	new CounterContainer(
																new ConverterContainer(
																		geometryContainer,
																		Geometry2DConverter.DEFAULT_INSTANCE
																)
															);	
		
		// Read the universe (needed for space filling curves)
		universe = Rectangles.readSingletonRectangle(new File(xxl.applications.indexStructures.Common.getDataPath()+filenameTree+".universe"), new DoublePointRectangle(2));
		System.out.println("Universe read:");
		System.out.println(universe);
		System.out.println();
		
		// initialize the RTree with the descriptor-factory method, a
		// container for storing the nodes and the minimum and maximum
		// capacity of them
		if (tree.equalsIgnoreCase("x")) {
			int entrySize = Math.max(dataSize,descriptorSize+8);
			int xTreeMaxCap = (blockSize - 6) / entrySize;
			int xTreeMinCap = (int) (xTreeMaxCap * minMaxFactor);
			((XTree) rtree).initialize(Common.GET_DESCRIPTOR, container, xTreeMinCap, xTreeMaxCap, 2);
		}
		else
			rtree.initialize(Common.GET_DESCRIPTOR, container, blockSize, dataSize, descriptorSize, minMaxFactor);

		final VisualOutput outputPanel = showData ? new VisualOutput("RTree- Test",universe,700) : null;
		
		// initialize an iterator over the first input-file
		Iterator<Geometry2D> geometryInput0 = Geometry2DFileIO.read(
														Geometry2DConverter.DEFAULT_INSTANCE, 
														new File(xxl.applications.indexStructures.Common.getDataPath()+filenameTree)
													);
		
		// insert the geometries into the geometry-container
		Cursor cursor = new Mapper<Geometry2D, KPE>(
				new AbstractFunction<Geometry2D, KPE>(){
					public KPE invoke(Geometry2D g){
						return new KPE(g.getMBR(), geometryCounterContainer.insert(g), LongConverter.DEFAULT_INSTANCE);
					}
				},
				showData ? new VisualGeometry2DCursor( geometryInput0, outputPanel,	Color.DARK_GRAY )								
						 : geometryInput0
		);
		
		final Function<KPE, Geometry2D> getGeometry = new AbstractFunction<KPE,Geometry2D>(){
			public Geometry2D invoke(KPE k){
				return (Geometry2D) geometryCounterContainer.get(k.getID());
			}
		};

		/*********************************************************************/
		/*                             INSERTION                             */
		/*********************************************************************/

		long t1,t2;
		t1 = System.currentTimeMillis();
		
		if (!bulk) {
			// insert an iterator of objects by inserting every single object
			while (cursor.hasNext()) {
				Convertable c = (Convertable) cursor.next();
				rtree.insert(c);
			}
		}
		else {
			// or by bulk-insertion
			if (compare!=null)
				cursor = new MergeSorter(cursor,compare,12,4*4096,4*4096);
			
			new SortBasedBulkLoading(rtree, cursor, new Constant(container));
		}
		cursor.close();
		
		t2 = System.currentTimeMillis();
		TestFramework.list.add(new Long(t2-t1));
		TestFramework.list.add(new Integer(rtree.height()));
		
		System.out.println("Time for insertion: "+(t2-t1));
		System.out.println("Insertion complete, height: "+rtree.height()+", universe: ");
		System.out.println(rtree.rootDescriptor());
		System.out.println();
		System.out.println("Accessing the BufferedContainer");
		System.out.println(upperCounterContainer);
		System.out.println();
		System.out.println("Accessing the ConverterContainer and the BlockFileContainer");
		System.out.println(lowerCounterContainer);
		System.out.println();
		System.out.println("Accessing the Geometry- Container:");
		System.out.println(geometryCounterContainer);
		System.out.println();
				
		upperCounterContainer.reset();
		lowerCounterContainer.reset();
		geometryCounterContainer.reset();
		
		if (bufferSize>0) {
			System.out.println("Flushing buffers");
			bufferedContainer.flush();
		}
		System.out.println();	
		System.out.println("Accessing the BufferedContainer");
		System.out.println(upperCounterContainer);
		System.out.println();
		System.out.println("Accessing the ConverterContainer and the BlockFileContainer");
		System.out.println(lowerCounterContainer);
		System.out.println();

		/*********************************************************************/
		/*                  ADDITIONAL CHECKS FOR CONSISTENCE                */
		/*********************************************************************/

		System.out.print("Checking descriptors... ");
		TestFramework.list.add(new Boolean(rtree.checkDescriptors()));
		System.out.println("done.");

		System.out.print("Checking number of entries (between min and max?)... ");
		TestFramework.list.add(new Integer(rtree.checkNumberOfEntries()));
		System.out.println("done.");
		System.out.println();
				
		upperCounterContainer.reset();
		lowerCounterContainer.reset();
		
		outputPanel.repaint();
		outputPanel.push();
		outputPanel.setRepaintImmediately(repaintImmediately);
		
		System.out.println( "\n"+
		"*********************************************************************\n"+
		"*                     PERFORMING REGION QUERY                       *\n"+
		"*********************************************************************\n");
		
		final Geometry2D region = Geometry2DFactory.createEllipse(
									Geometry2DFactory.createPoint(
										universe.getCorner(false).getValue(0)+universe.deltas()[0]/2d,
										universe.getCorner(false).getValue(1)+universe.deltas()[1]*0.75d
									), 0.5, 0.2,100									
								);
		
		cursor = new RegionQuery(rtree, region, getGeometry);
		
		if(showData) cursor = new DecoratorCursor<KPE>(cursor){
										public KPE next(){
											KPE next = super.next();
											outputPanel.draw(getGeometry.invoke(next),Color.GREEN);
											return next;
										}
									};								
		
		System.out.println("Query object:"+region);
		outputPanel.draw(region,Color.RED);
		t1 = System.currentTimeMillis();
		int results = Cursors.count(cursor);
		cursor.close();		
		t2 = System.currentTimeMillis();
		TestFramework.list.add(new Long(t2-t1));

		System.out.println("\nTime for query: "+(t2-t1) +" ms.");
		System.out.println("Number of results: "+results);
		System.out.println("Accessing the BufferedContainer");
		System.out.println(upperCounterContainer);
		System.out.println();
		System.out.println("Accessing the ConverterContainer and the BlockFileContainer");
		System.out.println(lowerCounterContainer);
		System.out.println();
		System.out.println("Accessing the Geometry- Container:");
		System.out.println(geometryCounterContainer);
		System.out.println();
		
		upperCounterContainer.reset();
		lowerCounterContainer.reset();
		geometryCounterContainer.reset();

		outputPanel.repaint();
		outputPanel.waitForUser();
		outputPanel.peek();
		
		System.out.println( "\n"+
				"*********************************************************************\n"+
				"*                   K- NEAREST-NEIGHBOR QUERY                       *\n"+
				"*********************************************************************\n");
			
		System.out.println("Performing a nearest neighbor query against the tree \n"
						 + "determining the 150 nearest neighbor entries at target level \n"
						 + "concerning the centre point of the universe: ");
		final Point2DAdapter centre = Geometry2DFactory.createPoint(
								universe.getCorner(false).getValue(0)+universe.deltas()[0]/2d,
								universe.getCorner(false).getValue(1)+universe.deltas()[1]/2d
								);
		
		cursor = new KNearestNeighborQuery(
									new NearestNeighborQuery(rtree,	centre,	getGeometry ),
									150
								);
		
		if(showData) cursor = new DecoratorCursor<KPE>(cursor){
										public KPE next(){
											KPE next = super.next();
											outputPanel.draw(getGeometry.invoke(next),Color.GREEN);
											return next;
										}
									};								
		
		System.out.println("Query object:"+centre);
		
		t1 = System.currentTimeMillis();
		results = Cursors.count(cursor);
		cursor.close();		
		t2 = System.currentTimeMillis();
		TestFramework.list.add(new Long(t2-t1));

		System.out.println("\nTime for query: "+(t2-t1) +" ms.");
		System.out.println("Accessing the BufferedContainer");
		System.out.println(upperCounterContainer);
		System.out.println();
		System.out.println("Accessing the ConverterContainer and the BlockFileContainer");
		System.out.println(lowerCounterContainer);
		System.out.println();
		System.out.println("Accessing the Geometry- Container:");
		System.out.println(geometryCounterContainer);
		System.out.println();
		
		
		upperCounterContainer.reset();
		lowerCounterContainer.reset();
		geometryCounterContainer.reset();

		outputPanel.repaint();
		outputPanel.waitForUser();
		outputPanel.peek();
		System.out.println( "\n"+
				"*********************************************************************\n"+
				"*                     PERFORMING DISTANCE QUERY                     *\n"+
				"*********************************************************************\n");
		
		System.out.println("Performing a distance query against the tree determining \n"+
						   "the geometries within a distance of 0.1 degree (~10km) to \n"+
						   "the centre point of the universe: ");
		double distance = 0.1;
		cursor = new DistanceQuery(	new NearestNeighborQuery(rtree,	centre,	getGeometry ), distance);
		
		cursor = new DecoratorCursor<KPE>(cursor){
										public KPE next(){
											KPE next = super.next();
											outputPanel.draw(getGeometry.invoke(next),Color.GREEN);
											return next;
										}
									};								
		
		System.out.println("Query object:"+centre);
		outputPanel.draw(centre.buffer(distance),Color.RED);

		t1 = System.currentTimeMillis();
		results = Cursors.count(cursor);
		cursor.close();		
		t2 = System.currentTimeMillis();
		TestFramework.list.add(new Long(t2-t1));

		System.out.println("\nTime for query: "+(t2-t1) +" ms.");
		System.out.println("Number of results: "+results);
		System.out.println();
		System.out.println("Accessing the BufferedContainer");
		System.out.println(upperCounterContainer);
		System.out.println();
		System.out.println("Accessing the ConverterContainer and the BlockFileContainer");
		System.out.println(lowerCounterContainer);
		System.out.println();
		System.out.println("Accessing the Geometry- Container:");
		System.out.println(geometryCounterContainer);
		System.out.println();
				
		upperCounterContainer.reset();
		lowerCounterContainer.reset();
		geometryCounterContainer.reset();

		outputPanel.repaint();
		outputPanel.waitForUser();		
		/*********************************************************************/
		/*                   RANGE QUERY WITH ROOT DESCRIPTOR                */
		/*********************************************************************/
		
		System.out.println("Querying root descriptor");
		int numberOfElements = Cursors.count(rtree.query(rtree.rootDescriptor(), targetLevel));
		// equivalent with Cursors.count(rtree.query())
		System.out.println("Number of results: "+numberOfElements);

		TestFramework.list.add(new Integer(numberOfElements));

		/*********************************************************************/
		/*                       REMOVING ALL ELEMENTS                       */
		/*********************************************************************/

		System.out.println("Removing all elements");

		t1 = System.currentTimeMillis();
				
		rtree.clear();
				
		t2 = System.currentTimeMillis();
		TestFramework.list.add(new Long(t2-t1));
		System.out.println("Time for removal: "+(t2-t1));

		System.out.println();
		
		numberOfElements = Cursors.count(rtree.query());
		System.out.println("Number of elements in the tree: "+numberOfElements);
		
		System.out.println("Closing application");
		
		container.close();
		
		// delete files
		if (fileContainer instanceof BlockFileContainer)
			((BlockFileContainer) fileContainer).delete();
		else if (fileContainer instanceof MultiBlockContainer)
			((MultiBlockContainer) fileContainer).delete();
		geometryContainer.delete();
		
		fs.close();
		
		outputPanel.waitForUser();
		outputPanel.dispose();
	}
}
