/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.core.util.timers;

/**
 * The timer interface is the base of efficient timers which are 
 * based on Ticks as smallest units. Each implementation of this
 * interface can have a different number of ticks per second.
 */
public interface Timer {
	/** 
	 * Starts the timer.
	 */
	public void start();
	/**
	 * Returns the time in ticks since the last start-call.
	 * @return number of ticks
	 */
	public long getDuration();
	/**
	 * Returns the number of ticks per second.
	 * @return number of ticks
	 */
	public long getTicksPerSecond();
	/**
	 * Returns a String with information about the Timer.
	 * @return timer info
	 */
	public String timerInfo();
}
