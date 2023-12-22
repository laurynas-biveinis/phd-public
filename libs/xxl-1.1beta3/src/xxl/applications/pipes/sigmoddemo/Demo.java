/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2006 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307,
USA

	http://www.xxl-library.de

bugs, requests for enhancements: request@xxl-library.de

If you want to be informed on new versions of XXL you can 
subscribe to our mailing-list. Send an email to 
	
	xxl-request@lists.uni-marburg.de

without subject and the word "subscribe" in the message body. 
*/

package xxl.applications.pipes.sigmoddemo;

import static xxl.core.pipes.operators.AbstractPipe.AbstractPipeMetaDataManagement.AVG_INPUT_OUTPUT_RATIO;
import static xxl.core.pipes.operators.AbstractPipe.AbstractPipeMetaDataManagement.AVG_INPUT_RATE;
import static xxl.core.pipes.operators.AbstractPipe.AbstractPipeMetaDataManagement.AVG_MEM_USAGE;
import static xxl.core.pipes.operators.AbstractPipe.AbstractPipeMetaDataManagement.AVG_OUTPUT_RATE;
import static xxl.core.pipes.operators.AbstractPipe.AbstractPipeMetaDataManagement.INPUT_OUTPUT_RATIO;
import static xxl.core.pipes.operators.AbstractPipe.AbstractPipeMetaDataManagement.INPUT_RATE;
import static xxl.core.pipes.operators.AbstractPipe.AbstractPipeMetaDataManagement.MEM_USAGE;
import static xxl.core.pipes.operators.AbstractPipe.AbstractPipeMetaDataManagement.OUTPUT_RATE;
import static xxl.core.pipes.operators.AbstractPipe.AbstractPipeMetaDataManagement.VAR_INPUT_OUTPUT_RATIO;
import static xxl.core.pipes.operators.AbstractPipe.AbstractPipeMetaDataManagement.VAR_INPUT_RATE;
import static xxl.core.pipes.operators.AbstractPipe.AbstractPipeMetaDataManagement.VAR_MEM_USAGE;
import static xxl.core.pipes.operators.AbstractPipe.AbstractPipeMetaDataManagement.VAR_OUTPUT_RATE;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import xxl.applications.pipes.Common;
import xxl.applications.pipes.SWTPerformanceMonitor;
import xxl.applications.pipes.SWTSink;
import xxl.applications.pipes.auctions.Bid;
import xxl.applications.pipes.auctions.ClosedAuction;
import xxl.applications.pipes.auctions.NEXMarkGeneratorSource;
import xxl.applications.pipes.auctions.OpenAuction;
import xxl.applications.pipes.auctions.mapping.Person;
import xxl.applications.pipes.traffic.Highway;
import xxl.applications.pipes.traffic.HighwaySource;
import xxl.applications.pipes.traffic.Vehicle;
import xxl.core.collections.queues.ListQueue;
import xxl.core.collections.queues.MemoryManageableListQueue;
import xxl.core.collections.sweepAreas.HashSAImplementor;
import xxl.core.comparators.InverseComparator;
import xxl.core.cursors.Cursor;
import xxl.core.cursors.sources.EmptyCursor;
import xxl.core.cursors.sources.SingleObjectCursor;
import xxl.core.functions.Function;
import xxl.core.math.functions.AggregationFunction;
import xxl.core.math.statistics.parametric.aggregates.CountAwareAverage;
import xxl.core.pipes.elements.TemporalObject;
import xxl.core.pipes.memoryManager.GlobalMemoryManager;
import xxl.core.pipes.memoryManager.MemoryManageable;
import xxl.core.pipes.memoryManager.UniformStrategy;
import xxl.core.pipes.operators.filters.Filter;
import xxl.core.pipes.operators.groupers.HashGrouper;
import xxl.core.pipes.operators.identities.BufferPipe;
import xxl.core.pipes.operators.joins.IndexJoin;
import xxl.core.pipes.operators.joins.TemporalJoin;
import xxl.core.pipes.operators.joins.TemporalJoin.TemporalJoinListSA;
import xxl.core.pipes.operators.mappers.Mapper;
import xxl.core.pipes.operators.mappers.TemporalAggregator;
import xxl.core.pipes.operators.mappers.TemporalAggregator.TemporalAggregatorSA;
import xxl.core.pipes.operators.unions.TemporalUnion;
import xxl.core.pipes.operators.unions.Union;
import xxl.core.pipes.operators.unions.TemporalUnion.TemporalUnionSA;
import xxl.core.pipes.operators.windows.TemporalWindow;
import xxl.core.pipes.scheduler.ComparatorStrategy;
import xxl.core.pipes.scheduler.Controllable;
import xxl.core.pipes.scheduler.MemSizeComparator;
import xxl.core.pipes.scheduler.StrategyProcessor;
import xxl.core.pipes.scheduler.TimeSliceScheduler;
import xxl.core.pipes.sources.Source;
import xxl.core.predicates.Predicate;


/**
 * Contains all information for a demo. It also provides some
 * static methods to get complete demos.
 */
public class Demo {
    
	/*******************************************************************/
    /*                        SETTINGS                                 */
    /*******************************************************************/    

	/**
     * The path to the traffic files. 
     */
	public static final String TRAFFIC_DATA_DIR = Common.getTrafficPath();
    
	/**
     * The path to the nexmark files. 
     */
	public static final String AUCTION_DATA_DIR = Common.getNEXMarkPath();    
    
    /**
     * The path to the nexmark mapping. 
     */
	public static final String CASTOR_MAPPING_FILE = Common.getNEXMarkPath()+"NEXMark-mapping.xml";
    
    /**
     * Creates all standard metadata. ("INPUT_RATE", "AVG_INPUT_RATE", "VAR_INPUT_RATE", 
     * "OUTPUT_RATE", "AVG_OUTPUT_RATE", "VAR_OUTPUT_RATE","MEM_SIZE", "AVG_MEM_SIZE", "VAR_MEM_SIZE", 
     * "SELECTIVITY", "AVG_SELECTIVITY", "VAR_SELECTIVITY")
     */
	public static final Object[] ALL_METADATA = {INPUT_RATE, AVG_INPUT_RATE, VAR_INPUT_RATE, OUTPUT_RATE, 
    		AVG_OUTPUT_RATE, VAR_OUTPUT_RATE, MEM_USAGE, AVG_MEM_USAGE, VAR_MEM_USAGE, INPUT_OUTPUT_RATIO, AVG_INPUT_OUTPUT_RATIO, VAR_INPUT_OUTPUT_RATIO};
    
    /**
     * Creates some standard metadata: ("INPUT_RATE", "AVG_INPUT_RATE", 
     *  "OUTPUT_RATE", "MEM_SIZE", "SELECTIVITY", "AVG_SELECTIVITY", "VAR_SELECTIVITY")
     */
	public static final Object[] DEFAULT_METADATA = {INPUT_RATE, AVG_INPUT_RATE, OUTPUT_RATE, 
		MEM_USAGE, INPUT_OUTPUT_RATIO, AVG_INPUT_OUTPUT_RATIO, VAR_INPUT_OUTPUT_RATIO};
    /**
     * Creates some standard metadata: ("INPUT_RATE", "OUTPUT_RATE", "VAR_INPUT_RATE",
     * "VAR_OUTPUT_RATE") 
     */    
    public static final Object[] DEFAULT_METADATA2 = {INPUT_RATE, OUTPUT_RATE, VAR_INPUT_RATE, VAR_OUTPUT_RATE};

    /*******************************************************************/
    /*               USE-CASES: traffic scenario                       */
    /*******************************************************************/
    
    /**
     * removes measurement errors. 
     */    
    public static Source clean(Source source) {
    	return new Filter(source, 
    		new Predicate() {
    			protected Vehicle v;
    			protected double length, speed;
    			
    			public boolean invoke(Object o) {
    				v = (Vehicle)((TemporalObject)o).getObject();
    				length = v.getLength();
    				speed = v.getSpeed();
    				if (length < 1d || length > 50d)
    					return false;
    				if (speed <  0d || speed > 100d)
    					return false;
    				return true; 	
    			}
    		}
    	);
    }
        	
	/**
	 * returns the traffic demo
	 * @return the actual traffic demo 
	 */
	public static Demo getTrafficDemo() {
		Demo d = new Demo("Traffic", null);
		Query[] q = new Query[5];
		
		q[0] = d.new Query("Remove noise from the sensors at position 166600 in direction 'S'."
				,"SELECT timestamp, distance, lane, speed, length, direction "
				+"\nFROM Sensor1, ..., Sensor5"
				+"\nWHERE length >= '1' AND length <= '50' AND"
				+"\n      speed >= '0' AND speed  <= '100';"
				, "images/16600_S.gif"
				, "images/16600_S_plan.gif"
				, "txt/16600_S.txt"
				,new int[]{80,20}
				,new int[]{60,40}) {
		
		    SWTPerformanceMonitor monitor;
		    SWTSink sink;
		
		    public void startQuery(Composite c) {
		        c.setLayout(new FillLayout());
				TabFolder tf = new TabFolder(c, SWT.NULL);
				
				HighwaySource source1 = Highway.getSourceByDate(TRAFFIC_DATA_DIR,
				        new GregorianCalendar(1993, 3, 11).getTime(), 'S', 16600, (short)1, 0.1f);
				HighwaySource source2 = Highway.getSourceByDate(TRAFFIC_DATA_DIR,
				        new GregorianCalendar(1993, 3, 11).getTime(), 'S', 16600, (short)2, 0.1f);
				HighwaySource source3 = Highway.getSourceByDate(TRAFFIC_DATA_DIR,
				        new GregorianCalendar(1993, 3, 11).getTime(), 'S', 16600, (short)3, 0.1f);
				HighwaySource source4 = Highway.getSourceByDate(TRAFFIC_DATA_DIR,
				        new GregorianCalendar(1993, 3, 11).getTime(), 'S', 16600, (short)4, 0.1f);
				HighwaySource source5 = Highway.getSourceByDate(TRAFFIC_DATA_DIR,
				        new GregorianCalendar(1993, 3, 11).getTime(), 'S', 16600, (short)5, 0.1f);

				Union union = new Union(new Source[]{source1, source2, source3, source4, source5});

				Filter filter = new Filter(union, new Predicate() {

				    protected Vehicle v;
				    protected double length, speed;

				    public boolean invoke(Object o) {
				        v = (Vehicle)o;
				        length = v.getLength();
				        speed = v.getSpeed();
				        if (length < 1d || length > 50d) return false;
				        if (speed < 0d || speed > 100d) return false;
				        return true;
				    }
				});

				sink = new SWTSink(filter, "SWTSink", true, tf, SWT.MIN);

				filter.getMetaDataManagement().include(
					INPUT_RATE, OUTPUT_RATE, 
					INPUT_OUTPUT_RATIO, AVG_INPUT_OUTPUT_RATIO, VAR_INPUT_OUTPUT_RATIO
				);
				
				monitor = new SWTPerformanceMonitor(filter.getMetaData(), tf, SWT.NONE);
				
				
				TabItem item1 = new TabItem(tf, SWT.NONE);
				item1.setText("SWTSink");
				item1.setControl(sink);
				
				TabItem item2 = new TabItem(tf, SWT.NONE);
				item2.setText("MetaData Monitor (Filter)");
				item2.setControl(monitor);				
		    }
		    
		    public void stopQuery() {			
		    	if (sink != null)
		    		sink.closeAllSources();
			}
		};
		
		q[1] = d.new Query("What has been the average length of HOVs driving in direction Oakland within the last 15 minutes."
				,"SELECT AVG(length) "
				+"\nFROM (SELECT length"
				+"\n     FROM Sensor1 [Range 15 minutes]"
				+"\n     WHERE lane = '1';"
				+"\n     UNION ALL"
				+"\n     ..."
				+"\n     UNION ALL"
				+"\n     SELECT length"
				+"\n     FROM Sensor18 [Range 15 minutes]"
				+"\n     WHERE lane = '1';);"
				, "images/traffic_lane1_to_oakland.gif"
				, "images/traffic_lane1_to_oakland_plan.gif"
				, "txt/traffic_lane1_to_oakland.txt"
				,new int[]{80,20}
				,new int[]{60,40}) {
		
		    SWTSink sink;
		    SWTSink sink2;
		    SWTPerformanceMonitor monitor;
		
		    public void startQuery(Composite c) {
		        c.setLayout(new FillLayout());
				TabFolder tf = new TabFolder(c, SWT.NULL);
				
				HighwaySource[] sources = Highway.getSourcesByDate(TRAFFIC_DATA_DIR,
		                new GregorianCalendar(1993, 3, 15).getTime(), 'N', // direction
		                0.1f
		        );

		        Mapper[] mappers = new Mapper[sources.length];
		        final long lifespan = 15*60*1000; 
		        TemporalWindow[] windows = new TemporalWindow[sources.length];
		        
		        for (int i = 0; i < sources.length; i++) {
		            mappers[i] = new Mapper(sources[i], Vehicle.MAP_TO_TEMPORAL_OBJECT);
		            windows[i] = new TemporalWindow(mappers[i], lifespan);
		        }
		        final int objectSize = 31;
		        TemporalUnion union = new TemporalUnion(windows, new TemporalUnionSA(sources.length, objectSize));
		        Filter filter = new Filter(clean(union), new Predicate() {
		            public boolean invoke(Object o) {
		                return ((Vehicle) ((TemporalObject) o).getObject()).getLane() == 1; // HOV lane
		            }
		        });
		        Mapper map = new Mapper(filter, new Function() {
		            public Object invoke(Object o) {
		                TemporalObject to = (TemporalObject) o;
		                // project to length
		                return new TemporalObject(new Double(((Vehicle) to.getObject()).getLength()), to.getTimeInterval());
		            }
		        });
		        
		        TemporalAggregator aggregator = new TemporalAggregator(map, 0,
		                new TemporalAggregatorSA(new CountAwareAverage()) // build average						
		        );
		        
		        // conversions
		        Mapper conv = new Mapper(aggregator, new Function() {
		            protected DecimalFormat df = new DecimalFormat( "0.00" );
		            
		            public Object invoke(Object o) {
		                TemporalObject t = (TemporalObject)o;
		                return "("+df.format(((Object[])t.getObject())[0])+" (m); ["+new Date(t.getStart())+", "+new Date(t.getEnd())+"))";              
		            }
		        });
		        
				sink = new SWTSink(conv, "SWTSink", true, tf, SWT.MIN);

				sink2 = new SWTSink(filter, "Filter", true, tf, SWT.MIN);
				
				union.getMetaDataManagement().include(DEFAULT_METADATA);

				
				aggregator.getMetaDataManagement().include(INPUT_RATE, OUTPUT_RATE, MEM_USAGE,
						AVG_INPUT_OUTPUT_RATIO, INPUT_OUTPUT_RATIO, VAR_INPUT_OUTPUT_RATIO);
				SWTPerformanceMonitor monitor3 = new SWTPerformanceMonitor(aggregator.getMetaData(), tf, SWT.NONE);
				
				monitor = new SWTPerformanceMonitor(union.getMetaData(), tf, SWT.NONE);
				
				filter.getMetaDataManagement().include(DEFAULT_METADATA2);
				
				SWTPerformanceMonitor monitor2 = new SWTPerformanceMonitor(filter.getMetaData(), tf, SWT.NONE);
				
				TabItem item1 = new TabItem(tf, SWT.NONE);
				item1.setText("SWTSink");
				item1.setControl(sink);
				
				TabItem item2 = new TabItem(tf, SWT.NONE);
				item2.setText("Filter");
				item2.setControl(sink2);
				
				TabItem item3 = new TabItem(tf, SWT.NONE);
				item3.setText("MetaData Monitor (Union)");
				item3.setControl(monitor);
				
				TabItem item4 = new TabItem(tf, SWT.NONE);
				item4.setText("MetaData Monitor (Filter 1)");
				item4.setControl(monitor2);
				
				TabItem item5 = new TabItem(tf, SWT.NONE);
				item5.setText("MetaData Monitor (TemporalAgregator)");
				item5.setControl(monitor3);

		    }
		    
		    public void stopQuery() {			
		    	if (sink != null)
		    		sink.closeAllSources();
		    	if (sink2 != null)
		    		sink2.closeAllSources();
			}
		};		
		
		q[2] = d.new Query("At which sections of the highway in direction San Jose has the average speed been below 15 m/s constantly for 15 minutes."
				,"SELECT AVG(speed) AS avgSpeed, distance "
				+"\nFROM (SELECT speed, distance"
				+"\n     FROM SensorRow1 [Range 15 minutes]"
				+"\n     UNION ALL"
				+"\n     ..."
				+"\n     UNION ALL"
				+"\n     SELECT speed, distance"
				+"\n     FROM SensorRow17 [Range 15 minutes]"
				+"\n     );"
				+"\nWHERE avgSpeed < 15;"
				, "images/sections_to_san_jose.gif"
				, "images/sections_to_san_jose_plan.gif"
				, "txt/sections_to_san_jose.txt"
				,new int[]{80,20}
				,new int[]{60,40}) {
		
		    SWTSink sink;
		    SWTSink sink2;
		
		    public void startQuery(Composite c) {
		        c.setLayout(new FillLayout());
				TabFolder tf = new TabFolder(c, SWT.NULL);
								
				HighwaySource[] sources = Highway.getSourcesByDate(TRAFFIC_DATA_DIR,
		                new GregorianCalendar(1993, 2, 16).getTime(), 'S', // direction
		                0.5f);

		        Mapper[] mappers = new Mapper[sources.length];
		        TemporalWindow[] windows = new TemporalWindow[sources.length];
		        final long lifespan = 900000; // window size: 15 minutes
		        for (int i = 0; i < sources.length; i++) {
		            mappers[i] = new Mapper(sources[i], Vehicle.MAP_TO_TEMPORAL_OBJECT);
		        	windows[i] = new TemporalWindow(mappers[i], lifespan);
		    	}
		        
		        final int noOfSections = sources.length;
		        TemporalAggregator[] aggs = new TemporalAggregator[noOfSections];
		        for (int i = 0; i < noOfSections; i++) {
		            aggs[i] = new TemporalAggregator(windows[i], 
		                    new TemporalAggregatorSA(
		                            new AggregationFunction<Vehicle,AvgSpeedPerSection>() {
		                                protected final CountAwareAverage avg = new CountAwareAverage();
		                                
		                                public AvgSpeedPerSection invoke(AvgSpeedPerSection agg, Vehicle next) {
		                                    Vehicle v = (Vehicle)next;
		                                    if (agg == null) {
		                                        return new AvgSpeedPerSection(avg.invoke(null, new Double(v.getSpeed())), v.getDistanceToCity());
		                                    }
		                                    return new AvgSpeedPerSection(
		                                        avg.invoke(((AvgSpeedPerSection)agg).getAggregate(), new Double(v.getSpeed())), 
		                                        v.getDistanceToCity()
		                                    ); 
		                                }
		                            }
		                    )
		            );
		        }
		        
		        Union union = new Union(aggs);
		                
		        final double speedThreshold = 15.0d; 
		        Filter filter = new Filter(union, new Predicate() {
		            public boolean invoke(Object o) {
		                AvgSpeedPerSection agg = (AvgSpeedPerSection)((TemporalObject)o).getObject();
		                if (agg.speed < speedThreshold)
		                    return true;
		                return false;
		            }
		        });
		        
		        // conversions
		        Mapper map = new Mapper(filter, new Function() {
		            public Object invoke(Object o) {
		                TemporalObject t = (TemporalObject)o;
		                return "("+t.getObject().toString()+", ["+new Date(t.getStart())+", "+new Date(t.getEnd())+"))";              
		            }
		        });
		        
				sink = new SWTSink(map, "SWTSink", true, tf, SWT.MIN);
				sink2 = new SWTSink(mappers[2], "Map 3", true, tf, SWT.MIN);
				
				union.getMetaDataManagement().include(DEFAULT_METADATA);
				
				SWTPerformanceMonitor monitor = new SWTPerformanceMonitor(union.getMetaData(), tf, SWT.NONE);
				
				
				TabItem item1 = new TabItem(tf, SWT.NONE);
				item1.setText("SWTSink");
				item1.setControl(sink);
				
				TabItem item2 = new TabItem(tf, SWT.NONE);
				item2.setText("Map 3");
				item2.setControl(sink2);
				
				TabItem item3 = new TabItem(tf, SWT.NONE);
				item3.setText("MetaData Monitor (Union)");
				item3.setControl(monitor);
				
		    }
		    
		    public void stopQuery() {			
		    	if (sink != null)
		    		sink.closeAllSources();
		    	if (sink2 != null)
		    		sink2.closeAllSources();
			}
		};

		q[3] = d.new Query("Example using BufferPipes and Scheduler."
				,"SELECT timestamp, distance, lane, speed, length, direction "
				+"\nFROM Sensor1, ..., Sensor5"
				+"\nWHERE length >= '1' AND length <= '50' AND"
				+"\n      speed >= '0' AND speed  <= '100';"
				, "images/16600_S.gif"
				, "images/scheduling_plan.gif"
				, "txt/scheduling.txt"
				,new int[]{80,20}
				,new int[]{60,40}) {
		
		    SWTPerformanceMonitor[] monitors = new SWTPerformanceMonitor[6];
		    SWTSink sink;
		    TimeSliceScheduler scheduler;
		    StrategyProcessor proc;
		    HighwaySource[] sources;
		
		    public void startQuery(Composite c) {
		        c.setLayout(new FillLayout());
				TabFolder tf = new TabFolder(c, SWT.NULL);
				
				
				HighwaySource source1 = Highway.getSourceByDate(TRAFFIC_DATA_DIR,
		                new GregorianCalendar(1993, 3, 11).getTime(), 'S', 16600, (short)1, 0.1f);
		        HighwaySource source2 = Highway.getSourceByDate(TRAFFIC_DATA_DIR,
		                new GregorianCalendar(1993, 3, 11).getTime(), 'S', 16600, (short)2, 0.1f);
		        HighwaySource source3 = Highway.getSourceByDate(TRAFFIC_DATA_DIR,
		                new GregorianCalendar(1993, 3, 11).getTime(), 'S', 16600, (short)3, 0.1f);
		        HighwaySource source4 = Highway.getSourceByDate(TRAFFIC_DATA_DIR,
		                new GregorianCalendar(1993, 3, 11).getTime(), 'S', 16600, (short)4, 0.1f);
		        HighwaySource source5 = Highway.getSourceByDate(TRAFFIC_DATA_DIR,
		                new GregorianCalendar(1993, 3, 11).getTime(), 'S', 16600, (short)5, 0.1f);
		        
		        sources = new HighwaySource[]{source1, source2, source3, source4, source5};
		        final int[] sourceIDs = new int[]{0, 1, 2, 3, 4};
		        
		        // size of a vehicle instance: 31 bytes
		        final BufferPipe[] bps = new BufferPipe[5];
		        for (int i = 0; i < sources.length; i++) {
		            bps[i] = new BufferPipe(sources[i], sourceIDs[i], new ListQueue(), 1, 31);
		            bps[i].assignMemSize(MemoryManageable.MAXIMUM);
		            bps[i].getMetaDataManagement().include(DEFAULT_METADATA);
		        }
		       
		        Union union = new Union(bps);
		        
		        final BufferPipe bp = new BufferPipe(union, 0, new ListQueue(), 1, 31);
		        bp.getMetaDataManagement().include(DEFAULT_METADATA);
		        bp.assignMemSize(MemoryManageable.MAXIMUM);
		        
		        Filter filter = new Filter(bp, new Predicate() {

		            protected Vehicle v;
		            protected double length, speed;

		            public boolean invoke(Object o) {
		                v = (Vehicle)o;
		                length = v.getLength();
		                speed = v.getSpeed();
		                if (length < 1d || length > 50d) return false;
		                if (speed < 0d || speed > 100d) return false;
		                return true;
		            }
		        });
		        
		        // scheduler settings
		        
	        	// MTIQ Strategy
		        proc = new StrategyProcessor(
		                new ComparatorStrategy( // MTIQ-Strategy
			                new Controllable[] {bps[0], bps[1], bps[2], bps[3], bps[4], bp}, 
			                new InverseComparator(new MemSizeComparator())
			            ), 
		                0, 
		                1000
		        );
		        proc.start();
		        
		        // Round-Robin Strategy
//		        proc = new StrategyProcessor(
//		                new RoundRobinStrategy(
//		                        new Controllable[] {bps[0], bps[1], bps[2], bps[3], bps[4], bp} 
//		                ), 
//		                0, 
//		                1000
//		        );
//		        proc.start();
		        
		        
		        scheduler = new TimeSliceScheduler(true);
		        scheduler.schedule(proc);
		        scheduler.schedule(source1.getProcessor());
		        scheduler.schedule(source2.getProcessor());
		        scheduler.schedule(source3.getProcessor());
		        scheduler.schedule(source4.getProcessor());
		        scheduler.schedule(source5.getProcessor());
		      
		        
		        sink = new SWTSink(filter, "SWTSink", true, tf, SWT.MIN);
		        
		        
		        monitors[0] = new SWTPerformanceMonitor(bps[0].getMetaData(), tf, SWT.NONE);
		        monitors[1] = new SWTPerformanceMonitor(bps[1].getMetaData(), tf, SWT.NONE);
		        monitors[2] = new SWTPerformanceMonitor(bps[2].getMetaData(), tf, SWT.NONE);
		        monitors[3] = new SWTPerformanceMonitor(bps[3].getMetaData(), tf, SWT.NONE);
		        monitors[4] = new SWTPerformanceMonitor(bps[4].getMetaData(), tf, SWT.NONE);
		        monitors[5] = new SWTPerformanceMonitor(bp.getMetaData(), tf, SWT.NONE);
		   
				
				TabItem item1 = new TabItem(tf, SWT.NONE);
				item1.setText("SWTSink");
				item1.setControl(sink);
				
				TabItem item2 = new TabItem(tf, SWT.NONE);
				item2.setText("MetaData Monitor (BufferPipe 1)");
				item2.setControl(monitors[0]);
				
				TabItem item3 = new TabItem(tf, SWT.NONE);
				item3.setText("MetaData Monitor (BufferPipe 2)");
				item3.setControl(monitors[1]);
				
				TabItem item4 = new TabItem(tf, SWT.NONE);
				item4.setText("MetaData Monitor (BufferPipe 3)");
				item4.setControl(monitors[2]);
				
				TabItem item5 = new TabItem(tf, SWT.NONE);
				item5.setText("MetaData Monitor (BufferPipe 4)");
				item5.setControl(monitors[3]);
				
				TabItem item6 = new TabItem(tf, SWT.NONE);
				item6.setText("MetaData Monitor (BufferPipe 5)");
				item6.setControl(monitors[4]);
				
				TabItem item7 = new TabItem(tf, SWT.NONE);
				item7.setText("MetaData Monitor (BufferPipe 6)");
				item7.setControl(monitors[5]);
		    }
		    
		    public void stopQuery() {		    	
		        if (scheduler != null) {
			    	scheduler.stopScheduling(proc);
			    	if (! sources[0].isClosed())
			    		scheduler.stopScheduling(sources[0].getProcessor());
			    	if (! sources[1].isClosed())
			    		scheduler.stopScheduling(sources[1].getProcessor());
			    	if (! sources[2].isClosed())
			    		scheduler.stopScheduling(sources[2].getProcessor());
			    	if (! sources[3].isClosed())
			    		scheduler.stopScheduling(sources[3].getProcessor());
			    	if (! sources[4].isClosed())
			    		scheduler.stopScheduling(sources[4].getProcessor());
		        }
		        if (sink != null)
		        	sink.closeAllSources();
			}
		};
		
		q[4] = d.new Query("Example using BufferPipes, Scheduler and Memory Manager."
				,"SELECT timestamp, distance, lane, speed, length, direction "
				+"\nFROM Sensor1, ..., Sensor5"
				+"\nWHERE length >= '1' AND length <= '50' AND"
				+"\n      speed >= '0' AND speed  <= '100';"
				, "images/16600_S.gif"
				, "images/scheduling_plan.gif"
				, "txt/scheduling_and_memory_manager.txt"
				,new int[]{80,20}
				,new int[]{60,40}) {
		
		    SWTPerformanceMonitor[] monitors = new SWTPerformanceMonitor[6];
		    SWTSink sink;
		    TimeSliceScheduler scheduler;
		    StrategyProcessor proc;
		    HighwaySource[] sources;
		
		    public void startQuery(Composite c) {
		        c.setLayout(new FillLayout());
				TabFolder tf = new TabFolder(c, SWT.NULL);
				
				final int objectSize = 31;
				
				HighwaySource source1 = Highway.getSourceByDate(TRAFFIC_DATA_DIR,
		                new GregorianCalendar(1993, 3, 11).getTime(), 'S', 16600, (short)1, 0.1f);		       
		        HighwaySource source2 = Highway.getSourceByDate(TRAFFIC_DATA_DIR,
		                new GregorianCalendar(1993, 3, 11).getTime(), 'S', 16600, (short)2, 0.1f);		       
		        HighwaySource source3 = Highway.getSourceByDate(TRAFFIC_DATA_DIR,
		                new GregorianCalendar(1993, 3, 11).getTime(), 'S', 16600, (short)3, 0.1f);		        
		        HighwaySource source4 = Highway.getSourceByDate(TRAFFIC_DATA_DIR,
		                new GregorianCalendar(1993, 3, 11).getTime(), 'S', 16600, (short)4, 0.1f);		     
		        HighwaySource source5 = Highway.getSourceByDate(TRAFFIC_DATA_DIR,
		                new GregorianCalendar(1993, 3, 11).getTime(), 'S', 16600, (short)5, 0.1f);
		        
		        
		        sources = new HighwaySource[]{source1, source2, source3, source4, source5};
		        final int[] sourceIDs = new int[]{0, 1, 2, 3, 4};
		        
		        // size of a vehicle instance: 31 bytes
		        final BufferPipe[] bps = new BufferPipe[5];
		        for (int i = 0; i < sources.length; i++) {
		            bps[i] = new BufferPipe(sources[i], sourceIDs[i], new MemoryManageableListQueue(objectSize), 1, objectSize);
		            bps[i].getMetaDataManagement().include(DEFAULT_METADATA);
		            //bps[i].assignMemSize(MemoryManageable.MAXIMUM);
		        }
		       
		        Union union = new Union(bps);
		        
		        final BufferPipe bp = new BufferPipe(union, 0,  new MemoryManageableListQueue(objectSize), 1, objectSize);
		        bp.getMetaDataManagement().include(DEFAULT_METADATA2);
		        //bp.assignMemSize(MemoryManageable.MAXIMUM);
		        
		        Filter filter = new Filter(bp, new Predicate() {

		            protected Vehicle v;
		            protected double length, speed;

		            public boolean invoke(Object o) {
		                v = (Vehicle)o;
		                length = v.getLength();
		                speed = v.getSpeed();
		                if (length < 1d || length > 50d) return false;
		                if (speed < 0d || speed > 100d) return false;
		                return true;
		            }
		        });
		        
		        // scheduler settings
		        
	        	// MTIQ Strategy
		        proc = new StrategyProcessor(
		                new ComparatorStrategy( // MTIQ-Strategy
			                new Controllable[] {bps[0], bps[1], bps[2], bps[3], bps[4], bp}, 
			                new InverseComparator(new MemSizeComparator())
			            ), 
		                0, 
		                1000
		        );
		        proc.start();
		        
		        // Round-Robin Strategy
//		        proc = new StrategyProcessor(
//		                new RoundRobinStrategy(
//		                        new Controllable[] {bps[0], bps[1], bps[2], bps[3], bps[4], bp} 
//		                ), 
//		                0, 
//		                1000
//		        );
//		        proc.start();
		        
		        
		        scheduler = new TimeSliceScheduler(true);
		        scheduler.schedule(proc);
		        scheduler.schedule(source1.getProcessor());
		        scheduler.schedule(source2.getProcessor());
		        scheduler.schedule(source3.getProcessor());
		        scheduler.schedule(source4.getProcessor());
		        scheduler.schedule(source5.getProcessor());
		      
		        
		        // memory manager settings
		        
		        final int globalMemSize = 4096;
		        // objectSize is 31 bytes, so 132 elements can be stored at all.
		        
		        MemoryManageable[] mm = new MemoryManageable[] {bps[0], bps[1], bps[2], bps[3], bps[4], bp}; 
		        UniformStrategy s = new UniformStrategy(globalMemSize, mm);
		        new GlobalMemoryManager(s);
		        
		        for (int i=0; i< mm.length; i++)
		        	s.requestForMemory(mm[i], mm[i].getPreferredMemSize());		        
		        
		        sink = new SWTSink(filter, "SWTSink", true, tf, SWT.MIN);		        
		        
		        monitors[0] = new SWTPerformanceMonitor(bps[0].getMetaData(), tf, SWT.NONE);
		        monitors[1] = new SWTPerformanceMonitor(bps[1].getMetaData(), tf, SWT.NONE);
		        monitors[2] = new SWTPerformanceMonitor(bps[2].getMetaData(), tf, SWT.NONE);
		        monitors[3] = new SWTPerformanceMonitor(bps[3].getMetaData(), tf, SWT.NONE);
		        monitors[4] = new SWTPerformanceMonitor(bps[4].getMetaData(), tf, SWT.NONE);
		        monitors[5] = new SWTPerformanceMonitor(bp.getMetaData(), tf, SWT.NONE);
		   
				
				TabItem item1 = new TabItem(tf, SWT.NONE);
				item1.setText("SWTSink");
				item1.setControl(sink);
				
				TabItem item2 = new TabItem(tf, SWT.NONE);
				item2.setText("MetaData Monitor (BufferPipe 1)");
				item2.setControl(monitors[0]);
				
				TabItem item3 = new TabItem(tf, SWT.NONE);
				item3.setText("MetaData Monitor (BufferPipe 2)");
				item3.setControl(monitors[1]);
				
				TabItem item4 = new TabItem(tf, SWT.NONE);
				item4.setText("MetaData Monitor (BufferPipe 3)");
				item4.setControl(monitors[2]);
				
				TabItem item5 = new TabItem(tf, SWT.NONE);
				item5.setText("MetaData Monitor (BufferPipe 4)");
				item5.setControl(monitors[3]);
				
				TabItem item6 = new TabItem(tf, SWT.NONE);
				item6.setText("MetaData Monitor (BufferPipe 5)");
				item6.setControl(monitors[4]);
				
				TabItem item7 = new TabItem(tf, SWT.NONE);
				item7.setText("MetaData Monitor (BufferPipe 6)");
				item7.setControl(monitors[5]);
		    }
		    
		    public void stopQuery() {			
		        if (scheduler != null) {
			    	scheduler.stopScheduling(proc);
			        if (! sources[0].isClosed())
			        	scheduler.stopScheduling(sources[0].getProcessor());
			        if (! sources[1].isClosed())
			        	scheduler.stopScheduling(sources[1].getProcessor());
			        if (! sources[2].isClosed())
			        	scheduler.stopScheduling(sources[2].getProcessor());
			        if (! sources[3].isClosed())
			        	scheduler.stopScheduling(sources[3].getProcessor());
			        if (! sources[4].isClosed())
			        	scheduler.stopScheduling(sources[4].getProcessor());
		        }
		        if (sink != null)
		        	sink.closeAllSources();
			}
		};
		
		d.setQueries(q);
		return d;
	}
	
	 /*******************************************************************/
    /*                 USE-CASES: online auctions                      */
    /*******************************************************************/

    /************************************************/
    /* ==> ASSUMPTION: timestamp is in seconds      */
    /************************************************/
	
	/**
	 * 
	 * @return the nexmark demo
	 */
	public static Demo getNEXMarkDemo() {
		Demo d = new Demo("NEXMark", null);
		Query[] q = new Query[6];
		
		q[0] = d.new Query("Select all bids on items 5, 11, 22, 42 and 120."
				,"SELECT itemID, price"
				+"\nFROM Bid"
				+"\nWHERE itemID = 5 OR itemID = 11 OR "
				+"\n      itemID = 22 OR itemID = 42 OR itemID = 120;"
				, "images/nexmark.gif"
				, "images/selection_plan.gif"
				, "txt/selection.txt"
				,new int[]{80,20}
				,new int[]{60,40}) {

		    SWTSink sink;		    
		    
			public void startQuery(Composite c) {
			    c.setLayout(new FillLayout());
				TabFolder tf = new TabFolder(c, SWT.NULL);			    			    
			    
			    final int[] itemIDs = new int[] {5, 11, 22, 42, 120};
		        
		        NEXMarkGeneratorSource bids = new NEXMarkGeneratorSource(AUCTION_DATA_DIR,
		                CASTOR_MAPPING_FILE, NEXMarkGeneratorSource.BID_STREAM);		 

		        Filter filter = new Filter(bids, new Predicate() {
		            public boolean invoke(Object o) {
		                Bid bid = (Bid)((TemporalObject)o).getObject();
		                long itemID = bid.getItemID();
		                for (int i = 0; i < itemIDs.length; i++)
		                    if (itemIDs[i] == itemID)
		                        return true;
		                return false;
		            }
		        });
		        
		        Mapper map = new Mapper(filter, new Function() {
		            public Object invoke(Object o) {
		                Bid bid = (Bid)((TemporalObject)o).getObject();
		                return new ItemPrice(bid.getItemID(), bid.getBid());
		            }
		        });


		        sink = new SWTSink(map, "SWTSink", true, tf, SWT.MIN);

		        map.getMetaDataManagement().include(DEFAULT_METADATA2);
		        
		        SWTPerformanceMonitor monitor = new SWTPerformanceMonitor(map.getMetaData(), tf, SWT.NONE);
				
				TabItem item1 = new TabItem(tf, SWT.NONE);
				item1.setText("SWTSink");
				item1.setControl(sink);
				
				TabItem item2 = new TabItem(tf, SWT.NONE);
				item2.setText("MetaData Monitor (Map)");
				item2.setControl(monitor);
				
			
			}			
			public void stopQuery() {
				if (sink != null)
					sink.closeAllSources();
			}
		};
		
		q[1] = d.new Query("Monitor the average closing price across items in each category over the last hour."
				,"SELECT categoryID, AVG(price)"
				+"\nFROM ClosedAuctions [RANGE 1 hour]"
                +"\nGROUP BY categoryID;"			
				, "images/nexmark.gif"
				, "images/avg_closing_price_plan.gif"
				, "txt/avg_closing_price.txt"
				,new int[]{80,20}
				,new int[]{60,40}) {

		    SWTSink sink;
		    SWTSink sink2;
		    
			public void startQuery(Composite c) {
			    c.setLayout(new FillLayout());
				TabFolder tf = new TabFolder(c, SWT.NULL); 		


			    NEXMarkGeneratorSource closedAuctions = new NEXMarkGeneratorSource(AUCTION_DATA_DIR,
		                CASTOR_MAPPING_FILE, NEXMarkGeneratorSource.CLOSED_AUCTION_STREAM, 
		                NEXMarkGeneratorSource.GET_CLOSED_AUCTION_FUNCTION(), 0.5f, true
		        );		       

		        final long lifespan = 1*60*60; // 1 hour
		        TemporalWindow window = new TemporalWindow(closedAuctions, lifespan);
		                
		        int noOfCategories = 302;
		        HashGrouper g = new HashGrouper(window, new Function() {
		            public Object invoke(Object o) {
		                ClosedAuction c = (ClosedAuction) ((TemporalObject) o).getObject();
		                return new Integer(c.getCategory());
		            }
		        }, noOfCategories);
		        	        
		        TemporalAggregator[] aggregators = new TemporalAggregator[noOfCategories]; 
		        for (int i = 0; i < noOfCategories; i++) 
		            aggregators[i] = new TemporalAggregator(g.getReferenceToGroup(i), 
	                    new TemporalAggregatorSA(
	                            new AggregationFunction<ClosedAuction,AvgClosingPrice>() { 
	                                protected final CountAwareAverage avg = new CountAwareAverage();
	                                
	                                public AvgClosingPrice invoke(AvgClosingPrice agg, ClosedAuction next) {
	                                    ClosedAuction c = (ClosedAuction) next; 
	                                    if (agg == null)
	                                        return new AvgClosingPrice(avg.invoke(null,new Double(c.getSellPrice())),
	                                                c.getCategory()
	                                        );
	                                    AvgClosingPrice aggPrice = (AvgClosingPrice)agg;
	                                    return new AvgClosingPrice(avg.invoke(aggPrice.getAggregate(), new Double(c.getSellPrice())), 
	                                        aggPrice.category
	                                    );
	                                } 
	                            }
	                        )
	                ); 
		        
		        Union union = new Union(aggregators);

		        sink = new SWTSink(union, "SWTSink", true, tf, SWT.MIN);
		        
		        sink2 = new SWTSink(window, "Window", true, tf, SWT.MIN);
		        
		        window.getMetaDataManagement().include(DEFAULT_METADATA2);
		        
		        SWTPerformanceMonitor monitor = new SWTPerformanceMonitor(window.getMetaData(), tf, SWT.NONE);
					
		        	
				
				TabItem item1 = new TabItem(tf, SWT.NONE);
				item1.setText("SWTSink");
				item1.setControl(sink);
				
				TabItem item2 = new TabItem(tf, SWT.NONE);
				item2.setText("Window");
				item2.setControl(sink2);
				
				TabItem item3 = new TabItem(tf, SWT.NONE);
				item3.setText("MetaData Monitor (Window)");
				item3.setControl(monitor);
			}			
			
			public void stopQuery() {
				if (sink != null)
					sink.closeAllSources();
				if (sink != null)
					sink2.closeAllSources();
			}
		};
		
		q[2] = d.new Query("Report all auctions that closed within 5 hours of their opening."
				,"SELECT OpenAuction.*"
				+"\nFROM OpenAuction [Range 5 hours] O, ClosedAuction [NOW] C"
				+"\nWHERE O.auctionID = C.auctionID;"
				, "images/nexmark.gif"
				, "images/short_auctions_plan.gif"
				, "txt/short_auctions.txt"
				,new int[]{80,20}
				,new int[]{60,40}) {

		    SWTSink sink;		    
		    
			public void startQuery(Composite c) {
			    c.setLayout(new FillLayout());
				TabFolder tf = new TabFolder(c, SWT.NULL);			    			    
				
				NEXMarkGeneratorSource openAuctions = new NEXMarkGeneratorSource(AUCTION_DATA_DIR,
		                CASTOR_MAPPING_FILE, NEXMarkGeneratorSource.OPEN_AUCTION_STREAM, 1f);		       
		        
		        NEXMarkGeneratorSource closedAuctions = new NEXMarkGeneratorSource(AUCTION_DATA_DIR,
		                CASTOR_MAPPING_FILE, NEXMarkGeneratorSource.CLOSED_AUCTION_STREAM, 1f);		        
		        
		        final long lifespan1 = 5*60*60; // 5 h
		        TemporalWindow window1 = new TemporalWindow(openAuctions, lifespan1);
		        
		        final long lifespan2 = 1; // NOW
		        TemporalWindow window2 = new TemporalWindow(closedAuctions, lifespan2);
		    
		        final Function[] hashFunctions = new Function[2];
		        final int buckets = 1117;
		        hashFunctions[0] = new Function() {
					public Object invoke(Object o) {
						return new Integer((int)((OpenAuction)((TemporalObject)o).getObject()).getAuctionID()%buckets);
					}
				};
				 hashFunctions[1] = new Function() {
					public Object invoke(Object o) {
						return new Integer((int)((ClosedAuction)((TemporalObject)o).getObject()).getAuctionID()%buckets);
					}
				};
				
				final Predicate[] joinPredicates = new Predicate[2]; 
				joinPredicates[0] = new Predicate() {
		        	public boolean invoke(Object o1, Object o2) {
		        	    OpenAuction open = (OpenAuction)((TemporalObject)o2).getObject();
		        	    ClosedAuction close = (ClosedAuction)((TemporalObject)o1).getObject();
		        	    return open.getAuctionID() == close.getAuctionID();
		        	}
				};
				joinPredicates[1] = new Predicate() {
		        	public boolean invoke(Object o1, Object o2) {
		        	    OpenAuction open = (OpenAuction)((TemporalObject)o1).getObject();
		        	    ClosedAuction close = (ClosedAuction)((TemporalObject)o2).getObject();
		        	    return open.getAuctionID() == close.getAuctionID();
		        	}
				};
		        
		        TemporalJoin join = new TemporalJoin(window1, window2, 0, 1,
		            new TemporalJoinListSA(
						new HashSAImplementor(hashFunctions),
						0, joinPredicates, TemporalObject.INTERVAL_OVERLAP_REORGANIZE
					),
					new TemporalJoinListSA(
						new HashSAImplementor(hashFunctions),
						1, joinPredicates, TemporalObject.INTERVAL_OVERLAP_REORGANIZE
					),
					new Function() {
		            	public Object invoke(Object o1, Object o2) {
		            	    return o1;
		            	}
		        	}
		        );
			  

		        sink = new SWTSink(join, "SWTSink", true, tf, SWT.MIN);

		        join.getMetaDataManagement().include(DEFAULT_METADATA2);
			        
		        SWTPerformanceMonitor monitor = new SWTPerformanceMonitor(join.getMetaData(), tf, SWT.NONE);
		        
				TabItem item1 = new TabItem(tf, SWT.NONE);
				item1.setText("SWTSink");
				item1.setControl(sink);
				
				TabItem item2 = new TabItem(tf, SWT.NONE);
				item2.setText("MetaData Monitor (Join)");
				item2.setControl(monitor);
			
			}			
			public void stopQuery() {
				if (sink != null)
					sink.closeAllSources();
			}
		};
		
		q[3] = d.new Query("For each closed auction report all available information on the buyer."
				,"SELECT Person.*"
				+"\nFROM ClosedAuction C, Person P"
				+"\nWHERE C.buyerID = P.ID;"
				, "images/nexmark.gif"
				, "images/buyer_plan.gif"
				, "txt/buyer.txt"
				,new int[]{80,20}
				,new int[]{60,40}) {

		    SWTSink sink;		    
		    
			public void startQuery(Composite c) {
			    c.setLayout(new FillLayout());
				TabFolder tf = new TabFolder(c, SWT.NULL);			    			    
			    
				// sequential scan; alternatively a B+-tree could be used
		       final Function queryFunction = new Function() {
		           public Object invoke(Object queryObject) {
		               final ClosedAuction auction = (ClosedAuction)((TemporalObject)queryObject).getObject();
		               Cursor cursor = NEXMarkGeneratorSource.getPersonCursor(AUCTION_DATA_DIR+"person.xml", CASTOR_MAPPING_FILE);
		               cursor.open();
		               while (cursor.hasNext()) {
		                   Person next = (Person)cursor.next();
		                   if (auction.getBuyerId() == next.getId()) {
		                       cursor.close();
		                       return new SingleObjectCursor(next);
		                   }
		               }
		               cursor.close();
		               return EmptyCursor.DEFAULT_INSTANCE;
		           }
		       };
		       final Function closeFunction = new Function() {
		           public Object invoke() {
		               return null; 
		           }
		       };
		       final Function newResult = new Function() {
		           public Object invoke(Object o1, Object o2) {
		               return o2;
		           }
		       };       
		       
		       NEXMarkGeneratorSource closedAuctions = new NEXMarkGeneratorSource(AUCTION_DATA_DIR,
		               	CASTOR_MAPPING_FILE, NEXMarkGeneratorSource.CLOSED_AUCTION_STREAM,
		       			NEXMarkGeneratorSource.GET_CLOSED_AUCTION_FUNCTION(), 0.5f, true);		      
		       
		       IndexJoin join = new IndexJoin(closedAuctions,
		               queryFunction,
		               closeFunction,
		               newResult
		       );

		        sink = new SWTSink(join, "SWTSink", true, tf, SWT.MIN);

		        join.getMetaDataManagement().include(DEFAULT_METADATA2);
				
		        SWTPerformanceMonitor monitor = new SWTPerformanceMonitor(join.getMetaData(), tf, SWT.NONE);
						
		        
				TabItem item1 = new TabItem(tf, SWT.NONE);
				item1.setText("SWTSink");
				item1.setControl(sink);
				
				TabItem item2 = new TabItem(tf, SWT.NONE);
				item2.setText("MetaData Monitor (Join)");
				item2.setControl(monitor);
			
			}			
			public void stopQuery() {
				if (sink != null)
					sink.closeAllSources();
			}
		};
		
		q[4] = d.new Query("Return the highest bid in the recent 10 minutes (fixed window)."
				,"SELECT itemID, MAX(price)"
				+"\nFROM Bid [FIXEDRANGE 10 minutes];"
				, "images/nexmark.gif"
				, "images/highest_bid_fixed_window_plan.gif"
				, "txt/highest_bid_fixed_window.txt"
				,new int[]{80,20}
				,new int[]{60,40}) {

		    SWTSink sink;
		    SWTSink sink2;
		    
			public void startQuery(Composite c) {
			    c.setLayout(new FillLayout());
				TabFolder tf = new TabFolder(c, SWT.NULL);
			    			    
				NEXMarkGeneratorSource bids = new NEXMarkGeneratorSource(AUCTION_DATA_DIR,
		                CASTOR_MAPPING_FILE, NEXMarkGeneratorSource.BID_STREAM);
				
		        final long windowSize = 10*60; // 10 min.
		        TemporalWindow fixedWindow = new TemporalWindow(bids, windowSize);
		        
		        TemporalAggregator aggregator = new TemporalAggregator(fixedWindow,
		                new TemporalAggregatorSA(
		                        new AggregationFunction<Bid,LinkedList<Bid>>() {
		                            protected Bid bid;
		                            protected LinkedList maxima;
		                            protected float maxValue;
		                            
		                            public LinkedList<Bid> invoke (LinkedList<Bid> agg, Bid next) {
		                                bid = (Bid)next;
		                                if (agg == null) {
		                                    maxima = new LinkedList();
		                                    maxima.add(bid);
		                                    return maxima;
		                                }
		                                maxima = (LinkedList)agg;
		                                maxValue = ((Bid)maxima.getFirst()).getBid();
		                                if (bid.getBid() > maxValue) {
		                                    maxima = new LinkedList();
		                                    maxima.add(bid);
		                                    return maxima;
		                                }
		                                if (bid.getBid() == maxValue) {
		                                    LinkedList<Bid> l = (LinkedList<Bid>)maxima.clone();
		                                    l.add(bid);
		                                    return l;
		                                }
		                                return (LinkedList<Bid>)maxima.clone();
		                            }
		                        }
		                )
		        );
		      
				
		        sink = new SWTSink(aggregator, "SWTSink", true, tf, SWT.MIN);
		        sink2 = new SWTSink(fixedWindow, "Window", true, tf, SWT.MIN);
		        
		        aggregator.getMetaDataManagement().include(DEFAULT_METADATA2);
					        
		        SWTPerformanceMonitor monitor = new SWTPerformanceMonitor(aggregator.getMetaData(), tf, SWT.NONE);				
				
				TabItem item1 = new TabItem(tf, SWT.NONE);
				item1.setText("SWTSink");
				item1.setControl(sink);
				
				TabItem item2 = new TabItem(tf, SWT.NONE);
				item2.setText("Window");
				item2.setControl(sink2);
				
				TabItem item3 = new TabItem(tf, SWT.NONE);
				item3.setText("MetaData Monitor (Aggregator)");
				item3.setControl(monitor);
			
			}			
			public void stopQuery() {
				if (sink != null)
					sink.closeAllSources();
				if (sink != null)
					sink2.closeAllSources();
			}
		};
		
		q[5] = d.new Query("Select the items with the most bids in the past 10 minutes."
				,"SELECT itemID, num"
				+"\nFROM (SELECT B1.itemID AS itemID, COUNT(*) AS num"
				+"\n      FROM Bid [RANGE 10 minutes] B1"
				+"\n      GROUP BY B1.itemID)"
				+"\nWHERE num >= ALL (SELECT COUNT(*)"
				+"\n                  FROM Bid [RANGE 10 minutes] B2"
				+"\n                  GROUP BY B2.itemID);"
				, "images/nexmark.gif"
				, "images/hot_items_plan.gif"
				, "txt/hot_items.txt"
				,new int[]{80,20}
				,new int[]{60,40}) {

		    SWTSink sink;
		    SWTSink sink2;
		    SWTSink sink3;
		    
			public void startQuery(Composite c) {
			    c.setLayout(new FillLayout());
				TabFolder tf = new TabFolder(c, SWT.NULL);
			    			    
				
				NEXMarkGeneratorSource bids = new NEXMarkGeneratorSource(AUCTION_DATA_DIR,
		                CASTOR_MAPPING_FILE, NEXMarkGeneratorSource.BID_STREAM);

		        final long lifespan = 10*60;
		        TemporalWindow window = new TemporalWindow(bids, lifespan);
		        
		        final int noOfItems = 1054;
		        HashGrouper group = new HashGrouper(window, new Function() {
		            public Object invoke(Object o) {
		                return new Integer((int)((Bid)((TemporalObject)o).getObject()).getItemID());
		            }
		        }, noOfItems);
		        
		        TemporalAggregator[] aggs = new TemporalAggregator[noOfItems];
		        for (int i = 0; i < noOfItems; i++) {
		            aggs[i] = new TemporalAggregator(group.getReferenceToGroup(i), 
		                    new TemporalAggregatorSA(
		                            new AggregationFunction<Bid,BidCount>() {
		                                protected Bid bid;
		                              
		                                public BidCount invoke(BidCount agg, Bid next) {
		                                    bid = (Bid)next;
		                                    if (agg == null) 
		                                        return new BidCount(bid.getItemID(), 1);
		                                    return new BidCount(((BidCount)agg).itemID, ((BidCount)agg).count+1);
		                                }
		                            }
		                    )
		            );
		        }
		        TemporalUnion union = new TemporalUnion(aggs, new TemporalUnionSA(noOfItems));
		        // improvement: use heartbeats
		        
		        TemporalAggregator max = new TemporalAggregator(union, 
		                new TemporalAggregatorSA(
		                        new AggregationFunction<BidCount,LinkedList<BidCount>>() {
		                            protected BidCount bidCount;
		                            protected LinkedList maxima;
		                            protected long maxValue;
		                            
		                            public LinkedList<BidCount> invoke (LinkedList<BidCount> agg, BidCount next) {
		                                bidCount = (BidCount)next;
		                                if (agg == null) {
		                                    maxima = new LinkedList();
		                                    maxima.add(bidCount);
		                                    return maxima;
		                                }
		                                maxima = (LinkedList)agg;
		                                maxValue = ((BidCount)maxima.getFirst()).count;
		                                if (bidCount.count > maxValue) {
		                                    maxima = new LinkedList();
		                                    maxima.add(bidCount);
		                                    return maxima;
		                                }
		                                if (bidCount.count == maxValue) {
		                                	LinkedList<BidCount> l = (LinkedList<BidCount>)maxima.clone();
		                                    l.add(bidCount);
		                                    return l;
		                                }
		                                return (LinkedList<BidCount>)maxima.clone();
		                            }
		                        }
		                )
		        );        


		        sink = new SWTSink(max, "SWTSink", true, tf, SWT.MIN);
		        sink2 = new SWTSink(window, "Window", true, tf, SWT.MIN);
		        sink3 = new SWTSink(group.getReferenceToGroup(42), "Group 42", true, tf, SWT.MIN);
		        
		        union.getMetaDataManagement().include(DEFAULT_METADATA2);
					
				SWTPerformanceMonitor monitor = new SWTPerformanceMonitor(union.getMetaData(), tf, SWT.NONE);					
				
				TabItem item1 = new TabItem(tf, SWT.NONE);
				item1.setText("SWTSink");
				item1.setControl(sink);
				
				TabItem item2 = new TabItem(tf, SWT.NONE);
				item2.setText("Window");
				item2.setControl(sink2);
				
				TabItem item3 = new TabItem(tf, SWT.NONE);
				item3.setText("Group 42");
				item3.setControl(sink3);
				
				TabItem item4 = new TabItem(tf, SWT.NONE);
				item4.setText("MetaData Monitor (Union)");
				item4.setControl(monitor);
			
			}			
			public void stopQuery() {
				if (sink != null)
					sink.closeAllSources();
				if (sink2 != null)
					sink2.closeAllSources();
				if (sink3 != null)
					sink3.closeAllSources();
			}
		};	
		
		d.setQueries(q);
		return d;
	}
	
	/**	
	 * Contains all information to demonstrate a query in PipeDemo. 
	 */
	public abstract class Query {
			
		/**
		 * A flag, signing that the vertical and horizontal weights
		 * are valid. if they are valid, they will be used. 
		 */
		boolean weightValid;
		
		/**
		 * The name of the query, that will appear in a comboxbox.
		 */
		private String name;
		
		/**
		 * A path to a picture. It will be displayed in the left window and
		 * should be used to illustrate the query in general. 
		 */
		private String image;
		
		/**
		 * The CQL representation of a query that can be displayed
		 * in the right window.
		 */
		private String cql;
		
		/**
		 * The java code representation of a query that can be displayed
		 * in the right window.
		 */
		private String javaFile;
		
		/**
		 * The graph representation of a query that can be displayed
		 * in the right window.
		 */
		private String queryImage;
		
		/**
		 * determines the propotion between the space of the upper 
		 * (consisting of the right and left window) and lower area 
		 * of the shell.
		 */
		private int[] verticalWeights;

		/**
		 * determines the propotion between the space of the right 
		 * and left window.
		 */
		private int[] horizontalWeights;
		
		/**
		 * Creates a new Query <br>	 
		 * @param name The name of the query, that will appear in a comboxbox.
		 * @param cql The CQL representation of a query that can be displayed
		 * in the right window.
		 * @param image A path to a picture. It will be displayed in the left window and
		 * should be used to illustrate the query in general.
		 * @param queryImage The graph representation of a query that can be displayed
		 * in the right window.
		 * @param javaFile The java code representation of a query that can be displayed
		 * in the right window.
		 * @param verticalWeights determines the propotion between the space of the upper 
		 * (consisting of the right and left window) and lower area of the shell.
		 * @param horizontalWeights determines the propotion between the space of the right 
		 * and left window.
		 */
		public Query(String name, String cql, String image, String queryImage, 
				String javaFile, int[] verticalWeights, int[] horizontalWeights) {
			check(verticalWeights);
			check(horizontalWeights);		
			this.verticalWeights = verticalWeights;
			this.horizontalWeights = horizontalWeights;
			this.name = name;
			this.cql = cql;		
			this.image = image;
			this.queryImage = queryImage;
			this.javaFile = javaFile;		
		}
		
		
		/**
		 * Creates a new Query <br> 
		 * @param name The name of the query, that will appear in a comboxbox.
		 * @param cql The CQL representation of a query that can be displayed
		 * in the right window.
		 * @param image A path to a picture. It will be displayed in the left window and
		 * should be used to illustrate the query in general.
		 * @param queryImage The graph representation of a query that can be displayed
		 * in the right window.
		 * @param javaFile The java code representation of a query that can be displayed
		 * in the right window.
		 */
		 public Query(String name, String cql, String image, String queryImage, String javaFile) {
			this(name, cql, image, queryImage, javaFile, new int[]{-1, -1}, new int[]{-1, -1});
		}
		
	   /**
		* Returns a string representation of the query. 
		* @return returns a string representation of the query.
	 	*/		 
		public String toString() {
			return "Query: "+ name+" CQL:  "+cql+" Image: "+image;
		}
		
	   /**
		* Returns the name that will be displayed in the combobox. 
		* @return returns the name that will be displayed in the combobox.
	 	*/		 
		public String getName() {
			return name;
		}	

		private void check(int[] values ) {
			if (values == null)
				throw new IllegalArgumentException("array must not be null");
			if (values.length != 2)
				throw new IllegalArgumentException("array length must be two");
			if ((values[0] >= 0 && values[1] <0)||(values[1] >= 0 && values[0] <0))
				throw new IllegalArgumentException("both values must be >= zero or below");
			if (values[0] >= 0 && values[1] >= 0)
				weightValid = true;
		}
		
		
	   /**
		* return true, if the weights are valid.
		* @return true, if the weights are valid.
		*/
		public boolean isWeightValid() {
			return weightValid;
		}

	   /**
		* return the cql representation of the query.
		* @return the cql representation of the query.
		*/		
		public String getCQL() {
			return cql;
		}
		
	   /**
		* return the path to the picture, that illustrates the demo.
		* @return the path to the picture.
		*/		
		public String getImage() {
			return image;
		}
		
		/**
		 * @return Returns the horizontalWeights.
		 */	
		public int[] getHorizontalWeights() {
			return horizontalWeights;
		}		

		/**
		 * @return Returns the verticalWeights.
		 */
		public int[] getVerticalWeights() {
			return verticalWeights;
		}

		/**
		 * @param weightValid The weightValid to set.
		 */
		public void setWeightValid(boolean weightValid) {
			this.weightValid = weightValid;
		}
		/**
		 * @param horizontalWeights The horizontalWeights to set.
		 */
		public void setHorizontalWeights(int[] horizontalWeights) {
			this.horizontalWeights = horizontalWeights;
		}
		/**
		 * @return Returns the javaFile.
		 */
		public String getJavaFile() {
			return javaFile;
		}
		/**
		 * @return Returns the queryImage.
		 */
		public String getQueryImage() {
			return queryImage;
		}

		/**
		 * This method is invoked to execute the demo. it can use
		 * the lower area for illustration (e.g. swtsink) 
		 * @param c 
		 */
		public abstract void startQuery(Composite c);
		
		/**
		 * This method is invoked to stop the demo. it has to clear all
		 * non graphical objects that were created for the demo.
		 *
		 */
		public abstract void stopQuery();
	}
	
	private String name;
	private Query[] queries;
	
	/**
	 * Constructs a new demo with the specified name and the given queries. 
	 * @param name
	 * @param queries
	 */
	public Demo(String name, Query[] queries){
		this.name = name;
		this.queries = queries;
	}
		
	/**
	 * Returns a string representation of the demo. 
	 * @return returns a string representation of the demo.
	 */
	public String toString() {
		return "Demo: "+ queries+" Queries: "+queries.toString();
	}	
	

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @param queries The queries to set.
	 */
	public void setQueries(Query[] queries) {
		this.queries = queries;
	}
	
	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return Returns the queries.
	 */
	public Query[] getQueries() {
		return queries;
	}
	
	/**
     * A helper class. 
     */
	public static class AvgSpeedPerSection {
        
        protected DecimalFormat df = new DecimalFormat( "0.00" );
        protected double speed;
        protected int distanceToCity;
        protected Number[] agg;
        
        public AvgSpeedPerSection(Number[] agg, int distanceToCity) {
            this.agg = agg;
            this.speed = ((Double)agg[0]).doubleValue();
            this.distanceToCity = distanceToCity;
        }
        
        public Number[] getAggregate() {
            return agg;
        }
        
        public String toString() {
            return "(avg. speed: "+df.format(speed)+"(m/s); distance to Marina: "+distanceToCity+"(feet))";
        }
        
		/**
		 * @return Returns the agg.
		 */
		public Number[] getAgg() {
			return agg;
		}
		/**
		 * @param agg The agg to set.
		 */
		public void setAgg(Number[] agg) {
			this.agg = agg;
		}
		/**
		 * @return Returns the df.
		 */
		public DecimalFormat getDf() {
			return df;
		}
		/**
		 * @param df The df to set.
		 */
		public void setDf(DecimalFormat df) {
			this.df = df;
		}
		/**
		 * @return Returns the distanceToCity.
		 */
		public int getDistanceToCity() {
			return distanceToCity;
		}
		/**
		 * @param distanceToCity The distanceToCity to set.
		 */
		public void setDistanceToCity(int distanceToCity) {
			this.distanceToCity = distanceToCity;
		}
		/**
		 * @return Returns the speed.
		 */
		public double getSpeed() {
			return speed;
		}
		/**
		 * @param speed The speed to set.
		 */
		public void setSpeed(double speed) {
			this.speed = speed;
		}
    }
    
    /**
     * A helper class. 
     */
    public static class ItemPrice {
        
        protected DecimalFormat df = new DecimalFormat( "0.00" );
        protected long itemID;
        protected float price;
        
        public ItemPrice(long itemID, float price) {
            this.itemID = itemID;
            this.price = price;
        }
        
        public String toString() {
            return "(itemID: "+itemID+", price: "+df.format(price)+" €)";
        }
        
    }
    
    /**
     * A helper class. 
     */
    public static class AvgClosingPrice {
        
		/**
		 * @return Returns the category.
		 */
		public int getCategory() {
			return category;
		}
        protected DecimalFormat df = new DecimalFormat( "0.00" );
        protected int category;
        protected double avgPrice;
        protected Number[] agg;
        
        public AvgClosingPrice(Number[] agg, int category) {
            this.agg = agg;
            this.category = category;
            this.avgPrice = ((Double)agg[0]).doubleValue();
        }
        
        public Number[] getAggregate() {
            return agg;
        }
        
        public String toString() {
            return "(category: "+category+"; average closing price: "+df.format(avgPrice)+" €)";
        }
    }
    
    /**
     * A helper class. 
     */
    public static class BidCount {
        
        protected long itemID;
        protected long count;
        
        public BidCount(long itemID, long count) {
            this.itemID = itemID;
            this.count = count;
        }
        
        public String toString() {
            return "itemID: "+itemID+"; count: "+count;
        }

		public long getCount() {
			return count;
		}

		public long getItemID() {
			return itemID;
		}
    }
    
    /**
     * A helper class. 
     */
    public static class ClosingPrice {
        
        protected DecimalFormat df = new DecimalFormat( "0.00" );
        protected long auctionID;
        protected long buyerID;
        protected float sellingPrice;
        
        public ClosingPrice(ClosedAuction ca) {
            this.auctionID = ca.getAuctionID();
            this.buyerID = ca.getBuyerId();
            this.sellingPrice = ca.getSellPrice();
        }
        
        public String toString() {
            StringBuffer s = new StringBuffer();
            s.append("(auctionID: "+auctionID);
            if (buyerID == -1)
                s.append(" -> item not sold)");
            else
                s.append(", buyerID: "+buyerID+", selling price: "+df.format(sellingPrice)+" €)");
            return s.toString();
        }
    }
	
	
	
}
