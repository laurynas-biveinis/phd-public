/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2007 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

All rights reserved.
*/

package xxl.applications.xml;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import xxl.core.cursors.MetaDataCursor;
import xxl.core.io.converters.SizeConverter;
import xxl.core.relational.metaData.ResultSetMetaDatas;
import xxl.core.relational.tuples.ArrayTuple;
import xxl.core.util.WrappingRuntimeException;
import xxl.core.util.XXLSystem;
import xxl.core.xml.relational.sax.Sax;
import xxl.core.xml.storage.LiteralNode;
import xxl.core.xml.storage.MarkupNode;
import xxl.core.xml.storage.Node;

/**
 * Class that contains common functionality for the XML examples.
 */
public class Common {
	/**
	 * This class is not instanciable
	 */
	private Common () {
	}

	/** 
	 * Constructs the output directory and returns the path. The returned path contains a file separator 
	 * at the end.
	 *
	 * @return String - the XMLOutPath
	 */
	public static String getXMLOutPath() {
		String path = XXLSystem.getOutPath() + System.getProperty("file.separator") +
			"output" + System.getProperty("file.separator") + 
			"applications" + System.getProperty("file.separator") + 
			"xml";
		File f = new File(path);
		f.mkdirs();
		return path + System.getProperty("file.separator");
	}
	
	/** 
	 * Returns the XMLDataPath. The returned path contains a file separator 
	 * at the end.
	 *
	 * @return String - the XMLDataPath
	 */
	public static String getXMLDataPath() {
		return XXLSystem.getRootPath() + System.getProperty("file.separator") + 
			"data" + System.getProperty("file.separator") +
			"xml" + System.getProperty("file.separator");
	}
	
	/** 
	 * Returns the relational data path. The returned path contains a file separator 
	 * at the end.
	 *
	 * @return String - the relational data path
	 */
	public static String getRelationalDataPath() {
		return XXLSystem.getRootPath() + System.getProperty("file.separator") + 
			"data" + System.getProperty("file.separator") +
			"relational" + System.getProperty("file.separator");
	}
	
	/** 
	 * Returns a special XML-Source as MetaDataCursor. Not all of the sources
	 * are available with standard XXL packaging.
	 *
	 * @param nr Number of the source (0 to 6).
	 * @return The source as MetaDataCursor.
	 */
	public static MetaDataCursor getSource(int nr) {
		MetaDataCursor cursor=null;
		
		String path = getXMLDataPath();
		
		switch (nr) {
			case 0:
				cursor = new xxl.core.xml.relational.sax.XMLMetaDataCursor(
						Sax.InputStreamFactory(new File(path+"database.xml")),
						"/table/people/row",
						"/table/people/meta",
						null,
						Sax.DEFAULT_IDENTIFIER_MAP(),
						ArrayTuple.FACTORY_METHOD);
				break;
			case 1:
				cursor = 
					new xxl.core.xml.relational.sax.XMLMetaDataCursor(
						Sax.InputStreamFactory(new File(path+"database_meta_behind_data.xml")), 
						"/table/people/row",
						"/table/meta",
						null, 
						Sax.DEFAULT_IDENTIFIER_MAP(),
						ArrayTuple.FACTORY_METHOD);
				break;
			case 2:
				cursor = 
					new xxl.core.xml.relational.sax.XMLMetaDataCursor(
						Sax.InputStreamFactory(new File(path+"database_without_meta.xml")),
						"/table/people/row",
						null,
						null, 
						Sax.DEFAULT_IDENTIFIER_MAP(),
						ArrayTuple.FACTORY_METHOD);
				break;
			case 3:
				cursor = 
					new xxl.core.xml.relational.sax.XMLMetaDataCursor(
						Sax.InputStreamFactory(new File(path+"database.xml")),
						"/table/web/row",
						"/table/web/meta",
						null, 
						Sax.DEFAULT_IDENTIFIER_MAP(),
						ArrayTuple.FACTORY_METHOD);
				break;
			case 4:
				cursor = new xxl.core.xml.relational.sax.XMLMetaDataCursor(
						Sax.InputStreamFactory(new File(path+"people.xml")),
						"/table/people/row",
						"/table/people/meta",
						null,
						Sax.DEFAULT_IDENTIFIER_MAP(),
						ArrayTuple.FACTORY_METHOD);
				break;
			case 5:
				cursor = 
					new xxl.core.xml.relational.sax.XMLMetaDataCursor(
						Sax.InputStreamFactory(new File(path+"web.xml")),
						"/table/web/row",
						"/table/web/meta",
						null, 
						Sax.DEFAULT_IDENTIFIER_MAP(),
						ArrayTuple.FACTORY_METHOD);
				break;
			case 6: // Data not included within distribution!
				cursor = 
					new xxl.core.xml.relational.sax.XMLMetaDataCursor(
						Sax.InputStreamFactory(new File(path+"actors.xml")),
						"/table/actors/row",
						"/table/actors/meta",
						null,
						Sax.DEFAULT_IDENTIFIER_MAP(),
						ArrayTuple.FACTORY_METHOD);
				break;
		}
		
		return cursor;
	}

	/** 
	 * Outputs the specified cursor to System.out. 
	 * @param cursor The cursor which is gone through.
	 */
	public static void output(MetaDataCursor cursor) {
		while (cursor.hasNext())
			System.out.println(cursor.next());
	}

	/**
	 * Outputs the meta data of the specified cursor to System.out. 
	 * @param cursor The cursor of which the meta data is taken.
	 */
	public static void outputMetaData(MetaDataCursor cursor) {
		try {			
			ResultSetMetaData rsmd = ResultSetMetaDatas.getResultSetMetaData(cursor);

			for (int i=1;i<=rsmd.getColumnCount();i++) {
				System.out.println("column "+i);
				System.out.println("columnname= "+rsmd.getColumnName(i));
				System.out.println("typename= "+rsmd.getColumnTypeName(i));
				System.out.println("classname= "+rsmd.getColumnClassName(i));
				System.out.println("type= "+rsmd.getColumnType(i));
				System.out.println("precision= "+rsmd.getPrecision(i));
				System.out.println("scale= "+rsmd.getScale(i));
				System.out.println("isautoincrement= "+rsmd.isAutoIncrement(i));
				System.out.println("issigned= "+rsmd.isSigned(i));
				System.out.println("iscurrency= "+rsmd.isCurrency(i));
				System.out.println("isnullable= "+rsmd.isNullable(i));
				System.out.println();
			}
		}
		catch (SQLException e) {
			throw new WrappingRuntimeException(e);
		}
	}
	
	/** 
	 * Returns a small sample XML-tree in internal representation.
	 *
	 * @return Node - the tree. 
	 */
	public static Node getSampleTree() {
		MarkupNode speech = new MarkupNode();
		speech.setTagName("SPEECH");
		speech.setInternalId((byte) 1);
		
		MarkupNode speaker = new MarkupNode();
		speaker.setTagName("SPEAKER");
		speaker.setInternalId((byte) 2);
		speaker.setInternalParentId((byte) 1);
		
		LiteralNode lit1 = new LiteralNode(); //OTHELLO
		lit1.setContent("OTHELLO".getBytes());
		lit1.setInternalId((byte) 5);
		lit1.setInternalParentId((byte) 2);
		
		speaker.addChildNode(lit1);
		
		MarkupNode line1 = new MarkupNode();
		line1.setTagName("LINE");
		line1.setInternalId((byte) 3);
		line1.setInternalParentId((byte) 1);

		LiteralNode lit2 = new LiteralNode(); //Let me see your eyes;
		lit2.setContent("Let me see your eyes;".getBytes());
		lit2.setInternalId((byte) 6);
		lit2.setInternalParentId((byte) 3);
		
		line1.addChildNode(lit2);
		
		MarkupNode line2 = new MarkupNode();
		line2.setTagName("LINE");
		line2.setInternalId((byte) 4);
		line2.setInternalParentId((byte) 1);

		LiteralNode lit3 = new LiteralNode(); //Look in my face.
		lit3.setContent("Look in my face.".getBytes());
		lit3.setInternalId((byte) 7);
		lit3.setInternalParentId((byte) 4);

		line2.addChildNode(lit3);

		speech.addChildNode(speaker);
		speech.addChildNode(line1);
		speech.addChildNode(line2);
		
		return speech;
	}

	/**
	 * Writes the tree to a File.
	 * @param root The tree to be stored.
	 * @param filename Name of the file to which the tree is written.
	 * @param converter Converter which can serialize the tree.
	 * @throws IOException
	 */
	public static void writeTree(Node root, String filename, SizeConverter converter) throws IOException {
		// write to disk
		RandomAccessFile raf = new RandomAccessFile(new File(filename),"rw");
		converter.write(raf, root);
		raf.close();
	}

	/**
	 * Reads the tree from a File.
	 * @param filename Name of the file with a tree inside.
	 * @param converter Converter which can deserialize the tree.
	 * @return The root of the tree which was read.
	 * @throws IOException
	 */
	public static Node readTree(String filename, SizeConverter converter) throws IOException {
		RandomAccessFile raf =  new RandomAccessFile(new File(filename),"r");
		Node root = (Node) converter.read(raf, null);
		raf.close();
		return root;
	}
}
