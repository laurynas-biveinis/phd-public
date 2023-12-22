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

import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import xxl.core.collections.sweepAreas.HashSAImplementor;
import xxl.core.cursors.AbstractCursor;
import xxl.core.cursors.unions.Sequentializer;
import xxl.core.functions.Function;
import xxl.core.functions.NTuplify;
import xxl.core.math.statistics.parametric.aggregates.Average;
import xxl.core.pipes.elements.TemporalObject;
import xxl.core.pipes.operators.Pipes;
import xxl.core.pipes.operators.joins.TemporalJoin;
import xxl.core.pipes.operators.joins.TemporalJoin.TemporalJoinListSA;
import xxl.core.pipes.operators.mappers.TimeGranularity;
import xxl.core.pipes.operators.windows.TemporalWindow;
import xxl.core.pipes.processors.Processor;
import xxl.core.pipes.queryGraph.QueryExecutor;
import xxl.core.pipes.sinks.SinkIsDoneException;
import xxl.core.pipes.sinks.VisualSink;
import xxl.core.pipes.sources.RandomNumber;
import xxl.core.pipes.sources.Source;
import xxl.core.pipes.sources.SourceIsClosedException;
import xxl.core.predicates.Predicate;
import xxl.core.predicates.Predicates;
import xxl.core.util.metaData.CompositeMetaData;
import xxl.core.util.metaData.MetaDataHandler;

/**
 * Allows to log metadata from query components with metadata which have been created with a metadatafactory.
 * In principle this implementation can be used for arbitary metadata ('CompositeMetaData') as long the meta data
 * identifier refers to a function and the function call results in a double.
 * After the processor of the metadatalogger has terminated, the map containes all measured values. Depending on
 * parameters passed as int (logMode), the meta data are logged or painted before termination, i.e use
 * 'new MetaDataLogger(mdf, logger, predicate, MetaDataLogger.ONE_DIAGRAM);' to paint all
 * collected meta data in one diagram.   
 * 
 */
public class MetaDataLogger {

	/*
	 * different log modes reserved positions: 0-9
	 */
	/**
	 * Deprecated.  
	 */
	public static final int NOTHING = 0;
	
	/**
	 * Deprecated.
	 **/
	public static final int LOG = 1;

	/**
	 * Deprecated.
	 **/
	public static final int PAINT = 2;
	
	/**
	 * Signs, that the underlying should be closed after the invocation of
	 * continueLogging is false
	 */
	public static final int CLOSE_LOGGER_ON_EXIT = 512;
	
	/*
	 * additional informations for logging reserved positions: 10 - 19
	 */
	
	/**
	 * Writes each metadata in the logfile.
	 */
	public static final int WRITE_ALL = 1024;
	
	/**
	 * Writes the average of each metadata in the logfile.
	 */
	public static final int SUMMARIZE_ALL = 2048;
	
	/*
	 * additional informations for painting reserved positions: 20 - 29
	 */
	
	/**
	 * Saves the lists for metadata logger
	 */
	public static final int SAVE_LISTS = 4096;

	/**
	 * Writes the average of each metadata in the logfile.
	 */
	public static final int SUMMARIZE_ALL_WITHOUT_NEGATIVES = 8192;

	
	/**
	 * Paints all metadata in one diagram.
	 */
	public static final int ONE_DIAGRAM = 1048576;
	
	/**
	 * Paints for each metadata a seperate diagram.
	 */
	public static final int MULTIPLE_DIAGRAMS = 2097152;
	
	/**
	 * Return true, if the source is closed.
	 */
	public static Predicate notSourceIsClosed(final Source source) {
		return new Predicate (){
			public boolean invoke() {
				return !source.isClosed();
			}			
		};
	}
		
	public class MetaDataAccessor extends Processor {			
		protected MetaDataLogger mdl;
		protected boolean terminate;
		
		public MetaDataAccessor(MetaDataLogger mdl, long period) {
			// make sure that accessor starts immediately
			super(new Sequentializer<Long>(new AbstractCursor(){
				boolean notCalled = true;
				@Override
				protected boolean hasNextObject() {
					return notCalled;
				}
				@Override
				protected Object nextObject() {
					notCalled = false;
					return 0;
				}
			}
			, getConstantDelayManager(period)));			
			this.mdl = mdl;			
		}
		
		public void process() {
			try {
				if (mdl.continueLogging.invoke() && !terminate)
				{			
					mdl.creatSingleLogEntry();
				}
				else {
					mdl.terminate();
					terminate();
				}
		    } catch (SourceIsClosedException se) {
		        terminate();
		        mdl.terminate();
		    } catch (SinkIsDoneException se) {
		        terminate();
		        mdl.terminate();
		    }	
		}	
	}

	protected CompositeMetaData cmd;
	protected Logger logger;
	protected Predicate continueLogging;
	protected MetaDataAccessor accessor;
	protected Map map;
	protected int time;	
	protected Object additionalInfo;
	protected boolean started;
	
	/**
	 * @return Returns the additionalInfo.
	 */
	public Object getAdditionalInfo() {
		return additionalInfo;
	}
	/**
	 * @param additionalInfo The additionalInfo to set.
	 */
	public void setAdditionalInfo(Object additionalInfo) {
		this.additionalInfo = additionalInfo;
	}
	/**
	 * A Bitset, containing the configuration of the meta data logger.  
	 */
	protected BitSet logConfig;
	
	/**
	 * Creates a new MetaDataLogger that uses the given composite metadata to create metadata. If
	 * logger is not null, it is used for logging, otherwise a default logger is created. The predicate
	 * determines, if the logging should continue. The int logMode determines the log modes, that can be combined,
	 * i.e. MetaDataLogger.ONE_DIAGRAM paints all measured values in one diagramm,  MetaDataLogger.ONE_DIAGRAM | MULTIPLE_DIAGRAMS
	 * paints all measured values in one diagramm and each serie in a separate diagram. All measured values are stored as 
	 * (md key, list)in the map, the list contains double[2] entry: (time, value). 
	 * @param cmd composite metadata
	 * @param logger the logger
	 * @param continueLogging the predicate
	 * @param logMode the log mode can be ORed with the class constants, i.e. 
	 * MetaDataLogger.ONE_DIAGRAM creates a logger, that paints all
	 * recorded meta data at the end in one diagram. 
	 * @param updatePeriod the metadata refresh rate
	 */
	public MetaDataLogger(CompositeMetaData cmd, Logger logger, Predicate continueLogging, int logMode, long updatePeriod, boolean startLoggingOnBegin) {
		if (cmd == null)
			throw new IllegalArgumentException("mdf must not be null");
		this.cmd = cmd;
		if (logger == null) {			
			this.logger = Logger.getLogger("LOGGER");
			Handler handler = new ConsoleHandler();
			handler.setFormatter(new SimpleFormatter());
			this.logger.addHandler(handler);
		}
		else			
			this.logger = logger;		
		this.continueLogging = continueLogging == null ? Predicates.TRUE : continueLogging;		
		this.map = new HashMap();
		time = 0;		
		this.logConfig = new BitSet();
		setLogMode(logMode);
		accessor = new MetaDataAccessor(this, updatePeriod);
		if (startLoggingOnBegin)
			startLogging();
	}
	
	/**
	 * Creates a new MetaDataLogger that uses the given composite metadata to create metadata. If
	 * logger is not null, it is used for logging, otherwise a default logger is created. The predicate
	 * determines, if the logging should continue. The int logMode determines the log modes, that can be combined,
	 * i.e. MetaDataLogger.ONE_DIAGRAM paints all measured values in one diagramm,  MetaDataLogger.ONE_DIAGRAM | MULTIPLE_DIAGRAMS
	 * paints all measured values in one diagramm and each serie in a separate diagram. All measured values are stored as 
	 * (md key, list)in the map, the list contains double[2] entry: (time, value). 
	 * @param cmd composite metadata
	 * @param logger the logger
	 * @param continueLogging the predicate
	 * @param logMode the log mode can be ORed with the class constants, i.e. 
	 * MetaDataLogger.ONE_DIAGRAM creates a logger, that paints all
	 * recorded meta data at the end in one diagram. 
	 * @param updatePeriod the metadata refresh rate
	 */
	public MetaDataLogger(CompositeMetaData cmd, Logger logger, Predicate continueLogging, int logMode, long updatePeriod) {
		this(cmd,logger, continueLogging, logMode, updatePeriod, true);
	}
	
	/**
	 * Creates a new MetaDataLogger that uses the given composite metadata to create metadata. If
	 * logger is not null, it is used for logging, otherwise a default logger is created. The predicate
	 * determines, if the logging should continue. The int logMode determines the log modes, that can be combined,
	 * i.e. MetaDataLogger.ONE_DIAGRAM paints all measured values in one diagramm,  MetaDataLogger.ONE_DIAGRAM | MULTIPLE_DIAGRAMS
	 * paints all measured values in one diagramm and each serie in a separate diagram. All measured values are stored as 
	 * (md key, list)in the map, the list contains double[2] entry: (time, value). 
	 * Uses 500 millis for refresh rate. 
	 * @param cmd composite metadata
	 * @param logger the logger
	 * @param continueLogging the predicate
	 * @param logMode the log mode can be ORed with the class constants, i.e. 
	 * MetaDataLogger.ONE_DIAGRAM creates a logger, that paints all recorded meta data at the end in one diagram.
	 */
	public MetaDataLogger(CompositeMetaData cmd, Logger logger, Predicate continueLogging, int logMode) {
		this(cmd, logger, continueLogging, logMode, 500);
	}

	
	/**
	 * Creates a new MetaDataLogger that uses the given composite metadata to create metadata. If
	 * logger is not null, it is used for logging, otherwise a default logger is created. The predicate
	 * determines, if the logging should continue. logMode is set to NOTHING, which means that all values are
	 * stored in the map, but nothing is done further just before termination. All measured values are stored 
	 * as (md key, list)in the map, the list contains double[2] entry: (time, value). Uses 500 millis for refresh rate.
	 * @param cmd composite metadata
	 * @param logger the logger
	 * @param continueLogging the predicate
	 * MetaDataLogger.ONE_DIAGRAM creates a logger, that paints all
	 * recorded meta data at the end in one diagram.
	 */
	public MetaDataLogger(CompositeMetaData cmd, Logger logger, Predicate continueLogging) {
		this(cmd, logger, continueLogging, NOTHING);
	}

	/**
	 * Creates a new MetaDataLogger that uses the given metadatafactory to create metadata. uses a default 
	 * logger for logging. The predicate is set to Predicates.TRUE which leads to termination if the associated
	 * sink is done. logMode is set to NOTHING, which means that all values are stored in the map, but nothing 
	 * is done further just before termination. All measured values are stored as (md key, list)in the map,
	 * the list contains double[2] entry: (time, value). Uses 500 millis for refresh rate.
	 * @param cmd composite metadata
	 */
	public MetaDataLogger(CompositeMetaData cmd) {
		this(cmd, null,Predicates.TRUE);
	}
	
	
	/**
	 * Returns the logger. That allows to add or remove log functionality during runtime.
	 * @return Returns the logger.
	 */
	public Logger getLogger() {
		return logger;
	}

	/**
	 * @return Returns the mdf.
	 */
	public CompositeMetaData getCMD() {
		return cmd;
	}
	/**
	 * @param cmd The cmd to set.
	 */
	public void setCMD(CompositeMetaData cmd) {
		if (cmd == null )
			throw new IllegalArgumentException("mdf must not be null");
		this.cmd = cmd;		
	}
	
	/**
	 * This method is invoked by the meta data accessor and logs meta data periodically.
	 */
	protected void creatSingleLogEntry() {
		 for (Iterator it =cmd.identifiers(); it.hasNext(); ) {
		 	String next = (String)it.next();
		 	((MetaDataHandler)cmd.get(next)).refresh();
	 		double result =	((Number)((MetaDataHandler)cmd.get(next)).getMetaData()).doubleValue();
		 	List l = (List)map.get(next);
		 	if (l == null) {
		 		l = new LinkedList();
		 		l.add(new double[]{time, result});
		 		map.put(next, l);
		 	}
		 	else {
		 		l.add(new double[]{time, result});
		 	}
		 }
		 time += accessor.getLastDelay();
	}
	
	/**
	 * Log an entry, if WRITE_ALL or SUMMARIZE_ALL positions have been 
	 * in the logConfig.	 
	 */
	protected void createLog() {
		if (logConfig.get(getPosition(WRITE_ALL))) {
			StringBuffer result = new StringBuffer();
				result.append("TIME\t");
			for (Iterator it =map.keySet().iterator(); it.hasNext(); )
				result.append(it.next()+"\t");
			List[] lists = new List[map.keySet().size()];
			int i=0;
			for (Iterator it =map.keySet().iterator(); it.hasNext(); i++) {
				String next = (String)it.next();
				lists[i] = (List)map.get(next);
			}
			for (int j=0; j<lists[0].size(); j++)
				for (int k=0; k<lists.length; k++)				
					if (k==0)
						result.append("\n"+((double[])lists[k].get(j))[0]+"\t"+((double[])lists[k].get(j))[1]);
					else
						result.append("\t"+((double[])lists[k].get(j))[1]);
			logger.info(result.toString());					
		}
		if (logConfig.get(getPosition(SUMMARIZE_ALL))) {
			for (Iterator it =map.keySet().iterator(); it.hasNext(); ) {
				String next = (String)it.next();
				List l = (List)map.get(next);
				Double avgValue = new Double(0);
				Average average = new Average();
				for (int i=0; i<l.size(); i++) {
					Double d = new Double(((double[])l.get(i))[1]);
					avgValue = i==0 ? (Double)average.invoke(null, d) : (Double)average.invoke(avgValue, d);										
				}
				logger.info(next+": "+avgValue);
			}			
		}
		if (logConfig.get(getPosition(SUMMARIZE_ALL_WITHOUT_NEGATIVES))) {
			for (Iterator it =map.keySet().iterator(); it.hasNext(); ) {
				String next = (String)it.next();
				List l = (List)map.get(next);
				Double avgValue = new Double(0);
				Average average = new Average();
				for (int i=0; i<l.size(); i++) {
					Double d = new Double(((double[])l.get(i))[1]);
					if (d<0)
						continue;
					avgValue = i==0 ? (Double)average.invoke(null, d) : (Double)average.invoke(avgValue, d);										
				}
				logger.info(next+": "+avgValue);
			}			
		}		
		if (logConfig.get(getPosition(SAVE_LISTS))) {
			if (additionalInfo ==null || !(additionalInfo instanceof String))
				System.out.println("No save operation done because of missing additional info");
			
			String directory = (String)additionalInfo;			
			for (Iterator it =map.keySet().iterator(); it.hasNext();) {
				String next = (String)it.next();
				List l = (List)map.get(next);
				UserOutput.saveRawData(directory, next, new List[]{l});
			}			
		}
		if (!logConfig.get(getPosition(WRITE_ALL)) && !logConfig.get(getPosition(SUMMARIZE_ALL))
				&& !logConfig.get(getPosition(SAVE_LISTS)) && !logConfig.get(getPosition(SUMMARIZE_ALL_WITHOUT_NEGATIVES)))
			System.out.println("No log operation done because of missing flags");
	}
	/**
	 * Paints a log entry, if ONE_DIAGRAM or MULTIPLE_DIAGRAMS positions have been 
	 * in the logConfig.
	 */
	protected void createDiagrams() {
		if (logConfig.get(getPosition(ONE_DIAGRAM))) {
			List[] lists = new List[map.keySet().size()];
			String[] descriptions = new String[map.keySet().size()];
			int i=0;
			for (Iterator it =map.keySet().iterator(); it.hasNext(); i++) {
				descriptions[i] = (String)it.next();
				lists[i] = (List)map.get(descriptions[i]);				
			}			
			new UserOutput("Output for all logged metadata", lists, descriptions, UserOutput.NON_INCREASING_VALUES);
		}
		if (logConfig.get(getPosition(MULTIPLE_DIAGRAMS))) {
			for (Iterator it =map.keySet().iterator(); it.hasNext(); ) {
				String next = (String)it.next();
				List l = (List)map.get(next);
				new UserOutput("Single output for "+next, new List[]{l}, new String[]{next}, UserOutput.NON_INCREASING_VALUES);
			}
		}
		if (!logConfig.get(getPosition(ONE_DIAGRAM)) && !logConfig.get(getPosition(MULTIPLE_DIAGRAMS)))
			System.out.println("No paint operation done because of missing flags");
	}
	
	/**
	 * Terminates the logging.
	 * If WRITE_ALL, SUMMARIZE_ALL or SUMMARIZE_ALL_WITHOUT_NEGATIVES has been set in the logConfig, a log entry is created.
	 * If ONE_DIAGRAM or MULTIPLE_DIAGRAMS has been set in the logConfig, the log entry is illustrated.
	 * If CLOSE_LOGGER_ON_EXIT has been set in the logConfig, all underlying 
	 * handlers are closed.
	 */
	protected void terminate() {
		if (logConfig.get(getPosition(WRITE_ALL)) || logConfig.get(getPosition(SUMMARIZE_ALL)) 
				|| logConfig.get(getPosition(SUMMARIZE_ALL_WITHOUT_NEGATIVES)) || logConfig.get(getPosition(SAVE_LISTS)))
			createLog();
		
		if (logConfig.get(getPosition(ONE_DIAGRAM)) || logConfig.get(getPosition(MULTIPLE_DIAGRAMS)))
			createDiagrams();

		if (logConfig.get(getPosition(CLOSE_LOGGER_ON_EXIT))) {
			Handler[] handlers = logger.getHandlers();
			for (int i=0 ; i< handlers.length ; i++)
				handlers[i].close();			
		}
	}
	
	/**
	 * Helper method.
	 * @param value
	 * @return position
	 */
	protected int getPosition(int value) {
		if (value == 0)
			return 0;
		int value2 =1;
		for (int i=0; value2<= value; i++, value2*=2)
			if (value2 == value)
				return i;
		return -1;
	}

	/**
	 * @return Returns the map.
	 */
	public Map getMap() {
		return map;
	}
	
	public void setLogMode(int logMode) {
		if (logMode >=0) {
			this.logConfig.clear();
			int temp =1;
			int index;
			for (index=0; temp<logMode ;temp*=2,index++);
			while (index >=0) {
				if ((temp) <= logMode) {
					logMode -= (temp);
					this.logConfig.set(index);
				}
				index--;
				temp /= 2;
			}
			return;
		}
		throw new IllegalArgumentException("Only non-negative integers allowed for logMode");
	}
	
	public void startLogging() {
		if (started)
			throw new IllegalStateException("Logger already started.");
		started = true;
		accessor.start();
	}
	
	public void stopLogging() {
		accessor.terminate = true;
	}
	
	/**
	 * Example
	 * @param args
	 */
	public static void main(String[] args) {
		final int noOfElements1 = 2000;
		final int noOfElements2 = 1500;
		final int buckets = 10;
		final int startInc = 50;
		final int intervalSize = 100;
		final long seed = 42;
		final int g = 2;
		final HashMap<Integer,List<Long>> in1 = new HashMap<Integer,List<Long>>();
		final HashMap<Integer,List<Long>> in2 = new HashMap<Integer,List<Long>>();
		final Integer[] hashCodes = new Integer[buckets];
		for (int i = 0; i < buckets; i++)
			hashCodes[i] = new Integer(i);

		final Function<TemporalObject<Integer>,Integer> hash = new Function<TemporalObject<Integer>,Integer>() {
			@Override
			public Integer invoke(TemporalObject<Integer> o) {
				return o.getObject()%buckets;
			}
		};
					
		RandomNumber<Integer> r1 = new RandomNumber<Integer>(
			RandomNumber.DISCRETE, noOfElements1, startInc/2
		);
		RandomNumber<Integer> r2 = new RandomNumber<Integer>(
			RandomNumber.DISCRETE, noOfElements2, startInc/2
		);		
		
		Source<TemporalObject<Integer>> s1 = Pipes.decorateWithRandomTimeIntervals(r1, in1, hashCodes, startInc, intervalSize, seed);
		Source<TemporalObject<Integer>> s2 = Pipes.decorateWithRandomTimeIntervals(r2, in2, hashCodes, startInc, intervalSize, seed);
		
		final TemporalWindow<Integer> w1 = new TemporalWindow<Integer>(s1, intervalSize);		
		w1.addChangeOfWindowSize(1000, 50);
		
		final TemporalWindow<Integer> w2 = new TemporalWindow<Integer>(s2, intervalSize);
		w2.addChangeOfWindowSize(600, 250);		
		
		final TimeGranularity<Integer> tg1 = new TimeGranularity<Integer>(w1, g);
		//tg1.addChangeOfTimeGranularity(1500, 15);
		//tg1.setMetaDataFactory(mdf_tg1);
		
		final TimeGranularity<Integer> tg2 = new TimeGranularity<Integer>(w2, g);	
		//tg2.addChangeOfTimeGranularity(800, 5);
		//tg2.setMetaDataFactory(mdf_tg2);

		// Symmetric Hash-Join
		TemporalJoin<Integer,Object[]> join = new TemporalJoin<Integer,Object[]>(tg1, tg2, 0, 1,
			new TemporalJoinListSA<Integer>(
				new HashSAImplementor<TemporalObject<Integer>>(hash, 2),
				0, 2
			),
			new TemporalJoinListSA<Integer>(
				new HashSAImplementor<TemporalObject<Integer>>(hash, 2),
				1, 2
			),
			NTuplify.DEFAULT_INSTANCE
		);
		join.getMetaDataManagement().include(INPUT_RATE, AVG_INPUT_RATE, VAR_INPUT_RATE, 
				OUTPUT_RATE, AVG_OUTPUT_RATE, VAR_OUTPUT_RATE, MEM_USAGE,
				AVG_MEM_USAGE, VAR_MEM_USAGE, INPUT_OUTPUT_RATIO, AVG_INPUT_OUTPUT_RATIO,
				VAR_INPUT_OUTPUT_RATIO
		);
		VisualSink sink = new VisualSink(join, "Join", true);
		QueryExecutor exec = new QueryExecutor();
		new MetaDataLogger(join.getMetaData(), null, notSourceIsClosed(join), MULTIPLE_DIAGRAMS);
		exec.registerQuery(sink);
		exec.startAllQueries();
	}
}

