package xxl.tests.collections.queues.io;

import java.util.Random;

import xxl.core.collections.queues.ListQueue;
import xxl.core.collections.queues.io.RandomAccessFileQueue;
import xxl.core.functions.Constant;
import xxl.core.io.converters.DoubleConverter;
import xxl.core.io.converters.IntegerConverter;
import xxl.core.io.converters.TemporalObjectConverter;
import xxl.core.pipes.elements.TemporalObject;
import xxl.core.pipes.elements.TimeInterval;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class RandomAccessFileQueue.
 */
public class TestRandomAccessFileQueue {

	/**
	 * The main method contains some examples how to use a
	 * RandomAccessFileQueue. It can also be used to test the
	 * functionality of a RandomAccessFileQueue.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main (String [] args) {

		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////

		System.out.println("Test 1\n======\n");
		// create a new random access file queue with ...
		RandomAccessFileQueue<Integer> queue = new RandomAccessFileQueue<Integer>(
			// a new file
			"queue.dat",
			// an integer converter
			IntegerConverter.DEFAULT_INSTANCE,
			// an input buffer size of 4 bytes
			new Constant<Integer>(4),
			// an output buffer size of 4 bytes
			new Constant<Integer>(4)
		);
		// open the queue
		queue.open();
		// insert the integers from 0 to 9 into the queue
		for (int i = 0; i < 10; i++)
			queue.enqueue(i);
		// print 5 elements of the queue
		int i = 0;
		while (i < 5 && !queue.isEmpty()) {
			i = queue.dequeue();
			System.out.println(i);
		}
		// insert the integers from 20 to 29 into the queue
		for (i = 20; i < 30; i++)
			queue.enqueue(i);
		// print all elements of the queue
		while (!queue.isEmpty())
			System.out.println(queue.dequeue());
		System.out.println();
		// close and clear the queue after use
		queue.close();
		queue.clear();

		//////////////////////////////////////////////////////////////////
		//                      Usage example (2).                      //
		//////////////////////////////////////////////////////////////////
	
		System.out.println("\n\nTest 2\n======\n");
		RandomAccessFileQueue<TemporalObject<Double>> randomAccessFileQueue = new RandomAccessFileQueue<TemporalObject<Double>>(
				"C:\\temp\\queue",
				new TemporalObjectConverter<Double>(new DoubleConverter()),
				new Constant<Integer>(96),
				new Constant<Integer>(96)
			);
		ListQueue<TemporalObject<Double>> referenceQueue = new ListQueue<TemporalObject<Double>>(); 
		
		Random r = new Random();
		int wc=0, rc=0, ok=0, errors=0;
		randomAccessFileQueue.open();
		referenceQueue.open();
		for (int j=0; j<1000; j++) {
			boolean write = wc==500 ? false : wc>rc ? r.nextBoolean() : true;
			if (write) {
				long s = wc;
				long e = wc+100;
				Double d = r.nextDouble();
				TemporalObject<Double> to = new TemporalObject<Double>(d, new TimeInterval(s,e));
				randomAccessFileQueue.enqueue(to);
				referenceQueue.enqueue(to);
				wc++;
			}
			else {
				TemporalObject<Double> to1 = randomAccessFileQueue.dequeue();
				TemporalObject<Double> to2 = referenceQueue.dequeue();
				rc++;
				if (!to1.equals(to2)) {
					System.err.println(to1+" <> "+to2+"  ("+wc+"-"+rc+")");
					System.err.flush();
					errors++;
				}
				else {
					ok++;
				}
			}			
		}
		randomAccessFileQueue.close();
		referenceQueue.close();
		System.out.println(ok+" object ok");
		System.out.println(errors+" errors");

	}

	
}
