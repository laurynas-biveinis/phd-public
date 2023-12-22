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

package xxl.core.pipes.scheduler;

import java.util.NoSuchElementException;

import xxl.core.pipes.processors.Processor;
import xxl.core.pipes.processors.SchedulableProcessor;
import xxl.core.pipes.processors.UrgentProcessor;

/**
 * A implementation of <code>Scheduler</code> using a <code>Strategy</code>.
 * @since 1.1
 */
public class StrategyScheduler extends Thread implements Scheduler {
  	
  	/**
  	 * The strategy used by this scheduler
  	 */
  	protected Strategy strategy;
  	
  	/**
  	 * The time each Processor will be scheduled for.
  	 */
  	protected long schedulingTime = 50;
  	
  	private final Object WAIT = new Object();
  	private final Object LOCK = new Object();
  	
  	private Processor activeProc = null;
  	
  	/**
  	 * Constructs a new scheduler that uses the given strategy.
  	 *
  	 * @param strategy the Strategy used by the new constructed scheduler.
  	 */
  	public StrategyScheduler(Strategy strategy) {
  		this.strategy = strategy;
  	}
  	
  	/**
  	 * Sets the time that each processor will be scheduled for.
  	 *
  	 * @param time the scheduling time in milliseconds
  	 */
  	public void setSchedulingTime(long time) {
  		schedulingTime = time;
  	}
  	
  	/**
  	 * Returns the time that each processor will be scheduled for.
  	 * @return the time that each processor will be scheduled for.
  	 */
  	public long getSchedulingTime() {
  		return schedulingTime;
  	}
  	
  	/**
	 * Returns the maximum value for priorities supported by this scheduler.
	 * As this scheduler does not use priorites, Integer.MAX_VALUE is returned.
	 *
	 * @return the maximum value for priorities supported by this scheduler
	 */
	public int getMaxSchedulingPriority() {
		return Integer.MAX_VALUE;
	}
	
	/**
	 * Returns the minimum value for priorities supported by this scheduler
	 * As this scheduler does not use priorites, Integer.MIN_VALUE is returned.
	 *
	 * @return the minimum value for priorities supported by this scheduler
	 */
	public int getMinSchedulingPriority(){
		return Integer.MIN_VALUE;
	}
	
	/**
	 * Returns the normal value for priorities supported by this scheduler
	 * As this scheduler does not use priorities, 0 is returned.
	 * @return the normal value for priorities supported by this scheduler
	 */
	public int getNormSchedulingPriority() {
		return 0;
	}
	
	/**
	 * When a SchedulableProcessors <code>setScheduler<code>-Method is called
	 * it will call this method to register itself to this scheduler.
	 * 
	 * @param proc the SchedulableProcessor to be scheduled
	 * @throws IllegalArgumentException if proc does not implement StrategyInput
	 * @see Processor#setScheduler(Scheduler)
	 */
	public void schedule(SchedulableProcessor proc) throws IllegalArgumentException{
		try {
			synchronized(strategy) {
				strategy.register((ControllableStrategyProcessor) proc);
			}
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("Argument has to be an instance of ControllableStrategyProcessor.");
		}
		proc.setPriority(2);
	}
	
	/**
	 * When a UrgentProcessors <code>setScheduler<code>-Method is called
	 * it will call this method to register itself to this scheduler.
	 * 
	 * @param proc the UrgentProcessor to be scheduled
	 * @throws IllegalArgumentException if proc does not implement StrategyInput
	 * @see Processor#setScheduler(Scheduler)
	 */
	public void schedule(UrgentProcessor proc) {
		proc.setPriority(7);
	}


	/**
	 * As the order of processors is determined by the strategy, 
	 * this method only throws an UnsupportedOperationException.
	 *
	 * @param proc the Processor to be scheduled
	 * @param predecessor the predecessing Processor
	 *
	 * @throws UnsupportedOperationException always
	 */
	public void scheduleAfter(SchedulableProcessor proc, SchedulableProcessor predecessor) {
		throw new UnsupportedOperationException("Scheduling order is determined by the strategy.");
	}
	
	/**
	 * Causes this scheduler to stop scheduling a certain Processor.
	 *
	 * @param proc the Processor not to be scheduled anymore
	 * @throws IllegalArgumentException if proc does not implement StrategyInput
	 */
	public void stopScheduling(SchedulableProcessor proc) throws IllegalArgumentException{
		try {
			synchronized(strategy) {
				strategy.deregister((ControllableStrategyProcessor) proc);
			}
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("Argument has to be an instance of ControllableStrategyProcessor.");
		}
		
		synchronized(LOCK) {
  			if (proc == activeProc) synchronized(WAIT) {
  				WAIT.notify();
  			}
  		}
	}

	/**
	 * Causes this scheduler to stop scheduling a certain Processor.
	 *
	 * @param proc the Processor not to be scheduled anymore
	 * @throws IllegalArgumentException if proc does not implement StrategyInput
	 */
	public void stopScheduling(UrgentProcessor proc){
	}

	/**
	 * Called by a Processor when it is started.
	 *
	 * @param proc the started Processor
	 */
	public void reportStart(SchedulableProcessor proc) {
	}
	
	/**
	 * Called by a Processor when it is started.
	 *
	 * @param proc the started Processor
	 */
	public void reportStart(UrgentProcessor proc){
	}

	/**
	 * Called by a Processor when it is paused until wakeup, i.e. when it's 
	 * <code>pauseUntilWakeUp<code>-Methode is called or its <code>pause</code>-Method
	 * is called with <code>0</code> as argument.
	 *
	 * @param proc the blocked Processor
	 * @see Processor#pauseUntilWakeUp()
	 * @see Processor#pause(long)
	 */
	public void reportBlocked(SchedulableProcessor proc){
	}

	/**
	 * Called by a Processor when it is paused until wakeup, i.e. when it's 
	 * <code>pauseUntilWakeUp<code>-Methode is called or its <code>pause</code>-Method
	 * is called with <code>0</code> as argument.
	 *
	 * @param proc the blocked Processor
	 * @see Processor#pauseUntilWakeUp()
	 * @see Processor#pause(long)
	 */
	public void reportBlocked(UrgentProcessor proc){
	}

	/**
	 * Called by a Processor when it's <code>pause</code>-Method is called 
	 * with a positive argument.
	 *
	 * @param proc the blocked Processor
	 * @param time
	 * @see Processor#pause(long)
	 */
	public void reportBlocked(SchedulableProcessor proc, long time) {
	}
	
	/**
	 * Called by a Processor when it's <code>pause</code>-Method is called 
	 * with a positive argument.
	 *
	 * @param proc the blocked Processor
	 * @param time
	 * @see Processor#pause(long)
	 */
	public void reportBlocked(UrgentProcessor proc, long time) {
	}

	/**
	 * Called by a Processor when it's woken up by calling its
	 * <code>wakeUp</code>-Method after it had been put to sleep.
	 *
	 * @param proc the now unblocked Processor
	 * @see Processor#wakeUp()
	 */
	public void reportUnblocked(SchedulableProcessor proc) {
	}

	/**
	 * Called by a Processor when it's woken up by calling its
	 * <code>wakeUp</code>-Method after it had been put to sleep.
	 *
	 * @param proc the now unblocked Processor
	 * @see Processor#wakeUp()
	 */
	public void reportUnblocked(UrgentProcessor proc) {
	}

	/**
	 * Called by a Processor when its <code>setSchedulingPriority</code>-Method
	 * has been called.
	 *
	 * @param proc the processor
	 * @see Processor#setSchedulingPriority(int)
	 */
	public void reportPriorityChange(SchedulableProcessor proc) {
	}
	
	/**
	 * Called by a Processor when its <code>setSchedulingPriority</code>-Method
	 * has been called.
	 *
	 * @param proc the processor
	 * @see Processor#setSchedulingPriority(int)
	 */
	public void reportPriorityChange(UrgentProcessor proc) {
	}

	/**
	 * Called by a Processor directly before it terminates.
	 *
	 * @param proc the terminating Processor
	 */
	public void reportTermination(SchedulableProcessor proc) {
		try {
			synchronized(strategy) {
				strategy.deregister((ControllableStrategyProcessor) proc);
			}
		} catch (ClassCastException e) {
			//Falls proc kein ControllableProcessor ist, interessiert er sowieso nicht.
		}
	}
	
	/**
	 * Called by a Processor directly before it terminates.
	 *
	 * @param proc the terminating Processor
	 */
	public void reportTermination(UrgentProcessor proc) {	
	}
	
	/**
	 * In this method, all the scheduling work is done. 
   	 */
  	@Override
	public void run() {
  		while (true) {
  			Processor proc = null;
  			synchronized(strategy) {
  				try {
					proc = (Processor)strategy.next();
				} catch (NoSuchElementException e) {
					e.printStackTrace(System.err);
				}
  			}
  			if (proc != null) {
  				synchronized(LOCK) {
  					if (activeProc != null) activeProc.setPriority(2);
  					activeProc = proc;
  					activeProc.setPriority(4);
  				}
  			}
  			try {
  				synchronized(WAIT) {
  					WAIT.wait(schedulingTime);
  				}
  			} catch (InterruptedException e) {}
  		}
  	}
}