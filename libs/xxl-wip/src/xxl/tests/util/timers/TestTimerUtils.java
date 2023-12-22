package xxl.tests.util.timers;

import xxl.core.util.timers.TimerUtils;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class TimerUtils.
 */
public class TestTimerUtils {

	/**
	 * Performs a timer test using the factory method.
	 * @param args can be used to submit parameters when the main method is called
	 */
	public static void main(String args[]) {
		TimerUtils.timerTest(TimerUtils.FACTORY_METHOD.invoke());
	}

}
