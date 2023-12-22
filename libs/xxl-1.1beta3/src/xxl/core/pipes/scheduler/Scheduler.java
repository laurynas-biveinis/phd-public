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

package xxl.core.pipes.scheduler;

import xxl.core.pipes.processors.SchedulableProcessor;
import xxl.core.pipes.processors.UrgentProcessor;

/**
 * A Scheduler for Processors. <code>SchedulableProcessor</code>s and 
 * <code>UrgentProcessor</code>s can be treated separately.
 *
 * @since 1.1
 */
public interface Scheduler {
	
	/**
	 * Returns the maximum value for priorities supported by this scheduler.
	 * @return the maximum value for priorities supported by this scheduler
	 */
	public int getMaxSchedulingPriority();
	
	/**
	 * Returns the minimum value for priorities supported by this scheduler.
	 * @return the minimum value for priorities supported by this scheduler
	 */
	public int getMinSchedulingPriority();
	
	/**
	 * Returns the normal value for priorities supported by this scheduler.
	 * @return the normal value for priorities supported by this scheduler
	 */
	public int getNormSchedulingPriority();
	
	/**
	 * When a SchedulableProcessors <code>setScheduler<code>-Method is called,
	 * it will call this method to register itself to this scheduler.
	 * 
	 * @param proc the Processor to be scheduled
	 * @see xxl.core.pipes.processors.Processor#setScheduler(Scheduler)
	 */
	public void schedule(SchedulableProcessor proc);
	
	/**
	 * When a UrgentProcessors <code>setScheduler<code>-Method is called
	 * it will call this method to register itself to this scheduler.
	 * 
	 * @param proc the Processor to be scheduled
	 * @see xxl.core.pipes.processors.Processor#setScheduler(Scheduler)
	 */
	public void schedule(UrgentProcessor proc);

	/**
	 * Causes this scheduler to schedule a Processor right after 
	 * another one (optional operation).
	 *
	 * @param proc the Processor to be scheduled
	 * @param predecessor the predecessing Processor
	 *
	 * @throws UnsupportedOperationException if this scheduler doesn't support this method
	 * @throws java.util.NoSuchElementException if predecesssor is not registered to this scheduler
	 */
	public void scheduleAfter(SchedulableProcessor proc, SchedulableProcessor predecessor);
	
	/**
	 * Causes this scheduler to stop scheduling a certain Processor.
	 *
	 * @param proc the Processor not to be scheduled anymore
	 */
	public void stopScheduling(SchedulableProcessor proc);

	/**
	 * Causes this scheduler to stop scheduling a certain Processor.
	 *
	 * @param proc the Processor not to be scheduled anymore
	 */
	public void stopScheduling(UrgentProcessor proc);

	/**
	 * Called by a Processor when it is started.
	 *
	 * @param proc the started Processor
	 */
	public void reportStart(SchedulableProcessor proc);
	
	/**
	 * Called by a Processor when it is started.
	 *
	 * @param proc the started Processor
	 */
	public void reportStart(UrgentProcessor proc);

	/**
	 * Called by a Processor when it is paused until wakeup, i.e. when it's 
	 * <code>pauseUntilWakeUp<code>-Methode is called or its <code>pause</code>-Method
	 * is called with <code>0</code> as argument.
	 *
	 * @param proc the blocked Processor
	 * @see xxl.core.pipes.processors.Processor#pauseUntilWakeUp()
	 * @see xxl.core.pipes.processors.Processor#pause(long)
	 */
	public void reportBlocked(SchedulableProcessor proc);

	/**
	 * Called by a Processor when it is paused until wakeup, i.e. when it's 
	 * <code>pauseUntilWakeUp<code>-Methode is called or its <code>pause</code>-Method
	 * is called with <code>0</code> as argument.
	 *
	 * @param proc the blocked Processor
	 * @param time
	 * @see xxl.core.pipes.processors.Processor#pauseUntilWakeUp()
	 * @see xxl.core.pipes.processors.Processor#pause(long)
	 */
	public void reportBlocked(UrgentProcessor proc);

	/**
	 * Called by a Processor when it's <code>pause</code>-Method is called 
	 * with a positive argument.
	 *
	 * @param proc the blocked Processor
	 * @see xxl.core.pipes.processors.Processor#pause(long)
	 */
	public void reportBlocked(SchedulableProcessor proc, long time);
	
	/**
	 * Called by a Processor when it's <code>pause</code>-Method is called 
	 * with a positive argument.
	 *
	 * @param proc the blocked Processor
	 * @param time
	 * @see xxl.core.pipes.processors.Processor#pause(long)
	 */
	public void reportBlocked(UrgentProcessor proc, long time);

	/**
	 * Called by a Processor when it's woken up by calling its
	 * <code>wakeUp</code>-Method after it had been put to sleep.
	 *
	 * @param proc the now unblocked Processor
	 * @see xxl.core.pipes.processors.Processor#wakeUp()
	 */
	public void reportUnblocked(SchedulableProcessor proc);

	/**
	 * Called by a Processor when it's woken up by calling its
	 * <code>wakeUp</code>-Method after it had been put to sleep.
	 *
	 * @param proc the now unblocked Processor
	 * @see xxl.core.pipes.processors.Processor#wakeUp()
	 */
	public void reportUnblocked(UrgentProcessor proc);

	/**
	 * Called by a Processor when its <code>setSchedulingPriority</code>-Method
	 * has been called.
	 *
	 * @param proc the processor
	 * @see xxl.core.pipes.processors.Processor#setSchedulingPriority(int)
	 */
	public void reportPriorityChange(SchedulableProcessor proc);
	
	/**
	 * Called by a Processor when its <code>setSchedulingPriority</code>-Method
	 * has been called.
	 *
	 * @param proc the processor
	 * @see xxl.core.pipes.processors.Processor#setSchedulingPriority(int)
	 */
	public void reportPriorityChange(UrgentProcessor proc);

	/**
	 * Called by a Processor directly before it terminates.
	 *
	 * @param proc the terminating Processor
	 */
	public void reportTermination(SchedulableProcessor proc);
	
	/**
	 * Called by a Processor directly before it terminates.
	 *
	 * @param proc the terminating Processor
	 */
	public void reportTermination(UrgentProcessor proc);
}

