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

package xxl.core.pipes.processors;

import java.util.Iterator;

import xxl.core.util.timers.Timer;

/**
 * A <code>Processor</code> that may be scheduled freely by a <code>Scheduler</code>.
 * Freely means, that such a processor is allowed to be blocked over a longer time by the scheduler.
 * This should be ok for normal components in a query graph, but maybe not for sources.
 *
 * @see xxl.core.pipes.scheduler.Scheduler
 * @see Processor
 * @since 1.1
 */
public abstract class SchedulableProcessor extends Processor {
	
	/**
	 * Helps to create a new thread simulating 
	 * an autonomous component in a query graph.
	 * 
	 * @param delayManager delayManager, which produces delays between 
	 * 		two successive <CODE>process</CODE> calls (in milliseconds).
	 * @param timer
	 * @param sleep
	 */
	public SchedulableProcessor(Iterator<Long> delayManager, Timer timer, boolean sleep) {
		super(delayManager, timer, sleep);
	}
	
	/**
	 * Creates a new schedulable processor. If a scheduler is set, this processor will
	 * register to this scheduler and set its scheduling priority to the norm scheduling priority 
	 * defined by the used scheduler.
	 *
	 * @param period Delay between two successive <CODE>process</CODE> calls (in milliseconds).
	 */
	public SchedulableProcessor(long period) {
		super(period);
		if (scheduler != null) {
			scheduler.schedule(this);
			schedulingPriority = scheduler.getNormSchedulingPriority();
		}
	}
	
	/**
	 * Reports to the scheduler, if there is one, the start of this processor 
	 * by calling <code>Scheduler.reportStart(SchedulableProcessor)</code>.
	 *
	 * @see xxl.core.pipes.scheduler.Scheduler
	 */
	@Override
	protected void onStart() {
		if (scheduler != null) {
			scheduler.reportStart(this);
		}
	}
	
	/**
	 * Reports to the scheduler, if there is one, that this processor blocks for a unknown time 
	 * by calling <code>Scheduler.reportBlocked(SchedulableProcessor)</code>.
	 *
	 * @see xxl.core.pipes.scheduler.Scheduler
	 */
	@Override
	protected void onBlocked() {
		if (scheduler != null) {
			scheduler.reportBlocked(this);
		}
	}

	/**
	 * Reports to the scheduler, if there is one, that this processor blocks for <code>millis</code> ms
	 * by calling <code>Scheduler.reportBlocked(SchedulableProcessor, long)</code>.
	 *
	 * @param millis The number of milli seconds this processor will block.
	 * @see xxl.core.pipes.scheduler.Scheduler
	 */
	@Override
	protected void onBlocked(long millis) {
		if (scheduler != null) {
			scheduler.reportBlocked(this, millis);
		}
	}

	/**
	 * Reports to the scheduler, if there is one, that this processor unblocks
	 * by calling <code>Scheduler.reportUnblocked(SchedulableProcessor)</code>.
	 *
	 * @see xxl.core.pipes.scheduler.Scheduler
	 */
	@Override
	protected void onUnblocked() {
		if (scheduler != null) {
			scheduler.reportUnblocked(this);
		}
	}

	/**
	 * Reports to the scheduler, if there is one, that this processor terminates
	 * by calling <code>Scheduler.reportTermination(SchedulableProcessor)</code>.
	 *
	 * @see xxl.core.pipes.scheduler.Scheduler
	 */
	@Override
	protected void onTermination() {
		if (scheduler != null) {
			scheduler.reportTermination(this);
		}
	}

	/**
	 * Reports to the scheduler, if there is one, that this processor changes his scheduling priority
	 * by calling <code>Scheduler.reportPriorityChange(SchedulableProcessor)</code>.
	 *
	 * @see xxl.core.pipes.scheduler.Scheduler
	 */
	@Override
	protected void onPriorityChange() {
		if (scheduler != null) {
			scheduler.reportPriorityChange(this);
		}
	}
	
}