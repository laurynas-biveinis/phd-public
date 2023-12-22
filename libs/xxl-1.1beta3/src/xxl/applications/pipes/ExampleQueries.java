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
package xxl.applications.pipes;

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
import java.util.List;
import java.util.Random;

import xxl.applications.pipes.auctions.Bid;
import xxl.applications.pipes.auctions.ClosedAuction;
import xxl.applications.pipes.auctions.NEXMarkGeneratorSource;
import xxl.applications.pipes.auctions.OpenAuction;
import xxl.applications.pipes.auctions.mapping.Person;
import xxl.applications.pipes.sigmoddemo.Demo.AvgClosingPrice;
import xxl.applications.pipes.sigmoddemo.Demo.AvgSpeedPerSection;
import xxl.applications.pipes.sigmoddemo.Demo.BidCount;
import xxl.applications.pipes.traffic.Highway;
import xxl.applications.pipes.traffic.HighwaySource;
import xxl.applications.pipes.traffic.Vehicle;
import xxl.core.collections.sweepAreas.DefaultMemoryManageableSA;
import xxl.core.collections.sweepAreas.HashSAImplementor;
import xxl.core.collections.sweepAreas.ImplementorBasedSweepArea;
import xxl.core.collections.sweepAreas.SweepArea;
import xxl.core.cursors.Cursor;
import xxl.core.cursors.sources.DiscreteRandomNumber;
import xxl.core.cursors.sources.EmptyCursor;
import xxl.core.cursors.sources.Repeater;
import xxl.core.cursors.sources.SingleObjectCursor;
import xxl.core.functions.Function;
import xxl.core.functions.NTuplify;
import xxl.core.math.functions.AggregationFunction;
import xxl.core.math.statistics.parametric.aggregates.CountAwareAverage;
import xxl.core.math.statistics.parametric.aggregates.Maximum;
import xxl.core.pipes.elements.TemporalObject;
import xxl.core.pipes.elements.TimeInterval;
import xxl.core.pipes.elements.TimeStampedObject;
import xxl.core.pipes.operators.AbstractPipe;
import xxl.core.pipes.operators.AbstractTimeStampPipe;
import xxl.core.pipes.operators.Pipe;
import xxl.core.pipes.operators.Pipes;
import xxl.core.pipes.operators.differences.TemporalDifference;
import xxl.core.pipes.operators.differences.TemporalDifference.TemporalDifferenceSA;
import xxl.core.pipes.operators.distincts.TemporalDistinct;
import xxl.core.pipes.operators.distincts.TemporalDistinct.TemporalDistinctSA;
import xxl.core.pipes.operators.filters.Dropper;
import xxl.core.pipes.operators.filters.Filter;
import xxl.core.pipes.operators.groupers.HashGrouper;
import xxl.core.pipes.operators.joins.IndexJoin;
import xxl.core.pipes.operators.joins.Join;
import xxl.core.pipes.operators.joins.Joins;
import xxl.core.pipes.operators.joins.TemporalJoin;
import xxl.core.pipes.operators.joins.TemporalJoin.TemporalJoinListSA;
import xxl.core.pipes.operators.mappers.Aggregator;
import xxl.core.pipes.operators.mappers.Mapper;
import xxl.core.pipes.operators.mappers.TemporalAggregator;
import xxl.core.pipes.operators.mappers.TimeGranularity;
import xxl.core.pipes.operators.mappers.TemporalAggregator.TemporalAggregatorSA;
import xxl.core.pipes.operators.unions.TemporalUnion;
import xxl.core.pipes.operators.unions.Union;
import xxl.core.pipes.operators.unions.TemporalUnion.TemporalUnionSA;
import xxl.core.pipes.operators.windows.TemporalWindow;
import xxl.core.pipes.queryGraph.QueryExecutor;
import xxl.core.pipes.sinks.AbstractSink;
import xxl.core.pipes.sinks.Printer;
import xxl.core.pipes.sinks.Tester;
import xxl.core.pipes.sinks.VisualSink;
import xxl.core.pipes.sources.CursorSource;
import xxl.core.pipes.sources.Enumerator;
import xxl.core.pipes.sources.RandomNumber;
import xxl.core.pipes.sources.Source;
import xxl.core.pipes.sources.SyncTimeStampSource;
import xxl.core.predicates.Equal;
import xxl.core.predicates.Predicate;
import xxl.core.util.random.DiscreteRandomWrapper;
import xxl.core.util.random.JavaDiscreteRandomWrapper;

/**
 * Provides numberous examples to demonstrate the usage of pipes for stream processing.<br>
 * Content<br>
 * 1) Different types of data sources and sinks.<br>
 * 	1.1) Simple synthetic sources<br>
 *  1.2) NexMark generator<br>
 *  1.2) Real data set from Free Service Patrol<br>
 * 2) Queries with focus on a single operator<br>
 * 	2.1) Selections<br>
 *  2.2) Maps (projection, temporal window, timegranularity)<br>
 *  2.3) Unions<br>
 *  2.4) Joins<br>
 *  2.5) Aggregates<br>
 *  2.6) Distinct.<br>
 *  2.7) Difference.<br>
 * 3) Complex queries <br>
 *  3.1) Queries using real data sets from Free Service Patrol <br>
 *  3.1.1) Merge some streams via TemporalUnion and remove noise with Filter.<br>
 *  3.1.2) Merge streams with TemporalUnion and compute the average speed.<br>
 *  3.1.3) Jam detection involves Mapper, Temporalwindow, TemporalAggregate, and Filter.<br>
 *  3.2) Queries using NexMark<br>
 *  3.2.1) Monitor the average closing price across items in each category over the last hour.<br>
 *  3.2.2) Report all auctions that closed within 5 hours of their opening.<br>
 *  3.2.3) Merge stream and relation: For each closed auction report all available information on the buyer.<br>
 *  3.2.4) Get hot items: Select the items with the most bids in the past 10 minutes.<br>
 * 4) Metadata examples<br>
 */
public class ExampleQueries {
	
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
	
	public static Function<Object[], String> objectArrayToString() {
		return new Function<Object[], String>(){
			StringBuffer buf = new StringBuffer();
			@Override
			public String invoke(Object[] o) {
				buf.delete(0, buf.length());
				buf.append("[");
				for (int i=0; i< o.length; i++)
					buf.append(o[i].toString() +(i<o.length-1 ? ",":""));
				buf.append("]");
				return buf.toString();
			}
		};		
	}
	
	public static Function<TemporalObject<Object[]>, String> objectArrayTOToString() {
		return new Function<TemporalObject<Object[]>, String>(){
			StringBuffer buf = new StringBuffer();
			@Override
			public String invoke(TemporalObject<Object[]> o) {
				buf.delete(0, buf.length());
				buf.append("<[");
				for (int i=0; i< o.getObject().length; i++)
					buf.append(o.getObject()[i].toString() +(i<o.getObject().length-1 ? ",":""));
				buf.append("]"+o.getTimeInterval().toString()+">");
				return buf.toString();
			}
		};		
	}
	
    /**
     * Removes measurement errors. 
     */    
    public static Source<TemporalObject<Vehicle>> clean(Source<TemporalObject<Vehicle>> source) {
    	return new Filter<TemporalObject<Vehicle>>(source, 
    		new Predicate<TemporalObject<Vehicle>>() {    			
    			protected double length, speed;
    			
    			@Override
				public boolean invoke(TemporalObject<Vehicle> v) {    				
    				length = v.getObject().getLength();
    				speed = v.getObject().getSpeed();
    				if (length < 1d || length > 50d)
    					return false;
    				if (speed <  0d || speed > 100d)
    					return false;
    				return true; 	
    			}
    		}
    	);
    }
	
	public static<I,O> void insertGUIBeforeAndAfter(QueryExecutor exe, Pipe<I,O> pipe){
		insertGUIBeforeAndAfter(exe, pipe, null, null);
	}

	/** 
	 * A helper method to demonstrate the effect of operators. It registers VisualSink that display
	 *  the elements of all inputs and the output of the given operator.  
	 */
	public static<I,O> void insertGUIBeforeAndAfter(QueryExecutor exe, Pipe<I,O> pipe, Function<I,?> inputMap, Function<O,?> outputMap) {		
		VisualSink[]inputs = new VisualSink[pipe.getNoOfSources()];
		VisualSink<?> output;
		for (int i = 0; i <= inputs.length; i++) {
			if (i<inputs.length) {
				if (inputMap == null)
					inputs[i]= new VisualSink<I>(pipe.getSource(i), "Input "+i+" of "+pipe.getClass().getSimpleName(), true);
				else 
					inputs[i]= new VisualSink<I>(new Mapper(pipe.getSource(i), inputMap), "Input "+i+" of "+pipe.getClass().getSimpleName(), true);				
				inputs[i].getFrame().setLocation(i%2==0 ? 0: 630, i/2 * 400);
				exe.registerQuery(inputs[i]);
			}
			else {	
				
				if (outputMap == null)
					output= new VisualSink(pipe, "Output of "+pipe.getClass().getSimpleName(), true);
				else 
					output= new VisualSink(new Mapper(pipe, outputMap), "Output of "+pipe.getClass().getSimpleName(), true);
				output.getFrame().setLocation(i%2==0 ? 0: 630, i/2 * 400);
				exe.registerQuery(output);
			}
		}
	}
	
	/**
	* Create a new source producing 200 discrete random integer with a constant output rate
	* of 5 millis and writes its elements to System.out
	*/
	public static void sourceRandomIntegers() {
		int numberOfElements = 200;
		int period = 5;
		Source<Integer> source = new RandomNumber<Integer>(
				new JavaDiscreteRandomWrapper(), // new JavaDiscreteRandomWrapper(100) to restrict the range of the values
				numberOfElements,
				period);
		AbstractSink<Integer> printer = new Printer<Integer>(source);
		QueryExecutor exe = new QueryExecutor(); 
		exe.registerAndStartQuery(printer);
	}
	
	/**
	 * Create a stream consisting of integer from 1000 to 12000 with a constant output rate
	 * of 5 millis. Write every 50 millis how many elements have been seen so far to System.out.
	 */
	public static void sourceEnumerator() {
		int start = 1000;
		int end = 12000;
		int period = 5;
		int outputEvery = 50;
		Source<Integer> source = new Enumerator(start, end, period);
		AbstractSink<Integer> printer = new Tester<Integer>(source, outputEvery);
		QueryExecutor exe = new QueryExecutor(); 
		exe.registerAndStartQuery(printer);
	}
	
	/**
	 * Create a source that uses an Iterator and the class CursorSource to produce
	 * elements and write its elements to VisualSink.
	 */
	public static void sourceCursorSource() {
		int period = 100;
		// Adding the even integer from 100 to 500 to a list 
		List<Integer> list = new LinkedList<Integer>();
		for (int i=100; i<=500; i++)
			if (i%2 == 0)
				list.add(i);						
		Source<Integer> source = new CursorSource<Integer>(list.iterator(), period);
		AbstractSink<Integer> sink = new VisualSink<Integer>(source,"source that uses an Iterator and the class CursorSource", true);
		QueryExecutor exe = new QueryExecutor();
		exe.registerQuery(sink); // QueryExecutor is not needed, as there is only one query and the gui provides a start button 
	}
	
	/** 
	 * Create a source that creates a open auction stream using NEXMark.
	 * CQL
	 * SELECT * FROM OpenAution;
	 */
	public static void sourceNEXMark() {
		// generate a source that delivers a bid stream
		Source<OpenAuction> auction = new NEXMarkGeneratorSource(AUCTION_DATA_DIR,
                CASTOR_MAPPING_FILE, NEXMarkGeneratorSource.OPEN_AUCTION_STREAM);  
		VisualSink <OpenAuction> sink = new VisualSink <OpenAuction>(auction, "sourceNEXMark - SELECT * FROM OpenAution;",  true); 
	}
	
	/**
	 * Create a source that emits data collected from the FSP.
	 * CQL
	 * SELECT *
	 * FROM Sensor1, ..., Sensor5"
	 */
	public static void sourceHighway() {
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

		Union<Vehicle> union = new Union<Vehicle>(new Source[]{source1, source2, source3, source4, source5});

		Filter<Vehicle> filter = new Filter<Vehicle>(union, new Predicate<Vehicle>() {
		    protected double length, speed;		   
		    @Override
			public boolean invoke(Vehicle v) {		        
		        length = v.getLength();
		        speed = v.getSpeed();
		        if (length < 1d || length > 50d) return false;
		        if (speed < 0d || speed > 100d) return false;
		        return true;
		    }
		});
		new VisualSink<Vehicle>(filter," Create a source that emits data collected from the FSP - SELECT * FROM Sensor1, ..., Sensor5", true);		
	}
	
	/**
	 * Generates a synthetic stream and drops every second element.
	 */
	public static void selectionSimple() {
		Predicate<Integer> predicate = new Predicate<Integer> () {
			@Override
			public boolean invoke (Integer o) {
				return o%2 == 0; 
			}
		};
		AbstractPipe<Integer, Integer> selection = new Filter<Integer>(
				new Enumerator(1000, 10),
				predicate);
		QueryExecutor exe = new QueryExecutor();		
		insertGUIBeforeAndAfter(exe, selection);		
		exe.startAllQueries();
		}
	
	/** 
	 * Report every Bid for a selected auction.
	 */
	public static void selectionWatchAuction() {
		final long selectedAuction = 42;
		// generate a source that delivers a bid stream
		Source<TemporalObject<Bid>> bids = new NEXMarkGeneratorSource(AUCTION_DATA_DIR,
                CASTOR_MAPPING_FILE, NEXMarkGeneratorSource.BID_STREAM);  

		Predicate<TemporalObject<Bid>> predicate = new Predicate<TemporalObject<Bid>> () {
			@Override
            public boolean invoke(TemporalObject<Bid> bid) {
                return selectedAuction == bid.getObject().getItemID(); // A Bid is reported, if its auction id is equal to the selected auction id 
            }
		};
		AbstractPipe<TemporalObject<Bid>, TemporalObject<Bid>> selection = new Filter<TemporalObject<Bid>>(
				bids,
				predicate);
		QueryExecutor exe = new QueryExecutor();		
		insertGUIBeforeAndAfter(exe, selection);		
		exe.startAllQueries();
		}
	/** 
	 * Example for load shedding in the highway szenario.
	 * This query could be part of a query, which detects taffic jams. If
	 * the system load is high, the system may decide to drop some elements.    
	 */
	public static void selectionLoadShedding() {
		final double speed = 20;
		// Create a source that delivers elements from the FSP real data set.
		Source<Vehicle> source = Highway.getSourceByDate(TRAFFIC_DATA_DIR,
	        // GregorianCalendar(date, direction ('N' or 'S'), measure station identifier, lane number (usually from 0 to 5));
			new GregorianCalendar(1993, 3, 11).getTime(), 'S', 16600, (short)1);
		// Example for semantic load shedding. Drop any elements where the speed is above 50 miles per hour
		Predicate<Vehicle> dropPredicate = new Predicate<Vehicle> () {
			@Override
            public boolean invoke(Vehicle v) {
                return  v.getSpeed() > speed;
            }
		};
		// generate a source that delivers a bid stream
		AbstractPipe<Vehicle, Vehicle> dropper = new Dropper(source, dropPredicate);
		// Example for random load shedding. Drop the half of all elements.
		//AbstractPipe<Vehicle, Vehicle> dropper = new Dropper(source, 2);
		QueryExecutor exe = new QueryExecutor();		
		insertGUIBeforeAndAfter(exe, dropper);		
		exe.startAllQueries();
		}

	/**
	 *  Apply projection to stream in highway Szenario using the class Mapper. An element (timestamp,id, lane, length, speed)
	 *  is mapped to (id, lane, speed).
	 *  CQL
	 *  SELECT id, lane, speed FROM Sensor1;
	 */
	public static void mapSimpleMapper(){
		// A simple class to store and display the selected attributes
		class IDLaneSpeed {
			long id;
			short lane;
			double speed;
			public IDLaneSpeed(long id, short lane, double speed) {
				super();
				this.id = id;
				this.lane = lane;
				this.speed = speed;
			}
			
			@Override
			public String toString() {
				return "id: "+id+" lane: "+lane+" speed: "+speed;
			}
		}
		// Create a source that delivers elements from the FSP real data set.
		Source<Vehicle> source = Highway.getSourceByDate(TRAFFIC_DATA_DIR,
		        // GregorianCalendar(date, direction ('N' or 'S'), measure station identifier, lane number (usually from 0 to 5));
				new GregorianCalendar(1993, 3, 11).getTime(), 'S', 16600, (short)1);
		// Function maps an instance of Vehicle to an instance of IDLaneSpeed, therefore Function<Vehicle, IDLaneSpeed>.
		Function<Vehicle, IDLaneSpeed> mapping = new Function<Vehicle, IDLaneSpeed>() {
			@Override
			public IDLaneSpeed invoke(Vehicle v) {
				return new IDLaneSpeed(v.getDistanceToCity(), v.getLane(), v.getSpeed());
			}
		};
		AbstractPipe<Vehicle, IDLaneSpeed> projection = new Mapper<Vehicle, IDLaneSpeed>(source, mapping);
		QueryExecutor exe = new QueryExecutor();		
		insertGUIBeforeAndAfter(exe, projection);		
		exe.startAllQueries();
	}
	
	/** 
	 * Map an Object to TemporalObject. This is important for stateful temporal operators,
	 * because it determines expiration. In the example, an Integer is mapped to an instance of
	 * Temporalobject. Afterwards the lifespan is set to 1000 via TemporalWindow.
	 */
	public static void mapToTempObj(){
		int start = 1000;
		int end = 12000;
		int period = 5;
		final long windowSize = 1000;
		 // Create a stream consisting of integer from 1000 to 12000 with a constant output rate
		 // of 5 millis.
		Source<Integer> source = new Enumerator(start, end, period);
		// Map an Integer i to (i, [time, time + 1)), where time is the system time and window the window size above.
		Function<Integer, TemporalObject<Integer>> mapping = new Function<Integer, TemporalObject<Integer>>() {
			long curtime = 0;
			@Override
			public TemporalObject<Integer> invoke(Integer i) {
				// If window size may not be changed at runtime, one can use
				// return new TemporalObject<Integer>(i, new TimeInterval(curtime, curtime+windowsize));
				return new TemporalObject<Integer>(i, new TimeInterval(curtime+=5, curtime+1));
			}
		};
		AbstractPipe<Integer, TemporalObject<Integer>> mapper = new Mapper<Integer, TemporalObject<Integer>>(source, mapping);
		// TemporalWindow allows to change the windowsize during runtime.
		AbstractPipe<TemporalObject<Integer>, TemporalObject<Integer>> window = new TemporalWindow<Integer>(mapper, windowSize);
		QueryExecutor exe = new QueryExecutor();		
		insertGUIBeforeAndAfter(exe, window);		
		exe.startAllQueries();
	}
		
	/**
	 * Set timegranularity for TemporalObject. 
	 */
	public static void mapSetGranularity(){
		final int d = 20; // application time distance between two elements.
		final int l = 250; // lifespan
		final int g = 100; // granularity		
		// create a snthetic stream that delivers Integers. 
		Enumerator e = new Enumerator(10,1000);		
		// Provide mapping from Integer to TemporalObject. To demonstrate the time granularity operator,
		// only the Timeintervals are of interest.
		Function<Integer,TemporalObject<Integer>> mapToTSO = new Function<Integer,TemporalObject<Integer>>() {
			protected long counter = 0;
			@Override
			public TemporalObject<Integer> invoke(Integer o) {
				TemporalObject<Integer> tso = new TemporalObject<Integer>(o, new TimeInterval(counter*d, counter++*d+l));
				return tso;
			}
		};
		// 
		Mapper<Integer,TemporalObject<Integer>> m1 = new Mapper<Integer,TemporalObject<Integer>>(e, mapToTSO);
		// assign new granularity to stream (that is setting it from 1 to 100)
		TimeGranularity<Integer> granularity = new TimeGranularity<Integer>(m1, g);		
		QueryExecutor exe = new QueryExecutor();		
		insertGUIBeforeAndAfter(exe, granularity);		
		exe.startAllQueries();
	}
	
	/** 
	 * Use Union to merge streams.	 
	 */
	public static void unionMerge() {
		// create three synthetic sources
		Enumerator e1 = new Enumerator(0, 10, 100);
		Enumerator e2 = new Enumerator(10, 20, 100);
		Enumerator e3 = new Enumerator(20, 30, 100);
		// create union, merging the sources
		Union<Integer> union = new Union<Integer>(new Source[]{e1, e2, e3});
		QueryExecutor exe = new QueryExecutor();		
		insertGUIBeforeAndAfter(exe, union);		
		exe.startAllQueries();			
	}
	
	/** 
	 * Use TemporalUnion to merge streams and ensure timestamp order in output stream.
	 */
	public static void unionTemporal() {
		final int noOfElements1 = 10000;
		final int noOfElements2 = 5000;
		final int intervalSize = 50;
		final int startInc = 25;
		// for the synthetic sources
		RandomNumber<Integer> r1 = new RandomNumber<Integer>(
			RandomNumber.DISCRETE, noOfElements1, 0
		);
		RandomNumber<Integer> r2 = new RandomNumber<Integer>(
			RandomNumber.DISCRETE, noOfElements2, 0
		);		
		// decorate Integers with random timeintervals
		final Function<Object,Function<Integer,TemporalObject<Integer>>> decorateWithRandomIntervals = new Function<Object,Function<Integer,TemporalObject<Integer>>>() {
			@Override
			public Function<Integer,TemporalObject<Integer>> invoke() {
				return new Function<Integer,TemporalObject<Integer>>() {
					Random random = new Random();
					long start;
					long newStart, newEnd;
					
					@Override
					public TemporalObject<Integer> invoke(Integer o) {
						newStart = start+random.nextInt(startInc);
						newEnd   = newStart+1+random.nextInt(intervalSize-1);
						TemporalObject<Integer> object = new TemporalObject<Integer>(o, 
							new TimeInterval(newStart, newEnd)
						);
						start = newStart;
						return object;
					}
				};
			}
		};
		// create two synthetic sources		
		Source<TemporalObject<Integer>> s1 = new Mapper<Integer, TemporalObject<Integer>>(r1, decorateWithRandomIntervals.invoke());
		Source<TemporalObject<Integer>> s2 = new Mapper<Integer, TemporalObject<Integer>>(r2, decorateWithRandomIntervals.invoke());	
		// create union that merges streams and ensures timestamp order in output stream
		TemporalUnion<Integer> union = new TemporalUnion<Integer>(s1, s2);
		QueryExecutor exe = new QueryExecutor();		
		insertGUIBeforeAndAfter(exe, union);
		// throws an exception if timestamp ordering is violated
		Pipes.verifyStartTimeStampOrdering(union);
		exe.startAllQueries();
	}
	
	/** 
	 * A join without temporal semantic.
	 */
	public static void joinSimpleNonTemporalHashJoin() {
		final int objectSize = 8; // Integer size
	    final int preferredMemSize = objectSize*100;
		final int noOfBuckets = 17; // for hashing
		// create two synthetic data sources 
	    Enumerator e0 = new Enumerator(100, 10);
		Enumerator e1 = new Enumerator(100, 5);
		// the join hash function
	    final Function<Integer,Integer> hashFunction = new Function<Integer,Integer>() {
			@Override
			public Integer invoke(Integer o) {
				return o % noOfBuckets;
			}
		};
		// create two sweeparea status structures, that store elements
		SweepArea<Integer> sa0 = new ImplementorBasedSweepArea<Integer>(
			new HashSAImplementor<Integer>(hashFunction, 2),
			0, false, new Equal<Integer>(), 2
		);
		SweepArea<Integer> sa1 = new ImplementorBasedSweepArea<Integer>(
			new HashSAImplementor<Integer>(hashFunction, 2),
			1, false, new Equal<Integer>(), 2
		);	
		// memory-adaptive join		
		Join<Integer,Object[]> join = new Join<Integer,Object[]>(e0, e1, 0, 1, DefaultMemoryManageableSA.getMemoryManageableSA(sa0, objectSize, preferredMemSize).invoke(), DefaultMemoryManageableSA.getMemoryManageableSA(sa1, objectSize, preferredMemSize).invoke(), NTuplify.DEFAULT_INSTANCE);			
		QueryExecutor exe = new QueryExecutor();		
		insertGUIBeforeAndAfter(exe, join, null, objectArrayToString());
		exe.startAllQueries();
	}
	
	/**
	 * Computes a temporal Cartesian Product.
	 */
	public static void joinTemporalCartesianProduct() {
		final long start = 0; // application time start with 0.
		final long inc = 5; // application time distance between two elements.
		final long windowSize = 100; // lifespan.
		// create two synthetic data sources 
	    Enumerator e0 = new Enumerator(100, 10);
		Enumerator e1 = new Enumerator(100, 5);
		// provide mapping to TemporalObject.
		Function<Integer, TemporalObject<Integer>> mapping = new Function<Integer, TemporalObject<Integer>>() {
			long timeStamp=start-inc;
			@Override
			public TemporalObject<Integer> invoke(Integer i) {
				timeStamp += inc;
				return new TemporalObject<Integer>(i, new TimeInterval(timeStamp, timeStamp+windowSize));
			}
		};
		Source<TemporalObject<Integer>> s0 = new Mapper<Integer, TemporalObject<Integer>>(e0, mapping);
		Source<TemporalObject<Integer>> s1 = new Mapper<Integer, TemporalObject<Integer>>(e1, mapping);
		TemporalJoin<Integer, Object[]> join = Joins.TemporalCartesianProduct(s0, s1, 0, 1);
		// corresponds to
//		TemporalJoin<Integer, Object[]> join2 = new TemporalJoin<Integer, Object[]>(
//				s0,
//				s1,
//				0,
//				1,
//				new TemporalJoinHeapSA<Integer>(
//					new ListSAImplementor<TemporalObject<Integer>>(new LinkedList<TemporalObject<Integer>>()),
//					0,
//					2
//				),
//				new TemporalJoinHeapSA<Integer>(
//					new ListSAImplementor<TemporalObject<Integer>>(new LinkedList<TemporalObject<Integer>>()),
//					1,
//					2
//				),
//				NTuplify.DEFAULT_INSTANCE
//			);
		QueryExecutor exe = new QueryExecutor();		
		insertGUIBeforeAndAfter(exe, join, null, objectArrayTOToString());
		exe.startAllQueries();
	}
	
	/** 
	 * A temporal nested loops join example that uses an asymmetric join predicate.
	 */
	public static void joinTemporalNestedLoopsJoin() {
		final long start = 0; // application time start with 0.
		final long inc = 5; // application time distance between two elements.
		final long windowSize = 100; // lifespan.
		// create two synthetic data sources 
	    Enumerator e0 = new Enumerator(100, 10);
		Enumerator e1 = new Enumerator(100, 5);
		// provide mapping to TemporalObject.
		Function<Integer, TemporalObject<Integer>> mapping = new Function<Integer, TemporalObject<Integer>>() {
			long timeStamp=start-inc;
			@Override
			public TemporalObject<Integer> invoke(Integer i) {
				timeStamp += inc;
				return new TemporalObject<Integer>(i, new TimeInterval(timeStamp, timeStamp+windowSize));
			}
		};
		// Here we choose an asymmetric join predicate. Note it is not necessary to check for temporal overlap.
		Predicate<TemporalObject<Integer>> theta = new Predicate<TemporalObject<Integer>>() {
			@Override
			public boolean invoke(TemporalObject<Integer> o1, TemporalObject<Integer> o2) {
				return o1.getObject() < o2.getObject();
			}
		};

		Source<TemporalObject<Integer>> s0 = new Mapper<Integer, TemporalObject<Integer>>(e0, mapping);
		Source<TemporalObject<Integer>> s1 = new Mapper<Integer, TemporalObject<Integer>>(e1, mapping);
		TemporalJoin<Integer, Object[]> join = Joins.TemporalSNJ(s0, s1, 0, 1, theta);
		// corresponds to
//		TemporalJoin<Integer, Object[]> join = new TemporalJoin<Integer, Object[]>(
//			s0,
//			s1,
//			0,
//			1,
//			new TemporalJoinHeapSA<Integer>(
//				new ListSAImplementor<TemporalObject<Integer>>(new LinkedList<TemporalObject<Integer>>()),
//				0,
//				theta,
//				2
//			),
//			new TemporalJoinHeapSA<Integer>(
//				new ListSAImplementor<TemporalObject<Integer>>(new LinkedList<TemporalObject<Integer>>()),
//				1,
//				Predicates.swapArguments(theta),
//				2
//			),
//			NTuplify.DEFAULT_INSTANCE
//		);
		QueryExecutor exe = new QueryExecutor();		
		insertGUIBeforeAndAfter(exe, join, null, objectArrayTOToString());
		exe.startAllQueries();
	}
	
	public static void joinTemporalHashJoin() {
		final long start = 0; // application time start with 0.
		final long inc = 5; // application time distance between two elements.
		final long windowSize = 100; // lifespan.
		// create two synthetic data sources 
	    Enumerator e0 = new Enumerator(100, 10);
		Enumerator e1 = new Enumerator(100, 5);
		// provide mapping to TemporalObject.
		Function<Integer, TemporalObject<Integer>> mapping = new Function<Integer, TemporalObject<Integer>>() {
			long timeStamp=start-inc;
			@Override
			public TemporalObject<Integer> invoke(Integer i) {
				timeStamp += inc;
				return new TemporalObject<Integer>(i, new TimeInterval(timeStamp, timeStamp+windowSize));
			}
		};
		Source<TemporalObject<Integer>> s0 = new Mapper<Integer, TemporalObject<Integer>>(e0, mapping);
		Source<TemporalObject<Integer>> s1 = new Mapper<Integer, TemporalObject<Integer>>(e1, mapping);
		TemporalJoin<Integer, Object[]> join = Joins.TemporalIntegerSHJ(s0, s1, 17);
		QueryExecutor exe = new QueryExecutor();		
		insertGUIBeforeAndAfter(exe, join, null, objectArrayTOToString());
		exe.startAllQueries();
	}
	
	/** 
	 * Example for the Aggregator	 
	 */
	public static void aggregatorExample() {
		Source<Integer> source = new RandomNumber<Integer>(new JavaDiscreteRandomWrapper(), 100, 10);
		Aggregator<Integer,Number> agg = new Aggregator<Integer,Number>(
			source,
			new Maximum()
			// Alternatives:
			// new Variance()
			// new Average() 
			// new Minimum()
			// new Sum()
			// new LastNthAverage(20);
		);
		QueryExecutor exe = new QueryExecutor();
		insertGUIBeforeAndAfter(exe, agg);
		exe.startAllQueries();
	}
	
	public static void aggregatorTemporalExample() {
		final int noOfElements = 1000;		
		final int startInc = 20;
		final int intervalSize = 120;
		final long seed = 42;
		
		RandomNumber<Integer> r = new RandomNumber<Integer>(
			RandomNumber.DISCRETE, noOfElements, 0
		);		
		
		Source<TemporalObject<Integer>> s = new Mapper<Integer,TemporalObject<Integer>>(r,
			new Function<Integer,TemporalObject<Integer>>() {
				Random random = new Random(seed);
				long start, end;
				long newStart, newEnd;
				@Override
				public TemporalObject<Integer> invoke(Integer o) {					
					newStart = start+random.nextInt(startInc);
					newEnd   = newStart+1+random.nextInt(intervalSize-1);
					if (start == newStart) newEnd = end+random.nextInt(intervalSize-1);
					TemporalObject<Integer> object = new TemporalObject<Integer>(o, 
						new TimeInterval(start = newStart, end = newEnd)
					);
					return object;
				}
			}
		);	
		final AggregationFunction<Number,Number> aggFunction = new Maximum();
		// Alternatives:
		// new Variance()
		// new Average() 
		// new Minimum()
		// new Sum()
		// new LastNthAverage(20);
		TemporalAggregator<Integer,Number> agg = new TemporalAggregator<Integer,Number>(s, aggFunction);
		QueryExecutor exe = new QueryExecutor();
		insertGUIBeforeAndAfter(exe, agg);
		exe.startAllQueries();
	}
	
	/**
	 * Example for the TemporalDistinct.	 
	 */
	public static void distinctTemporalExample() {		
		int noOfElements = 500;
		long startInc = 5;
		final int length = 500;
		final int valueRange =5;
		// create a stream of timestamped objects
		SyncTimeStampSource<Integer> source= new SyncTimeStampSource<Integer>(
				new DiscreteRandomNumber(new DiscreteRandomWrapper(){
					Random r = new Random(42);
					public int nextInt() {
						return r.nextInt(valueRange);
					}
				}, noOfElements),
				new Repeater<Long>(startInc));
		Pipe<TimeStampedObject<Integer>, TemporalObject<Integer>> map = new AbstractTimeStampPipe<Integer, Integer, TimeStampedObject<Integer>, TemporalObject<Integer>>(source) {
			@Override
			public void processObject(TimeStampedObject<Integer> o, int sourceID) throws IllegalArgumentException {
				transfer(new TemporalObject<Integer>(o.getObject(), new TimeInterval(o.getTimeStamp(), o.getTimeStamp()+ length)));
			}			
		}; 
		TemporalDistinct<Integer> distinct = new TemporalDistinct<Integer>(map, 0, new TemporalDistinctSA<Integer>());
		QueryExecutor exe = new QueryExecutor();
		insertGUIBeforeAndAfter(exe, distinct);
		exe.startAllQueries();
	}
	
	/**
	 * Example for the TemporalDifference.	 
	 */
	public static void differenceTemporalExample() {
		final int noOfElements1 = 20;
		final int valueRange1 =4;
		final int noOfElements2 = 10;
		final int valueRange2 =5;
		final int buckets = 10;
		final int startInc = 50;
		final int intervalSize = 100;
		final long seed = 42;
		
		final Function<TemporalObject<Integer>,Integer> hash = new Function<TemporalObject<Integer>,Integer>() {
			@Override
			public Integer invoke(TemporalObject<Integer> o) {
				return o.getObject()%buckets;
			}
		};
			
		RandomNumber<Integer> r1 = new RandomNumber<Integer>(
			new DiscreteRandomWrapper(){
				Random r = new Random(42);
				public int nextInt() {
					return r.nextInt(valueRange1);
				}
			}, noOfElements1, 500);
		
		RandomNumber<Integer> r2 = new RandomNumber<Integer>(
				new DiscreteRandomWrapper(){
					Random r = new Random(42);
					public int nextInt() {
						return r.nextInt(valueRange2);
					}
				}, noOfElements2, 500);
		
		Source<TemporalObject<Integer>> s1 = Pipes.decorateWithRandomIntervals(r1, startInc, intervalSize, seed);
		Source<TemporalObject<Integer>> s2 = Pipes.decorateWithRandomIntervals(r2, startInc, intervalSize, 2*seed);
				
		TemporalDifference<Integer> difference = new TemporalDifference<Integer>(s1, s2, 0, 1, 
			new TemporalDifferenceSA<Integer>(new HashSAImplementor<TemporalObject<Integer>>(hash, 2), 0, 36, 4096*4096, 1024*1024),
			new TemporalDifferenceSA<Integer>(new HashSAImplementor<TemporalObject<Integer>>(hash, 2), 1, 36, 4096*4096, 1024*1024)
		);
		QueryExecutor exe = new QueryExecutor();
		insertGUIBeforeAndAfter(exe, difference);
		exe.startAllQueries();
	}
	
	/**
	 * 3.1.1)
	 * Merge some streams via TemporalUnion and remove noise with Filter.
	 * CQL
	 * SELECT timestamp, distance, lane, speed, length, direction
	 * FROM Sensor1, ..., Sensor5
	 * WHERE length >= '1' AND length <= '50' AND
	 * speed >= '0' AND speed  <= '100';
	 */
	public static void queryHighwayRemoveNoise() {
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

		Union<Vehicle> union = new Union<Vehicle>(new Source[]{source1, source2, source3, source4, source5});

		Filter<Vehicle> filter = new Filter<Vehicle>(union, new Predicate<Vehicle>() {
		    protected double length, speed;
		    @Override
			public boolean invoke(Vehicle v) {		        
		        length = v.getLength();
		        speed = v.getSpeed();
		        if (length < 1d || length > 50d) return false;
		        if (speed < 0d || speed > 100d) return false;
		        return true;
		    }
		});
		new VisualSink<Vehicle>(filter," Merged streams after noise has been removed", true);
	}
	
	/**
	 * 3.1.2)
	 * Merge some streams via TemporalUnion and compute the average speed.
	 * CQL
	 * SELECT AVG(length) 
	 * FROM (SELECT length
	 * FROM Sensor1 [Range 15 minutes]
     * WHERE lane = '1';
     * UNION ALL
     * ...
     * UNION ALL
     * SELECT length
     * FROM Sensor18 [Range 15 minutes]
     * WHERE lane = '1';);
	 */
	public static void queryHighwayMergeAndAggregate() {

		HighwaySource[] sources = Highway.getSourcesByDate(TRAFFIC_DATA_DIR,
                new GregorianCalendar(1993, 3, 15).getTime(), 'N', // direction
                0.1f
        );

        Mapper[] mappers = new Mapper[sources.length];
        final long lifespan = 15*60*1000; 
        TemporalWindow[] windows = new TemporalWindow[sources.length];
        
        for (int i = 0; i < sources.length; i++) {
            mappers[i] = new Mapper<Vehicle, TemporalObject<Vehicle>>(sources[i], Vehicle.MAP_TO_TEMPORAL_OBJECT);
            windows[i] = new TemporalWindow<Vehicle>(mappers[i], lifespan);
        }
        final int objectSize = 31;
        TemporalUnion<Vehicle> union = new TemporalUnion<Vehicle>(windows, new TemporalUnionSA<Vehicle>(sources.length, objectSize));
        Filter<TemporalObject<Vehicle>> filter = new Filter<TemporalObject<Vehicle>>(clean(union), new Predicate<TemporalObject<Vehicle>>() {
            @Override
			public boolean invoke(TemporalObject<Vehicle> o) {
                return o.getObject().getLane() == 1; // HOV lane
            }
        });
        Mapper<TemporalObject<Vehicle>, TemporalObject<Double>> map = new Mapper<TemporalObject<Vehicle>, TemporalObject<Double>>(filter, new Function<TemporalObject<Vehicle>, TemporalObject<Double>>() {
            @Override
			public TemporalObject<Double> invoke(TemporalObject<Vehicle> o) {                
                // project to length
                return new TemporalObject<Double>(new Double(((Vehicle) o.getObject()).getLength()), o.getTimeInterval());
            }
        });
        
        TemporalAggregator aggregator = new TemporalAggregator(map, 0,
                new TemporalAggregatorSA(new CountAwareAverage()) // build average						
        );
        
        // conversions
        Mapper<TemporalObject<Number[]>, String> conv = new Mapper<TemporalObject<Number[]>, String>(aggregator, 
        	new Function<TemporalObject<Number[]>, String>() {
	            protected DecimalFormat df = new DecimalFormat( "0.00" );
	            @Override
				public String invoke(TemporalObject<Number[]> t) {	                
	                return "("+df.format(t.getObject()[0])+" (m); ["+new Date(t.getStart())+", "+new Date(t.getEnd())+"))";              
	            }
	        });        
        new VisualSink<String>(conv,"Merge some streams via TemporalUnion and compute the average speed.", true);
	}
	
	/**
	 * 3.1.2)
	 * Jam detection involves Mapper, Temporalwindow, Temporalaggregate, Filter
	 * CQL
	 * SELECT AVG(speed) AS avgSpeed, distance 
	 * FROM ( SELECT speed, distance
     * 		  FROM SensorRow1 [Range 15 minutes]
     * 		  UNION ALL
     * 		  ...
     * 		  UNION ALL
     * 		  SELECT speed, distance
     * 		  FROM SensorRow17 [Range 15 minutes]
     * 		);
     * WHERE avgSpeed < 15;
	 */
	public static void queryHighwayJamDetection() {
		HighwaySource[] sources = Highway.getSourcesByDate(TRAFFIC_DATA_DIR,
                new GregorianCalendar(1993, 2, 16).getTime(), 'S', // direction
                0.5f);

        Mapper[] mappers = new Mapper[sources.length];
        TemporalWindow[] windows = new TemporalWindow[sources.length];
        final long lifespan = 900000; // window size: 15 minutes
        for (int i = 0; i < sources.length; i++) {
            mappers[i] = new Mapper<Vehicle, TemporalObject<Vehicle>>(sources[i], Vehicle.MAP_TO_TEMPORAL_OBJECT);
        	windows[i] = new TemporalWindow<Vehicle>(mappers[i], lifespan);
    	}
        
        final int noOfSections = sources.length;
        TemporalAggregator[] aggs = new TemporalAggregator[noOfSections];
        for (int i = 0; i < noOfSections; i++) {
            aggs[i] = new TemporalAggregator<Vehicle, AvgSpeedPerSection>(windows[i], 
                    new TemporalAggregatorSA<Vehicle, AvgSpeedPerSection>(
                            new AggregationFunction<Vehicle,AvgSpeedPerSection>() {
                                protected final CountAwareAverage avg = new CountAwareAverage();
                                @Override
								public AvgSpeedPerSection invoke(AvgSpeedPerSection agg, Vehicle v) {                                    
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
        
        Union<TemporalObject<AvgSpeedPerSection>> union = new Union<TemporalObject<AvgSpeedPerSection>>(aggs);
                
        final double speedThreshold = 15.0d; 
        Filter<TemporalObject<AvgSpeedPerSection>> filter = new Filter<TemporalObject<AvgSpeedPerSection>>(union, new Predicate<TemporalObject<AvgSpeedPerSection>>() {
            @Override
			public boolean invoke(TemporalObject<AvgSpeedPerSection> agg) {                
                if (agg.getObject().getSpeed() < speedThreshold)
                    return true;
                return false;
            }
        });
        
        // conversions
        Mapper<TemporalObject<AvgSpeedPerSection>, String> map = new Mapper<TemporalObject<AvgSpeedPerSection>, String>(filter, 
       		new Function<TemporalObject<AvgSpeedPerSection>, String>() {
	            @Override
				public String invoke(TemporalObject<AvgSpeedPerSection> t) {	                
	                return "("+t.getObject().toString()+", ["+new Date(t.getStart())+", "+new Date(t.getEnd())+"))";              
	            }
        });
        
        new VisualSink<String>(map,"Jam detection", true);	
	}
	

	/**
	 * 3.2.1)
	 * Monitor the average closing price across items in each category over the last hour.
	 * CQL
	 * SELECT categoryID, AVG(price)
	 * FROM ClosedAuctions [RANGE 1 hour]
	 * GROUP BY categoryID;
	 */
	public static void queryNEXMarkAVGClosingPrice() {
	    NEXMarkGeneratorSource closedAuctions = new NEXMarkGeneratorSource(AUCTION_DATA_DIR,
	            CASTOR_MAPPING_FILE, NEXMarkGeneratorSource.CLOSED_AUCTION_STREAM, 
	            NEXMarkGeneratorSource.GET_CLOSED_AUCTION_FUNCTION(), 0.5f, true
	    );		       

	    final long lifespan = 1*60*60; // 1 hour
	    TemporalWindow<ClosedAuction> window = new TemporalWindow<ClosedAuction>(closedAuctions, lifespan);
	            
	    int noOfCategories = 302;
	    HashGrouper<TemporalObject<ClosedAuction>> g = new HashGrouper<TemporalObject<ClosedAuction>>(window, new Function<TemporalObject<ClosedAuction>, Integer>() {
	        @Override
			public Integer invoke(TemporalObject<ClosedAuction> o) {
	            return new Integer(o.getObject().getCategory());
	        }
	    }, noOfCategories);
	    	        
	    TemporalAggregator[] aggregators = new TemporalAggregator[noOfCategories]; 
	    for (int i = 0; i < noOfCategories; i++) 
	        aggregators[i] = new TemporalAggregator<ClosedAuction, AvgClosingPrice>(g.getReferenceToGroup(i), 
	            new TemporalAggregatorSA<ClosedAuction, AvgClosingPrice>(
	                    new AggregationFunction<ClosedAuction,AvgClosingPrice>() { 
	                        protected final CountAwareAverage avg = new CountAwareAverage();	                        
	                        @Override
							public AvgClosingPrice invoke(AvgClosingPrice agg, ClosedAuction next) {
	                            ClosedAuction c = (ClosedAuction) next; 
	                            if (agg == null)
	                                return new AvgClosingPrice(avg.invoke(null,new Double(c.getSellPrice())),
	                                        c.getCategory()
	                                );
	                            AvgClosingPrice aggPrice = (AvgClosingPrice)agg;
	                            return new AvgClosingPrice(avg.invoke(aggPrice.getAggregate(), new Double(c.getSellPrice())), 
	                                aggPrice.getCategory()
	                            );
	                        } 
	                    }
	                )
	        ); 
	    
	    Union<TemporalObject<AvgClosingPrice>> union = new Union<TemporalObject<AvgClosingPrice>>(aggregators);

	    new VisualSink<TemporalObject<AvgClosingPrice>>(union,"Average closing price", true);			
	}

	/**
	 * 3.2.2)
	 * Report all auctions that closed within 5 hours of their opening.
	 * CQL
	 * SELECT OpenAuction.*
	 * FROM OpenAuction [Range 5 hours] O, ClosedAuction [NOW] C
	 * WHERE O.auctionID = C.auctionID;
	 */
	public static void queryNEXMarkJoinCloseOpenAuction() {
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
	  

        new VisualSink<Vehicle>(join,"Join over Streams closed and cpen aution", true);
	}

	/**
	 * 3.2.3)
	 * Merge stream and relation: For each closed auction report all available information on the buyer.
	 * CQL
	 * SELECT Person.*
	 * FROM ClosedAuction C, Person P
	 * WHERE C.buyerID = P.ID;
	 */
	public static void queryNEXMarkStreamAndRelation() {
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
	       new VisualSink<Vehicle>(join,"Merge stream and relation", true);
	}

	/**
	 * 3.2.3)
	 * Get hot items: Select the items with the most bids in the past 10 minutes.
	 * SELECT itemID, num
	 * FROM (SELECT B1.itemID AS itemID, COUNT(*) AS num
     *  	 FROM Bid [RANGE 10 minutes] B1
     *  	 GROUP BY B1.itemID)
	 * WHERE num >= ALL (SELECT COUNT(*)
     *             		 FROM Bid [RANGE 10 minutes] B2
     *             		 GROUP BY B2.itemID);
	 */
	public static void queryNEXMarkHotItems() {
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
                                    return new BidCount(((BidCount)agg).getItemID(), ((BidCount)agg).getCount()+1);
                                }
                            }
                    )
            );
        }
        TemporalUnion union = new TemporalUnion(aggs, new TemporalUnionSA(noOfItems));
        
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
                                maxValue = ((BidCount)maxima.getFirst()).getCount();
                                if (bidCount.getCount() > maxValue) {
                                    maxima = new LinkedList();
                                    maxima.add(bidCount);
                                    return maxima;
                                }
                                if (bidCount.getCount() == maxValue) {
                                	LinkedList<BidCount> l = (LinkedList<BidCount>)maxima.clone();
                                    l.add(bidCount);
                                    return l;
                                }
                                return (LinkedList<BidCount>)maxima.clone();
                            }
                        }
                )
        );
        
        new VisualSink<Vehicle>(max,"Hot items", true);
	}
	
	public static void metaDataSelection() {
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

		VisualSink sink = new VisualSink(filter, "Filter", true);
		// meta data initialization
		filter.getMetaDataManagement().include(
			INPUT_RATE, OUTPUT_RATE, 
			INPUT_OUTPUT_RATIO, AVG_INPUT_OUTPUT_RATIO, VAR_INPUT_OUTPUT_RATIO
		);
		// meta data visualization
		SWTPerformanceMonitor.FACTORY_METHOD(filter.getMetaData());		
	}
	
	public static void metaDataJoin() {
		final long start = 0; // application time start with 0.
		final long inc = 5; // application time distance between two elements.
		final long windowSize = 100; // lifespan.
		// create two synthetic data sources 
	    Enumerator e0 = new Enumerator(10000, 10);
		Enumerator e1 = new Enumerator(10000, 5);
		// provide mapping to TemporalObject.
		Function<Integer, TemporalObject<Integer>> mapping = new Function<Integer, TemporalObject<Integer>>() {
			long timeStamp=start-inc;
			@Override
			public TemporalObject<Integer> invoke(Integer i) {
				timeStamp += inc;
				return new TemporalObject<Integer>(i, new TimeInterval(timeStamp, timeStamp+windowSize));
			}
		};
		// Here we choose an asymmetric join predicate. Note it is not necessary to check for temporal overlap.
		Predicate<TemporalObject<Integer>> theta = new Predicate<TemporalObject<Integer>>() {
			@Override
			public boolean invoke(TemporalObject<Integer> o1, TemporalObject<Integer> o2) {
				return o1.getObject() < o2.getObject();
			}
		};

		Source<TemporalObject<Integer>> s0 = new Mapper<Integer, TemporalObject<Integer>>(e0, mapping);
		Source<TemporalObject<Integer>> s1 = new Mapper<Integer, TemporalObject<Integer>>(e1, mapping);
		TemporalJoin<Integer, Object[]> join = Joins.TemporalSNJ(s0, s1, 0, 1, theta);
		VisualSink sink = new VisualSink(join, "Join", true);
		// meta data initialization
		join.getMetaDataManagement().include(INPUT_RATE, AVG_INPUT_RATE, VAR_INPUT_RATE, OUTPUT_RATE, 
        		AVG_OUTPUT_RATE, VAR_OUTPUT_RATE, MEM_USAGE, AVG_MEM_USAGE, VAR_MEM_USAGE, INPUT_OUTPUT_RATIO,
        		AVG_INPUT_OUTPUT_RATIO, VAR_INPUT_OUTPUT_RATIO);
		// meta data visualization
		SWTPerformanceMonitor.FACTORY_METHOD(join.getMetaData());		

	}
	
	public static void metaDataAggregator() {
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
		// meta data initialization
        aggregator.getMetaDataManagement().include(INPUT_RATE, AVG_INPUT_RATE, VAR_INPUT_RATE, OUTPUT_RATE, 
        		AVG_OUTPUT_RATE, VAR_OUTPUT_RATE, MEM_USAGE, AVG_MEM_USAGE, VAR_MEM_USAGE, INPUT_OUTPUT_RATIO,
        		AVG_INPUT_OUTPUT_RATIO, VAR_INPUT_OUTPUT_RATIO);
        new VisualSink<Vehicle>(aggregator,"Highest bid in the recent 10 minutes", true);        
		// meta data visualization
        SWTPerformanceMonitor.FACTORY_METHOD(aggregator.getMetaData());	
	}

	/**
	 * Provides numberous examples to demonstrate the usage of pipes for stream processing.
	 */
	public static void main(String[] args) {
		//-------------------------------------------------------------------------------------------
		// ************ 1) Different types of data sources (synthetic and real) and sinks ***********
		//-------------------------------------------------------------------------------------------
		
		/* 
		 * Create a source producing 200 discrete random integer with a constant output rate
		 * of 5 millis and writes its elements to System.out.
		*/
		//sourceRandomIntegers();
		
		/* 
		 * Create a stream consisting of integer from 1000 to 12000 with a constant output rate
		 * of 5 millis. Write every 50 millis how many elements have been seen so far and current
		 * system time to System.out.
		 */
		//sourceEnumerator();

		/* 
		 * Create a source that uses an Iterator and the class CursorSource to produce
		 * elements and write its elements to VisualSink.
		 */
		//sourceCursorSource();
		
		/*
		 * Create a source that creates a open auction stream using NEXMark.
		 */
		//sourceNEXMark();
		
		/*
		 * Create a source that emits data collected from the FSP. 
		 */
		//sourceHighway();
		
		//----------------------------------------------------------
		// ************ 2.1) Single operators: selection ***********
		//----------------------------------------------------------
		
		// Generates a synthetic stream and drops every second element. 
		//selectionSimple();

		// Report every Bid for a selected auction.
		//selectionWatchAuction();
		
		// Example for load shedding in the highway szenario.
		//selectionLoadShedding();
		
		//---------------------------------------------------------------------------------------------------
		// ************ 2.2) Single operators: maps (projection, temporal window, timegranularity) **********
		//---------------------------------------------------------------------------------------------------
		
		/* Apply projection to stream in highway Szenario using the class Mapper. An element (timestamp,id, lane, length, speed)
		 * is mapped to (id, lane, speed).
		 */
		//mapSimpleMapper();
				
		/* Map an Object to TemporalObject. This is important for stateful temporal operators,
		 * because it determines expiration. In the example, an Integer is mapped to an instance of
		 * Temporalobject. Afterwards the lifespan is set to 1000 via TemporalWindow.
		 */
		//mapToTempObj();
		
		// Set timegranularity for TemporalObjects.
		//mapSetGranularity();
		
		//----------------------------------------------------------
		// ************ 2.3) Single operators: unions **************
		//----------------------------------------------------------
		// Use Union to merge streams
		//unionMerge();		
		// Use TemporalUnion to merge streams and ensure timestamp order in output stream.
		//unionTemporal();
		//----------------------------------------------------------
		// ************ 2.4) Single operators: joins ***************
		//----------------------------------------------------------
		
		// A simple equi-join without temporal semantic
		// joinSimpleNonTemporalHashJoin();
 
		// Computes a temporal Cartesian Product.
		// joinTemporalCartesianProduct();
		
		// A temporal nested loops join example that uses an asymmetric join predicate.
		//joinTemporalNestedLoopsJoin();
		
		// Joining two synthetic streams with a temporal hash join
		//joinTemporalHashJoin();		

		//----------------------------------------------------------
		// ************ 2.5) Single operators: aggregates **********
		//----------------------------------------------------------
		
		//Example for the Aggregator
		//aggregatorExample();
		
		//Example for the TemporalAggregator
		//aggregatorTemporalExample();
		
		//----------------------------------------------------------
		// ************ 2.6) Single operators: distinct ************
		//----------------------------------------------------------		
		//Example for the TemporalDistinct
		//distinctTemporalExample();

		//----------------------------------------------------------
		// ************ 2.6) Single operators: difference ************
		//----------------------------------------------------------
		//Example for the TemporalDifference
		//differenceTemporalExample();
		
		//---------------------------------------------------------------------
	    // * 3.1) Queries using real data sets from Free Service Patrol *******
		//---------------------------------------------------------------------

		// 3.1.1)
		// Merge some streams via TemporalUnion and remove noise with Filter.
		//queryHighwayRemoveNoise();
		
		// 3.1.2)
		// Merge some streams via TemporalUnion and compute the average speed. 
		//queryHighwayMergeAndAggregate();

		// 3.1.3)
		// Jam detection involves Mapper, Temporalwindow, Temporalaggregate, Filter
		//queryHighwayJamDetection();

		//---------------------------------------------------------------------
	    // ******************** 3.2) Queries using NexMark ********************
		//---------------------------------------------------------------------
		
		// 3.2.1)
		// Monitor the average closing price across items in each category over the last hour.
		//queryNEXMarkAVGClosingPrice();
		
		
		// 3.2.2)
		// Report all auctions that closed within 5 hours of their opening.		
		//queryNEXMarkJoinCloseOpenAuction();

		// 3.2.3)
		// Merge stream and relation: For each closed auction report all available information on the buyer.
		//queryNEXMarkStreamAndRelation();
		
		// 3.2.4)
		// Get hot items: Select the items with the most bids in the past 10 minutes.
		//queryNEXMarkHotItems();
		
		//---------------------------------------------------------------------
	    // *********************** 4) Metadata examples ***********************
		//---------------------------------------------------------------------
		//metaDataSelection();
		//metaDataJoin();
		//metaDataAggregator();
	}

}
