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

import xxl.core.pipes.processors.CircularProcessorList;
import xxl.core.pipes.processors.Processor;
import xxl.core.pipes.processors.SchedulableProcessor;
import xxl.core.pipes.processors.UrgentProcessor;

//erste Version: alle Threads in einer Liste (auch die ungestarteten)
//               Vorteil: ordnungserhaltend
/**
 * This scheduler schedules Processors with a priority-based round robin.
 * It simply loops over all the SchedulableProcessors and lets them run for
 * a time proportional to their scheduling priority (in fact, the scheduling 
 * priority in milliseconds) by giving them a high thread priority for that 
 * time.<br>
 * UrgentProcessord are being given an even higher thread priority and then 
 * are left to themselves.
 *
 * @since 1.1
 */
public class TimeSliceScheduler extends Thread implements Scheduler {
  
  /**
   * The maximum value for priorities supported by this scheduler.
   */
  public final static int MAX_SCHEDULING_PRIORITY = 1000;
  
  /**
   * The minimum value for priorities supported by this scheduler.
   */
  public final static int MIN_SCHEDULING_PRIORITY = 10;
  
  /**
   * The normal value for priorities supported by this scheduler.
   */
  public final static int NORM_SCHEDULING_PRIORITY = 50;
  
  private final Object WAIT = new Object();
  private final Object LOCK = new Object();
  
  //note that CircularProcessorList is synchronized
  protected CircularProcessorList procList = new CircularProcessorList();
  protected Processor activeProc = null;
  protected boolean terminatesIfListEmpty;
  
  /**
   * Constructs a new TimeSliceScheduler.
   */
  public TimeSliceScheduler(boolean terminates) {
  	//setPriority(6);
  	setPriority(MAX_PRIORITY);
    this.terminatesIfListEmpty = terminates;
  }
  
  /**
   * Returns the maximum value for priorities supported by this scheduler
   * @return the maximum value for priorities supported by this scheduler
   */	
  public int getMaxSchedulingPriority() {
  	return MAX_SCHEDULING_PRIORITY;
  }
  
  /**
   * Returns the mimimum value for priorities supported by this scheduler
   * @return the mimimum value for priorities supported by this scheduler
   */	
  public int getMinSchedulingPriority() {
  	return MIN_SCHEDULING_PRIORITY;
  }
  
  /**
   * Returns the normal value for priorities supported by this scheduler
   * @return the normal value for priorities supported by this scheduler
   */	
  public int getNormSchedulingPriority() {
  	return NORM_SCHEDULING_PRIORITY;
  }
  
  /**
   * When a SchedulableProcessor's <code>setScheduler<code>-Method is called
   * it will call this method to register itself to this scheduler.
   * 
   * @param proc the Processor to be scheduled
   * @see Processor#setScheduler(Scheduler)
   */
  public void schedule(SchedulableProcessor proc) {
  	if (procList.contains(proc)) return;
  	if (!proc.isTerminated()) {
  	    System.out.println("Setting MIN_PRIORITY of "+proc.getName());
  		proc.setPriority(MIN_PRIORITY);
  		procList.add(proc);
  	}
  }
  
  /**
   * When a UrgentProcessor's <code>setScheduler<code>-Method is called
   * it will call this method to register itself to this scheduler.
   * 
   * @param proc the Processor to be scheduled
   * @see Processor#setScheduler(Scheduler)
   */
  public void schedule(UrgentProcessor proc) {
  	proc.setPriority(7);
  }
  
  /**
   * Causes this scheduler to schedule a Processor right after 
   * another one.
   *
   * @param proc the Processor to be scheduled
   * @param predecessor the predecessing Processor
   *
   * @throws NoSuchElementException if predecessor is not registered to this scheduler
   */
  public void scheduleAfter(SchedulableProcessor proc, SchedulableProcessor predecessor) {
  	if (!procList.contains(predecessor)) throw new NoSuchElementException();
  	if (!proc.isTerminated()) {
  		if (procList.contains(proc)) {
  			synchronized(LOCK) {
  				//unschoen, wenn proc gerade laeuft, wird danach mit seinem neuen Nachfolger fortgesetzt
  				procList.remove(proc);
  				procList.addAfter(proc, predecessor);
  			}
  		}
  		else {
  			proc.setPriority(2);
  			procList.addAfter(proc, predecessor); //may throw NoSuchElementException
  		}
  		
  	}
  }
  
  /**
   * Causes this scheduler to stop scheduling a certain Processor.
   *
   * @param proc the Processor not to be scheduled anymore
   */	
  public void stopScheduling(SchedulableProcessor proc) {
  	procList.remove(proc);
  	synchronized(LOCK) {
  		if (proc == activeProc) synchronized(WAIT) {
  			WAIT.notify();
  		}
  	}
  }
  
  /**
   * Causes this scheduler to stop scheduling a certain UrgentProcessor.
   * This implementation does not do anything as UrgentProcessors are 
   * simply being given a very high (thread-)priority and then left to themselves.
   *
   * @param proc the UrgentProcessor not to be scheduled anymore
   */	
  public void stopScheduling(UrgentProcessor proc) {
  	//interessiert in dieser Version nicht;
  }

  /**
   * Called by a Processor when it is started.
   *
   * @param proc the started Processor
   */	
  public void reportStart(SchedulableProcessor proc) {
  	//interessiert in dieser Version nicht;
  }
  
  /**
   * Called by a UrgentProcessor when it is started.
   *
   * @param proc the started Processor
   */	
  public void reportStart(UrgentProcessor proc) {
  	//unwichtig
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
  public void reportBlocked(SchedulableProcessor proc) {
  	procList.setBlocked(proc);
  	synchronized (LOCK) {
  		if (proc == activeProc) synchronized(WAIT) {
  			WAIT.notify();
  		}
  	}
  }
  
  /**
   * Called by a UrgentProcessor when it is paused until wakeup, i.e. when it's 
   * <code>pauseUntilWakeUp<code>-Methode is called or its <code>pause</code>-Method
   * is called with <code>0</code> as argument.
   * This implementation does not use this information at all.
   *
   * @param proc the blocked Processor
   * @see Processor#pauseUntilWakeUp()
   * @see Processor#pause(long)
   */
  public void reportBlocked(UrgentProcessor proc) {
  	//todo, geht aber auch so
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
  	procList.setBlockedUntil(proc, System.currentTimeMillis()+time);
  	synchronized (LOCK) {
  		if (proc == activeProc) synchronized(WAIT) {
  			WAIT.notify();
  		}
  	}
  }

  /**
   * Called by a UrgentProcessor when it's <code>pause</code>-Method is called 
   * with a positive argument.
   * This implementation does not use this information at all.
   *
   * @param proc the blocked Processor
   * @see Processor#pause(long)
   */
  public void reportBlocked(UrgentProcessor proc, long time) {
  	//todo, geht aber auch so
  }
  
  /**
   * Called by a Processor when it's woken up by calling its
   * <code>wakeUp</code>-Method after it had been put to sleep.
   *
   * @param proc the now unblocked Processor
   * @see Processor#wakeUp()
   */
  public void reportUnblocked(SchedulableProcessor proc) {
  	procList.setUnblocked(proc);
  }
  
  /**
   * Called by a UrgentProcessor when it's woken up by calling its
   * <code>wakeUp</code>-Method after it had been put to sleep.
   * This implementation does not use this information at all.
   *
   * @param proc the now unblocked Processor
   * @see Processor#wakeUp()
   */
  public void reportUnblocked(UrgentProcessor proc) {
  	//todo, geht aber auch so
  }

  /**
   * Called by a Processor when its <code>setSchedulingPriority</code>-Method
   * has been called.
   *
   * @param proc the processor
   * @see Processor#setSchedulingPriority(int)
   */
  public void reportPriorityChange(SchedulableProcessor proc) {
  	//interessiert fuer diese Version nicht
  }

  /**
   * Called by a UrgentProcessor when its <code>setSchedulingPriority</code>-Method
   * has been called.
   * This implementation does not use this information at all.
   *
   * @param proc the processor
   * @see Processor#setSchedulingPriority(int)
   */
  public void reportPriorityChange(UrgentProcessor proc){
  	//hmm, braucht das wer?
  }
  
  
  /**
   * Called by a Processor directly before it terminates.
   *
   * @param proc the terminating Processor
   */
  public void reportTermination(SchedulableProcessor proc) {
  	stopScheduling(proc);
  }
  
  /**
   * Called by a UrgentProcessor directly before it terminates.
   *
   * @param proc the terminating Processor
   */
  public void reportTermination(UrgentProcessor proc) {
  	stopScheduling(proc);
  }
  
  
  /**
   * In this method, all the scheduling work is done. 
   */
  @Override
	public void run() {
	  	while (true) {
	  		long sleepTime = 100;
	  		//System.out.println("procList: "+procList.size());
	  		if (terminatesIfListEmpty && procList.isEmpty())
	  		    return;
	  		if (!procList.isEmpty()) {
	  			Processor proc = procList.getNextUnblocked();
	  			if ( proc != null) synchronized(LOCK) {
	  				if (activeProc != null) {
	  				    System.out.println("Decrease priority of "+activeProc.getName());
	  				    activeProc.setPriority(MIN_PRIORITY);
	  				}
	  				activeProc = proc;
	  				if (activeProc != null) {
	  				    System.out.println("Increase priority of "+activeProc.getName());
	  					activeProc.setPriority(4);
	  					sleepTime = activeProc.getSchedulingPriority();
	  				}
	  			}
	  		}
	  		try {
	  			synchronized(WAIT) {
	  			    System.out.println("Scheduler start waiting: "+sleepTime);
	  				WAIT.wait(sleepTime);
	  				System.out.println("Scheduler stopped waiting");
	  			}
	  		} catch (InterruptedException e) {}
	  	}
	  }
	}