/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.applications.xml.storage;


import xxl.applications.xml.Common;
import xxl.core.collections.containers.Container;
import xxl.core.collections.containers.MapContainer;
import xxl.core.collections.containers.io.BlockFileContainer;
import xxl.core.collections.containers.io.ConverterContainer;
import xxl.core.collections.containers.recordManager.FirstFitStrategy;
import xxl.core.collections.containers.recordManager.IdentityTIdManager;
import xxl.core.collections.containers.recordManager.RecordManager;
import xxl.core.collections.containers.recordManager.TId;
import xxl.core.io.InputStreams;
import xxl.core.io.converters.LongConverter;
import xxl.core.io.converters.SizeConverter;
import xxl.core.util.WrappingRuntimeException;
import xxl.core.xml.storage.Node;

/**
 * Performs a simple XML storage test. 
 * A sample tree is written to the record manager and an 
 * underlying BlockFileContainer. For the record manager,
 * the FirstFitStrategy is used. Then, the record is 
 * read again and the main memory representation is rebuild.
 * This representation is written to a file (othello2.xml)
 * and compared with the original tree (othello.xml).
 */
public class SimpleXMLStorageTest {
	
	private static int mode=1;
	private static int pageSize=8192;

	/**
	 * The main method starts the application.
	 * @param args The command line options are ignored here.
	 */
	public static void main(String[] args) {
		boolean equal = false;
		
		try {
			SizeConverter converter = Node.getSubtreeConverter(
				TId.getConverter(LongConverter.DEFAULT_INSTANCE)
			);
			Container container = null;
			
			if (mode==0)
				container = new MapContainer(false);
			else if (mode==1)
				container = 
					new ConverterContainer(
						new RecordManager(
							new BlockFileContainer(
								Common.getXMLOutPath()+"xmlContainer", 
								pageSize
							),
							pageSize,
							new FirstFitStrategy(), 
							new IdentityTIdManager(LongConverter.DEFAULT_INSTANCE),
							0),
						converter
					);
			
			Node root = Common.getSampleTree();

			Common.writeTree(root,Common.getXMLOutPath()+"othello.bin", converter);
			
			int size = converter.getSerializedSize(root);
			System.out.println("Size of the tree: "+size);

			// insert record
			Object id = container.insert(root);
			// query record
			Node restoredRoot = (Node) container.get(id);
			container.close();
			
			Common.writeTree(restoredRoot,Common.getXMLOutPath()+"othello2.bin", converter);
			
			equal = InputStreams.compareFiles(Common.getXMLOutPath()+"othello.bin",Common.getXMLOutPath()+"othello2.bin");
		}
		catch (Exception e) {
			throw new WrappingRuntimeException(e);
		}

		if (equal)
			System.out.println("SimpleXMLStorageTest was successful");
		else
			throw new RuntimeException("SimpleXMLStorageTest ERROR: Files are not equal!");
	}
}
