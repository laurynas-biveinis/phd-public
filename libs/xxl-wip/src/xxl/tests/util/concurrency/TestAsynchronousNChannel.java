package xxl.tests.util.concurrency;

import xxl.core.util.concurrency.AsynchronousNChannel;
import xxl.core.util.concurrency.Channel;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class AsynchronousNChannel.
 */
public class TestAsynchronousNChannel {

	/**
	 * Demonstrates the use of an asynchronous channel.
	 * 
	 * @param args the input arguments of the main method
	 */
	public static void main(String args[]) {
		System.out.println("AsynchronousNChannel");
		System.out.println("A Performance test");
		
		final int numberOfObjects = 100000;
		final Integer ia[] = new Integer[numberOfObjects];
		for (int i=0; i<numberOfObjects; i++)
			ia[i] = new Integer(i);
		
		// Construction of two demo threads
		int size=1;
		while (size<10000) {
			System.out.print("Size of the channel: "+size);
			
			final Channel c = new AsynchronousNChannel(size);
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
			size *= 2;
		}
	}

}
