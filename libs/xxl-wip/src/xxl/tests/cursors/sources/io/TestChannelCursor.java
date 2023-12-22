package xxl.tests.cursors.sources.io;

import xxl.core.cursors.sources.io.ChannelCursor;
import xxl.core.util.concurrency.Channel;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class ChannelCursor.
 */
public class TestChannelCursor {

	/**
	 * The main method contains some examples to demonstrate the usage and the
	 * functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to submit
	 *        parameters when the main method is called.
	 */
	public static void main(String[] args) {
		final Channel channel = new xxl.core.util.concurrency.AsynchronousChannel();
		
		System.out.println("Example 1");
		new Thread() {
			public void run() {
				channel.put(new Integer(17));
				channel.put(new Integer(42));
				channel.put(new Integer(1));
				channel.put(new Integer(4));
				channel.put(new Integer(13));
				channel.put(null);
				System.out.println("Thread terminates!");
			}
		}.start();
		
		ChannelCursor cursor = new ChannelCursor(channel);
		
		cursor.open();
		
		while (cursor.hasNext())
			System.out.println(cursor.next());
		
		cursor.close();
		try { Thread.sleep(100); } catch (Exception e) {} // Waiting for the output

		System.out.println("Example 2: close called before the end of the iteration.");
		new Thread() {
			public void run() {
				channel.put(new Integer(17));
				channel.put(new Integer(42));
				channel.put(new Integer(1));
				channel.put(new Integer(4));
				channel.put(new Integer(13));
				channel.put(null);
				System.out.println("Thread terminates!");
			}
		}.start();
		
		cursor = new ChannelCursor(channel);
		
		cursor.open();
		
		System.out.println(cursor.next());
		System.out.println(cursor.next());
		System.out.println(cursor.next());
		System.out.println("Closing the cursor now");
		
		cursor.close();

	}

}
