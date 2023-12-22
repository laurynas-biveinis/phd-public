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

package xxl.core.pipes.operators;

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
import xxl.core.pipes.memoryManager.MemoryMonitorable;
import xxl.core.pipes.metaData.DefaultMetaDataHandler;
import xxl.core.pipes.metaData.MetaDataDependencies;
import xxl.core.pipes.metaData.PeriodicEvaluationMetaDataHandler;
import xxl.core.pipes.metaData.TriggeredEvaluationMetaDataHandler;
import xxl.core.pipes.operators.PipeMetaDataManagement.ContainsPipeMetaDataDependencies;
import xxl.core.pipes.queryGraph.AbstractNode;
import xxl.core.pipes.queryGraph.Node;
import xxl.core.pipes.sinks.Sink;
import xxl.core.pipes.sinks.SinkIsDoneException;
import xxl.core.pipes.sinks.SinkMetaDataManagement;
import xxl.core.pipes.sources.Source;
import xxl.core.pipes.sources.SourceIsClosedException;
import xxl.core.pipes.sources.SourceMetaDataManagement;
import xxl.core.util.metaData.MetaDataException;
import xxl.core.util.metaData.MetaDataHandler;

/**
 * Superclass for each kind of internal operators in a query graph. Abstract
 * implementation of the interface {@link Pipe}, which
 * extens the interfaces <CODE>Source</CODE> and <CODE>Sink</CODE>. <BR>
 * A pipe consists of two parts: a sink where the elements stream in and
 * get processed and a source, which transfers the results to
 * all subscribed sinks. 
 * <I>Note:</I> The dataflow is aligned from the inital sources of a query graph to
 * its terminal sinks as usual. But the controlflow may vary. Sometimes the calls start from 
 * a terminal sink and end by the initial sources, e.g., in the case of an <CODE>open</CODE> call.
 * But during a <CODE>done</CODE> call the direction is contrary. <BR>
 * Forwarding method calls in both directions within a query graph lead to a very complex
 * architecture that combines the advantages of data-driven and demand-driven processing.
 * The facts that the initial autonomous sources of a query graph use threads simulating their activity,
 * even other pipes start further threads with regard to their processing and concurrently
 * terminal sinks (like graphical user interfaces etc.) may run in own threas requires
 * that synchronisation aspects become very important, because a pipe can be accessed 
 * by multiple sources and/or several sinks. The modifier <CODE>volatile</CODE> is used for
 * some class attributes to avoid cache effects, if various threads read these attributes 
 * calling a non-synchronized method. The modifier <CODE>synchronized</CODE> is often specified for methods to prevent
 * concurrent method calls, which may lead to inconsistent information, if the internal arrays would 
 * simultaneously be changed.
 * 
 * @param <I> type of the incoming objects
 * @param <O> type of the outgoing objects
 */
public abstract class AbstractPipe<I,O> extends AbstractNode implements Pipe<I,O> {

	/**
	 * Flag that signals, if the source part of this pipe has already been opened.
	 */
	protected volatile boolean isOpened;
	
	/**
	 * Flag that signals, if the source part of this pipe has already been closed.
	 */
	protected volatile boolean isClosed;
	
	/**
	 * Flag that signals, if the sink part of this pipe has finished its processing.
	 */
	protected volatile boolean isDone;
	
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
	 * Sources where the sink part of this pipe got subscribed to. The ID this pipe used
	 * by its subscription is stored in the array <CODE>sourceIDs</CODE>
	 * at the same position.
	 */
	protected Source<? extends I>[] sources;
	
	/** 
	 * The source IDs this pipe used during its subscriptions to its sources.
	 */
	protected int[] sourceIDs;
	
	/**
	 * Indicates the isDone status of the connected sources.
	 */
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
	public static class PipeMetaDataDependenciesContainer extends NodeMetaDataDependenciesContainer implements ContainsPipeMetaDataDependencies {
		
		protected Map<Integer,Map<Object,Object[]>> metaDataRequiredFromSources;
		protected Map<Integer,Map<Object,Object[]>> metaDataRequiredFromSinks;
		
		/**
		 * @param className
		 */
		public PipeMetaDataDependenciesContainer(Class<? extends Node> className) {
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
		
		/* (non-Javadoc)
		 * @see xxl.core.pipes.sinks.SinkMetaDataManagement.ContainsSinkMetaDataDependencies#getMetaDataDependenciesOnSource(int)
		 */
		public Map<Object,Object[]> getMetaDataDependenciesOnSource(int sourceIndex) {
			if (metaDataRequiredFromSources == null || !metaDataRequiredFromSources.containsKey(sourceIndex))
				return Collections.emptyMap();
			return metaDataRequiredFromSources.get(sourceIndex);
		}
		
	}

	protected volatile boolean measureOutputRate = false;
	protected volatile boolean measureInputRate = false;

	
	/**
	 * Inner class for metadata management. 
	 */
	public class AbstractPipeMetaDataManagement extends AbstractNodeMetaDataManagement implements PipeMetaDataManagement {
		
		//	AVAILABLE METADATA
		public static final String OUTPUT_RATE = "OUTPUT_RATE"; // elements per ms
		public static final String AVG_OUTPUT_RATE = "AVG_OUTPUT_RATE";	
		public static final String VAR_OUTPUT_RATE = "VAR_OUTPUT_RATE";	
		public static final String NO_OF_SINKS = "NO_OF_SINKS";
		
		public static final String INPUT_RATE = "INPUT_RATE"; // elements per ms
		public static final String AVG_INPUT_RATE = "AVG_INPUT_RATE";	
		public static final String VAR_INPUT_RATE = "VAR_INPUT_RATE";	
		public static final String NO_OF_SOURCES = "NO_OF_SOURCES";
		
		public static final String MEM_USAGE = "MEM_USAGE";
		public static final String AVG_MEM_USAGE = "AVG_MEM_USAGE";
		public static final String VAR_MEM_USAGE = "VAR_MEM_USAGE";
		
		public static final String INPUT_OUTPUT_RATIO = "INPUT_OUTPUT_RATIO";
		public static final String AVG_INPUT_OUTPUT_RATIO = "AVG_INPUT_OUTPUT_RATIO";
		public static final String VAR_INPUT_OUTPUT_RATIO = "VAR_INPUT_OUTPUT_RATIO";
		
		public static final String SYNC_TRIGGER = "SYNC_TRIGGER"; 
						
		/*
		 * ATTRIBUTES REQUIRED TO PROVIDE METADATA
		 */
		protected long outputCounter; // output counter
		
		protected long inputCounter; // input counter
						
		protected long updatePeriod;
		
		/**
		 * Stores for each sourceID (key) a list of metadata identifiers (value) this source depends on.
		 */
		protected HashMap<Integer,List<Object>> sourceDependencies;
		
		/**
		 * Stores for each sinkID (key) a list of metadata identifiers (value) this sinks depends on.
		 */
		protected HashMap<Integer,List<Object>> sinkDependencies;
				
		/**
		 * 
		 */
		public AbstractPipeMetaDataManagement() {
			this.sourceDependencies = null;
			this.sinkDependencies = null;
			this.updatePeriod = 500;
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
			AbstractPipe.this.graph.RLock.lock();
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
				AbstractPipe.this.graph.RLock.unlock();
			}
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
			AbstractPipe.this.graph.RLock.lock();
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
				AbstractPipe.this.graph.RLock.unlock();
			}
		}
		
		/* (non-Javadoc)
		 * @see xxl.core.pipes.sources.SourceMetaDataManagement#notifySinks(java.lang.Object[])
		 */
		public void notifySinks(Object... changedMDIdentifiers) {
			AbstractPipe.this.graph.RLock.lock();
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
				AbstractPipe.this.graph.RLock.unlock();
			}
		}
		
		/* (non-Javadoc)
		 * @see xxl.core.pipes.sources.SourceMetaDataManagement#upstreamNotification(int, java.lang.Object[])
		 */
		public void upstreamNotification(int sinkID, Object... changedMDIdentifiers) {
			AbstractPipe.this.graph.RLock.lock();
			try {
				int sinkIndex = Pipes.getSinkIndex(sinkIDs, sinkID);
				Object[] affectedMD = MetaDataDependencies.affectsUpstreamMetaData((Class<? extends Source>)enclosingClass, sinkIndex, changedMDIdentifiers);
				for (Object md : affectedMD) 
					((MetaDataHandler)metaData.get(md)).refresh();
			}
			finally {
				AbstractPipe.this.graph.RLock.unlock();
			}
		}
		
		/* (non-Javadoc)
		 * @see xxl.core.pipes.sinks.SinkMetaDataManagement#notifySources(java.lang.Object[])
		 */
		public void notifySources(Object... changedMDIdentifiers) {
			AbstractPipe.this.graph.RLock.lock();
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
				AbstractPipe.this.graph.RLock.unlock();
			}
		}
		
		/* (non-Javadoc)
		 * @see xxl.core.pipes.sinks.SinkMetaDataManagement#downstreamNotification(int, java.lang.Object[])
		 */
		public void downstreamNotification(int sourceID, Object... changedMDIdentifiers) {
			AbstractPipe.this.graph.RLock.lock();
			try {
				int sourceIndex = Pipes.getSourceIndex(sourceIDs, sourceID);
				Object[] affectedMD = MetaDataDependencies.affectsDownstreamMetaData((Class<? extends Sink>)enclosingClass, sourceIndex, changedMDIdentifiers);
				for (Object md : affectedMD) 
					((MetaDataHandler)metaData.get(md)).refresh();
			}
			finally {
				AbstractPipe.this.graph.RLock.unlock();
			}
		}
		
		/* (non-Javadoc)
		 * @see xxl.core.pipes.queryGraph.AbstractNode.AbstractNodeMetaDataManagement#globalNotification(java.lang.Object[])
		 */
		@Override
		public void globalNotification(Object... changedMDIdentifiers) {
			localNotification(changedMDIdentifiers);
			notifySources(changedMDIdentifiers);
			notifySinks(changedMDIdentifiers);
		}
				
		/* (non-Javadoc)
		 * @see xxl.core.pipes.sinks.SinkMetaDataManagement#sourceDependency(boolean, int, java.lang.Object[])
		 */
		public boolean sourceDependency(boolean create, int sourceID, Object... metaDataIdentifiers) {
			return manageExternalDependency(create, sourceDependencies, sourceID, metaDataIdentifiers);
		}
				
		/**
		 * @param sourceID
		 * @return
		 */
		protected boolean removeAllSourceDependencies(final int sourceID) {
			return removeAllExternalDependencies(sourceDependencies, sourceID);
		}
		
		/* (non-Javadoc)
		 * @see xxl.core.pipes.sources.SourceMetaDataManagement#sinkDependency(boolean, int, java.lang.Object[])
		 */
		public boolean sinkDependency(boolean create, int sinkID, Object... metaDataIdentifiers) {
			return manageExternalDependency(create, sinkDependencies, sinkID, metaDataIdentifiers);
		}
				
		/**
		 * @param sinkID
		 * @return
		 */
		protected boolean removeAllSinkDependencies(final int sinkID) {
			return removeAllExternalDependencies(sinkDependencies, sinkID);
		}
				
		/* (non-Javadoc)
		 * @see xxl.core.pipes.queryGraph.AbstractNode.AbstractNodeMetaDataManagement#include(java.lang.Object[])
		 */
		@Override
		public boolean include(Object... metaDataIdentifiers) throws MetaDataException {
			boolean ret = manageDependenciesOnSources(true, metaDataIdentifiers);
			ret &= manageDependenciesOnSinks(true, metaDataIdentifiers);
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
			ret &= manageDependenciesOnSinks(false, metaDataIdentifiers);
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
			if (metaDataIdentifier.equals(MEM_USAGE)) {
				metaData.add(metaDataIdentifier, new DefaultMetaDataHandler<Integer>(this, metaDataIdentifier,
					new Function<Object,Integer>() {
						protected MemoryMonitorable mm = ((AbstractPipe.this) instanceof MemoryMonitorable) ? ((MemoryMonitorable)AbstractPipe.this) : null;
						
						@SuppressWarnings("unchecked")
						@Override
						public Integer invoke() {
							synchronized(metaDataManagement) {
								return mm != null ? mm.getCurrentMemUsage() : 0;
							}
						}
					})
				);
				return true;
			}
			if (metaDataIdentifier.equals(AVG_MEM_USAGE)) {
				metaData.add(metaDataIdentifier, PeriodicEvaluationMetaDataHandler.aggregateMetaDataFragment(metaDataManagement, this, MEM_USAGE, AVG_MEM_USAGE, new Average(), Double.NaN, updatePeriod));
				return true;
			}
			if (metaDataIdentifier.equals(VAR_MEM_USAGE)) {
				metaData.add(metaDataIdentifier, PeriodicEvaluationMetaDataHandler.aggregateMetaDataFragment(metaDataManagement, this, MEM_USAGE, VAR_MEM_USAGE, new Variance(), Double.NaN, updatePeriod));
				return true;
			}
			if (metaDataIdentifier.equals(INPUT_OUTPUT_RATIO)) {
				metaData.add(metaDataIdentifier, new TriggeredEvaluationMetaDataHandler<Double>(this, metaDataIdentifier,
					new Function<Object, Double>() {
				
						@SuppressWarnings("unchecked")
						@Override
						public Double invoke() {							
							synchronized(metaDataManagement) {
								double sel = Double.NaN;	
								Double i = ((MetaDataHandler<Double>)metaData.get(INPUT_RATE)).getMetaData();
								Double o = ((MetaDataHandler<Double>)metaData.get(OUTPUT_RATE)).getMetaData();
								if (i != null && o != null) {
									double inputRate = i.doubleValue();
									double outputRate = o.doubleValue();
									if (inputRate != Double.NaN && outputRate != Double.NaN && inputRate > 0 && outputRate > 0) 
										sel = inputRate / outputRate;
								}
								return sel;
							}
						}
					}, 2)
				);
				return true;
			}
			if (metaDataIdentifier.equals(AVG_INPUT_OUTPUT_RATIO)) {
				metaData.add(metaDataIdentifier, TriggeredEvaluationMetaDataHandler.aggregateMetaDataFragment(metaDataManagement, this, INPUT_OUTPUT_RATIO, AVG_INPUT_OUTPUT_RATIO, new Average(), Double.NaN));
				return true;
			}
			if (metaDataIdentifier.equals(VAR_INPUT_OUTPUT_RATIO)) {
				metaData.add(metaDataIdentifier, TriggeredEvaluationMetaDataHandler.aggregateMetaDataFragment(metaDataManagement, this, INPUT_OUTPUT_RATIO, VAR_INPUT_OUTPUT_RATIO, new Variance(), Double.NaN));
				return true;
			}
			if (metaDataIdentifier instanceof String && ((String)metaDataIdentifier).startsWith(SYNC_TRIGGER+"_")) {
//				final String indexString = ((String)metaDataIdentifier).substring(SYNC_TRIGGER.length()+1);
//				try {
//					new Integer(indexString);
//				}
//				catch (NumberFormatException e) {
//					return false;
//				}
				metaData.add(metaDataIdentifier, PeriodicEvaluationMetaDataHandler.trigger(metaDataManagement, this, metaDataIdentifier, updatePeriod));
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
			if (metaDataIdentifier.equals(INPUT_RATE)) {
				this.inputCounter = 0;
				measureInputRate = false;
				((PeriodicEvaluationMetaDataHandler)metaData.get(metaDataIdentifier)).close();
				metaData.remove(metaDataIdentifier);
				return true;
			}
			if (metaDataIdentifier instanceof String && ((String)metaDataIdentifier).startsWith(SYNC_TRIGGER+"_")) {
				metaData.remove(metaDataIdentifier);
				return true;
			}
			if (metaDataIdentifier.equals(AVG_MEM_USAGE) || metaDataIdentifier.equals(VAR_MEM_USAGE) ) {
				((PeriodicEvaluationMetaDataHandler)metaData.get(metaDataIdentifier)).close();
				metaData.remove(metaDataIdentifier);
				return true;
			}
			if (metaDataIdentifier.equals(AVG_OUTPUT_RATE) || 
				metaDataIdentifier.equals(VAR_OUTPUT_RATE) ||
				metaDataIdentifier.equals(AVG_INPUT_RATE) ||
				metaDataIdentifier.equals(VAR_INPUT_RATE) ||
				metaDataIdentifier.equals(MEM_USAGE) ||
				metaDataIdentifier.equals(INPUT_OUTPUT_RATIO) || 
				metaDataIdentifier.equals(AVG_INPUT_OUTPUT_RATIO) || 
				metaDataIdentifier.equals(VAR_INPUT_OUTPUT_RATIO) || 
				metaDataIdentifier.equals(NO_OF_SINKS) ||
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
			AbstractPipe.this.graph.RLock.lock();
			try {
				MetaDataHandler mdh = (MetaDataHandler)getSource(sourceIndex).getMetaData().get(metaDataIdentifier); 
				return mdh.getMetaData(); 
			}
			catch (Exception e) {
				return unknown;
			}
			finally {
				AbstractPipe.this.graph.RLock.unlock();
			}
		}
		
		/* (non-Javadoc)
		 * @see xxl.core.pipes.sources.SourceMetaDataManagement#getMetaDataFragmentFromSink(int, java.lang.Object, java.lang.Object)
		 */
		public Object getMetaDataFragmentFromSink(int sinkIndex, Object metaDataIdentifier, Object unknown) {
			AbstractPipe.this.graph.RLock.lock();
			try {
				MetaDataHandler mdh = (MetaDataHandler)getSink(sinkIndex).getMetaData().get(metaDataIdentifier);
				return mdh.getMetaData();
			}
			catch (Exception e) {
				return unknown;
			}
			finally {
				AbstractPipe.this.graph.RLock.unlock();
			}
		}
		
	}
	
	
	/** 
	 * Helps to create a new pipe as an internal component of a query graph. <BR>
	 * The subscription to the specified sources is fulfilled immediately.
	 *
	 * @param sources This pipe gets subscribed to the specified sources.
	 * @param IDs This pipe uses the given IDs for subscription.
	 * @throws java.lang.IllegalArgumentException If <CODE>sources.length != IDs.length</CODE>.
	 */ 
	public AbstractPipe(Source<? extends I>[] sources, int[] sourceIDs) {
		if (sources.length != sourceIDs.length)
			throw new IllegalArgumentException("sources.length must be equal to IDs.length!");
		for (int i = 0; i < sources.length; i++)
			if (!Pipes.connect(sources[i], this, sourceIDs[i]))
				throw new IllegalArgumentException("Connection with source "+sources[i]+" failed.");
		this.isClosed = false;
		this.isDone = false;
		this.isOpened = false;
	}

	/** 
	 * Helps to create a new pipe as an internal component of a query graph. <BR>
	 * The subscription to the specified sources is fulfilled immediately.
	 *
	 * @param sources This pipe gets subscribed to the specified sources.
	 * @throws java.lang.IllegalArgumentException If <CODE>sources.length != IDs.length</CODE>.
	 */ 
	public AbstractPipe(Source<? extends I>[] sources) {		
		for (int i = 0; i < sources.length; i++)
			if (!Pipes.connect(sources[i], this, i))
				throw new IllegalArgumentException("Connection with source "+sources[i]+" failed.");
		this.isClosed = false;
		this.isDone = false;
		this.isOpened = false;
	}

	/** 
	 * Helps to create a new pipe as an internal component of a query graph. <BR>
	 * The subscription to the specified source is fulfilled immediately.
	 *
	 * @param source This pipe gets subscribed to the specified source.
	 * @param ID This pipe uses the given ID for subscription.
	 */ 
	@SuppressWarnings("unchecked")
	public AbstractPipe(Source<? extends I> source, int sourceID) {
		this(new Source[]{source}, new int[]{sourceID});
	}

	/** 
	 * Helps to create a new pipe as an internal component of a query graph. <BR>
	 * The subscription to the specified source is fulfilled immediately.
	 * The <CODE>DEFAULT_ID</CODE> is used for subscription.
	 *
	 * @param source This pipe gets subscribed to the specified source.
	 */ 
	@SuppressWarnings("unchecked")
	public AbstractPipe(Source<? extends I> source) {
		this(new Source[]{source});
	}

	/** 
	 * No subscriptions will be performed.
	 */ 
	public AbstractPipe() {
		this.isClosed = false;
		this.isDone = false;
		this.isOpened = false;
	}

	static {
		MetaDataDependencies.addLocalMetaDataDependencies(AbstractPipe.class, AbstractPipe.AbstractPipeMetaDataManagement.AVG_INPUT_RATE, AbstractPipe.AbstractPipeMetaDataManagement.INPUT_RATE);
		MetaDataDependencies.addLocalMetaDataDependencies(AbstractPipe.class, AbstractPipe.AbstractPipeMetaDataManagement.VAR_INPUT_RATE, AbstractPipe.AbstractPipeMetaDataManagement.INPUT_RATE);
		MetaDataDependencies.addLocalMetaDataDependencies(AbstractPipe.class, AbstractPipe.AbstractPipeMetaDataManagement.AVG_OUTPUT_RATE, AbstractPipe.AbstractPipeMetaDataManagement.OUTPUT_RATE);
		MetaDataDependencies.addLocalMetaDataDependencies(AbstractPipe.class, AbstractPipe.AbstractPipeMetaDataManagement.VAR_OUTPUT_RATE, AbstractPipe.AbstractPipeMetaDataManagement.OUTPUT_RATE);
		MetaDataDependencies.addLocalMetaDataDependencies(AbstractPipe.class, AbstractPipe.AbstractPipeMetaDataManagement.INPUT_OUTPUT_RATIO, AbstractPipe.AbstractPipeMetaDataManagement.INPUT_RATE, AbstractPipe.AbstractPipeMetaDataManagement.OUTPUT_RATE);
		MetaDataDependencies.addLocalMetaDataDependencies(AbstractPipe.class, AbstractPipe.AbstractPipeMetaDataManagement.AVG_INPUT_OUTPUT_RATIO, AbstractPipe.AbstractPipeMetaDataManagement.INPUT_OUTPUT_RATIO);
		MetaDataDependencies.addLocalMetaDataDependencies(AbstractPipe.class, AbstractPipe.AbstractPipeMetaDataManagement.VAR_INPUT_OUTPUT_RATIO, AbstractPipe.AbstractPipeMetaDataManagement.INPUT_OUTPUT_RATIO);		
		MetaDataDependencies.addLocalMetaDataDependencies(AbstractPipe.class, AbstractPipe.AbstractPipeMetaDataManagement.AVG_MEM_USAGE, AbstractPipe.AbstractPipeMetaDataManagement.MEM_USAGE);
		MetaDataDependencies.addLocalMetaDataDependencies(AbstractPipe.class, AbstractPipe.AbstractPipeMetaDataManagement.VAR_MEM_USAGE, AbstractPipe.AbstractPipeMetaDataManagement.MEM_USAGE);
	}
	
	/**
	 * Opens this pipe, i.e. all underlying sources will be openend
	 * if they have not been opened already. <BR>
	 * Sets the flag <CODE>isOpenend</CODE>.
	 *
	 * @throws xxl.core.pipes.sources.SourceIsClosedException If the pipe has already been closed.
	 */
	public void open() throws SourceIsClosedException {
		if (isClosed)
			throw new SourceIsClosedException("Pipe has already been closed.");
		if (isOpened) return;

		isOpened = true;
		graph.RLock.lock();
		try {		
			if (sources == null) return;
			for (int i = 0; i < sources.length; i++)
				if (!sources[i].isOpened())
					sources[i].open();
		}
		finally {
			graph.RLock.unlock();
		}
	}

	/* (non-Javadoc)
	 * @see xxl.core.pipes.sources.Source#close()
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
			processingWLock.unlock();
			try {
				if (sources != null) {
					for (int i = 0; i < sources.length; i++)
						if (!sources[i].isClosed()) {
							sources[i].unsubscribe(this, sourceIDs[i]);
							sources[i].close();
						}
				}
				sources = null;
				sourceIDs = null;
				sourcesDone = null;
			}
			finally {
				graph.WLock.unlock();
			}
		}
	}

	/* (non-Javadoc)
	 * @see xxl.core.pipes.sinks.Sink#process(java.lang.Object, int)
	 */
	public void process(I o, int sourceID) throws IllegalArgumentException {
		processingWLock.lock();
		graph.RLock.lock();
		try {
			if (isClosed) return;		
			processObject(o, sourceID);
			if (measureInputRate)
				synchronized(metaDataManagement) {
					if (measureInputRate)
						((AbstractPipeMetaDataManagement)metaDataManagement).inputCounter++;
				}
		}
		finally {
			graph.RLock.unlock();
			processingWLock.unlock();
		}
	}
	
	 /**
	  * This method has to be overwritten to process each incoming element.
	  * The <CODE>transfer</CODE> method has to be called inside this method 
	  * to transfer the processing results to all subscribed sinks.
	  * Consequently, its execution is considered to be atomar.
	  * 
	  * @param o The element streaming in.
	  * @param ID One of the IDs this pipe specified during its subscription by the underlying sources. 
	  * @throws java.lang.IllegalArgumentException If the given element or ID is incorrect.
 	  */
	public abstract void processObject(I o, int sourceID) throws IllegalArgumentException;

	/* (non-Javadoc)
	 * @see xxl.core.pipes.sources.Source#transfer(java.lang.Object)
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
							((AbstractPipeMetaDataManagement)metaDataManagement).outputCounter++;
				    }
				}
			}
		}
		finally {
			graph.RLock.unlock();
		}
	}

	/* (non-Javadoc)
	 * @see xxl.core.pipes.sinks.Sink#done(int)
	 */
	public void done(int sourceID) {
		if (isClosed || isDone) return;
		
		processingWLock.lock();
		try {
			updateDoneStatus(sourceID);
			if (isDone) signalDone();
		}
		finally {
			processingWLock.unlock();
		}
			
	}
	
	/**
	 * Updates the status of isDone of the source with the given ID. If all sources are done, the 'global' isDone is set <CODE>true</CODE>.
	 * 
	 * @param sourceID ID of the source
	 */
	public void updateDoneStatus(int sourceID) {
		graph.RLock.lock();
		try {
			if(!isClosed && !isDone && sources != null) {
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
	
	/**
	 * If this pipe is not closed and isDone is <CODE>true</CODE>, done is called on all connected sinks. 
	 */
	public void signalDone() {
		graph.RLock.lock();
		try {
			if (!isClosed && isDone && sinks != null) {
				for (int i = 0; i < sinks.length; i++) {
					sinks[i].done(sinkIDs[i]);
				}
			}
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
		processingRLock.lock();
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
			processingRLock.unlock();
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
	    ((AbstractPipeMetaDataManagement)metaDataManagement).removeAllSinkDependencies(sinkID);
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
	 * got subscribed to this pipe's source part. <BR>
	 * A linear pass through the interal sink array is performed to determine
	 * the position of the first matching sink. This position is used to access the
	 * internal <CODE>sinkID</CODE> array, because during a subscription both arrays are updated
	 * at the same position. 
	 *
	 * @param sink The sink whose subscription ID should be detected.
	 * @return The first matching ID of the specified sink.
	 * @throws xxl.core.pipes.sources.SourceIsClosedException If the pipe has already been closed.
	 * @throws java.lang.IllegalArgumentException If the specified sink is not subscribed.
	 */
	/* (non-Javadoc)
	 * @see xxl.core.pipes.sources.Source#getSinkID(xxl.core.pipes.sinks.Sink)
	 */
	public int getSinkID(Sink<? super O> sink) throws SourceIsClosedException, IllegalArgumentException {
		graph.RLock.lock();
		try {
			if (sinkIDs == null)
				throw new IllegalArgumentException("No sinks have been subscribed and therefore no IDs exist.");
			for (int i = 0; i < sinks.length; i++)
				if (sink == sinks[i])
					return sinkIDs[i];
		}
		finally {
			graph.RLock.unlock();
		}
		throw new IllegalArgumentException("Specified sink is not subscribed.");
	}

	/** 
	 * Returns the first matching ID this pipe used during its subscription by the specified source.
	 * This ID has already been stored, when the <CODE>addSource</CODE> method was invoked. <BR>
	 * A linear pass through the interal source array is performed to determine
	 * the position of the first matching source. This position is used to access the
	 * internal <CODE>sourceID</CODE> array, because during a subscription both arrays are updated
	 * at the same position. 
	 *
	 * @param source The source this pipe got subscribed to.
	 * @return The ID this pipe specified during its subscription.
	 * @throws xxl.core.pipes.sinks.SinkIsDoneException If the pipe has already finished its processing.
	 * @throws java.lang.IllegalArgumentException If the specified source has not been added.
	 */
	public int getSourceID(Source<? extends I> source) throws SinkIsDoneException, IllegalArgumentException {
		graph.RLock.lock();
		try {
			if (sources != null)
				for (int i = 0; i < sources.length; i++)
					if (source == sources[i])
						return sourceIDs[i];
		}
		finally {
			graph.RLock.unlock();
		}
		throw new IllegalArgumentException("Specified source does not exist.");
	}

	/**
	 * Returns the number of sinks that are currently subscribed to this pipe. <BR>
	 * The length of the internal sink array is returned.
	 *
	 * @return The number of sinks.
	 * @throws xxl.core.pipes.sources.SourceIsClosedException If the pipe has already been closed.
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

	/**
	 * Returns a reference to the source belonging to the specified index, i.e.,
	 * the index which was returned for this source when the method <CODE>addSource</CODE>
	 * was called. <BR>
	 * The index corresponds to the source's position in the internal 
	 * stored array of sources, this pipe got subscribed to. 
	 * 
	 * @param index The index used to access sources in the internal array of this pipe.
	 * @return The source belonging to the specified index.
	 * @throws xxl.core.pipes.sinks.SinkIsDoneException If the pipe has already finished its processing.
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
	 * Returns the number of sources that are currently connected with this pipe. <BR>
	 * The length of the internal source array is returned.
	 *
	 * @return The number of sources.
	 * @throws xxl.core.pipes.sinks.SinkIsDoneException If the pipe has already finished its processing.
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
	 * Tests if this pipe is already opened. <BR>
	 * Returns the value of the flag <CODE>isOpened</CODE>.
	 *
	 * @return Returns <CODE>true</CODE>, if the pipe is already opened, otherwise <CODE>false</CODE>.
	 */
	public boolean isOpened() {
		return isOpened;
	}

	/**
	 * Detects if this pipe has already been closed. <BR>
	 * Returns the value of the flag <CODE>isClosed</CODE>.
	 *
	 * @return Returns <CODE>true</CODE>, if the pipe has already been closed, otherwise <CODE>false</CODE>.
	 */
	public boolean isClosed() {
		return isClosed;
	}

	/**
	 * Returns <CODE>true</CODE>, if all connected sources are done, otherwise <CODE>false</CODE>. 
	 *
	 * @return Returns <CODE>true</CODE>, if all connected sources are done, otherwise <CODE>false</CODE>.
	 */
	public boolean isDone() {
		return isDone;
	}

	/**
	 * Similar to the <CODE>subscribe</CODE> method, this method
	 * establishes a connection between this pipe and a source during runtime. The intention of
	 * both methods is to build up a double-linked query graph, which is completely navigatable. <BR>
	 * The method should be used to store a reference to the given underlying source
	 * together with the ID specified during this pipe's subscription. <BR>
	 * The source and the ID used during subscription are stored in internal arrays.
	 * Their positions in both arrays are equal.
	 *
	 * @param source The source this pipe got subscribed to.
	 * @param ID The ID this pipe used during its subscription by the specified source.
	 * @return Returns <CODE>true</CODE>, if the add operation was successful, otherwise <CODE>false</CODE>.
	 * @throws xxl.core.pipes.sinks.SinkIsDoneException If the pipe has already finished its processing.
	 */
	@SuppressWarnings("unchecked")
	public boolean addSource(Source<? extends I> source, int sourceID) throws SinkIsDoneException {
		if (isDone || isClosed)
			throw new SinkIsDoneException("No further elements will be processed.");
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
	 * In conformity to the <CODE>unsubscribe</CODE> method, this
	 * method removes the connection between this pipe and a source on the pipe's side.
	 * Therefore it removes the given source with ID <CODE>ID</CODE> from the
	 * internal arrays used to store this pipe's references to its sources.
	 * 
	 * @param source The source this pipe got subscriped to.
	 * @param ID The ID this pipe used during its subscription by the specified source.
	 * @return Returns <CODE>true</CODE>, if the remove operation was successful, otherwise <CODE>false</CODE>.
	 * @throws xxl.core.pipes.sinks.SinkIsDoneException If the sink has already finished its processing.
	 */
	@SuppressWarnings("unchecked")
	public boolean removeSource(Source<? extends I> source, int sourceID) throws SinkIsDoneException {
		if (isClosed)
			throw new SinkIsDoneException("No further elements will be processed.");
		((AbstractPipeMetaDataManagement)metaDataManagement).removeAllSourceDependencies(sourceID);
		graph.WLock.lock();
		try {
			if (sources != null) 
				for (int i = 0; i < sources.length; i++)
					if (sources[i] == source && sourceIDs[i] == sourceID) {
						Source[] sourcesTmp = new Source[sources.length-1];
						int[] IDsTmp = new int[sourceIDs.length-1];
						boolean[] sourcesDoneTmp = new boolean[sourcesDone.length-1];
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
		
	/* (non-Javadoc)
	 * @see xxl.core.pipes.queryGraph.AbstractNode#createMetaDataManagement()
	 */
	@Override
	public void createMetaDataManagement() {
		if (metaDataManagement != null)
			throw new IllegalStateException("An instance of MetaDataManagement already exists.");
		metaDataManagement = new AbstractPipeMetaDataManagement();
	}
			
}
