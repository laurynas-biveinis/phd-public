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

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import xxl.applications.xml.Common;
import xxl.core.collections.containers.Container;
import xxl.core.collections.containers.MapContainer;
import xxl.core.collections.containers.io.BlockFileContainer;
import xxl.core.collections.containers.io.BufferedContainer;
import xxl.core.collections.containers.io.ConverterContainer;
import xxl.core.collections.containers.recordManager.FirstFitStrategy;
import xxl.core.collections.containers.recordManager.IdentityTIdManager;
import xxl.core.collections.containers.recordManager.RecordManager;
import xxl.core.collections.containers.recordManager.TId;
import xxl.core.cursors.Cursor;
import xxl.core.cursors.Cursors;
import xxl.core.functions.Function;
import xxl.core.io.Block;
import xxl.core.io.LRUBuffer;
import xxl.core.io.converters.Converter;
import xxl.core.io.converters.ZipConverter;
import xxl.core.relational.metaData.ColumnMetaDataResultSetMetaData;
import xxl.core.relational.metaData.StoredColumnMetaData;
import xxl.core.util.WrappingRuntimeException;
import xxl.core.util.reflect.Logger;
import xxl.core.util.reflect.TestFramework;
import xxl.core.xml.storage.BulkLoadingHandler;
import xxl.core.xml.storage.EXTree;
import xxl.core.xml.storage.Node;
import xxl.core.xml.storage.SAXHandlers;
import xxl.core.xml.storage.SimpleSplit;
import xxl.core.xml.storage.SubtreeConverter;
import xxl.core.xml.util.XPathCalculatorHandler;
import xxl.core.xml.util.XPathLocation;

/**
 * A test class for the nativ XML storage class EXTree using SAX to parse the xml file.
 */
public class EXTreeExampleSAX {

	/** 
	 * Returns a mapping function which writes a "." after 128 calls to invoke. 
	 * @param f Function to be mapped.
	 * @return Function Return function.
	 */
	public static Function<Object,Object> getDecoratorFunction(final Function f) {
		return new Function<Object,Object>() {
			int count=0;
			public Object invoke(List<? extends Object> list) {
				count++;
				if ((count & 127) == 0)
					System.out.print(".");
				return f.invoke(list);
			}
		};
	}

	public static final String containerModeDescription = "0: MapContainer, 1: RecordManager";
	public static int containerMode=1;
	public static final int containerModeMin=0;
	public static final int containerModeMax=1;

	public static final String bulkInsertionDescription = "Use bulk insertion instead of tuple insertion";
	public static boolean bulkInsertion = true;

	public static final String pageSizeDescription = "Size of a page in bytes";
	public static int pageSize = 512;
	public static final int pageSizeValues[] = new int[]{512, 1024, 2048, 4096, 8192};

	public static final String countDescription = "Number of iterations of the outer slope"; 
	public static int count = 1;

	public static final String objectBufferSizeDescription = "Size of the object buffer in number of subtrees"; 
	public static int objectBufferSize = 100;
	public static final int objectBufferSizeValues[] = new int[]{0, 50, 100, 150, 200};

	public static final String blockBufferSizeDescription = "Size of the block buffer in number of Blocks"; 
	public static int blockBufferSize = 0;
	public static final int blockBufferSizeValues[] = new int[]{0, 50, 100, 150, 200};

	public static final String numberOfQueriesDescription = "Number of XPath queries (always the same!)"; 
	public static int numberOfQueries = 10;

	public static final String xPathQueryStringDescription = "XPath query which is performed"; 
	public static String xPathQueryString = "/PLAY/ACT";
	// public static String xPathQueryString = "//SPEECH";
	// public static String xPathQueryString = "//ACT[4]//SCENE[3]//SPEECH";
	// public static String xPathQueryString = "/PLAY/ACT/SCENE/SPEECH";

	public static final String filenameDescription = "Name of the file used for insertion (without .xml)";
	public static String filename = "com_err";

	public static final boolean performContainerLogging = false;
	public static final boolean performCompression = false;

	public static final int treeQualityStepwide = 100;

	public static final boolean completeDeletion = true;

	// private static final String filename = "othello";
	// private static final String removePaths[] = new String[] {
	// 		"/SPEECH[1]/LINE[2]" };
	private static final String removePaths[] = new String[] {
		"/PLAY[1]/ACT[1]/SCENE[1]/SPEECH[2]",
		"/PLAY[1]/ACT[2]",	// the original ACT[3] is now ACT[2]!!!
		"/PLAY[1]/ACT[2]",
		"/PLAY[1]/ACT[2]",
		"/PLAY[1]/ACT[2]",
	};

	public static ResultSetMetaData getReturnRSMD() {
		return new ColumnMetaDataResultSetMetaData(
			new StoredColumnMetaData(false, false, true, false, ResultSetMetaData.columnNullable, true, 15, "TimeForXPathHandlerOnly", "TimeForXPathHandlerOnly", "", 15, 0, "", "", Types.DOUBLE, true, false, false),
			new StoredColumnMetaData(false, false, true, false, ResultSetMetaData.columnNullable, true, 15, "TimeForInsertion", "TimeForInsertion", "", 15, 0, "", "", Types.DOUBLE, true, false, false),
			new StoredColumnMetaData(false, false, true, false, ResultSetMetaData.columnNullable, true, 9, "NumberOfNodesAfterInsertion", "NumberOfNodesAfterInsertion", "", 9, 0, "", "", Types.INTEGER, true, false, false),
			new StoredColumnMetaData(false, false, true, false, ResultSetMetaData.columnNullable, true, 9, "NumberOfPagesOfTheContainer", "NumberOfPagesOfTheContainer", "", 9, 0, "", "", Types.INTEGER, true, false, false),
			new StoredColumnMetaData(false, false, true, false, ResultSetMetaData.columnNullable, true, 15, "TimeForQueries", "TimeForQueries", "", 15, 0, "", "", Types.DOUBLE, true, false, false),
			new StoredColumnMetaData(false, false, true, false, ResultSetMetaData.columnNullable, true, 9, "NumberOfResults", "NumberOfResults", "", 9, 0, "", "", Types.INTEGER, true, false, false),
			new StoredColumnMetaData(false, false, true, false, ResultSetMetaData.columnNullable, true, 15, "TimeForRemove", "TimeForRemove", "", 15, 0, "", "", Types.DOUBLE, true, false, false),
			new StoredColumnMetaData(false, false, true, false, ResultSetMetaData.columnNullable, true, 9, "NumberOfNodesAfterRemove", "NumberOfNodesAfterRemove", "", 9, 0, "", "", Types.INTEGER, true, false, false)
		);
	}

	public static Iterator getTestValues(String fieldName) {
		return null;
	}

	public static DefaultHandler getTreeDeletionHandler(final EXTree tree) {
		return new XPathCalculatorHandler() {
			public void endElement(java.lang.String uri, java.lang.String localName, java.lang.String qName) throws SAXException {
				XPathLocation xpe = (XPathLocation) currentXPathLocation.clone();
				xpe.setMarkupCountTo1();
				if (!tree.removeMarkup(xpe))
					throw new RuntimeException("Error removing: "+xpe);
				super.endElement(uri, localName, qName);
			}
		};
	}
// Default SAXDecoratorHandler
/*	int i=0;
	i++;
	if (treeQualityStepwide>0)
		if (i%treeQualityStepwide==0)
			System.out.println("Test of the TreeQuality "+tree.checkTreeQuality());
*/
	public static void performQueries(EXTree tree) {
		long t1,t2;
		int numberOfResults=-1;
		System.out.println();
		
		System.out.println("XPath Query "+xPathQueryString);

		Cursor cursor = tree.query(new XPathLocation(xPathQueryString));
		while (cursor.hasNext())
			System.out.println(cursor.next());

		System.out.println("  Perform the query "+numberOfQueries+" times");

		t1 = System.currentTimeMillis();
		for (int i=0; i<numberOfQueries; i++)
			numberOfResults = Cursors.count(tree.query(new XPathLocation(xPathQueryString)));
		t2 = System.currentTimeMillis();
		
		TestFramework.list.add(new Long(t2-t1));
		TestFramework.list.add(new Integer(numberOfResults));
		System.out.println("  Required time: "+(t2-t1)+"ms");
	}

	/**
	 * Example using the EXTree.
	 */
	public static void main(String[] args) {

		if (!TestFramework.processParameters("EXTreeExampleSAX\n", EXTreeExampleSAX.class, args, System.out))
			return;

		Container container=null;
		Container mainContainer=null;
		SubtreeConverter subtreeConverter=null;
		Converter containerConverter=null;
		int maxObjectSize = pageSize;
		
		if (containerMode==0) {
			container = new MapContainer(false);
			mainContainer = container;

			subtreeConverter = Node.getSubtreeConverter(container.objectIdConverter());
			
			if (performCompression)
				containerConverter = new ZipConverter(subtreeConverter);
		}
		else if (containerMode==1) {
			container = new BlockFileContainer(
				Common.getXMLOutPath()+"xmlContainer", 
				pageSize
			);
			// container = new MapContainer(true);
			
			if (blockBufferSize>0)
				container = new BufferedContainer(container, new LRUBuffer(blockBufferSize), true, true);

			RecordManager rm = 
				new RecordManager(
					container,
					pageSize,
					new FirstFitStrategy(), 
					new IdentityTIdManager(container.objectIdConverter()),
					0
				);
			
			maxObjectSize = rm.getMaxObjectSize();
			System.out.println("Maximum Size of Blocks inside the container: "+maxObjectSize);
			System.out.println("Insert and remove test block");
			
			Object id = rm.insert(new Block(maxObjectSize));
			rm.remove(id);
			
			mainContainer = rm;
			container = rm;

			subtreeConverter = Node.getSubtreeConverter(container.objectIdConverter());
			
			if (performCompression)
				containerConverter = new ZipConverter(subtreeConverter);
			else
				containerConverter = subtreeConverter;
		}
		
		if (containerConverter!=null)
			container = new ConverterContainer(container,containerConverter);
		else
			containerConverter = subtreeConverter;
		
		// container = new VerificationContainer(container,"");

		if (objectBufferSize>0)
			container = new BufferedContainer(container, new LRUBuffer(objectBufferSize), true, true);

		if (performContainerLogging)
			container = Logger.getContainerLogger(container, System.out);

		final EXTree tree = new EXTree (
			container, maxObjectSize, new SimpleSplit(container, subtreeConverter, maxObjectSize),subtreeConverter
		);

		SAXParserFactory spf = SAXParserFactory.newInstance();
		System.out.println("Parsing the document with SAX ...... ");
		FileOutputStream fos;
		
		try {
			while (count-->0) {
				long t1, t2;
				
				/////////////////////////////////////////////////////////////
				///// TESTS
				/////////////////////////////////////////////////////////////
				System.out.println();
				System.out.println("Testing XPathHandler");
				t1 = System.currentTimeMillis();
				spf.newSAXParser().parse(
					new java.io.File(Common.getXMLDataPath()+filename+".xml"),
					new XPathCalculatorHandler()
				);
				t2 = System.currentTimeMillis();
				
				TestFramework.list.add(new Long(t2-t1));
				System.out.println("  Required time: "+(t2-t1)+"ms");
				
				System.out.println("Number of Blocks inside the container: "+container.size());
				System.out.println("Main container infos:");
				System.out.println(mainContainer);
				
				if (!bulkInsertion) {
					/////////////////////////////////////////////////////////////
					///// TUPLE INSERTION
					/////////////////////////////////////////////////////////////
					System.out.println();
					System.out.println("Starting tuple insertion (this will take some time...)");
					t1 = System.currentTimeMillis();
					spf.newSAXParser().parse(
						new java.io.File(Common.getXMLDataPath()+filename+".xml"),
						SAXHandlers.getTreeInsertionHandler(tree)
					);
					t2 = System.currentTimeMillis();
					
					TestFramework.list.add(new Long(t2-t1));
					TestFramework.list.add(new Long(tree.numberOfNodes));
					TestFramework.list.add(new Long(container.size()));
					
					System.out.println("  Required time: "+(t2-t1)+"ms");
					System.out.println("  Number of inserted nodes: "+ tree.numberOfNodes);
	
					System.out.println("  Number of Blocks inside the container: "+container.size());
					System.out.println("Main container infos:");
					System.out.println(mainContainer);
		
					if (mainContainer instanceof RecordManager) {
						System.out.print("  Checking consistency of the RecordManger...");
						((RecordManager)mainContainer).checkConsistency();
						System.out.println("done");
					}
						
					System.out.println("  Writing tree to a xml-file (with proxy and scaffold nodes)");
					fos = new FileOutputStream(Common.getXMLOutPath()+"my"+filename+pageSize+".xml");
					tree.toXML(fos,true);
					fos.close();
					
					System.out.println("  Writing tree to a xml-file (without proxy and scaffold nodes)");
					fos = new FileOutputStream(Common.getXMLOutPath()+"treeinsert_"+filename+pageSize+".xml");
					tree.toXML(fos,false);
					fos.close();
					
					System.out.println("  Writing structure of tree to a xml-file");
					fos = new FileOutputStream(Common.getXMLOutPath()+"structure"+filename+pageSize+".xml");
					tree.treeTraversal(EXTree.getStructureOutputPredicate(new PrintStream(fos)));
					fos.close();
					
					System.out.println("  Checking tree quality");
					System.out.println(tree.checkTreeQuality());
					
					performQueries(tree);
					
					/////////////////////////////////////////////////////////////
					///// DELETION
					/////////////////////////////////////////////////////////////
					System.out.println();
					if (completeDeletion) {
						System.out.println("Starting deletion of all tags (this will take some time...)");
						t1 = System.currentTimeMillis();
						spf.newSAXParser().parse(
							new java.io.File(Common.getXMLDataPath()+filename+".xml"),
							getTreeDeletionHandler(tree)
						);
						t2 = System.currentTimeMillis();
						
						TestFramework.list.add(new Long(t2-t1));
						TestFramework.list.add(new Long(tree.numberOfNodes));
						
						System.out.println("  Required time: "+(t2-t1)+"ms");
						System.out.println("  Number of inserted nodes: "+ tree.numberOfNodes);
					}
					else {
						// delete a tag
						System.out.println("Delete tags");			
						for (int i=0; i<removePaths.length; i++) {
							System.out.println("  Delete: "+removePaths[i]);
							if (!tree.removeMarkup(new XPathLocation(removePaths[i])))
								throw new RuntimeException("XPath not found");
						}

						TestFramework.list.add(new Long(-1));
						TestFramework.list.add(new Long(tree.numberOfNodes));
					}
	
					System.out.println("  Number of Blocks inside the container: "+container.size());
					System.out.println("Main container infos:");
					System.out.println(mainContainer);

					if (mainContainer instanceof RecordManager) {
						System.out.print("  Checking consistency of the RecordManger...");
						((RecordManager)mainContainer).checkConsistency();
						System.out.println("done");
					}
	
					System.out.println("  Writing tree to a xml-file");
					fos = new FileOutputStream(Common.getXMLOutPath()+"TagRemoved"+filename+pageSize+".xml");
					tree.toXML(fos,true);
					fos.close();
				}
				else {
					/////////////////////////////////////////////////////////////
					///// BULK INSERTION
					/////////////////////////////////////////////////////////////
					System.out.println();
					System.out.println("Starting bulk insertion");
					t1 = System.currentTimeMillis();
					spf.newSAXParser().parse(
						new java.io.File(Common.getXMLDataPath()+filename+".xml"),
						new BulkLoadingHandler(tree, new TId(new Long(0),(short) 0))
					);
					t2 = System.currentTimeMillis();

					TestFramework.list.add(new Long(t2-t1));
					TestFramework.list.add(new Long(tree.numberOfNodes));
					TestFramework.list.add(new Long(container.size()));
					
					System.out.println("  Required time: "+(t2-t1)+"ms");
					System.out.println("  Number of inserted nodes: "+ tree.numberOfNodes);
	
					System.out.println("  Number of Blocks inside the container: "+container.size());
					System.out.println("Main container infos:");
					System.out.println(mainContainer);
					
					if (mainContainer instanceof RecordManager) {
						System.out.print("  Checking consistency of the RecordManger...");
						((RecordManager)mainContainer).checkConsistency();
						System.out.println(" done");
					}
					
					System.out.println("  Number of Blocks inside the container: "+container.size());
					System.out.println("Main container infos:");
					System.out.println(mainContainer);
	
					System.out.println("  Checking tree quality");
					System.out.println(tree.checkTreeQuality());
	
					System.out.println("  Writing tree to a xml-file (without proxy and scaffold nodes)");
					fos = new FileOutputStream(Common.getXMLOutPath()+"treebulk_"+filename+pageSize+".xml");
					tree.toXML(fos,false);
					fos.close();

					System.out.println("  Writing structure of tree to a xml-file");
					fos = new FileOutputStream(Common.getXMLOutPath()+"bulkstructure"+filename+pageSize+".xml");
					tree.treeTraversal(EXTree.getStructureOutputPredicate(new PrintStream(fos)));
					fos.close();

					performQueries(tree);
					
					/////////////////////////////////////////////////////////////
					///// DELETION, this time more efficient
					/////////////////////////////////////////////////////////////
					System.out.println();
					System.out.println("Delete the whole tree");
					t1 = System.currentTimeMillis();
					tree.setRootId(null);
					container.clear();
					t2 = System.currentTimeMillis();

					TestFramework.list.add(new Long(t2-t1));
					TestFramework.list.add(new Long(tree.numberOfNodes));

					System.out.println("  Required time: "+(t2-t1)+"ms");

					System.out.println("  Number of Blocks inside the container: "+container.size());
					System.out.println("Main container infos:");
					System.out.println(mainContainer);
				}
			}
		}
		catch (Exception e) {
			try {
				System.out.println("There was an error!!!");
				System.out.println("Number of nodes in the tree: "+tree.numberOfNodes);
				System.out.println("Writing tree to XML file to core.xml ...");
				
				fos = new FileOutputStream(Common.getXMLOutPath()+"core.xml");
				tree.toXML(fos,true);
				fos.close();
				
				Thread.sleep(100);
			}
			catch (Exception e2) {}
			throw new WrappingRuntimeException(e);
		}
		container.close();
	}
}
