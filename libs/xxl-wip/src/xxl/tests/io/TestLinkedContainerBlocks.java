package xxl.tests.io;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import xxl.core.collections.MapEntry;
import xxl.core.collections.containers.Container;
import xxl.core.collections.containers.io.BlockFileContainer;
import xxl.core.cursors.Cursor;
import xxl.core.cursors.Cursors;
import xxl.core.io.LinkedContainerBlocks;
import xxl.core.io.converters.Converter;
import xxl.core.io.converters.IntegerConverter;
import xxl.core.io.converters.MultiConverter;
import xxl.core.io.converters.StringConverter;
import xxl.core.util.XXLSystem;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class LinkedContainerBlocks.
 */
public class TestLinkedContainerBlocks {

	/**
	 * Outputs the map to System.out.
	 * @param map the map to be outputed.
	 */
	private static void outputMap(Map map) {
		Iterator it = map.entrySet().iterator();
		
		while (it.hasNext()) {
			java.util.Map.Entry me = (java.util.Map.Entry) it.next();
			System.out.println(me.getKey()+"\t"+me.getValue());
		}
	}
	
	/**
	 * Tests this class with a map, which is stored inside blocks with 64 bytes each.
	 * @param args Command line arguments are ignored here.
	 */
	public static void main(String args[]) {
		
		Map map = new HashMap();
		Map map2 = new HashMap();
		
		map.put(new Integer(0), "00ABCDEDGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz");
		map.put(new Integer(1), "01ABCDEDGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz");
		map.put(new Integer(2), "02ABCDEDGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz");
		map.put(new Integer(3), "03ABCDEDGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz");
		map.put(new Integer(4), "04ABCDEDGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz");
		map.put(new Integer(5), "05ABCDEDGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz");
		map.put(new Integer(6), "06ABCDEDGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz");
		map.put(new Integer(7), "07ABCDEDGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz");
		map.put(new Integer(8), "08ABCDEDGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz");
		map.put(new Integer(9), "09ABCDEDGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz");
		map.put(new Integer(10),"10ABCDEDGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz");
		
		int blockSize = 64;
		
		Container container; //  = new MapContainer();
		container = new BlockFileContainer(
			XXLSystem.getOutPath(new String[]{"output","core"})+File.separator+"maptest",
			blockSize
		);
		
		int headerSize = 5 + container.getIdSize();
		// 4 Bytes: real length of the block (int), 1 Byte: has link 
		// rest: for linking Blocks
		
		Converter mapEntryConverter = new MultiConverter(
			xxl.core.collections.MapEntry.FACTORY_METHOD,
			xxl.core.collections.MapEntry.TO_OBJECT_ARRAY_FUNCTION,
			IntegerConverter.DEFAULT_INSTANCE,
			StringConverter.DEFAULT_INSTANCE
		);
		
		Object pageId;
		Cursor cursor;
		Iterator it;
		int count1, count2, count3;
		
		System.out.println("Map before");
		outputMap(map);
		
		System.out.println("Write to container");
		it = map.entrySet().iterator();
		pageId = LinkedContainerBlocks.writeObjectsToLinkedContainerBlocks(container, mapEntryConverter, it, null, blockSize, headerSize);
		
		count1 = container.size();
		System.out.println("Number of pages (inside the container): "+count1);
		
		cursor = LinkedContainerBlocks.readObjectsFromLinkedContainerBlocks(container, mapEntryConverter, pageId, blockSize, headerSize);
		
		// now, it is a cursor of MapEntry objects
		while (cursor.hasNext()) {
			MapEntry me = (MapEntry) cursor.next();
			map2.put(me.getKey(),me.getValue());
		}
		
		System.out.println("Number of pages (inside the container): "+container.size());
		
		System.out.println("Reconstructed map");
		outputMap(map2);
		
		if (!map.equals(map2))
			throw new RuntimeException("Maps are not identical");
		
		System.out.println("Append some elements");
		map.put(new Integer(11),"11ABCDEDGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz");
		map.put(new Integer(12),"12ABCDEDGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz");
		map.put(new Integer(13),"13ABCDEDGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz");
		map.put(new Integer(14),"14ABCDEDGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz");
		
		System.out.println("Write to container");
		it = map.entrySet().iterator();
		pageId = LinkedContainerBlocks.writeObjectsToLinkedContainerBlocks(container, mapEntryConverter, it, pageId, blockSize, headerSize);
		count2 = container.size();
		System.out.println("Number of pages (inside the container): "+count2);
		
		System.out.println("Read from container");
		cursor = LinkedContainerBlocks.readObjectsFromLinkedContainerBlocks(container, mapEntryConverter, pageId, blockSize, headerSize);
		Cursors.last(cursor);
		
		System.out.println("Delete some elements");
		map.remove(new Integer(1));
		map.remove(new Integer(3));
		map.remove(new Integer(5));
		map.remove(new Integer(7));
		map.remove(new Integer(9));
		map.remove(new Integer(11));
		map.remove(new Integer(13));
		
		System.out.println("Write to container");
		it = map.entrySet().iterator();
		pageId = LinkedContainerBlocks.writeObjectsToLinkedContainerBlocks(container, mapEntryConverter, it, pageId, blockSize, headerSize);
		count3 = container.size();
		System.out.println("Number of pages (inside the container): "+count3);

		System.out.println("Read from container");
		cursor = LinkedContainerBlocks.readObjectsFromLinkedContainerBlocks(container, mapEntryConverter, pageId, blockSize, headerSize);
		Cursors.last(cursor);
		
		System.out.println("Remove all pages from container");
		LinkedContainerBlocks.removeLinkedBlocks(container, pageId, true);
		
		if (container.size()!=0)
			throw new RuntimeException("Test failed!");
		
		if (count2<=count1)
			throw new RuntimeException("Not enough pages inside the container after the second write command!");
		
		if (count3>=count2)
			throw new RuntimeException("To much pages inside the container after the third write command!");
		
		if (container instanceof BlockFileContainer)
			((BlockFileContainer)container).delete();
		
		System.out.println("Test finished successfully");
	}

}
