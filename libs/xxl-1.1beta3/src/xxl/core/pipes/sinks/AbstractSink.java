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

package xxl.core.pipes.sinks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import xxl.core.pipes.queryGraph.AbstractNode;
import xxl.core.pipes.queryGraph.Node;
import xxl.core.pipes.sinks.SinkMetaDataManagement.ContainsSinkMetaDataDependencies;
import xxl.core.pipes.sources.Source;
import xxl.core.pipes.sources.SourceIsClosedException;
import xxl.core.pipes.sources.SourceMetaDataManagement;
import xxl.core.util.metaData.MetaDataException;
import xxl.core.util.metaData.MetaDataHandler;

/**
 * Superclass for each kind of terminal sinks in a query graph. Abstract
 * implementation of the interface {@link Sink}. 
 * 
 * @since 1.1
 */
public abstract class AbstractSink<I> extends AbstractNode implements Sink<I> {
		
	/**
	 * Flag that signals, if this sink has finished its processing.
	 */
	protected volatile boolean isDone;
	
	/**
	 * Sources where this sink got subscribed to. The ID this sink used
	 * during its subscription is stored in the array <CODE>sourceIDs</CODE>
	 * at the same position.
	 */
	protected Source<? extends I>[] sources;
	
	/** 
	 * The IDs this sink used during its subscriptions to its sources.
	 */
	protected int[] sourceIDs;
	
	protected boolean[] sourcesDone;
	
	/*
	 * PROCESSING LOCK
	 */
	protected final ReentrantReadWriteLock processingRWLock = new ReentrantReadWriteLock();
	protected final ReadLock processingRLock = processingRWLock.readLock();
	protected final WriteLock processingWLock = processingRWLock.writeLock();
	
	/**
	 * Container for the meta data dependencies.
	 *
	 */
	public static class SinkMetaDataDependenciesContainer extends NodeMetaDataDependenciesContainer implements ContainsSinkMetaDataDependencies {

		protected Map<Integer,Map<Object,Object[]>> metaDataRequiredFromSources;
		
		/**
		 * @param className
		 */
		public SinkMetaDataDependenciesContainer(Class<? extends Node> className) {
			super(className);
		}
		
		/* (non-Javadoc)
		 * @see xxl.core.pipes.sinks.SinkMetaDataManagement.ContainsSinkMetaDataDependencies#addMetaDataDependenciesOnSource(int, java.lang.Object, java.lang.Object[])
		 */
		public void addMetaDataDependenciesOnSource(int sourceIndex, Object metaDataIdentifier, Object... dependencies) {
			if (metaDataRequiredFromSources == null)
				metaDataRequiredFromSources = new HashMap<Integer,Map<Object,Object[]>>();
			if (!metaDataRequiredFromSources.containsKey(sourceIndex)) 
				metaDataRequiredFromSources.put(sourceIndex, new HashMap<Object,Object[]>());
			Map<Object,Object[]> sourceRequirements = metaDataRequiredFromSources.get(sourceIndex);
			if (sourceRequirements.containsKey(metaDataIdentifier))
				throw new IllegalArgumentException("Only one dependency declaration allowed per class and source and metaDataIdentifier");				
			sourceRequirements.put(metaDataIdentifier, dependencies);
		}
			
		/* (non-Javadoc)
		 * @see xxl.core.pipes.sinks.SinkMetaDataManagement.ContainsSinkMetaDataDependencies#getMetaDataDependenciesOnSource(int, java.lang.Object)
		 */
		public Object[] getMetaDataDependenciesOnSource(int sourceIndex, Object metaDataIdentifier) {
			Map<Object,Object[]> map = getMetaDataDependenciesOnSource(sourceIndex);
			if (!map.containsKey(metaDataIdentifier))
				return new Object[0];
			return map.get(metaDataIdentifier);
		}
		
		/* (non-Javadoc)
		 * @see xxl.core.pipes.sinks.SinkMetaDataManagement.ContainsSinkMetaDataDependencies#getMetaDataDependenciesOnSource(int)
		 */
		public Map<Object,Object[]> getMetaDataDependenciesOnSource(int sourceIndex) {
			if (metaDataRequiredFromSources == null || !metaDataRequiredFromSources.containsKey(sourceIndex))
				return Collections.emptyMap();
			return metaDataRequiredFromSources.get(sourceIndex);
		}
		
	}
	
	/**
	 * Inner class for metadata management. 
	 */
	public class AbstractSinkMetaDataManagement extends AbstractNodeMetaDataManagement implements SinkMetaDataManagement {
		
		//	AVAILABLE METADATA
		public static final String INPUT_RATE = "INPUT_RATE"; // elements per ms
		public static final String AVG_INPUT_RATE = "AVG_INPUT_RATE";	
		public static final String VAR_INPUT_RATE = "VAR_INPUT_RATE";	
		public static final String NO_OF_SOURCES = "NO_OF_SOURCES";
						
		/*
		 * ATTRIBUTES REQUIRED TO PROVIDE METADATA
		 */
		protected long inputCounter;
		
		protected long updatePeriod;
		protected volatile boolean measureInputRate;
			
		/**
		 * Stores for each sourceID (key) a list of metadata identifiers (value) this source depends on.
		 */
		protected HashMap<Integer,List<Object>> sourceDependencies;
		
		
		/**
		 * 
		 */
		public AbstractSinkMetaDataManagement() {
			this.updatePeriod = 500;
			this.measureInputRate = false;
			this.sourceDependencies = null;
		}
						
		/**
		 * @param millis
		 */
		public synchronized void setUpdatePeriod(long millis) {
			this.updatePeriod = millis;
		}
				
		/* (non-Javadoc)
		 * @see xxl.core.pipes.sinks.SinkMetaDataManagement#getDependenciesOnSource(int, java.lang.Object)
		 */
		@SuppressWarnings("unchecked")
		public synchronized Object[] getDependenciesOnSource(int sourceIndex, Object metaDataIdentifier) {
			return MetaDataDependencies.getMetaDataDependenciesOnSource((Class<? extends Sink>)enclosingClass, sourceIndex, metaDataIdentifier);
		}
		
		/**
		 * @param include
		 * @param metaDataIdentifiers
		 * @return
		 */
		protected boolean manageDependenciesOnSources(boolean include, Object... metaDataIdentifiers) {
			AbstractSink.this.graph.RLock.lock();
			try {
				if (sources == null) return false;
				boolean ret = true;
				Object[] metaDataFromSources;
				SourceMetaDataManagement sourceMDM;
				for (Object metaDataIdentifier : metaDataIdentifiers) {
					for (int i = 0; i < sources.length; i++) {
						metaDataFromSources = getDependenciesOnSource(i, metaDataIdentifier);
						if (metaDataFromSources.length > 0) {
							try {
								sourceMDM = (SourceMetaDataManagement)sources[i].getMetaDataManagement();
								ret &= sourceMDM.sinkDependency(include, sourceIDs[i], metaDataFromSources);
							} catch (Exception e) {
								ret = false;
							}
						}
					}
				}
				return ret;
			}
			finally {
				AbstractSink.this.graph.RLock.unlock();
			}
		}
						
		/* (non-Javadoc)
		 * @see xxl.core.pipes.sinks.SinkMetaDataManagement#notifySources(java.lang.Object[])
		 */
		public void notifySources(Object... changedMDIdentifiers) {
			AbstractSink.this.graph.RLock.lock();
			try {
				if (sourceDependencies != null) 
					for (int i = 0; i < sources.length; i++) {
						if (sourceDependencies.containsKey(sourceIDs[i])) {
							Object[] md = sourceDependencies.get(sourceIDs[i]).toArray();
							List<Object> changedMD = new ArrayList<Object>();
							for (Object c : changedMDIdentifiers) 
								for (Object m : md) 
									if (c.equals(m)) 
										changedMD.add(c);
							if (changedMD.size() > 0) {
								((SourceMetaDataManagement)sources[i].getMetaDataManagement()).upstreamNotification(
									sourceIDs[i], changedMD.toArray()
								);
							}
						}
					}
			}
			finally {
				AbstractSink.this.graph.RLock.unlock();
			}
		}
		
		/* (non-Javadoc)
		 * @see xxl.core.pipes.sinks.SinkMetaDataManagement#downstreamNotification(int, java.lang.Object[])
		 */
		public void downstreamNotification(int sourceID, Object... changedMDIdentifiers) {
			AbstractSink.this.graph.RLock.lock();
			try {
				int sourceIndex = Pipes.getSourceIndex(sourceIDs, sourceID);
				Object[] affectedMD = MetaDataDependencies.affectsDownstreamMetaData((Class<? extends Sink>)enclosingClass, sourceIndex, changedMDIdentifiers);
				for (Object md : affectedMD) 
					((MetaDataHandler)metaData.get(md)).refresh();
			}
			finally {
				AbstractSink.this.graph.RLock.unlock();
			}
		}
		
		/* (non-Javadoc)
		 * @see xxl.core.pipes.queryGraph.AbstractNode.AbstractNodeMetaDataManagement#globalNotification(java.lang.Object[])
		 */
		@Override
		public void globalNotification(Object... changedMDIdentifiers) {
			localNotification(changedMDIdentifiers);
			notifySources(changedMDIdentifiers);
		}
		
		/* (non-Javadoc)
		 * @see xxl.core.pipes.sinks.SinkMetaDataManagement#sourceDependency(boolean, int, java.lang.Object[])
		 */
		public boolean sourceDependency(boolean create, int sourceID, Object... metaDataIdentifiers) {
			return manageExternalDependency(create, sourceDependencies, sourceID, metaDataIdentifiers);
		}
		
		protected boolean removeAllSourceDependencies(final int sourceID) {
			return removeAllExternalDependencies(sourceDependencies, sourceID);
		}
		
		/* (non-Javadoc)
		 * @see xxl.core.pipes.queryGraph.AbstractNode.AbstractNodeMetaDataManagement#include(java.lang.Object[])
		 */
		@Override
		public boolean include(Object... metaDataIdentifiers) throws MetaDataException {
			boolean ret = manageDependenciesOnSources(true, metaDataIdentifiers);
			ret &= super.include(metaDataIdentifiers);
			return ret;
		}
				
		/* (non-Javadoc)
		 * @see xxl.core.pipes.queryGraph.AbstractNode.AbstractNodeMetaDataManagement#exclude(java.lang.Object[])
		 */
		@Override
		public boolean exclude(Object... metaDataIdentifiers) throws MetaDataException {
			boolean ret = super.exclude(metaDataIdentifiers);
			ret &=	manageDependenciesOnSources(false, metaDataIdentifiers);
			return ret;
		}
		
		/* (non-Javadoc)
		 * @see xxl.core.util.AbstractMetaDataManagement#addMetaData(java.lang.Object)
		 */
		@Override
		protected boolean addMetaData(Object metaDataIdentifier) {
			if (metaDataIdentifier.equals(INPUT_RATE)) {
				this.inputCounter = 0;
				measureInputRate = true;
				metaData.add(metaDataIdentifier, new PeriodicEvaluationMetaDataHandler<Double>(this, metaDataIdentifier,
					new Function<Object,Double>() {
						@SuppressWarnings("unchecked")
						@Override
						public Double invoke() {
							synchronized(metaDataManagement) {
								double inputRate = (double)inputCounter/updatePeriod;
								inputCounter = 0;
								return inputRate;
							}
						}
					}, updatePeriod)
				);
				return true;
			}
			if (metaDataIdentifier.equals(AVG_INPUT_RATE)) {
				metaData.add(metaDataIdentifier, TriggeredEvaluationMetaDataHandler.aggregateMetaDataFragment(metaDataManagement, this, INPUT_RATE, AVG_INPUT_RATE, new Average(), Double.NaN));
				return true;
			}
			if (metaDataIdentifier.equals(VAR_INPUT_RATE)) {
				metaData.add(metaDataIdentifier, TriggeredEvaluationMetaDataHandler.aggregateMetaDataFragment(metaDataManagement, this, INPUT_RATE, AVG_INPUT_RATE, new Variance(), Double.NaN));
				return true;
			}
			if (metaDataIdentifier.equals(NO_OF_SOURCES)) {
				metaData.add(metaDataIdentifier, new DefaultMetaDataHandler<Integer>(this, metaDataIdentifier,					
					new Function<Object,Integer>() {
						@Override
						public Integer invoke () {
							synchronized(metaDataManagement) {
								return getNoOfSources();
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
			if (metaDataIdentifier.equals(INPUT_RATE)) {
				this.inputCounter = 0;
				measureInputRate = false;
				((PeriodicEvaluationMetaDataHandler)metaData.get(metaDataIdentifier)).close();
				metaData.remove(metaDataIdentifier);
				return true;
			}
			if (metaDataIdentifier.equals(AVG_INPUT_RATE) ||
				metaDataIdentifier.equals(VAR_INPUT_RATE) ||
				metaDataIdentifier.equals(NO_OF_SOURCES)) {
				metaData.remove(metaDataIdentifier);
				return true;
			}
			return false;
		}
		
		/* (non-Javadoc)
		 * @see xxl.core.pipes.sinks.SinkMetaDataManagement#getMetaDataFragmentFromSource(int, java.lang.Object, java.lang.Object)
		 */
		public Object getMetaDataFragmentFromSource(int sourceIndex, Object metaDataIdentifier, Object unknown) {	
			AbstractSink.this.graph.RLock.lock();
			try {
				return getSource(sourceIndex).getMetaData().get(metaDataIdentifier);
			}
			catch (Exception e) {
				return unknown;
			}
			finally {
				AbstractSink.this.graph.RLock.unlock();
			}
		}
		
	}
	

	/** 
	 * Helps to create a new terminal sink in a query graph.
	 *
	 * @param sources This sink gets subscribed to the specified sources.
	 * @param sourceIDs This sink uses the given IDs for subscription.
	 * @throws java.lang.IllegalArgumentException If <CODE>sources.length != sourceIDs.length</CODE>.
	 */ 
	public AbstractSink(Source<? extends I>[] sources, int[] sourceIDs) {
		if (sources.length != sourceIDs.length)
			throw new IllegalArgumentException("sources.length must be equal to IDs.length!");
		for (int i = 0; i < sources.length; i++)
			if (!Pipes.connect(sources[i], this, sourceIDs[i]))
				throw new IllegalArgumentException("Connection with source "+sources[i]+" failed.");
		this.isDone = false;
	}

	/** 
	 * Helps to create a new terminal sink in a query graph. <BR>
	 * 
	 * @param sources This sink gets subscribed to the specified sources.
	 * @throws java.lang.IllegalArgumentException If <CODE>sources.length != sourceIDs.length</CODE>.
	 */ 
	public AbstractSink(Source<? extends I>[] sources) {
		for (int i = 0; i < sources.length; i++)
			if (!Pipes.connect(sources[i], this, i))
				throw new IllegalArgumentException("Connection with source "+sources[i]+" failed.");		
		this.isDone = false;
	}

	/** 
	 * Helps to create a new terminal sink in a query graph.
	 *
	 * @param source This sink gets subscribed to the specified source.
	 * @param ID This sink uses the given ID for subscription.
	 */ 
	@SuppressWarnings("unchecked")
	public AbstractSink(Source<? extends I> source, int sourceID) {
		this(new Source[]{source}, new int[]{sourceID});
	}

	/** 
	 * Helps to create a new terminal sink in a query graph. <BR>
	 * The <CODE>DEFAULT_ID</CODE> is used for subscription.
	 *
	 * @param source This sink gets subscribed to the specified source.
	 */ 
	public AbstractSink(Source<? extends I> source) {
		this(source, DEFAULT_ID);
	}

	/** 
	 * No subscriptions will be performed.
	 */ 
	public AbstractSink() {
		this.isDone = false;
	}
	
	static {
		MetaDataDependencies.addLocalMetaDataDependencies(AbstractSink.class, AbstractSink.AbstractSinkMetaDataManagement.AVG_INPUT_RATE, AbstractSink.AbstractSinkMetaDataManagement.INPUT_RATE);
		MetaDataDependencies.addLocalMetaDataDependencies(AbstractSink.class, AbstractSink.AbstractSinkMetaDataManagement.VAR_INPUT_RATE, AbstractSink.AbstractSinkMetaDataManagement.INPUT_RATE);
	}

	/**
	 * This method will be called in the <CODE>transfer</CODE> method of each source
	 * this sink got subscribed to. The element to be transfered will be sent to this
	 * sink selecting it as the first parameter. The second parameter specifies
	 * the ID this sink used during its subscription. Therefore the ID determines from
	 * which source this elements is from. <BR>
	 * The element should not be cloned, if it is transfered to multiple sinks, which
	 * have been registered by a source. Due to performance reasons this source should rather 
	 * use the same element and pass it to all subscribed sinks based on the assumption
	 * that these sinks will not perform any changes on this element, unless they clone
	 * it themselves. <BR>
	 * The implementation consumes the element and updates metadata information. Furthermore,
	 * it calls the abstract method <CODE>processObject</CODE> in order to process the
	 * incoming object.
	 *
	 * @param o The element streaming in.
	 * @param sourceID One of the IDs this pipe specified during its subscription by the underlying sources. 
	 * @throws java.lang.IllegalArgumentException If the given element or ID is incorrect.
 	 */
	public void process(I o, int sourceID) throws IllegalArgumentException {
		processingWLock.lock();
		graph.RLock.lock();
		try {
			processObject(o, sourceID);
			if (((AbstractSinkMetaDataManagement)metaDataManagement).measureInputRate)
				synchronized(metaDataManagement) {
					if (((AbstractSinkMetaDataManagement)metaDataManagement).measureInputRate)
						((AbstractSinkMetaDataManagement)metaDataManagement).inputCounter++;
				}
		}
		finally {
			graph.RLock.unlock();
			processingWLock.unlock();
		}
	}
	
	/**
	  * This method has to be overwritten to process each incoming element.
	  * Its execution is considered to be atomar.
	  * 
	  * @param o The element streaming in.
	  * @param ID One of the IDs this pipe specified during its subscription by the underlying sources. 
	  * @throws java.lang.IllegalArgumentException If the given element or ID is incorrect.
	  */
	public abstract void processObject(I o, int sourceID) throws IllegalArgumentException;

	
	/**
	 * The method <CODE>done</CODE> is invoked, if the source with sourceID <CODE>sourceID</CODE> 
	 * has run out of elements. <BR>
	 *
	 * @param sourceID 
	 */
	public void done(int sourceID) {
		if (isDone) return;
		processingWLock.lock();
		try {
			updateDoneStatus(sourceID);
		}
		finally {
			processingWLock.unlock();
		}
	}
	
	/**
	 * Sets the status isDone of the source with the given ID to true. If all sources are done, the 'global' isDone is set to true.
	 * @param sourceID ID of the source
	 */
	protected void updateDoneStatus(int sourceID) {
		graph.RLock.lock();
		try {
			if(!isDone && sources != null) {
				isDone = true;
				for (int i = 0; i < sources.length; i++) {
					if (sourceIDs[i] == sourceID) 
						sourcesDone[i] = true;
					isDone &= sourcesDone[i];
				}
			}
			else return;
		}
		finally {
			graph.RLock.unlock();
		}
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.sinks.Sink#receivedDone(int)
	 */
	public boolean receivedDone(int sourceID) {
		if (isDone) return true;
		graph.RLock.lock();
		try {
			if (sourcesDone != null)
				for (int i = 0; i < sources.length; i++) 
					if (sourceIDs[i] == sourceID) 
						return sourcesDone[i];
			return false;
		}
		finally {
			graph.RLock.unlock();
		}
	}

	/**
	 * Returns a reference to the source belonging to the specified index, i.e.,
	 * the index which was returned for this source when the method <CODE>addSource</CODE>
	 * was called. <BR>
	 * The index corresponds to the source's position in the internal 
	 * stored array of sources this sink got subscribed to. 
	 * 
	 * @param index The index used to access sources in the internal array of this sink.
	 * @return The source belonging to the specified index.
	 * @throws xxl.core.pipes.sinks.SinkIsDoneException If the sink has already finished its processing.
	 * @throws java.lang.IllegalArgumentException If no sources have been added.
	 * @throws java.lang.IndexOutOfBoundsException If the specified index does not exist.
	 */
	public Source<? extends I> getSource(int index) throws SinkIsDoneException, IllegalArgumentException, IndexOutOfBoundsException {
		graph.RLock.lock();
		try {
			if (sources == null)
				throw new IllegalArgumentException("No sources have been added.");
			return sources[index];
		}
		finally {
			graph.RLock.unlock();
		}
	}

	/** 
	 * Returns the first matching ID this sink used during its subscription by the specified source.
	 * This ID has already been stored, when the <CODE>addSource</CODE> method was invoked. <BR>
	 * A linear pass through the interal source array is performed to determine
	 * the position of the first matching source. This position is used to access the
	 * internal ID array, because during a subscription both arrays are updated
	 * at the same position. 
	 *
	 * @param source The source this sink got subscribed to.
	 * @return The ID this sink specified during its subscription.
	 * @throws xxl.core.pipes.sinks.SinkIsDoneException If the sink has already finished its processing.
	 * @throws java.lang.IllegalArgumentException If the specified source has not been added.
	 */
	public int getSourceID(Source<? extends I> source) throws SinkIsDoneException, IllegalArgumentException {
		graph.RLock.lock();
		try {
			if (sources != null)
				for (int i = 0; i < sources.length; i++)
					if (source == sources[i])
						return sourceIDs[i];
			throw new IllegalArgumentException("Specified source does not exist.");
		}
		finally {
			graph.RLock.unlock();
		}
	}

	/**
	 * Returns the number of sources that are currently connected with this sink. <BR>
	 * The length of the internal source array is returned.
	 *
	 * @return The number of sources.
	 * @throws xxl.core.pipes.sinks.SinkIsDoneException If the sink has already finished its processing.
	 */
	public int getNoOfSources() throws SinkIsDoneException {
		graph.RLock.lock();
		try {
			if (sources == null) return 0;
			return sources.length;
		}
		finally {
			graph.RLock.unlock();
		}
	}

	/**
	 * Returns <CODE>true</CODE>, if all subscribed sources are done, otherwise <CODE>false</CODE>.
	 * 
	 * @param Returns <CODE>true</CODE>, if all subscribed sources are done, otherwise <CODE>false</CODE>.
	 */
	public boolean isDone() {
		return isDone;
	}

	/**
	 * Similar to the <CODE>subscribe</CODE> method of a source, this method
	 * establishes a connection between this sink and a source during runtime. The intention of
	 * both methods is to build up a double-linked query graph, which is completely navigatable. <BR>
	 * The method should be used to store a reference to the given underlying source
	 * together with the sourceID specified during this sink's subscription. <BR>
	 *
	 * The source and the ID used during subscription are stored in internal arrays.
	 * Their positions in both arrays are equal.
	 *
	 * @param source The source this sink got subscriped to.
	 * @param sourceID The ID this sinks used during its subscription by the specified source.
	 * @return Returns <CODE>true</CODE>, if the remove operation was successful, otherwise <CODE>false</CODE>.
	 * @throws xxl.core.pipes.sinks.SinkIsDoneException If the sink has already finished its processing.
	 */
	@SuppressWarnings("unchecked")
	public boolean addSource(Source<? extends I> source, int sourceID) throws SinkIsDoneException {
		if (isDone)
			throw new SinkIsDoneException("Sink is done. No further elements will be processed.");
		graph.WLock.lock();
		try {
			if (sources == null) {
				sources = new Source[]{source};
				sourceIDs = new int[] {sourceID};
				sourcesDone = new boolean[1];
				return true;
			}
			Source[] sourcesTmp = new Source[sources.length+1];
			int[] IDsTmp = new int[sourceIDs.length+1];
			boolean[] sourcesDoneTmp = new boolean[sources.length+1];
			System.arraycopy(sources, 0, sourcesTmp, 0, sources.length);
			System.arraycopy(sourceIDs, 0, IDsTmp, 0, sourceIDs.length);
			System.arraycopy(sourcesDone, 0, sourcesDoneTmp, 0, sourcesDone.length);
			sourcesTmp[sources.length] = source;
			IDsTmp[sources.length] = sourceID;
			sourcesDoneTmp[sources.length] = false;
			sources = sourcesTmp;
			sourceIDs = IDsTmp;
			sourcesDone = sourcesDoneTmp;
			return true;
		}
		finally {
			graph.WLock.unlock();
		}
	}

	/**
	 * In conformity to the <CODE>unsubscribe</CODE> method of a source, this
	 * method removes the connection between this sink and a source on the sink's side.
	 * Therefore it removes the given source with sourceID <CODE>sourceID</CODE> from the
	 * internal arrays used to store this sink's references to its sources.
	 * 
	 * @param source The source this sink got subscriped to.
	 * @param sourceID The sourceID this sinks used during its subscription by the specified source.
	 * @return Returns <CODE>true</CODE>, if the remove operation was successful, otherwise <CODE>false</CODE>.
	 * @throws xxl.core.pipes.sinks.SinkIsDoneException If the sink has already finished its processing.
	 */
	@SuppressWarnings("unchecked")
	public boolean removeSource(Source<? extends I> source, int sourceID) throws SinkIsDoneException {
		((AbstractSinkMetaDataManagement)metaDataManagement).removeAllSourceDependencies(sourceID);
		graph.WLock.lock();
		try {
			if (sources != null) 
				for (int i = 0; i < sources.length; i++)
					if (sources[i] == source && sourceIDs[i] == sourceID) {
						Source[] sourcesTmp = new Source[sources.length-1];
						int[] IDsTmp = new int[sourceIDs.length-1];
						boolean[] sourcesDoneTmp = new boolean[sources.length-1];
						System.arraycopy(sources, 0, sourcesTmp, 0, i);
						System.arraycopy(sourceIDs, 0, IDsTmp, 0, i);
						System.arraycopy(sourcesDone, 0, sourcesDoneTmp, 0, i);
						System.arraycopy(sources, i+1, sourcesTmp, i, sources.length-1-i);
						System.arraycopy(sourceIDs, i+1, IDsTmp, i, sourceIDs.length-1-i);
						System.arraycopy(sourcesDone, i+1, sourcesDoneTmp, i, sourcesDone.length-1-i);
						sources = sourcesTmp;
						sourceIDs = IDsTmp;
						sourcesDone = sourcesDoneTmp;
						return true;
					}
			return false;
		}
		finally {
			graph.WLock.unlock();
		}
	}
		
	/**
	 * Opens all connected sources.
	 */
	public void openAllSources() {
		if (isDone)
			throw new SourceIsClosedException("Sink is already done.");
		graph.RLock.lock();
		try {
			if (sources == null)
				throw new IllegalArgumentException("No sources have been added.");
			for (int i = 0; i < sources.length; i++) 
				sources[i].open();
		}
		finally {
			graph.RLock.unlock();
		}
	}
	
	/**
	 * Closes all connected sources.
	 */
	public void closeAllSources() {
		graph.WLock.lock();
		try {
			if (sources == null)
				throw new IllegalArgumentException("No sources have been added.");
			for (int i = 0; i < sources.length; i++) 
				sources[i].close();
		}
		finally {
			graph.WLock.unlock();
		}
	}
			
	/* (non-Javadoc)
	 * @see xxl.core.pipes.queryGraph.AbstractNode#createMetaDataManagement()
	 */
	@Override
	public void createMetaDataManagement() {
		if (metaDataManagement != null)
			throw new IllegalStateException("An instance of MetaDataManagement already exists.");
		metaDataManagement = new AbstractSinkMetaDataManagement();
	}
		
}
