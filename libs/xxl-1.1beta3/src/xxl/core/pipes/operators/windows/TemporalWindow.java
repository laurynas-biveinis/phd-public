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

package xxl.core.pipes.operators.windows;

import static xxl.core.pipes.operators.CostModelMetaDataIdentifiers.D_ESTIMATION;
import static xxl.core.pipes.operators.CostModelMetaDataIdentifiers.G_ESTIMATION;
import static xxl.core.pipes.operators.CostModelMetaDataIdentifiers.L_ESTIMATION;

import java.util.ArrayList;

import xxl.core.collections.MapEntry;
import xxl.core.functions.Function;
import xxl.core.pipes.elements.TemporalObject;
import xxl.core.pipes.elements.TimeInterval;
import xxl.core.pipes.metaData.DefaultMetaDataHandler;
import xxl.core.pipes.metaData.MetaDataDependencies;
import xxl.core.pipes.metaData.TriggeredEvaluationMetaDataHandler;
import xxl.core.pipes.operators.Pipes;
import xxl.core.pipes.operators.mappers.Mapper;
import xxl.core.pipes.operators.mappers.TemporalMapper;
import xxl.core.pipes.queryGraph.QueryExecutor;
import xxl.core.pipes.sinks.Printer;
import xxl.core.pipes.sources.Enumerator;
import xxl.core.pipes.sources.Source;

/**
 * This class should be used to model windows at the sources of a query plan. It
 * takes no care of the time granularity and simply sets the end timestamp of an
 * incoming element with respect to its start timestamp.
 * 
 * Note that the window function for setting the end timestamp has to maintain
 * the lexicographical order of the output stream.
 * @param <E> 
 */
public class TemporalWindow<E> extends TemporalMapper<E,E>  {

	// sets the end timestamp to start + windowSize + 1
	public static final Function<Long, Function<Long, Long>> SLIDING_WINDOW_FACTORY() {
		return new Function<Long, Function<Long, Long>>() {

			@Override
			public Function<Long, Long> invoke(final Long currentWindowSize) {
				return new Function<Long, Long>() {

					@Override
					public Long invoke(Long start) {
						return start + currentWindowSize + 1;
					}
				};
			}
		};
	}

	public static final Function<Long, Function<Long, Long>> FIXED_WINDOW_FACTORY(
			final long offset) {
		return new Function<Long, Function<Long, Long>>() {

			@Override
			public Function<Long, Long> invoke(final Long currentWindowSize) {
				return new Function<Long, Long>() {

					protected long currentStart = offset;

					protected long currentEnd;

					@Override
					public Long invoke(Long start) {
						currentEnd = currentStart + currentWindowSize + 1;
						while (start >= currentEnd) {
							currentStart = currentEnd;
							currentEnd = currentStart + currentWindowSize + 1;
						}
						return currentEnd;
					}
				};
			}
		};
	}

	// MapEntry -> time instant x window size / time granularity
	// Lists have to be ordered by time
	protected ArrayList<MapEntry<Long, Long>> windowSizesList = new ArrayList<MapEntry<Long, Long>>();

	protected long lastStart = Long.MIN_VALUE;

	protected Function<Long, Long> windowFunction;

	protected Function<Long, Function<Long, Long>> windowFactory;
	
	protected boolean isSlidingWindow = false;

	
	/**
	 * Inner class for metadata management. 
	 */
	public class TemporalWindowMetaDataManagement extends AbstractTemporalPipeMetaDataManagement {
				
		protected Double lEst;
		
		protected volatile boolean updateLEst;
		
		public TemporalWindowMetaDataManagement() {
			super();
			this.updateLEst = false;
			this.lEst = Double.NaN;
		}
		
		public synchronized void setL(double l) {
			if (isSlidingWindow && lEst!=l)
				throw new IllegalArgumentException("Current L of this sliding window is "+lEst);
			this.lEst = l;
			refresh(L_ESTIMATION);
		}
						
		/* (non-Javadoc)
		 * @see xxl.core.pipes.operators.AbstractTemporalPipe.AbstractTemporalPipeMetaDataManagement#addMetaData(java.lang.Object)
		 */
		@Override
		protected boolean addMetaData(Object metaDataIdentifier) {
			if (super.addMetaData(metaDataIdentifier))
				return true;
					
			if (metaDataIdentifier.equals(D_ESTIMATION) ||
				metaDataIdentifier.equals(G_ESTIMATION)) {
					metaData.add(metaDataIdentifier, 
						TriggeredEvaluationMetaDataHandler.keepMetaDataFragmentFromSource(this, metaDataIdentifier, 0, Double.NaN)
					);
					return true;
			}
			if (metaDataIdentifier.equals(L_ESTIMATION)) {
				updateLEst = true;
				metaData.add(metaDataIdentifier, new DefaultMetaDataHandler<Double>(this, metaDataIdentifier,
					new Function<Object,Double>() {			
						@Override
						public Double invoke() {
							synchronized(metaDataManagement) {
								if (isClosed || isDone)
									return Double.NaN;
								return lEst;
							}
						}
					})
				);
				return true;
			}
			return false;
		}
		
		/* (non-Javadoc)
		 * @see xxl.core.pipes.operators.AbstractTemporalPipe.AbstractTemporalPipeMetaDataManagement#removeMetaData(java.lang.Object)
		 */
		@Override
		protected boolean removeMetaData(Object metaDataIdentifier) {
			if (super.removeMetaData(metaDataIdentifier))
				return true;
			
			if (metaDataIdentifier.equals(D_ESTIMATION) ||
				metaDataIdentifier.equals(G_ESTIMATION)) {
				metaData.remove(metaDataIdentifier);
				return true;
			}
			if (metaDataIdentifier.equals(L_ESTIMATION)) {
				updateLEst = false;
				metaData.remove(metaDataIdentifier);
				return true;
			}
			return false;
		}
				
	}
	
	
	public TemporalWindow(Function<Long, Function<Long, Long>> windowFactory, final long initialWindowSize) {
		super(new Function<TemporalObject<E>,TemporalObject<E>>() {});
		this.windowFactory = windowFactory;
		addChangeOfWindowSize(0, initialWindowSize);
	}
	
	public TemporalWindow(Source<? extends TemporalObject<E>> source, int sourceID, Function<Long, Function<Long, Long>> windowFactory, long initialWindowSize) {
		this(windowFactory, initialWindowSize);
		if (!Pipes.connect(source, this, sourceID))
			throw new IllegalArgumentException("Problems occured while establishing the connections between the nodes."); 
	}

	public TemporalWindow(Source<? extends TemporalObject<E>> source,
			Function<Long, Function<Long, Long>> windowFactory,
			long initialWindowSize) {
		this(source, DEFAULT_ID, windowFactory, initialWindowSize);
	}

	// default: sliding window
	public TemporalWindow(Source<? extends TemporalObject<E>> source,
			long initialWindowSize) {
		this(source, SLIDING_WINDOW_FACTORY(), initialWindowSize);
		isSlidingWindow = true;
		synchronized(metaDataManagement) {
			((TemporalWindowMetaDataManagement)metaDataManagement).lEst = new Double(initialWindowSize+1);
		}
	}

	// unconnected sliding window
	public TemporalWindow(final long initialWindowSize) {
		this(SLIDING_WINDOW_FACTORY(), initialWindowSize);
		isSlidingWindow = true;
		synchronized(metaDataManagement) {
			((TemporalWindowMetaDataManagement)metaDataManagement).lEst = new Double(initialWindowSize+1);
		}		
	}
	
	static {
		MetaDataDependencies.addMetaDataDependenciesOnSource(TemporalWindow.class, 0, D_ESTIMATION, D_ESTIMATION);
		MetaDataDependencies.addMetaDataDependenciesOnSource(TemporalWindow.class, 0, L_ESTIMATION, L_ESTIMATION);
		MetaDataDependencies.addMetaDataDependenciesOnSource(TemporalWindow.class, 0, G_ESTIMATION, G_ESTIMATION);
	}

	public Function<TemporalObject<E>, TemporalObject<E>> getMappingFunction(
			final Function<Long, Long> windowFunction) {
		return new Function<TemporalObject<E>, TemporalObject<E>>() {
			@Override
			public TemporalObject<E> invoke(TemporalObject<E> tso) {
				return new TemporalObject<E>(tso.getObject(),
						new TimeInterval(tso.getStart(), windowFunction.invoke(tso.getStart())));
			}
		};
	}

	/* (non-Javadoc)
	 * @see xxl.core.pipes.operators.mappers.TemporalMapper#processObject(xxl.core.pipes.elements.TemporalObject, int)
	 */
	@Override
	public void processObject(TemporalObject<E> tso, int ID) {
		long start = tso.getStart();
		lastStart = start;

		// detect change of window size
		while (windowSizesList.size() > 0
				&& start >= windowSizesList.get(0).getKey()) {
			long newWindowSize = windowSizesList.get(0).getValue();
			// set new mapping function
			this.mapping = getMappingFunction(windowFactory.invoke(newWindowSize));
			TemporalWindowMetaDataManagement mdm = (TemporalWindowMetaDataManagement)metaDataManagement;
			if (isSlidingWindow) {				
				synchronized(metaDataManagement) {
					mdm.lEst = new Double(newWindowSize+1);
				}
				if (mdm.updateLEst)
					mdm.refresh(L_ESTIMATION);
			}
			windowSizesList.remove(0);
		}

		super.processObject(tso, ID);
	}

	/**
	 * @param pointInTime
	 * @param newWindowSize
	 */
	public void addChangeOfWindowSize(long pointInTime, long newWindowSize) {
		if (pointInTime <= lastStart)
			throw new IllegalArgumentException("It is not possible to change the window size for the past.");
		if (windowSizesList.size() > 0 && pointInTime <= windowSizesList.get(windowSizesList.size() - 1).getKey())
			throw new IllegalArgumentException("Another window size corresponding to the given point in time has been specified already.");
		windowSizesList.add(new MapEntry<Long, Long>(pointInTime, newWindowSize));
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.operators.mappers.TemporalMapper#createMetaDataManagement()
	 */
	@Override
	public void createMetaDataManagement() {
		if (metaDataManagement != null)
			throw new IllegalStateException("An instance of MetaDataManagement already exists.");
		metaDataManagement = new TemporalWindowMetaDataManagement();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Enumerator e = new Enumerator(500, 0);
		Mapper<Integer, TemporalObject<Integer>> m = new Mapper<Integer, TemporalObject<Integer>>(
				e, new Function<Integer, TemporalObject<Integer>>() {
					protected long count = 0;

					@Override
					public TemporalObject<Integer> invoke(Integer o) {
						return new TemporalObject<Integer>(o,
								new TimeInterval(count += 5, count + 10));
					}
				});

		long windowSize = 1000;
		TemporalWindow<Integer> w = new TemporalWindow<Integer>(m, windowSize); // sliding window (1000 ms)

		w.addChangeOfWindowSize(100, 500);
		w.addChangeOfWindowSize(200, 250);
		w.addChangeOfWindowSize(300, 100);

		Pipes.verifyStartTimeStampOrdering(w);
		Printer printer = new Printer<TemporalObject<Integer>>(w);
		
		QueryExecutor exec = new QueryExecutor();
		exec.registerQuery(printer);
		exec.startQuery(printer);
	}

}
