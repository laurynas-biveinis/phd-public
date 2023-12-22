package xxl.tests.util.timers;

import xxl.core.util.timers.JNITimer;
import xxl.core.util.timers.TimerUtils;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class JNITimer.
 */
public class TestJNITimer {

	/**
	 * Performs a timer test.
	 * @param args can be used to submit parameters when the main method is called
	 */
	public static void main(String args[]) {
		TimerUtils.timerTest(new JNITimer());
	}

}
