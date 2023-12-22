package xxl.tests.collections.containers.recordManager;

import java.util.Iterator;
import java.util.Map;

import xxl.core.collections.MapEntry;
import xxl.core.collections.containers.Container;
import xxl.core.collections.containers.MapContainer;
import xxl.core.collections.containers.recordManager.FirstFitStrategy;
import xxl.core.collections.containers.recordManager.IdentityTIdManager;
import xxl.core.collections.containers.recordManager.MapTIdManager;
import xxl.core.collections.containers.recordManager.RecordManager;
import xxl.core.collections.containers.recordManager.TIdManager;
import xxl.core.cursors.Cursor;
import xxl.core.io.Block;
import xxl.core.io.ByteArrayConversions;
import xxl.core.io.converters.LongConverter;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class RecordManager.
 */
public class TestRecordManager {

	private static void myInsert(Container rm, int value) {
		byte[]ba = new byte[4];
		ByteArrayConversions.convIntToByteArrayLE(value, ba);
		rm.insert(new Block(ba));
	}

	private static void outputObjects(Container rm) {
		boolean numbers[] = new boolean[100]; 

		System.out.println("All Objects in the RecordManager");
		Iterator it = rm.ids();
		int count=0;
		while (it.hasNext()) {
			Object id = it.next();
			Block b = (Block) rm.get(id);
			int value = ByteArrayConversions.convIntLE(b.array);
			System.out.println(value);
			if (numbers[value])
				throw new RuntimeException("Double number found ("+value+")");
			numbers[value] = true;
			count++;
		}
		System.out.println("Number of objects: "+count);
	}
	
	public static void main(String args[]) {
		// 0: Testing id-cursor, 1: Testing objects-cursor remove
		// 2: Testing objects-cursor update
		int testCase=4;
		int tidManager=0;
		final int maxNumber=19;

		TIdManager tidm=null;
		if (tidManager==0)
			tidm = new IdentityTIdManager(LongConverter.DEFAULT_INSTANCE);
		else
			tidm = new MapTIdManager(LongConverter.DEFAULT_INSTANCE);
		
		RecordManager rm = new RecordManager(
			new MapContainer(),
			32,
			new FirstFitStrategy(),
			tidm,
			0
		);
		for (int i=0; i<=maxNumber; i++)
			myInsert(rm,i);
		
		switch (testCase) {
			case 0: {
				System.out.println("Testing remove of id-Cursor");
				Iterator it = rm.ids();
				while (it.hasNext()) {
					Object id = it.next();
					Block b = (Block) rm.get(id);
					int value = ByteArrayConversions.convIntLE(b.array);
					
					if (value%2==0)
						it.remove();
				}
				break;
			}
			case 1: {
				System.out.println("Testing remove of objects-Cursor");
				Iterator it = rm.objects();
				while (it.hasNext()) {
					Block b = (Block) it.next();
					int value = ByteArrayConversions.convIntLE(b.array);
					
					if (value%2==0)
						it.remove();
				}
				break;
			}
			case 2: {
				System.out.println("Testing update of objects-Cursor");
				Cursor c= rm.objects();
				c.open();
				while (c.hasNext()) {
					Block b = (Block) c.next();
					int value = ByteArrayConversions.convIntLE(b.array);
					
					byte[]ba = new byte[4];
					ByteArrayConversions.convIntToByteArrayLE(maxNumber-value, ba);
					c.update(new Block(ba));
				}
				c.close();
				break;
			}
			case 3: {
				System.out.println("Testing remove of entries-Cursor");
				Iterator it = rm.entries();
				while (it.hasNext()) {
					Map.Entry me = (Map.Entry) it.next();
					System.out.println("Object: "+me);
					Block b = (Block) me.getValue();
					int value = ByteArrayConversions.convIntLE(b.array);
					
					if (value%2==0) {
						System.out.println("Remove last Object");
						it.remove();
					}
				}
				break;
			}
			case 4: {
				System.out.println("Testing update of entries-Cursor");
				Cursor c= rm.entries();
				c.open();
				while (c.hasNext()) {
					Map.Entry me = (Map.Entry) c.next(); 
					System.out.println("Object: "+me);
					Block b = (Block) me.getValue();
					int value = ByteArrayConversions.convIntLE(b.array);
					
					byte[]ba = new byte[4];
					ByteArrayConversions.convIntToByteArrayLE(maxNumber-value, ba);
					c.update(new MapEntry(me.getKey(),new Block(ba)));
				}
				c.close();
				break;
			}
		}
		outputObjects(rm);
	}

}
