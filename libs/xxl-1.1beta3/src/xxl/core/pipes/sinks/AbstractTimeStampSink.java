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
/**
 * 
 */
package xxl.core.pipes.sinks;

import xxl.core.pipes.elements.TimeStampedObject;
import xxl.core.pipes.memoryManager.heartbeat.Heartbeat;
import xxl.core.pipes.operators.Pipes;
import xxl.core.pipes.sources.Source;

/**
 * Superclass for sinks that receive <CODE>TimeStampedObjects</CODE>, i.e., the received elements consist of a value and 
 * a time stamp.
 * 
 * @param <I> type of the value of the elements
 * @param <I2> type of the implementation of TimeStampedObject
 *
 */
public abstract class AbstractTimeStampSink<I, I2 extends TimeStampedObject<I>> extends AbstractSink<I2> implements Heartbeat{

	/**
	 * An array containing the latest timestamp delivered by each source. 
	 */
	protected long[] latestTimeStamps;
	protected long minTimeStamp;
	protected long lastHB;
	protected volatile boolean activateHeartbeats;
	
	/**
	 * Helps to create a new sink that is connected with the given sources.
	 * 
	 * @param sources source to connect the sink with
	 * @param sourceIDs IDs of the sources to connect the sink with
	 */
	public AbstractTimeStampSink(Source<? extends I2>[] sources, int[] sourceIDs) {
		super(sources, sourceIDs);
		this.activateHeartbeats = false;
		this.lastHB = -1;
		this.minTimeStamp = 0;
	}

	/** 
	 * Helps to create a new sink that is connected with the given sources.
	 *
	 * @param sources sources to connect the sink with
	 */
	public AbstractTimeStampSink(Source<? extends I2>[] sources) {
		super(sources);
		this.activateHeartbeats = false;
		this.lastHB = -1;
		this.minTimeStamp = 0;
	}

	/**
	 * Helps to create a new sink that is connected with the given source.
	 * 
	 * @param sources source to connect the sink with
	 * @param sourceID ID of the source to connect the sink with
	 */
	public AbstractTimeStampSink(Source<? extends I2> source, int ID) {
		super(source, ID);
		this.activateHeartbeats = false;
		this.lastHB = -1;
		this.minTimeStamp = 0;
	}

	/**
	 * Helps to create a new sink that is connected with the given source.
	 * 
	 * @param sources source to connect the sink with
	 */
	public AbstractTimeStampSink(Source<? extends I2> source) {
		super(source);
		this.activateHeartbeats = false;
		this.lastHB = -1;
		this.minTimeStamp = 0;
	}

	/**
	 * Helps to create a new sink.
	 */
	public AbstractTimeStampSink() {
		super();
		this.activateHeartbeats = false;
		this.lastHB = -1;
		this.minTimeStamp = 0;
	}

	/* (non-Javadoc)
	 * @see xxl.core.pipes.sinks.AbstractSink#addSource(xxl.core.pipes.sources.Source, int)
	 */
	@Override
	public boolean addSource(Source<? extends I2> source, int sourceID) throws SinkIsDoneException {
		graph.WLock.lock();
		try {
			boolean ret = super.addSource(source, sourceID);	
			if (ret) {
				if (latestTimeStamps == null) {
					latestTimeStamps = new long[]{0};
					return ret;
				}
				long[] newTS = new long[latestTimeStamps.length+1];
				System.arraycopy(latestTimeStamps, 0, newTS, 0, latestTimeStamps.length);
				newTS[latestTimeStamps.length] = 0;
				latestTimeStamps = newTS;
			}
			return ret;
		}
		finally {
			graph.WLock.unlock();
		}
	}

	/* (non-Javadoc)
	 * @see xxl.core.pipes.sinks.AbstractSink#removeSource(xxl.core.pipes.sources.Source, int)
	 */
	@Override
	public boolean removeSource(Source<? extends I2> source, int sourceID) throws SinkIsDoneException {
		graph.WLock.lock();
		try {
			int index = -1;
			for (int i = 0; i < sources.length; i++)
				if (sources[i] == source && sourceIDs[i] == sourceID)
					index = i;
			boolean ret = super.removeSource(source, sourceID);	
			if (ret) {
				long[] newTS = new long[latestTimeStamps.length-1];
				System.arraycopy(latestTimeStamps, 0, newTS, 0, index);						
				if (index < latestTimeStamps.length-1) {
					System.arraycopy(latestTimeStamps, index+1, newTS, index, latestTimeStamps.length-1-index);
				}
				latestTimeStamps = newTS;
			}
			return ret;
		}
		finally {
			graph.WLock.unlock();
		}
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.sinks.AbstractSink#process(java.lang.Object, int)
	 */
	@Override
	public void process(I2 o, int sourceID) throws IllegalArgumentException {		
		processingWLock.lock();
		graph.RLock.lock();
		try {
			long time = o.getTimeStamp();
			int index = Pipes.getSourceIndex(sourceIDs, sourceID);
			latestTimeStamps[index] = Math.max(time, latestTimeStamps[index]);
			updateMinTimeStamp();
			lastHB = Math.max(lastHB, minTimeStamp);
			super.process(o, sourceID);
		}
		finally {
			graph.RLock.unlock();
			processingWLock.unlock();
		}		
	}
	
	/**
	 * Returns an array containing the latest timestamp delivered by each source.
	 * 
	 * @return An array containing the latest timestamp delivered by each source.
	 */
	public long[] getLatestTimeStamps() {
		processingRLock.lock();
		try {
			return latestTimeStamps;
	    }
		finally {
			processingRLock.unlock();
		}
	}

	/* (non-Javadoc)
	 * @see xxl.core.pipes.memoryManager.heartbeat.Heartbeat#heartbeat(long, int)
	 */
	public void heartbeat(long timeStamp, int sourceID) {		
		processingWLock.lock();
		try {
			if(!activateHeartbeats) return; // ignore
			if (sourceID != MEMORY_MANAGER) { // heartbeat from source
				int index = Pipes.getSourceIndex(sourceIDs, sourceID);
				if (latestTimeStamps[index] < timeStamp)				
					latestTimeStamps[index] = timeStamp;
			}
			else { // heartbeat from memory manager
				for (int i = 0; i < latestTimeStamps.length; i++) 
					latestTimeStamps[i] = Math.max(latestTimeStamps[i], timeStamp);
			}	
			updateMinTimeStamp();
			if (lastHB < minTimeStamp) {
				processHeartbeat(minTimeStamp, sourceID);
				lastHB = minTimeStamp;
			}
		}
		finally {
			processingWLock.unlock();	
		}
	}
	
	//	 only called inside synchronized blocks
	/**
	 * Updates minTimeStamp.
	 */
	protected void updateMinTimeStamp() {		
		minTimeStamp = Long.MAX_VALUE;
		for (long l : latestTimeStamps)
			minTimeStamp = Math.min(minTimeStamp, l);		
	}
	
	// returns true if a heartbeat should be generated
	// only called inside heartbeat()
	/**
	 * Returns true.
	 * 
	 * @param minTS
	 * @param sourceID
	 * @return true
	 */
	protected boolean processHeartbeat(long minTS, int sourceID) {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.sinks.AbstractSink#updateDoneStatus(int)
	 */
	@Override
	public void updateDoneStatus(int sourceID) {
		processingWLock.lock();
		graph.RLock.lock();
		try {
			super.updateDoneStatus(sourceID);
			latestTimeStamps[Pipes.getSourceIndex(sourceIDs, sourceID)] = Long.MAX_VALUE;
		}
		finally {
			graph.RLock.unlock();
			processingWLock.unlock();
		}
	}
	
	/**
	 * Activates or deactivates heartbeats for this sink. In order to activate heartbeats
	 * in a query graph top-down or bottom-up, use static methods provided in Heartbeats.
	 * 
	 * @param on true activates heartbeats. 
	 * @see xxl.core.pipes.memoryManager.heartbeat.Heartbeats
	 */
	public void activateHeartbeats(boolean on) {
		processingWLock.lock();
		try {
			this.activateHeartbeats = on;
		}
		finally {
			processingWLock.unlock();
		}
	}
	
	/**
	 * Returns minTimeStamp.
	 * 
	 * @return minTimeStamp
	 */
	public long getMinTimeStamp() {
		processingRLock.lock();
		try {
			return minTimeStamp;
	    }
		finally {
			processingRLock.unlock();
		}
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.pipes.sinks.AbstractSink#closeAllSources()
	 */
	@Override
	public void closeAllSources() {
		super.closeAllSources();
		processingWLock.lock();
		try {
			latestTimeStamps = null;
		}
		finally {
			processingWLock.unlock();
		}
	}
	
}
