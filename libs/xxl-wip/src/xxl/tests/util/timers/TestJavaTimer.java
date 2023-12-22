package xxl.tests.util.timers;

import xxl.core.util.timers.JavaTimer;
import xxl.core.util.timers.TimerUtils;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class JavaTimer.
 */
public class TestJavaTimer {
	
	/**
	 * Performs a timer test.
	 * @param args can be used to submit parameters when the main method is called
	 */
	public static void main(String args[]) {
		TimerUtils.timerTest(new JavaTimer());
	}

}
