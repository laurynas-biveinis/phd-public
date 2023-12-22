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

package xxl.core.pipes.sources;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import xxl.core.functions.Function;
import xxl.core.math.statistics.parametric.aggregates.Average;
import xxl.core.math.statistics.parametric.aggregates.Variance;
import xxl.core.pipes.metaData.DefaultMetaDataHandler;
import xxl.core.pipes.metaData.MetaDataDependencies;
import xxl.core.pipes.metaData.PeriodicEvaluationMetaDataHandler;
import xxl.core.pipes.metaData.TriggeredEvaluationMetaDataHandler;
import xxl.core.pipes.operators.Pipes;
import xxl.core.pipes.processors.SourceProcessor;
import xxl.core.pipes.queryGraph.AbstractNode;
import xxl.core.pipes.queryGraph.Node;
import xxl.core.pipes.sinks.Sink;
import xxl.core.pipes.sinks.SinkMetaDataManagement;
import xxl.core.pipes.sources.SourceMetaDataManagement.ContainsSourceMetaDataDependencies;
import xxl.core.util.metaData.MetaDataException;
import xxl.core.util.metaData.MetaDataHandler;

/**
 * Superclass for each kind of initial sources in a query graph. Abstract
 * implementation of the interface {@link Source}. <BR>
 * Simulates an active data source by starting a new thread,
 * i.e. an instance of the class <CODE>SourceProcessor</CODE>, which
 * periodically transfers elements to all subscribed sinks. <BR>
 * The next element of this source is accessed via the abstract <CODE>next</CODE> method,
 * which has to be overwritten by a concrete source inheriting from this
 * abstract class.
 *
 * @see xxl.core.pipes.processors.SourceProcessor
 * @since 1.1
 */
public abstract class AbstractSource<O> extends AbstractNode implements Source<O> {
		
	/**
	 * Flag that signals, if the source has already been opened.
	 */
	protected volatile boolean isOpened;
	
	/**
	 * Flag that signals, if the source has already been closed.
	 */
	protected volatile boolean isClosed;
	
	/** 
	 * Subscribed sinks. The IDs these sinks got
	 * registered with are stored in the array <CODE>sinkIDs</CODE>
	 * using the same position.
	 */
	protected Sink<? super O>[] sinks;
	
	/**
	 * IDs belonging to the subscribed sinks.
	 */
	protected int[] sinkIDs;
	
	/**
	 * Thread that simulates the activity of this source.
	 */
	protected SourceProcessor processor;
		
	/*
	 * PROCESSING LOCK
	 */
	protected final ReentrantReadWriteLock processingRWLock = new ReentrantReadWriteLock();
	protected final ReadLock processingRLock = processingRWLock.readLock();
	protected final WriteLock processingWLock = processingRWLock.writeLock();
	
	
	/**
	 * Container for the meta data dependencies of an AbstractSource.
	 */
	public static class SourceMetaDataDependenciesContainer extends NodeMetaDataDependenciesContainer implements ContainsSourceMetaDataDependencies {

		protected Map<Integer,Map<Object,Object[]>> metaDataRequiredFromSinks;
		
		/**
		 * @param className
		 */
		public SourceMetaDataDependenciesContainer(Class<? extends Node> className) {
			super(className);
		}
		
		/* (non-Javadoc)
		 * @see xxl.core.pipes.sources.SourceMetaDataManagement.ContainsSourceMetaDataDependencies#addMetaDataDependenciesOnSink(int, java.lang.Object, java.lang.Object[])
		 */
		public void addMetaDataDependenciesOnSink(int sinkIndex, Object metaDataIdentifier, Object... dependencies) {
			if (metaDataRequiredFromSinks == null)
				metaDataRequiredFromSinks = new HashMap<Integer,Map<Object,Object[]>>();
			if (!metaDataRequiredFromSinks.containsKey(sinkIndex)) 
				metaDataRequiredFromSinks.put(sinkIndex, new HashMap<Object,Object[]>());
			Map<Object,Object[]> sinkRequirements = metaDataRequiredFromSinks.get(sinkIndex);
			if (sinkRequirements.containsKey(metaDataIdentifier))
				throw new IllegalArgumentException("Only one dependency declaration allowed per class and sink and metaDataIdentifier");				
			sinkRequirements.put(metaDataIdentifier, dependencies);
		}
			
		/* (non-Javadoc)
		 * @see xxl.core.pipes.sources.SourceMetaDataManagement.ContainsSourceMetaDataDependencies#getMetaDataDependenciesOnSink(int, java.lang.Object)
		 */
		public Object[] getMetaDataDependenciesOnSink(int sinkIndex, Object metaDataIdentifier) {
			Map<Object,Object[]> map = getMetaDataDependenciesOnSink(sinkIndex);
			if (!map.containsKey(metaDataIdentifier))
				return new Object[0];
			return map.get(metaDataIdentifier);
		}
		
		/* (non-Javadoc)
		 * @see xxl.core.pipes.sources.SourceMetaDataManagement.ContainsSourceMetaDataDependencies#getMetaDataDependenciesOnSink(int)
		 */
		public Map<Object,Object[]> getMetaDataDependenciesOnSink(int sinkIndex) {
			if (metaDataRequiredFromSinks == null || !metaDataRequiredFromSinks.containsKey(sinkIndex))
				return Collections.emptyMap();
			return metaDataRequiredFromSinks.get(sinkIndex);
		}
		
	}

	
	protected volatile boolean measureOutputRate = false;

	/**
	 * Inner class for metadata management. 
	 */
	public class AbstractSourceMetaDataManagement extends AbstractNodeMetaDataManagement implements SourceMetaDataManagement {
		
		//	AVAILABLE METADATA
		public static final String OUTPUT_RATE = "OUTPUT_RATE"; // elements per ms
		public static final String AVG_OUTPUT_RATE = "AVG_OUTPUT_RATE";	
		public static final String VAR_OUTPUT_RATE = "VAR_OUTPUT_RATE";	
		public static final String NO_OF_SINKS = "NO_OF_SINKS";
		
		/*
		 * ATTRIBUTES REQUIRED TO PROVIDE METADATA
		 */
		protected long outputCounter;
		
		protected long updatePeriod;
			
		/**
		 * Stores for each sinkID (key) a list of metadata identifiers (value) this sinks depends on.
		 */
		protected HashMap<Integer,List<Object>> sinkDependencies;
		
		
		/**
		 * 
		 */
		public AbstractSourceMetaDataManagement() {
			this.updatePeriod = 500;
			this.sinkDependencies = null;
		}
				
		/**
		 * Sets the update period.
		 * 
		 * @param millis update period
		 */
		public synchronized void setUpdatePeriod(long millis) {
			this.updatePeriod = millis;
		}
					
		/* (non-Javadoc)
		 * @see xxl.core.pipes.sources.SourceMetaDataManagement#getDependenciesOnSink(int, java.lang.Object)
		 */
		@SuppressWarnings("unchecked")
		public synchronized Object[] getDependenciesOnSink(int sinkIndex, Object metaDataIdentifier) {
			return MetaDataDependencies.getMetaDataDependenciesOnSink((Class<? extends Source>)enclosingClass, sinkIndex, metaDataIdentifier);
		}
		
		/**
		 * @param include
		 * @param metaDataIdentifiers
		 * @return
		 */
		protected boolean manageDependenciesOnSinks(boolean include, Object... metaDataIdentifiers) {
			AbstractSource.this.graph.RLock.lock();
			try {
				if (sinks == null) return false;
				boolean ret = true;
				Object[] metaDataFromSinks;
				SinkMetaDataManagement sinkMDM;
				for (Object metaDataIdentifier : metaDataIdentifiers) {
					for (int i = 0; i < sinks.length; i++) {
						metaDataFromSinks = getDependenciesOnSink(i, metaDataIdentifier);
						if (metaDataFromSinks.length > 0) {
							try {
								sinkMDM = (SinkMetaDataManagement)sinks[i].getMetaDataManagement();
								ret &= sinkMDM.sourceDependency(include, sinkIDs[i], metaDataFromSinks);
							} catch (Exception e) {
								ret = false;
							}
						}
					}
				}
				return ret;
			}
			finally {
				AbstractSource.this.graph.RLock.unlock();
			}
		}
				
		/* (non-Javadoc)
		 * @see xxl.core.pipes.sources.SourceMetaDataManagement#notifySinks(java.lang.Object[])
		 */
		public void notifySinks(Object... changedMDIdentifiers) {
			AbstractSource.this.graph.RLock.lock();
			try {
				if (sinkDependencies != null) 
					for (int i = 0; i < sinks.length; i++) {
						if (sinkDependencies.containsKey(sinkIDs[i])) {
							Object[] md = sinkDependencies.get(sinkIDs[i]).toArray();
							List<Object> changedMD = new ArrayList<Object>();
							for (Object c : changedMDIdentifiers) 
								for (Object m : md) 
									if (c.equals(m)) 
										changedMD.add(c);
							if (changedMD.size() > 0) {
								((SinkMetaDataManagement)sinks[i].getMetaDataManagement()).downstreamNotification(
									sinkIDs[i], changedMD.toArray()
								);
							}
						}
					}
			}
			finally {
				AbstractSource.this.graph.RLock.unlock();
			}
		}
		
		/* (non-Javadoc)
		 * @see xxl.core.pipes.sources.SourceMetaDataManagement#upstreamNotification(int, java.lang.Object[])
		 */
		public void upstreamNotification(int sinkID, Object... changedMDIdentifiers) {
			AbstractSource.this.graph.RLock.lock();
			try {
				int sinkIndex = Pipes.getSinkIndex(sinkIDs, sinkID);
				Object[] affectedMD = MetaDataDependencies.affectsUpstreamMetaData((Class<? extends Source>)enclosingClass, sinkIndex, changedMDIdentifiers);
				for (Object md : affectedMD) 
					((MetaDataHandler)metaData.get(md)).refresh();
			}
			finally {
				AbstractSource.this.graph.RLock.unlock();
			}
		}
		
		/* (non-Javadoc)
		 * @see xxl.core.pipes.queryGraph.AbstractNode.AbstractNodeMetaDataManagement#globalNotification(java.lang.Object[])
		 */
		@Override
		public void globalNotification(Object... changedMDIdentifiers) {
			localNotification(changedMDIdentifiers);
			notifySinks(changedMDIdentifiers);
		}
		
		/* (non-Javadoc)
		 * @see xxl.core.pipes.sources.SourceMetaDataManagement#sinkDependency(boolean, int, java.lang.Object[])
		 */
		public boolean sinkDependency(boolean create, int sinkID, Object... metaDataIdentifiers) {
			return manageExternalDependency(create, sinkDependencies, sinkID, metaDataIdentifiers);
		}
				
		protected boolean removeAllSinkDependencies(final int sinkID) {
			return removeAllExternalDependencies(sinkDependencies, sinkID);
		}
		
		/* (non-Javadoc)
		 * @see xxl.core.pipes.queryGraph.AbstractNode.AbstractNodeMetaDataManagement#include(java.lang.Object[])
		 */
		@Override
		public boolean include(Object... metaDataIdentifiers) throws MetaDataException {
			boolean ret = manageDependenciesOnSinks(true, metaDataIdentifiers);
			ret &= super.include(metaDataIdentifiers);
			return ret;
		}
				
		/* (non-Javadoc)
		 * @see xxl.core.pipes.queryGraph.AbstractNode.AbstractNodeMetaDataManagement#exclude(java.lang.Object[])
		 */
		@Override
		public boolean exclude(Object... metaDataIdentifiers) throws MetaDataException {
			boolean ret = manageDependenciesOnSinks(false, metaDataIdentifiers);
			ret &= super.exclude(metaDataIdentifiers);
			return ret;
		}
			
		/* (non-Javadoc)
		 * @see xxl.core.util.AbstractMetaDataManagement#addMetaData(java.lang.Object)
		 */
		@Override
		protected boolean addMetaData(Object metaDataIdentifier) {		
			if (metaDataIdentifier.equals(OUTPUT_RATE)) {
				this.outputCounter = 0;
				measureOutputRate = true;
				metaData.add(metaDataIdentifier, new PeriodicEvaluationMetaDataHandler<Double>(this, metaDataIdentifier,
					new Function<Object,Double>() {
						@SuppressWarnings("unchecked")
						@Override
						public Double invoke() {
							synchronized(metaDataManagement) {
								double outputRate = (double)outputCounter/updatePeriod;
								outputCounter = 0;
								return outputRate;
							}
						}
					}, updatePeriod)
				);
				return true;
			}
			if (metaDataIdentifier.equals(AVG_OUTPUT_RATE)) {
				metaData.add(metaDataIdentifier, TriggeredEvaluationMetaDataHandler.aggregateMetaDataFragment(metaDataManagement, this, OUTPUT_RATE, AVG_OUTPUT_RATE, new Average(), Double.NaN));					
				return true;
			}
			if (metaDataIdentifier.equals(VAR_OUTPUT_RATE)) {
				metaData.add(metaDataIdentifier, TriggeredEvaluationMetaDataHandler.aggregateMetaDataFragment(metaDataManagement, this, OUTPUT_RATE, VAR_OUTPUT_RATE, new Variance(), Double.NaN));
				return true;
			}
			if (metaDataIdentifier.equals(NO_OF_SINKS)) {
				metaData.add(metaDataIdentifier, new DefaultMetaDataHandler<Integer>(this, metaDataIdentifier,
					new Function<Object,Integer>() {
						@Override
						public Integer invoke () {
							synchronized(metaDataManagement) {
								return getNoOfSinks();
							}
						}
					})
				);
				return true;
			}
			return false;
		}
		
		/* (non-Javadoc)
		 * @see xxl.core.util.AbstractMetaDataManagement#removeMetaData(java.lang.Object)
		 */
		@Override
		protected boolean removeMetaData(Object metaDataIdentifier) {
			if (metaDataIdentifier.equals(OUTPUT_RATE)) {
				this.outputCounter = 0;
				measureOutputRate = false;
				((PeriodicEvaluationMetaDataHandler)metaData.get(metaDataIdentifier)).close();
				metaData.remove(metaDataIdentifier);
				return true;
			}
			if (metaDataIdentifier.equals(AVG_OUTPUT_RATE) || 
				metaDataIdentifier.equals(VAR_OUTPUT_RATE) ||
				metaDataIdentifier.equals(NO_OF_SINKS)) {
					metaData.remove(metaDataIdentifier);
					return true;
			}
			return false;
		}
		
		/* (non-Javadoc)
		 * @see xxl.core.pipes.sources.SourceMetaDataManagement#getMetaDataFragmentFromSink(int, java.lang.Object, java.lang.Object)
		 */
		public Object getMetaDataFragmentFromSink(int sinkIndex, Object metaDataIdentifier, Object unknown) {
			AbstractSource.this.graph.RLock.lock();
			try {
				return getSink(sinkIndex).getMetaData().get(metaDataIdentifier);
			}
			catch (Exception e) {
				return unknown;
			}
			finally {
				AbstractSource.this.graph.RLock.unlock();
			}
		}
		
	}
	
	
	/** 
	 * Helps to create a new autonomous initial source in a query graph.
	 * 
	 * @param processor The thread simulating this source's activity.
	 */ 
	public AbstractSource(SourceProcessor processor) {
		this.processor = processor;
		if (processor != null)
			this.processor.registerSource(this);
		this.isClosed = false;
		this.isOpened = false;
	}

	/** 
	 * Helps to create a new autonomous initial source in a query graph.
	 * Creates a new instance of the class <CODE>SourceProcessor</CODE>
	 * using the given period.
	 * 
	 * @param period The constant period of time between two transferred successive elements.
	 */ 
	public AbstractSource(long period) {
		this(new SourceProcessor(period));
	}

	/** 
	 * Helps to create a new autonomous initial source in a query graph.
	 * Transfers the source's elements as fast as possible, i.e., the attribute
	 * <CODE>period</CODE> of a <CODE>SourceProcessor</CODE> is set to 0.
	 * The output rate is not measured.
	 */ 
	public AbstractSource() {
		this(0);
	}
	
	static {
		MetaDataDependencies.addLocalMetaDataDependencies(AbstractSource.class, AbstractSource.AbstractSourceMetaDataManagement.AVG_OUTPUT_RATE, AbstractSource.AbstractSourceMetaDataManagement.OUTPUT_RATE);
		MetaDataDependencies.addLocalMetaDataDependencies(AbstractSource.class, AbstractSource.AbstractSourceMetaDataManagement.VAR_OUTPUT_RATE, AbstractSource.AbstractSourceMetaDataManagement.OUTPUT_RATE);
	}

	/**
	 * Starts the associated processor.
	 */
	protected void startProcessor() {
		processingWLock.lock();
		try {
			if (processor != null) {
				State state = processor.getState();
				if (state.equals(State.NEW))
					processor.start();
				if (state.equals(State.TERMINATED))
					throw new IllegalStateException("Thread (Processor) did already terminate.");
			}
		}
		finally {
			processingWLock.unlock();
		}
	}
	
	/**
	 * Opens this source and starts the thread simulating the source's activity. <BR>
	 * Sets the flag <CODE>isOpenend</CODE>.
	 *
	 * @throws xxl.core.pipes.sources.SourceIsClosedException If the source has already been closed.
	 */
	public void open() throws SourceIsClosedException {
		if (isClosed)
			throw new SourceIsClosedException("Source has already been closed.");
		if (isOpened) return;
		
		isOpened = true;
		
		startProcessor();
	}

	/**
	 * If a source is closed, all subscribed sinks are unsubscribed. 
	 * <BR>
	 * Sets the flag <CODE>isClosed</CODE> and clears
	 * the flag <CODE>isOpened</CODE>.
	 */
	public void close() {
		if (isClosed) return;			
		
		processingWLock.lock();
		graph.WLock.lock();
		try {
			if (isClosed) return;
			if (sinks != null) {
				for (int i = 0; i < sinks.length; i++)
					this.unsubscribe(sinks[i], sinkIDs[i]);
			}
			sinks = null;
			sinkIDs = null;
			isOpened = false;
			isClosed = true;
		}
		finally {
			graph.WLock.unlock();
			processingWLock.unlock();
		}
	}

	/**
	 * Transfers the given element to all subscribed sinks.
	 * The <CODE>process</CODE> method is invoked for each of these sinks
	 * with the element to be transferred and
	 * the ID the corresponding sink initially specified during its subscription.
	 * <BR>
	 * Additionally, this method measures the outputRate if measureOutputRate is set.
	 * 
	 * @param o The element to be transferred.
	 */
	public void transfer(O o) {
		graph.RLock.lock();
		try {
			if (sinks != null) {
				for (int i = 0; i < sinks.length; i++)
					sinks[i].process(o, sinkIDs[i]);
				if (measureOutputRate) {
					synchronized(metaDataManagement) {
						if (measureOutputRate) 
							((AbstractSourceMetaDataManagement)metaDataManagement).outputCounter++;
				    }
				}
			}
		}
		finally {
			graph.RLock.unlock();
		}
	}

	/* (non-Javadoc)
	 * @see xxl.core.pipes.sources.Source#subscribe(xxl.core.pipes.sinks.Sink, int)
	 */
	@SuppressWarnings("unchecked")
	public boolean subscribe(Sink<? super O> sink, int sinkID) throws SourceIsClosedException {
		if (isClosed)
			throw new SourceIsClosedException("Pipe has already been closed.");
		graph.WLock.lock();
		try {
			if (sinks == null) {
				sinks = new Sink[]{sink};
				sinkIDs = new int[]{sinkID};
			}
			else {
				Sink[] sinksTmp = new Sink[sinks.length+1];
				int[] IDsTmp = new int[sinkIDs.length+1];
				System.arraycopy(sinks, 0, sinksTmp, 0, sinks.length);
				System.arraycopy(sinkIDs, 0, IDsTmp, 0, sinkIDs.length);
				sinksTmp[sinks.length] = sink;
				IDsTmp[sinks.length] = sinkID;
				sinks = sinksTmp;
				sinkIDs = IDsTmp;
			}
			return true;
		}
		finally {
			graph.WLock.unlock();
		}
	}

	/* (non-Javadoc)
	 * @see xxl.core.pipes.sources.Source#unsubscribe(xxl.core.pipes.sinks.Sink, int)
	 */
	@SuppressWarnings("unchecked")
	public boolean unsubscribe(Sink<? super O> sink, int sinkID) throws SourceIsClosedException {
		if (isClosed)
			throw new SourceIsClosedException("Pipe has already been closed.");
		((AbstractSourceMetaDataManagement)metaDataManagement).removeAllSinkDependencies(sinkID);
		graph.WLock.lock();
		try {
			if (sinks != null) {
				for (int i = 0; i < sinks.length; i++)
					if (sinks[i] == sink && sinkIDs[i] == sinkID) {
						Sink[] sinksTmp = new Sink[sinks.length-1];
						int[] IDsTmp = new int[sinkIDs.length-1];
						System.arraycopy(sinks, 0, sinksTmp, 0, i);
						System.arraycopy(sinkIDs, 0, IDsTmp, 0, i);
						System.arraycopy(sinks, i+1, sinksTmp, i, sinks.length-1-i);
						System.arraycopy(sinkIDs, i+1, IDsTmp, i, sinkIDs.length-1-i);
						sinks = sinksTmp;
						sinkIDs = IDsTmp;
						return true;
					}
			}
			return false;
		}
		finally {
			graph.WLock.unlock();
		}
	}

	/* (non-Javadoc)
	 * @see xxl.core.pipes.sources.Source#getSink(int)
	 */
	public Sink<? super O> getSink(int index) throws SourceIsClosedException, IllegalArgumentException, IndexOutOfBoundsException {
		graph.RLock.lock();
		try {
			if (sinks == null)
				throw new IllegalArgumentException("No sinks have been subscribed.");
			return sinks[index];
		}
		finally {
			graph.RLock.unlock();
		}
	}

	/**
	 * Returns the ID of the sink, which has initially been specified when the given sink 
	 * was subscribed to this source. <BR>
	 * A linear pass through the interal sink array is performed to determine
	 * the position of the first matching sink. This position is used to access the
	 * internal ID array, because during a subscription both arrays are updated
	 * at the same position. 
	 *
	 * @param sink The sink whose subscription ID should be detected.
	 * @return The first matching ID of the specified sink.
	 * @throws xxl.core.pipes.sources.SourceIsClosedException If the source has already been closed.
	 * @throws java.lang.IllegalArgumentException If the specified sink is not subscribed.
	 */
	public int getSinkID(Sink<? super O> sink) throws SourceIsClosedException, IllegalArgumentException {
		graph.RLock.lock();
		try {
			if (sinkIDs == null)
				throw new IllegalArgumentException("No sinks have been subscribed and therefore no sinkIDs exist.");
			for (int i = 0; i < sinks.length; i++)
				if (sink == sinks[i])
					return sinkIDs[i];
			throw new IllegalArgumentException("Specified sink is not subscribed.");
		}
		finally {
			graph.RLock.unlock();
		}
	}

	/**
	 * Returns the number of sinks that are currently subscribed to this source. <BR>
	 * The length of the internal sink array is returned.
	 *
	 * @return The number of sinks.
	 * @throws xxl.core.pipes.sources.SourceIsClosedException If the source has already been closed.
	 */
	public int getNoOfSinks() throws SourceIsClosedException {
		graph.RLock.lock();
		try {
			if (sinks == null) return 0;
			return sinks.length;
		}
		finally {
			graph.RLock.unlock();
		}
	}

	/* (non-Javadoc)
	 * @see xxl.core.pipes.sources.Source#isOpened()
	 */
	public boolean isOpened() {
		return isOpened;
	}

	/* (non-Javadoc)
	 * @see xxl.core.pipes.sources.Source#isClosed()
	 */
	public boolean isClosed() {
		return isClosed;
	}
	
	/** 
	 * Returns a reference to the thread simulating the 
	 * activity of this source.
	 *
	 * @return A reference to the thread simulating the activity of this source.
	 * @throws xxl.core.pipes.sources.SourceIsClosedException If the source has already been closed.
	 */
	public SourceProcessor getProcessor() throws SourceIsClosedException {
		return processor;
	}
	
	/**
	 * Signals that this source is done to all subscribed sinks. 
	 */
	public void signalDone() {
		if (isClosed) return;

		graph.RLock.lock();
		try {
			if (isClosed) return;
			if (sinks == null)
				throw new IllegalArgumentException("No sinks have been subscribed.");
			for (int i = 0; i < sinks.length; i++) 
				sinks[i].done(sinkIDs[i]);
		}
		finally {
			graph.RLock.unlock();
		}
	}
		
	/** 
	 * Abstract method that is called by the thread simulating the activity of this source
	 * with the intention to retrieve the next element. If this data source has no 
	 * further elements that can be transferred, the method should throw a new
	 * <CODE>NoSuchElementException</CODE>.
	 *
	 * @return The next element to be emitted by this source.
	 * @throws java.util.NoSuchElementException If this source has no further elements.
	 */
	public abstract O next() throws NoSuchElementException;
		
	/* (non-Javadoc)
	 * @see xxl.core.pipes.queryGraph.AbstractNode#createMetaDataManagement()
	 */
	@Override
	public void createMetaDataManagement() {
		if (metaDataManagement != null)
			throw new IllegalStateException("An instance of MetaDataManagement already exists.");
		metaDataManagement = new AbstractSourceMetaDataManagement();
	}
		
}