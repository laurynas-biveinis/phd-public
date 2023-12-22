package xxl.tests.util.concurrency;

import xxl.core.util.concurrency.AsynchronousChannel;
import xxl.core.util.concurrency.Channel;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class AsynchronousChannel.
 */
public class TestAsynchronousChannel {

	/**
	 * Demonstrates the use of an asynchronous channel.
	 * 
	 * @param args the input arguments of the main method
	 */
	public static void main(String args[]) {
		System.out.println("AsynchronousChannel");

		final Channel channel = new AsynchronousChannel();

		// Construction of two demo threads
		Thread t = new Thread () {
			public void run() {
				int number=100;
				
				while (number>0) {
					if (!channel.isEmpty()) {
						System.out.println("Received: "+((Integer) channel.take()).intValue());
						number--;
					}
					else
						System.out.println("Channel does not contain an object");
					try { Thread.sleep(1); } catch (Exception e) {}
				}
			}
		};
		t.start();
	
		int count = 0;
		while (t.isAlive()) {
			if (!channel.isFull())
				channel.put(new Integer(count++));
			// try { Thread.sleep(2); } catch (Exception e) {}
		}
		
		System.out.println("Performance test");
		
		final int numberOfObjects = 100000;
		final Integer ia[] = new Integer[numberOfObjects];
		for (int i=0; i<numberOfObjects; i++)
			ia[i] = new Integer(i);
		
		final Channel c = new AsynchronousChannel();
		long t1, t2;
		
		new Thread () {
			public void run() {
				for (int i=0; i<numberOfObjects; i++)
					c.put(ia[i]);
				c.put(null);
			}
		}.start();
		
		t1 = System.currentTimeMillis();
		int i=0;
		while (true) {
			Object o = c.take();
			if (o==null)
				break;
			else {
				if ( ((Integer) o).intValue()!=i )
					throw new RuntimeException("Returned Value is not correct!");
				i++;
			}
		}
		t2 = System.currentTimeMillis();
		
		System.out.println(" Time: "+(t2-t1)+"ms");
	}

}
