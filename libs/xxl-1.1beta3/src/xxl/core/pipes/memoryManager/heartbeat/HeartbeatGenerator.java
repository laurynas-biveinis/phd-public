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

package xxl.core.pipes.memoryManager.heartbeat;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import xxl.core.cursors.AbstractCursor;
import xxl.core.functions.Function;
import xxl.core.pipes.processors.UrgentProcessor;

/**
 * 
 */
public class HeartbeatGenerator extends UrgentProcessor {

	public static long DEFAULT_DELAY = 5000;
	
	public static class HBEntry {
		protected int index;
		protected int useID;
		
		public HBEntry(int index, int useID) {			
			this.index = index;
			this.useID = useID;
		}
		
		public String toString() {
			return "HBEntry index: "+index+" used id: "+useID;
		}
		
		@Override
		public boolean equals(Object o) {
			return ((HBEntry)o).index == index;
		}
	}
	
	protected ArrayList<Heartbeat> list;
	protected List<Long> delays;
	protected List<Long> tempDelays;
	protected List<Function<?, Long>> hbFunctions;
	protected List<HBEntry> nextHB;
	protected List<Integer> useID;

	/**
	 * Creates a new heartbeat generator using the given parameter. All given instances of
	 * Heartbeat use the same delay and id.
	 */
	public HeartbeatGenerator(Heartbeat[] hb, long delay, Function<?, Long>[] hbFunction, int useId) {		
		this();
		for (int i = 0; i < hbFunction.length; i++) {
			addSource(hb[i], delay, hbFunction[i], useId);		
		}
	}

	/**
	 * Creates a new heartbeat generator using the given parameter.
	 * @param hb
	 * @param delay
	 * @param hbFunction
	 * @param useId
	 */
	public HeartbeatGenerator(Heartbeat hb, long delay, Function<?, Long> hbFunction, int useId) {		
		this();
		addSource(hb, delay, hbFunction, useId);		
	}
	
	public HeartbeatGenerator(Heartbeat hb, long delay, Function<?, Long> hbFunction) {
		this(hb, delay, hbFunction, Heartbeat.MEMORY_MANAGER);
	}
	
	public HeartbeatGenerator() {
		super(1);
		list = new ArrayList<Heartbeat>();
		delays = new ArrayList<Long>();
		tempDelays = new ArrayList<Long>();
		hbFunctions = new ArrayList<Function<?, Long>>();
		nextHB = new LinkedList<HBEntry>();
		useID = new LinkedList<Integer>();
		
		this.delayManager = new AbstractCursor<Long>() {
			@Override
			protected boolean hasNextObject() {					
				return true;
			}
			@Override
			protected Long nextObject() {
				synchronized(HeartbeatGenerator.this) {
					if (list.size() > 0) {
						long min = Long.MAX_VALUE;				
						for (long l: tempDelays)
							min = Math.min(min, l);						
						for (int i=0; i < tempDelays.size(); i++) {
							tempDelays.set(i, tempDelays.get(i)-min);
							if (tempDelays.get(i) <= 0) {
								nextHB.add(new HBEntry(i, useID.get(i)));
								tempDelays.set(i, delays.get(i));
							}
						}					
						return min;						
					}
					return DEFAULT_DELAY;
				}
			}
			@Override
			public void remove() {
				throw new UnsupportedOperationException();					
			}				
		};
	}
	
	@Override
	public synchronized void process() {			
		if (list != null && list.size() >0){
			while (! nextHB.isEmpty()) {
				HBEntry entry = nextHB.remove(0);							
				long l = hbFunctions.get(entry.index).invoke();
				Heartbeat hb = list.get(entry.index);
				if (l != Heartbeat.NO_HEARTBEAT)
					try {
						hb.heartbeat(l, entry.useID);
					} catch(NullPointerException e) {
						removeSource(hb);
					}
				else {
					removeSource(hb);					
				}							
			}
		}
		else
			terminate();
	}
	
	public synchronized void addSource(Heartbeat hb, long delay, Function<?, Long> hbFunction) {
		addSource(hb, delay, hbFunction, Heartbeat.MEMORY_MANAGER);
	}
	
	/**
	 * 
	 * @param hb
	 * @param delay
	 * @param hbFunction
	 * @param useID
	 */
	public synchronized void addSource(Heartbeat hb, long delay, Function<?, Long> hbFunction, int useID) {
		if (hb == null)
			throw new IllegalArgumentException("hb must not be null.");
		if (hbFunction == null)
			throw new IllegalArgumentException("hbFunction must not be null.");
		if (delay <= 0)
			throw new IllegalArgumentException("Only positive values for delay allowed.");
		if (delay < 0 && delay != Heartbeat.MEMORY_MANAGER)
			throw new IllegalArgumentException("Only non negative values for delay allowed.");

		list.add(hb);
		delays.add(delay);
		tempDelays.add(delay);
		hbFunctions.add(hbFunction);
		this.useID.add(useID);
	}
	
	/**
	 * @param hb
	 */
	public synchronized void removeSource(Heartbeat hb) {
		int index = list.indexOf(hb);
		if (index >= 0) {
			updateNextHB(index);
			list.remove(index);
			delays.remove(index);
			tempDelays.remove(index);
			hbFunctions.remove(index);
			useID.remove(index);
			if (list.size() == 0)
				terminate();
		}
		else
			throw new IllegalArgumentException(hb+" not found in list "+list);
	}
	
	protected void updateNextHB(int index) {
		for (HBEntry e : nextHB)
			if (e.index > index)
				e.index--;
	}	
}
