package xxl.tests.util.reflect;

import xxl.core.util.reflect.SynchronizedWrapper;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class SynchronizedWrapper.
 */
public class TestSynchronizedWrapper {
	
	/** Interface needed in the example (only). */
	private interface TestWrapper {
		/**
		 * init example TestWrapper
		 */
		public void init();
		/**
		 * call example TestWrapper
		 */
		public void call();
	}
	
	/**
	 * Usage Example for SynchronizedWrapper.
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 * 	  	   submit parameters when the main method is called.
	 */
	public static void main(String args[]) {
		System.out.println("Wraping a class to be completly synchronized");
		System.out.println();

		TestWrapper a = new TestWrapper() {
			int i;
			public void init() {
				i=0;
			}
			public void call() {
				if (i!=0)
					System.out.println("Fehler in SynchronizedWrapper");
				i++;
				try { Thread.sleep(10); } catch (Exception e) {}
				i--;
			}
		};
		// and now the glory...
		// System.out.println(TestWrapper.class.getName());
		final TestWrapper aWrapped = (TestWrapper) SynchronizedWrapper.newInstance(a,"xxl.core.util.reflect.SynchronizedWrapper$TestWrapper");
		aWrapped.init();

		final xxl.core.util.concurrency.MutableBoolean cont = new xxl.core.util.concurrency.MutableBoolean(true);

		for (int i=0 ; i<10 ; i++) {
			new Thread () {
				public void run(){
					while (cont.get()) {
						aWrapped.call();
					}
				}
			}.start();
		}

		// Test it for two seconds.
		try { Thread.sleep(2000); } catch (Exception e) {}
		cont.set(false);

		System.out.println();
		System.out.println("Test successfully completed.");
	}

}
