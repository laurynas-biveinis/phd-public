package xxl.tests.cursors.identities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import xxl.core.cursors.Cursor;
import xxl.core.cursors.identities.TeeCursor;
import xxl.core.cursors.identities.TeeCursor.ListStorageArea;
import xxl.core.cursors.identities.TeeCursor.StorageArea;
import xxl.core.io.LRUBuffer;
import xxl.core.util.XXLSystem;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class TeeCursor.
 */
public class TestTeeCursor {

	/**
	 * Example which shows the usage of this class.
	 *  
	 * @param args Command line arguments are ignored here.
	 */
	public static void main(String[] args) {

		/******************************************************************************/
		/*         Example 1                                                          */
		/******************************************************************************/
		System.out.println("Example 1");
		System.out.println("---------");
		
		int numberOfCursors = 5;
		int numberOfElementsPerCursor = 100;
		boolean verbose = true;
		/**/
		StorageArea<Integer> storageArea = new ListStorageArea<Integer>(new java.util.ArrayList<Integer>()); 
		/*/
		StorageArea<Integer> storageArea = new QueueStorageArea<Integer>(new xxl.core.collections.queues.ArrayQueue<Integer>());
		/**/
		
		Cursor<Integer> cursor = new xxl.core.cursors.sources.Enumerator(numberOfElementsPerCursor);
		
		TeeCursor<Integer> tee = new TeeCursor<Integer>(cursor, storageArea, false); 
		
		List<Cursor<Integer>> cursors = new ArrayList<Cursor<Integer>>(numberOfCursors);
		boolean fullyConsumed[] = new boolean[numberOfCursors];
		
		cursors.add(tee);
		cursors.get(0).open();
		fullyConsumed[0]=false;
		
		for (int i = 1; i < numberOfCursors; i++) {
			cursors.add(tee.cursor());
			cursors.get(i).open();
			fullyConsumed[i]=false;
		}
		
		java.util.Random random = new java.util.Random();
		
		int totalNumberOfElements = 0;
		int numberOfFullyConsumedCursors = 0;
		int cursorNumber;
		while (numberOfFullyConsumedCursors < numberOfCursors) {
			cursorNumber = (int)(random.nextDouble() * numberOfCursors);
			if (!fullyConsumed[cursorNumber]) {
				if (cursors.get(cursorNumber).hasNext()) {
					Object element = cursors.get(cursorNumber).next();
					if (verbose)
						System.out.println("Cursor number: " + cursorNumber + ", Element: " + element);
					totalNumberOfElements++;
				}
				else {
					fullyConsumed[cursorNumber] = true;
					System.out.println("Cursor " + cursorNumber + " has finished its work");
					numberOfFullyConsumedCursors++;
				}
			}
		}
		
		Cursor<Integer> c2 = tee.cursor();
		if (xxl.core.cursors.Cursors.count(c2) != numberOfElementsPerCursor)
			throw new RuntimeException("Last cursor was not succesful");
		
		for (int i = 0; i < numberOfCursors; i++)
			cursors.get(i).close();
		
		cursor.close();
		
		if (totalNumberOfElements != numberOfElementsPerCursor * numberOfCursors)
			throw new RuntimeException("Not all elements were delivered");

		/******************************************************************************/
		/*         Example 2                                                          */
		/******************************************************************************/
		System.out.println("Example 2");
		System.out.println("---------");
		
		cursor = new xxl.core.cursors.sources.Enumerator(numberOfElementsPerCursor);
		cursor.open();
		
		tee = new TeeCursor<Integer>(cursor, storageArea, false); 
		
		cursors = new ArrayList<Cursor<Integer>>(numberOfCursors);
		fullyConsumed = new boolean[numberOfCursors];
		
		cursors.add(tee);
		cursors.get(0).open();
		fullyConsumed[0] = false;
		
		for (int i = 1; i < numberOfCursors; i++) {
			cursors.add(tee.cursor());
			cursors.get(i).open();
			fullyConsumed[i] = false;
		}
		
		random = new java.util.Random();
		
		numberOfFullyConsumedCursors = 0;
		totalNumberOfElements = 0;
		cursorNumber = 0;
		int step = 1;

		while (numberOfFullyConsumedCursors < numberOfCursors) {
			/*if (step == 5) {
				c2 = tee.cursor();
				if (xxl.core.cursors.Cursors.count(c2) != numberOfElementsPerCursor)
					throw new RuntimeException("Intermediate cursor was not succesful");
			}*/
			for (int i = 0; i < step; i++) {
				if (!fullyConsumed[cursorNumber]) {
					if (cursors.get(cursorNumber).hasNext()) {
						Object element = cursors.get(cursorNumber).next();
						if (verbose)
							System.out.println("Cursor number: " + cursorNumber + ", Element: " + element);
						totalNumberOfElements++;
					}
					else {
						fullyConsumed[cursorNumber] = true;
						System.out.println("Cursor " + cursorNumber + " has finished its work");
						numberOfFullyConsumedCursors++;
					}
				}
			}
			step++;
			cursorNumber = (cursorNumber+1)%numberOfCursors;
		}
		
		for (int i = 0; i < numberOfCursors; i++)
			cursors.get(i).close();
		
		cursor.close();
		
		if (totalNumberOfElements != numberOfElementsPerCursor * numberOfCursors)
			throw new RuntimeException("Not all elements were delivered");
		
		/*********************************************************************/
		/*                            Example 3                              */
		/*********************************************************************/
		String filename = 
			XXLSystem.getOutPath(new String[] {"output", "core"}) + File.separator + "TeeCursor.dat";
		
		TeeCursor<Integer> teeCursor = new TeeCursor<Integer>(
			new xxl.core.cursors.sources.Enumerator(100),
			new File(filename),
			xxl.core.io.converters.IntegerConverter.DEFAULT_INSTANCE,
			new LRUBuffer<Object, Object, Integer>(128), 
			1024
		);
		
		teeCursor.open();
		Cursor<Integer> tempCursor1 = teeCursor.cursor();
		tempCursor1.open();

		System.out.println("Consuming input iteration: ");
		while (teeCursor.hasNext())
			System.out.println(teeCursor.next());

		System.out.println("Consuming the first temporal generated cursor: ");
		while (tempCursor1.hasNext())
			System.out.println(tempCursor1.next());

		tempCursor1.close();
		Cursor<Integer> tempCursor2 = teeCursor.cursor();
		tempCursor2.open();
		
		System.out.println("Consuming the second temporal generated cursor: ");
		while (tempCursor2.hasNext())
			System.out.println(tempCursor2.next());
			
		tempCursor2.close();
		teeCursor.close();

		/*********************************************************************/
		/*                            Example 4                              */
		/*********************************************************************/
		
		teeCursor = new TeeCursor<Integer>(
			new xxl.core.cursors.sources.Enumerator(100),
			new File(filename),
			xxl.core.io.converters.IntegerConverter.DEFAULT_INSTANCE,
			new LRUBuffer<Object, Object, Integer>(128),
			1024
		);
		
		teeCursor.open();
		Cursor<Integer> tempCursor = teeCursor.cursor();
		tempCursor.open();

		System.out.println("An alternating consumption of one element of the input iteration and one element of the temporal generated cursor: ");
		while (teeCursor.hasNext() || tempCursor.hasNext()) {
			if (teeCursor.hasNext())
				System.out.println("RIGHT : " + teeCursor.next());
			if (tempCursor.hasNext())
				System.out.println("LEFT  : " + tempCursor.next());
		}

		tempCursor.close();
		teeCursor.close();

		System.out.println("Test finished sucessfully");
	}

}
