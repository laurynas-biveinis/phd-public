package xxl.tests.util.concurrency;

import xxl.core.util.concurrency.Semaphore;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class Semaphore.
 */
public class TestSemaphore {

	/**
	 * For demonstration purpose.
	 */
	private static class DemoThread extends Thread {
		
		/**
		 * An array of integer objects manipulated during the demonstration run.
		 */
		Integer count[];
		
		/**
		 * The used semaphore.
		 */
		Semaphore sem;
		
		/**
		 * Creates a new demonastration thread.
		 * 
		 * @param name the name of the thread.
		 * @param count an array of integer objects manipulated during the
		 *        demonstration run.
		 * @param sem the used semaphore.
		 */
		DemoThread(String name,Integer count[],Semaphore sem) {
			this.count = count;
			this.sem = sem;
			// Call Thread.setName
			setName(name);
		}

		/**
		 * run demonstration
		 */
		public void run() {
			for (int i=0 ; i<50 ; i++) {
				sem.acquire();
				try { Thread.sleep(20); } catch (InterruptedException e) {}
				System.out.println(getName() + " Iteration " + i);
				count[0] = new Integer(count[0].intValue()+1);
				sem.release();
			}
		}
	}

	/** 
	 * Demonstrates the use of a Semaphore to control two competing 
	 * threads.
	 * 
	 * @param args the input arguments of the main method
	 */
	public static void main(String args[]) {
		System.out.println("Controlling threads with a semaphore");
		final Semaphore sem = new Semaphore();

		// No thread starts working before I tell him.
		sem.acquire();

		// Resource: count[]
		Integer count[] = new Integer[1];
		count[0] = new Integer(0);
		
		// Construction of two demo threads
		Thread t1 = new DemoThread("Thread 1",count,sem);
		Thread t2 = new DemoThread("Thread 2",count,sem);
		// running...
		t1.start();
		t2.start();
		
		while (t1.isAlive() || t2.isAlive()) {
			// Controlling both threads
			System.out.println("Controller - Count: " + count[0]);
			try { Thread.sleep(200); } catch (InterruptedException e) {}
			System.out.println("Controller - Let threads work for a while...");
			sem.release();
			try { Thread.sleep(500); } catch (InterruptedException e) {}
			sem.acquire();
		}
		
		// Exception for main maker!
		if (count[0].intValue()!=100)
			throw new RuntimeException("Semaphores do not work correctly!");

		System.out.println("Test attempt(msec)");
		// The semaphore is locked at this time
		final long millis = System.currentTimeMillis();
		
		new Thread() {
			public void run() {
				if (sem.attempt(1000)) {
					long timediff = System.currentTimeMillis() - millis;
					System.out.println("Got the semaphore after " + timediff + "ms");
					sem.release();
				}
			}
		}.start();
		
		try { Thread.sleep(500); } catch (InterruptedException e) {}
		// Let the thread continue
		sem.release();
	}

}
