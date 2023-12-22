/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2006 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307,
USA

	http://www.xxl-library.de

bugs, requests for enhancements: request@xxl-library.de

If you want to be informed on new versions of XXL you can
subscribe to our mailing-list. Send an email to

	xxl-request@lists.uni-marburg.de

without subject and the word "subscribe" in the message body.
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
