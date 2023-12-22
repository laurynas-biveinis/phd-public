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

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Iterator;
import java.util.Random;

import xxl.core.cursors.AbstractCursor;
import xxl.core.cursors.Cursor;
import xxl.core.cursors.Cursors;
import xxl.core.cursors.sources.EmptyCursor;
import xxl.core.cursors.sources.Repeater;
import xxl.core.cursors.wrappers.IteratorCursor;
import xxl.core.functions.Function;
import xxl.core.pipes.scheduler.Scheduler;
import xxl.core.util.metaData.AbstractMetaDataManagement;
import xxl.core.util.metaData.CompositeMetaData;
import xxl.core.util.metaData.MetaDataManageable;
import xxl.core.util.metaData.MetaDataManagement;
import xxl.core.util.random.ContinuousRandomWrapper;
import xxl.core.util.timers.Timer;
import xxl.core.util.timers.TimerUtils;

/** 
 * Extension of the class {@link java.lang.Thread Thread}, which is used to 
 * simulate activity in a query graph. <BR>
 * The <CODE>run</CODE> method of this class looks as follows:  
 * <br><br>
 * <code><pre>
 * 	time = System.currentTimeMillis();
 * 	while (!terminate) {
 * 		if (period > 0)
 * 			delay();
 * 		process();
 * 	}
 * </code></pre>
 * A thread, in this framework named processor, executes
 * its abstract <CODE>process</CODE> method until
 * the flag <CODE>terminate</CODE> is set. Usually this
 * happens through another thread. An additional delay 
 * may be inserted, if the parameter <CODE>period</CODE> has been 
 * specified. The method <CODE>delay</CODE> invokes a (non-blocking) <CODE>wait</CODE> call
 * on an instance of a class inheriting from this abstract one.
 * Thereby this instance relinquishes any and all synchronization claims for the 
 * specified amount of time, so that other threads may be executed.
 * No class should inherit directly from <code>Processor</code> but from <code>SchedulableProcessor</code>
 * or <code>UrgentProcessor</code>.
 *
 * @see SchedulableProcessor
 * @see UrgentProcessor
 * @see java.lang.Thread
 * @since 1.1
 */
public abstract class Processor extends Thread implements MetaDataManageable<Object,Object> {

	/**
	 * The {@link xxl.core.pipes.scheduler.Scheduler Scheduler} used by all <code>Processor</code>s.
	 */
	protected static Scheduler scheduler = null;

	/**
	 * Set the <code>Scheduler</code>. All <code>Processor</code>>s will
	 * work with this scheduler.
	 */
	public static void setScheduler(Scheduler s) {
		scheduler = s;
	}

	/**
	 * Get a constant delaymanager which always delays period milliseconds.
	 * 
	 * @param period the constant period in milliseconds
	 * @return Iterator that delivers always the period
	 */
	public static Iterator<Long> getConstantDelayManager(long period) {
		return new Repeater<Long>(new Long(period));
	}

	// rate: elements per second
	public static Iterator<Long> getRandomDelayManager(
		final Timer timer,
		final double maxRate,
		final ContinuousRandomWrapper crw,
		final long duration
	) {	
		TimerUtils.warmup(timer);
		final long zeroTime = TimerUtils.getZeroTime(timer);	
			
		if (maxRate <= 0)
			throw new IllegalArgumentException("Maximum output rate has to be > 0.");
		return new AbstractCursor<Long>() {
			long time;
			Long delay;

			@Override
			public void open() {
				super.open();
				time = timer.getDuration();
				delay =  new Long((long) ((1d / (maxRate * crw.nextDouble()))*1000));
			}

			@Override
			public boolean hasNextObject() {
				return true;
			}

			@Override
			public Long nextObject() {
				long past = (long)((((double) timer.getDuration() - (zeroTime + time)) / timer.getTicksPerSecond())*1000);
				if (past > duration) {
					time = timer.getDuration();		
					delay = new Long((long) ((1d / (maxRate * crw.nextDouble()))*1000));
				}
				return delay;
			}
		};
	}
	
	// maxDelay in ms
	public static Iterator<Long> getRandomDelayManager(final int maxDelay, final long seed) {
	    return new AbstractCursor<Long>() {
	        protected Random random = new Random(seed);
	        
	        @Override
			public boolean hasNextObject() {
	            return true;
	        }
	        
	        @Override
			public Long nextObject() {
	           return new Long(random.nextInt(maxDelay));
	        }
	    };
	}	

	public class ProcessorMetaDataManagement extends AbstractMetaDataManagement<Object,Object> {

		public static final String CPU_TIME  = "CPU_TIME";
		public static final String USER_TIME = "USER_TIME";
		
		private boolean enableThreadCpuTimeMeasurement() {
			final ThreadMXBean threadMXbean = ManagementFactory.getThreadMXBean();
			if (threadMXbean.isThreadCpuTimeSupported() && threadMXbean.isCurrentThreadCpuTimeSupported()) {
				if (!threadMXbean.isThreadCpuTimeEnabled())
					threadMXbean.setThreadCpuTimeEnabled(true);
				return true;
			}
			return false;
		}
				
		@Override
		protected boolean addMetaData(Object metaDataIdentifier) {
			if (metaDataIdentifier.equals(CPU_TIME)) {
				if (!enableThreadCpuTimeMeasurement())
					return false;
				metaData.add(metaDataIdentifier, new Function<Object,Long>() {					
					protected final ThreadMXBean bean = ManagementFactory.getThreadMXBean();
					
					@Override
					public Long invoke() {
						return bean.getCurrentThreadCpuTime();
					}
				});
				return true;
			}
			if (metaDataIdentifier.equals(USER_TIME)) {
				if (!enableThreadCpuTimeMeasurement())
					return false;
				metaData.add(metaDataIdentifier, new Function<Object,Long>() {					
					protected final ThreadMXBean bean = ManagementFactory.getThreadMXBean();
					
					@Override
					public Long invoke() {
						return bean.getCurrentThreadCpuTime();
					}
				});
				return true;
			}
			return false;
		}

		@Override
		protected boolean removeMetaData(Object metaDataIdentifier) {
			if (metaDataIdentifier.equals(USER_TIME)) {
				if (metaData.contains(CPU_TIME)) 
					return true;
				ManagementFactory.getThreadMXBean().setThreadCpuTimeEnabled(false);
			}
			if (metaDataIdentifier.equals(CPU_TIME)) {
				if (metaData.contains(USER_TIME))
					return true;
				ManagementFactory.getThreadMXBean().setThreadCpuTimeEnabled(false);
			}
			return false;
		}
		
	}
	
	
	/**
	 * The delaymanager always determines the delay between 
	 * two successive <CODE>process</CODE> calls (in milliseconds).
	 * The return type is always a <CODE>Long</CODE>.
	 */
	protected Cursor<Long> delayManager = new EmptyCursor<Long>();

	/** 
	 * Delay between two successive <CODE>process</CODE> calls (in milliseconds).
	 */
	protected long delay = 0;

	/**
	 * Point in time, when the last <CODE>process</CODE> call 
	 * was executed.
	 */
	protected long time;

	/** 
	 * Remaining delay. The time needed to perform the
	 * <CODE>process</CODE> method is subtracted.
	 */
	protected long latency = 0;

	/**
	 * Remaining latency, if the parameter <CODE>period</CODE> has been
	 * chosen smaller than 15.625 ms on Windows plattforms or
	 * 19.99 ms on Linux plattforms, because the timer-interrupt 
	 * of these plattforms are only updated in these timer intervals.
	 * Therefore some <CODE>wait</CODE> calls delay the <CODE>process</CODE> execution 
	 * more than <CODE>period</CODE> milliseconds. In order to offer a nearly exact 
	 * timing this parameter stores the additional latency and 
	 * and the <CODE>wait</CODE> method is only invoked, if 
	 * this parameter is smaller than the specified parameter <CODE>period</CODE>.
	 */
	protected long additionalLatency = 0;

	/**
	 * Flag used to terminate this thread's execution.
	 */
	protected volatile boolean terminate = false;

	/**
	 * Flag that signals, if it is the first call to the 
	 * <CODE>delay</CODE> method.
	 */
	protected boolean first = true;

	/**
	 * Flag that signals, if the method <CODE>pauseUntilWakeUp</CODE>
	 * has been invoked and this instance still waits for a
	 * wake-up call.
	 */
	protected boolean waitForWakeUp;

	/**
	 * The scheduling priority for this <code>Processor>/code>.
	 */
	protected int schedulingPriority = 0;
	
	protected Timer timer;
	
	protected long zeroTime;
	
	protected boolean sleep;

	protected ProcessorMetaDataManagement metaDataManagement;
		
	/**
	 * Helps to create a new thread, called processor, simulating 
	 * an autonomous component in a query graph.
	 * 
	 * @param delayManager delayManager, which produce delay between two successive <CODE>process</CODE> calls (in milliseconds).
	 * @param timer
	 * @param sleep
	 */
	public Processor(Iterator<Long> delayManager, Timer timer, boolean sleep) {
		this.delayManager = Cursors.wrap(delayManager);
		this.timer = timer;
		this.sleep = sleep;
		initializeTimer();
		createMetaDataManagement();
	}
	
	public Processor(Iterator<Long> delayManager) {
		this(delayManager, TimerUtils.FACTORY_METHOD.invoke(), false);
	}
	
	/**
	 * Helps to create a new thread, called processor, simulating 
	 * an autonomous component in a query graph.
	 * 
	 * @param period Delay between two successive <CODE>process</CODE> calls (in milliseconds).
	 */
	public Processor(long period) {
		this(getConstantDelayManager(period), TimerUtils.FACTORY_METHOD.invoke(), false);
		if (period < 0)
			throw new IllegalArgumentException("Periods have to be >= 0.");
	}
	
	public Processor() {
		createMetaDataManagement();
	}

	/**
	 * Executes a loop of <CODE>delay</CODE> and <CODE>process</CODE> calls
	 * until the flag <CODE>terminate</CODE> is set, which stops the 
	 * execution of this thread.
	 */
	@Override
	public void run() {
		onStart();
		time = timer.getDuration();
		while (!terminate) {
			if (delayManager.hasNext()) {
				delay = delayManager.next().longValue();
				if (delay > 0)
					delay();
			}
			process();
		}
		onTermination();
	}

	/**
	 * Returns the scheduling priority of this <code>Processor</code>.
	 *
	 * @return The scheduling priority of this <code>Processor</code>.
	 */
	public synchronized int getSchedulingPriority() {
		return schedulingPriority;
	}

	/**
	 * Sets the scheduling priority of this Processor.
	 *
	 * @param newSchedulingPriority The new scheduling priority for this processor.
	 * 
	 * @exception IllegalArgumentException If the specified priority is not in the range
	 * 	given by <code>getMinSchedulingPriority()</code> and <code>getMaxSchedulingPriority()</code>
	 *	from the currently used scheduler. If no scheduler is used, all values are accepted.
	 */
	public synchronized void setSchedulingPriority(int newSchedulingPriority) {
		if (scheduler != null) {
			if ((newSchedulingPriority < scheduler.getMinSchedulingPriority())
				|| (newSchedulingPriority
					> scheduler.getMaxSchedulingPriority())) {
				throw new IllegalArgumentException(
					"Scheduling priority has to be between "
						+ scheduler.getMinSchedulingPriority()
						+ " and "
						+ scheduler.getMaxSchedulingPriority()
						+ ".");
			}
			if (newSchedulingPriority != schedulingPriority) {
				schedulingPriority = newSchedulingPriority;
				onPriorityChange();
			}
		}
	}

	/**
	 * Synthetic delay inserted between two successive <CODE>process</CODE> calls.
	 * The length of this delay is given by the user via the 
	 * parameter <CODE>period</CODE>. The time pertaining to the costs
	 * of the <CODE>process</CODE> method's execution are subtracted. <BR>
	 * The method <CODE>pauseExact</CODE> is used to realize a 
	 * nearly exact timing even for small delays.
	 */
	public void delay() {
		long duration = (long)((((double) timer.getDuration() - (zeroTime + time)) / timer.getTicksPerSecond())*1000);
		if ((latency = (delay - duration)) > 0) {
			if (!first) {
				if (sleep)
					try {
						Thread.sleep(latency);
					} catch (InterruptedException e) {
						System.err.println("ERROR : " + e.getMessage());
						e.printStackTrace(System.err);
					}
				else
					this.pauseExact(latency);	
			}
			else {
				first = false;
				if (sleep)
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {
						System.err.println("ERROR : " + e.getMessage());
						e.printStackTrace(System.err);
					}
				else
					this.pauseExact(delay);
			}
		}
		time = timer.getDuration();
	}

	/**
	 * Abstract method doing some user-defined operation periodically until
	 * this thread is terminated.
	 */
	public abstract void process();

	/**
	 * Causes the current thread to wait for the specified
	 * time (milliseconds). Calls <CODE>this.wait(millis)</CODE>.
	 * 
	 * @param millis The time to wait (in ms).
	 */
	public synchronized void pause(long millis) {
		if (millis == 0) {
			onBlocked();
		} else {
			onBlocked(millis);
		}
		try {
		 	this.wait(millis);
		} catch (InterruptedException e) {
			System.err.println("ERROR : " + e.getMessage());
			e.printStackTrace(System.err);
		}
	}

	/**
	 * Causes the current thread to wait until an explicit call to 
	 * <CODE>wakeUp</CODE> is applied to it. <BR>
	 * Sets the flag <CODE>waitForWakeUp</CODE>.
	 */
	public synchronized void pauseUntilWakeUp() {
		waitForWakeUp = true;
		pause(0);
		waitForWakeUp = false;
	}

	/**
	 * Causes the current thread to wait for the specified
	 * time (milliseconds), even if the parameter <CODE>millis</CODE> has been
	 * chosen smaller than 15.625 ms on Windows plattforms or
	 * 19.99 ms on Linux plattforms, because the timer-interrupt 
	 * of these plattforms are only updated in these timer intervals.
	 * Therefore some usual <CODE>wait</CODE> calls delay
	 * more than <CODE>millis</CODE> milliseconds. In order to offer a nearly exact 
	 * timing the parameter <CODE>additionalLatency</CODE> stores the additional latency
	 * and the <CODE>wait</CODE> method is only invoked, if 
	 * this parameter is smaller than the specified parameter <CODE>millis</CODE>.
	 *
	 * @param millis The time to wait (in ms).
	 */
	public synchronized void pauseExact(long millis) {
		if (additionalLatency >= millis) {
			additionalLatency = additionalLatency - millis;
			return;
		}
		long t = timer.getDuration();
		if (millis == 0) {
			onBlocked();
		} else {
			onBlocked(millis);
		}
		try {
			this.wait(millis);
		} catch (InterruptedException e) {
			System.err.println("ERROR : " + e.getMessage());
			e.printStackTrace(System.err);
		}
		long duration = (long)((((double) timer.getDuration() - (zeroTime + t)) / timer.getTicksPerSecond())*1000);
		additionalLatency += duration - millis;
		onUnblocked();
	}

	/**
	 * Wakes up a single thread that is waiting on this object's monitor
	 * by calling <CODE>this.notify()</CODE>.
	 */
	public synchronized void wakeUp() {
		this.notify();
		onUnblocked();
	}

	/**
	 * Sets the DelayManager, which produce the delays between two successive <CODE>process</CODE> calls. <BR>
	 *
	 * @param delayManager The DelayManager, which produce delay between two successive <CODE>process</CODE> calls (in milliseconds).
	 */
	public synchronized void setDelayManager(Iterator<Long> delayManager) {
		this.delayManager = new IteratorCursor<Long>(delayManager);
	}

	/**
	 * Returns the DelayManager, which produce the delay between two successive <CODE>process</CODE> calls (in milliseconds). <BR>
	 * Because the attribute <CODE>delayManager</CODE> is not denoted as volatile (modifier), read
	 * access has also to be synchronized with the aim to avoid missed updates of several threads monitoring
	 * this attribute.
	 *
	 * @return The delay between two successive <CODE>process</CODE> calls (in milliseconds).
	 */
	public synchronized Cursor<Long> getDelayManager() {
		return delayManager;
	}

	/**
	 * Returns the delay between the two last <CODE>process</CODE> calls (in milliseconds). <BR>
	 *
	 * @return The delay between the two last successive <CODE>process</CODE> calls (in milliseconds).
	 *
	 */
	public synchronized long getLastDelay() {
		return delay;
	}

	/** 
	 * Terminates this thread, i.e., this method
	 * sets the attribute <CODE>terminate</CODE>, which causes that
	 * the execution of this thread's <CODE>run</CODE> method
	 * is completed, whereupon the JVM stops and kills it.
	 */
	public void terminate() {
		terminate = true;
		wakeUp();
	}

	public boolean isTerminated() {
		return terminate;
	}

	/** 
	 * Returns <CODE>true</CODE>, if this thread waits for
	 * a wake-up call, otherwise <CODE>false</CODE>.
	 * 
	 * @return Returns <CODE>true</CODE>, if this thread waits for
	 * 		a wake-up call, otherwise <CODE>false</CODE>.
	 */
	public boolean waitsForWakeUp() {
		return waitForWakeUp;
	}

	/**
	 * This method has to be called when a processor starts. The call should be the
	 * first statement in the <code>run()</code> method.
	 * This method should be used to inform a scheduler.
	 */
	protected void onStart() {}

	/**
	 * This method has to be called if this processor blocks for a unknown time.
	 * This method should be used to inform a scheduler.
	 */
	protected void onBlocked() {}

	/**
	 * This method has to be called if this processor blocks for <code>millis</code> ms.
	 * This method should be used to inform a scheduler.
	 */
	protected void onBlocked(long millis) {}

	/**
	 * This method has to be called if this processor unblocks.
	 * This method should be used to inform a scheduler.
	 */
	protected void onUnblocked() {}

	/**
	 * This method has to be called when a processor treminates. The call should be the
	 * last statement in the <code>run()</code> method.
	 * This method should be used to inform a scheduler.
	 */
	protected void onTermination() {}

	/**
	 * This method has to be called if the scheduling priority of this processor changes.
	 * This method should be used to inform a scheduler.
	 */
	protected void onPriorityChange() {}

	protected void initializeTimer() {
		timer = TimerUtils.FACTORY_METHOD.invoke();
		TimerUtils.warmup(timer);
		zeroTime = TimerUtils.getZeroTime(timer);
	}

	/* (non-Javadoc)
	 * @see xxl.core.util.MetaDataManageable#createMetaDataManagement()
	 */
	public void createMetaDataManagement() {
		if (metaDataManagement != null)
			throw new IllegalStateException("An instance of MetaDataManagement already exists.");
		metaDataManagement = new ProcessorMetaDataManagement();
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.util.MetaDataProvider#getMetaData()
	 */
	public CompositeMetaData<Object,Object> getMetaData() {
		return metaDataManagement.getMetaData();
	}
	
	/* (non-Javadoc)
	 * @see xxl.core.util.MetaDataManageable#getMetaDataManagement()
	 */
	public MetaDataManagement<Object,Object> getMetaDataManagement() {
		return metaDataManagement;
	}
	
}