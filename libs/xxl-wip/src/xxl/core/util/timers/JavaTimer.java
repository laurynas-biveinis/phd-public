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
 * Implements the Timer interface based on java.lang.System.currentTimeMillis.
 */
public class JavaTimer implements Timer {
	
	/**
	 * The starting time of the current timer.
	 */
	protected long millis;
	
	/**
	 * Constructs a JavaTimer.
	 */
	public JavaTimer() {
	}
	
	/**
	 * Starts a JavaTimer.
	 */
	public void start() {
		millis = System.currentTimeMillis();
	}
	
	/**
	 * Returns time in ms since JavaTimer was started.
	 * @return returns time in ms since JavaTimer was started
	 */
	public long getDuration() {
		return System.currentTimeMillis()-millis;
	}
	
	/**
	 * Returns number of ticks per second (1000)
	 * @return returns number of ticks per second (1000)
	 */
	public long getTicksPerSecond() {
		return 1000;
	}
	
	/**
	 * Returns string "java.lang.System-Timer"
	 * @return returns string "java.lang.System-Timer"
	 */
	public String timerInfo() {
		return "java.lang.System-Timer";
	}
}
