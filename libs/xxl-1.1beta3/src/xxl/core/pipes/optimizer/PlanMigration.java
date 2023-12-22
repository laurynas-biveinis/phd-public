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

	http://www.mathematik.uni-marburg.de/DBS/xxl

bugs, requests for enhancements: xxl@mathematik.uni-marburg.de

If you want to be informed on new versions of XXL you can 
subscribe to our mailing-list. Send an email to 
	
	xxl-request@lists.uni-marburg.de

without subject and the word "subscribe" in the message body. 
*/
package xxl.core.pipes.optimizer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import xxl.core.cursors.mappers.Mapper;
import xxl.core.cursors.sources.DiscreteRandomNumber;
import xxl.core.cursors.sources.Repeater;
import xxl.core.functions.Function;
import xxl.core.pipes.elements.TemporalObject;
import xxl.core.pipes.elements.TimeInterval;
import xxl.core.pipes.operators.AbstractTemporalPipe;
import xxl.core.pipes.operators.Pipe;
import xxl.core.pipes.operators.Pipes;
import xxl.core.pipes.operators.filters.TemporalFilter;
import xxl.core.pipes.operators.joins.Joins;
import xxl.core.pipes.operators.joins.TemporalJoin;
import xxl.core.pipes.operators.windows.TemporalWindow;
import xxl.core.pipes.queryGraph.Graph;
import xxl.core.pipes.sinks.Printer;
import xxl.core.pipes.sinks.Sink;
import xxl.core.pipes.sources.Source;
import xxl.core.pipes.sources.SourceIsClosedException;
import xxl.core.pipes.sources.TimeStampCursorSource;
import xxl.core.predicates.Predicates;

public class PlanMigration<I,O> {
	
	public class ReferencePointSplit<E> extends AbstractTemporalPipe<E,E> {

		public class ReferencePointSplitMetaDataManagement extends AbstractPipeMetaDataManagement {			
			protected boolean measureOutputRate() {
				return measureOutputRate;
			}
			protected void incrementOutputCounter() {
				outputCounter++;
			}
		}
		
		@Override
		public void createMetaDataManagement() {
			if (metaDataManagement != null)
				throw new IllegalStateException("An instance of MetaDataManagement already exists.");
			metaDataManagement = new ReferencePointSplitMetaDataManagement();
		}

			
		protected long optPointInTime; // in application time
		protected boolean allSplit = false;
		protected Source<? extends TemporalObject<E>> source;
		protected Pipe<? super TemporalObject<E>, ?> oldPlan;
		protected Pipe<? super TemporalObject<E>, ?> newPlan;
		protected int original_ID;
		protected int newPlan_ID;
		protected boolean fixSubscriptions;
		
		public ReferencePointSplit(Source<? extends TemporalObject<E>> source, Pipe<? super TemporalObject<E>, ?> oldPlan, Pipe<? super TemporalObject<E>, ?> newPlan, long optPointInTime) {
			this.source = source;
			this.oldPlan = oldPlan;
			this.newPlan = newPlan;
			this.optPointInTime = optPointInTime;
			this.fixSubscriptions = false;
			
			// disconnect source and old plan
			original_ID = source.getSinkID(oldPlan);
			do {
				newPlan_ID = getID();
			}
			while (original_ID == newPlan_ID);
			
			
			if (!Pipes.disconnect(source, oldPlan, original_ID)) // disconnect source and oldPlanSource
				throw new IllegalArgumentException("Problems occured while trying to disconnect the source and the old query plan."); 
			
			// connect split with source
			if (!Pipes.connect(source, this, original_ID)) // connect with source
				throw new IllegalArgumentException("Problems occured while establishing the connections between the nodes."); 
		
			// subscribe old plan
			if (!Pipes.connect(this, oldPlan, original_ID)) // connect with old plan
				throw new IllegalArgumentException("Problems occured while establishing the connections between the nodes.");
			
			// subscribe new plan
			if (!Pipes.connect(this, newPlan, newPlan_ID)) // connect with new plan
				throw new IllegalArgumentException("Problems occured while establishing the connections between the nodes."); 
						
			// opening new plan and this node
			newPlan.open();
			this.open();

			// don't consider other subscriptions and unsubscription calls
			fixSubscriptions=true;
			activeSplits++;
			splits.add(this);
		}
		
		@Override
		public void transfer(TemporalObject<E> tso) {				
			graph.RLock.lock();
			try {
				long start = tso.getStart();
				long end = tso.getEnd();
				E obj = tso.getObject();
				
				if (start >= optPointInTime) {
					allSplit = true;
					sinks[0].done(sinkIDs[0]); // signal end of stream
				}
				
				if (start < optPointInTime && !sinks[0].isDone()) // transfer to old plan
					sinks[0].process(tso, sinkIDs[0]);
				if (end > optPointInTime && !sinks[1].isDone()) // transfer to new plan
					sinks[1].process(new TemporalObject<E>(obj, new TimeInterval(Math.max(start, optPointInTime-1), end)), sinkIDs[1]);				
				
				ReferencePointSplitMetaDataManagement rpsmdm = (ReferencePointSplitMetaDataManagement)metaDataManagement; 				
				if (rpsmdm.measureOutputRate()) {
					synchronized(metaDataManagement) {
						rpsmdm.incrementOutputCounter();
				    }
				}							
			}
			finally {
				graph.RLock.unlock();
			}
			
			if (allSplit) { 						
				planMigrationSplitDone();
			}
		}
		
		protected void remove() {
			fixSubscriptions=false;
			
			// unsubscribe new plan
			if (!Pipes.disconnect(ReferencePointSplit.this, newPlan, newPlan_ID)) // disconnect new plan
				throw new IllegalArgumentException("Problems occured while establishing the connections between the nodes."); 
			
			// unsubscribe old plan
//			if (!Pipes.disconnect(ReferencePointSplit.this, oldPlan, original_ID)) // disconnect old plan
//				throw new IllegalArgumentException("Problems occured while establishing the connections between the nodes.");
			
			// disconnect split with source
			if (!Pipes.disconnect(source, ReferencePointSplit.this, original_ID)) // disconnect source
				throw new IllegalArgumentException("Problems occured while establishing the connections between the nodes."); 

			// connect source and new plan
			if (!Pipes.connect(source, newPlan, original_ID)) // connect source and newPlanSource
				throw new IllegalArgumentException("Problems occured while trying to disconnect the source and the old query plan.");			
		}
		
		@Override
		public void processObject(TemporalObject<E> tso, int sourceID) {
			transfer(tso);
		}
				
		@Override
		public boolean subscribe(Sink<? super TemporalObject<E>> sink, int sinkID) throws SourceIsClosedException {
			graph.WLock.lock();
			try {
				if (fixSubscriptions)
					return false;
				return super.subscribe(sink, sinkID);
			}
			finally {
				graph.WLock.unlock();
			}
		}
		
		@Override
		public boolean unsubscribe(Sink<? super  TemporalObject<E>> sink, int sinkID) throws SourceIsClosedException {
			graph.WLock.lock();
			try {
				if (fixSubscriptions)
					return false;
				return super.unsubscribe(sink, sinkID);
			}
			finally {
				graph.WLock.unlock();
			}
		}		
	}
	
	public class ReferencePointUnion<E> extends AbstractTemporalPipe<E,E> {

		protected Source<? extends TemporalObject<E>> oldPlan;
		protected Source<? extends TemporalObject<E>> newPlan;
		protected Sink<? super TemporalObject<E>> sink;
		protected int original_ID;
		protected int oldPlan_ID;
		protected int newPlan_ID;
		protected long optPointInTime;
		protected boolean finishedOldPlan;
		protected ArrayList <TemporalObject<E>>buffer;
			
		public ReferencePointUnion(Source<? extends TemporalObject<E>> oldPlan, Source<? extends TemporalObject<E>> newPlan, Sink<? super TemporalObject<E>> sink, long optPointInTime) {
			this.oldPlan = oldPlan;
			this.newPlan = newPlan;
			this.sink = sink;
			this.finishedOldPlan = false;
			this.optPointInTime = optPointInTime;
								
			// disconnect sink and old plan
			original_ID = oldPlan.getSinkID(sink);
			
			
			if (!Pipes.disconnect(oldPlan, sink, original_ID)) // disconnect oldPlan and sink
				throw new IllegalArgumentException("Problems occured while trying to disconnect the sink and the old query plan."); 
			
			// connect old plan
			if (!Pipes.connect(oldPlan, this, 0)) // connect with old plan
				throw new IllegalArgumentException("Problems occured while establishing the connections between the nodes."); 
		
			// connect new plan
			if (!Pipes.connect(newPlan, this, 1)) // connect with new plan
				throw new IllegalArgumentException("Problems occured while establishing the connections between the nodes."); 
		
			// subscribe sink
			if (!Pipes.connect(this, sink, original_ID)) // connect with this
				throw new IllegalArgumentException("Problems occured while establishing the connections between the nodes.");
			
			buffer = new ArrayList<TemporalObject<E>>();
			activeUnions++;
			unions.add(this);
		}
		
		@Override
		public void processObject(TemporalObject<E> o, int sourceID) {
			if (sourceID == 0) { // from old plan
				transfer(o);
			}
			else { // from new plan
				if (o.getStart() == optPointInTime-1)
					return;
				if (finishedOldPlan) {
					transfer(o);
				}
				else {
					buffer.add(o);
				}
			}
		}
			
		@Override
		public void done(int sourceID) {
			if (isClosed || isDone) return;
			
			processingWLock.lock();			
		    try {
		    	if (isClosed || isDone) return;
		    	updateDoneStatus(sourceID);
				if (sourceID == 0) {
					Iterator <TemporalObject<E>> it = buffer.iterator();
					while (it.hasNext()) 
						transfer(it.next());
					buffer.clear();													
					finishedOldPlan=true;
					planMigrationUnionDone();
				}
				if (isDone()) {
					signalDone();
				}
			}
		    finally {
		    	processingWLock.unlock();
		    }
		}
		
		protected void remove() {
			// unsubscribe sink
			if (!Pipes.disconnect(ReferencePointUnion.this, sink, original_ID)) // disconnect with this
				throw new IllegalArgumentException("Problems occured while establishing the connections between the nodes.");
			
			// disconnect new plan
			if (!Pipes.disconnect(newPlan, ReferencePointUnion.this, 1)) // disconnect with new plan
				throw new IllegalArgumentException("Problems occured while establishing the connections between the nodes."); 
									
			// disconnect old plan
//			if (!Pipes.disconnect(oldPlan, ReferencePointUnion.this, 0)) // disconnect with old plan
//				throw new IllegalArgumentException("Problems occured while establishing the connections between the nodes.");
			
			// connect sink and new plan
			if (!Pipes.connect(newPlan, sink, original_ID)) // connect newPlan and sink
				throw new IllegalArgumentException("Problems occured while trying to disconnect the sink and the old query plan.");
			
			// close();								
		}
	}
	
	protected class PlanMigrationCloser extends Thread {
		public void run () {
			planGraph.WLock.lock();
			try {
				for(ReferencePointSplit rps : splits) 
					rps.remove();
				// close unions which close old plans
				for(ReferencePointUnion rpu : unions) 
					rpu.remove();
				for(ReferencePointUnion rpu : unions) 
					rpu.close();
			}
			finally {
				planGraph.WLock.unlock();
			}
			callback.invoke();
		}
	}
	
//	static void migrate(
//			List<? extends Source<? extends TemporalObject<?>>> sources,
//			List<Integer> sources_IDs,
//			List<? extends Pipe<? super TemporalObject<?>, ? extends TemporalObject<?>>> toOldPlan,
//			List<Integer> toOldPlan_IDs,
//			List<? extends Pipe<? super TemporalObject<?>, ? extends TemporalObject<?>>> toNewPlan,
//			List<Integer> toNewPlan_IDs,
//			List<? extends Sink<? super TemporalObject<?>>> sinks,
//			List<Integer> sinks_IDs,
//			List<? extends Source<? extends TemporalObject<?>>> fromOldPlan,
//			List<Integer> fromOldPlan_IDs,
//			List<? extends Source<? extends TemporalObject<?>>> fromNewPlan,
//			List<Integer> fromNewPlan_IDs,
//			long optPointInTime) {

	public PlanMigration (
			Graph planGraph,
			List<? extends Source<? extends TemporalObject<I>>> sources,
			List<? extends Pipe<? super TemporalObject<I>, ?>> toOldPlan,
			List<? extends Pipe<? super TemporalObject<I>, ?>> toNewPlan,
			List<? extends Sink<? super TemporalObject<O>>> sinks,
			List<? extends Source<? extends TemporalObject<O>>> fromOldPlan,
			List<? extends Source<? extends TemporalObject<O>>> fromNewPlan,
			long optPointInTime,
			Function callback) {	
		if (sources.size() != toOldPlan.size() ||
			sources.size() != toNewPlan.size() ||
			sinks.size() != fromOldPlan.size() ||
			sinks.size() != fromNewPlan.size())
				throw new IllegalArgumentException("Inconsistent number of arguments for plan migration");
		
		this.planGraph = planGraph;
		this.callback = callback;
		this.nextID = 0;
		splits = new ArrayList<ReferencePointSplit>();
		unions = new ArrayList<ReferencePointUnion>();
		planGraph.WLock.lock();		
		try {
			for (int i=0; i<sources.size(); i++) {
				new ReferencePointSplit<I>(sources.get(i), toOldPlan.get(i), toNewPlan.get(i), optPointInTime);
			}
			for (int i=0; i<sinks.size(); i++) {
				new ReferencePointUnion<O>(fromOldPlan.get(i), fromNewPlan.get(i), sinks.get(i), optPointInTime);
			}
		}
		finally {
			planGraph.WLock.unlock();
		}
	}
	
	protected Graph planGraph;
	protected int activeSplits;
	protected int activeUnions;
	protected int nextID;
	protected final Function callback;
	protected PlanMigrationCloser closer;
	protected ArrayList<ReferencePointSplit> splits;
	protected ArrayList<ReferencePointUnion> unions;
	
	private synchronized void planMigrationSplitDone() {
		activeSplits--;
		checkForCompletion();
	}
	
	private synchronized void planMigrationUnionDone() {
		activeUnions--;
		checkForCompletion();
	}
	
	private synchronized void checkForCompletion() {
		if (activeSplits==0 && activeUnions==0) {
			closer = new PlanMigrationCloser();
			closer.start();
		}
	}
	
	private int getID() {
		return nextID++;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final long windowsize = 2000;
		final int d = 100;
		Function<Integer, Integer> projection = new Function<Integer,Integer>() {
			public Integer invoke(Integer i1, Integer i2) {
				return i1;
			}			
		};
		final Function callback = new Function() {
			public Object invoke() {
				System.out.println("\nPlan migration finished!\n");
				return null;
			}
		};
		ArrayList<Source<TemporalObject<Integer>>> someSources = new ArrayList<Source<TemporalObject<Integer>>>();
		for (int i=0; i<4; i++)
			someSources.add(new TemporalWindow<Integer>(
					new TimeStampCursorSource<Integer>(d, new Mapper<Integer,Integer>(new Function<Integer,Integer>() {
						public Integer invoke(Integer i) {
							return i%41;
						}
					} ,new DiscreteRandomNumber(5*windowsize/d)), new Repeater<Integer>(d)),
					windowsize));

		
		final ArrayList<Source<TemporalObject<Integer>>> sources = new ArrayList<Source<TemporalObject<Integer>>>();
		final ArrayList<Sink<TemporalObject<Integer>>> sinks = new ArrayList<Sink<TemporalObject<Integer>>>();
		final ArrayList<Pipe<TemporalObject<Integer>,TemporalObject<Integer>>> toOldPlan = new ArrayList<Pipe<TemporalObject<Integer>,TemporalObject<Integer>>>();
		final ArrayList<Source<TemporalObject<Integer>>> fromOldPlan = new ArrayList<Source<TemporalObject<Integer>>>();
		final ArrayList<Pipe<TemporalObject<Integer>,TemporalObject<Integer>>> toNewPlan = new ArrayList<Pipe<TemporalObject<Integer>,TemporalObject<Integer>>>();
		final ArrayList<Source<TemporalObject<Integer>>> fromNewPlan = new ArrayList<Source<TemporalObject<Integer>>>();
		
		if (args.length==1 && args[0].equals("filter")) {
	
			// the source 
			Source<TemporalObject<Integer>> source = someSources.get(0);
			sources.add(source);
	
			// the sink
			Sink <TemporalObject<Integer>> sink = new Printer<TemporalObject<Integer>>(System.out, true);
			sinks.add(sink);
			
			// the old box		
			TemporalFilter<Integer> oldFilter1 = new TemporalFilter<Integer>(TemporalObject.<Integer>temporalPredicate(Predicates.TRUE));		
			TemporalFilter<Integer> oldFilter2 = new TemporalFilter<Integer>(TemporalObject.<Integer>temporalPredicate(Predicates.EVEN));
			Pipes.connect(oldFilter1, oldFilter2, 0);
			toOldPlan.add(oldFilter1);
			fromOldPlan.add(oldFilter2);
	
			// the new box
			TemporalFilter<Integer> newFilter2 = new TemporalFilter<Integer>(TemporalObject.<Integer>temporalPredicate(Predicates.TRUE));
			TemporalFilter<Integer> newFilter1 = new TemporalFilter<Integer>(TemporalObject.<Integer>temporalPredicate(Predicates.EVEN));
			Pipes.connect(newFilter1, newFilter2, 0);
			toNewPlan.add(newFilter1);
			fromNewPlan.add(newFilter2);
			
			// connect old box to build old plan
			Pipes.connect(source, oldFilter1, 0);
			Pipes.connect(oldFilter2, sink, 0);		
		}
		else if (args.length==1 && args[0].equals("join")) {

			// sources
			Source <TemporalObject<Integer>> source1 = someSources.get(0);
			Source <TemporalObject<Integer>> source2 = someSources.get(1);
			sources.add(source1);
			sources.add(source2);
						
			// the sink
			Sink <TemporalObject<Integer>> sink = new Printer<TemporalObject<Integer>>(System.out, true);
			sinks.add(sink);
	
			// the old box
			TemporalJoin<Integer, Integer> oldJoin = Joins.UnconnectedTemporalEquiSNJ(projection);
			TemporalFilter<Integer> oldFilter = new TemporalFilter<Integer>(TemporalObject.<Integer>temporalPredicate(Predicates.EVEN));
			Pipes.connect(oldJoin, oldFilter, 0);
			toOldPlan.add(oldJoin);
			toOldPlan.add(oldJoin);
			fromOldPlan.add(oldFilter);
	
			// the new box
			TemporalFilter<Integer> newFilter1 = new TemporalFilter<Integer>(TemporalObject.<Integer>temporalPredicate(Predicates.EVEN));
			TemporalFilter<Integer> newFilter2 = new TemporalFilter<Integer>(TemporalObject.<Integer>temporalPredicate(Predicates.EVEN));
			TemporalJoin<Integer, Integer> newJoin = Joins.UnconnectedTemporalEquiSNJ(projection);
			Pipes.connect(newFilter1, newJoin, 0);
			Pipes.connect(newFilter2, newJoin, 1);
			toNewPlan.add(newFilter1);
			toNewPlan.add(newFilter2);
			fromNewPlan.add(newJoin);
			
			// connect old box to build old plan
			Pipes.connect(source1, oldJoin, 0);
			Pipes.connect(source2, oldJoin, 1);
			Pipes.connect(oldFilter, sink, 0);						
		}
		else {
			System.out.println("usage: PlanMigration [filter|join]");
			return;			
		}
		new Thread() {
			public void run() {
				try {
					Thread.sleep(2*windowsize);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("\nPlan migration starts!\n");
				new PlanMigration<Integer,Integer>(Graph.DEFAULT_INSTANCE, sources, toOldPlan, toNewPlan, sinks, fromOldPlan, fromNewPlan, (long)(3*windowsize), callback);
			}
		}.start();
		
		for (Source source : sources)
			source.open();
	}

}
