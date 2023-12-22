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

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import xxl.applications.xml.Common;
import xxl.core.io.InputStreams;
import xxl.core.io.converters.LongConverter;
import xxl.core.io.converters.SizeConverter;
import xxl.core.util.WrappingRuntimeException;
import xxl.core.xml.storage.Node;

/**
 * A test class for the serialization of simple nodes (Markup, Literal).
 * 
 * The example builds a small tree and writes it to disk.
 * Then, this file is reread and written a second time. To
 * check the implementation, the two files are compared.
 */
public class NodeTest{
	/**
	 * The main method starts the application.
	 * @param args The command line options are ignored here.
	 */
	public static void main(String[] args) {
		boolean equal=false;
		final String filename = "othello";
		
		try {
			Node root = Common.getSampleTree();

			FileOutputStream fos = new FileOutputStream(Common.getXMLOutPath()+filename+".xml");
			root.toXML(null, new PrintStream(fos), true, null);
			fos.close();

			SizeConverter converter = Node.getSubtreeConverter(LongConverter.DEFAULT_INSTANCE);
			// root.setParentId(new Long(1));
			
			System.out.println("Expected size of the tree: "+converter.getSerializedSize(root));
			new File(Common.getXMLOutPath()+filename+".bin").delete();
			Common.writeTree(root,Common.getXMLOutPath()+filename+".bin", converter);
			root = Common.readTree(Common.getXMLOutPath()+filename+".bin", converter);

			System.out.println("Clone the tree");
			Node newRoot = (Node) root.clone();
			
			fos = new FileOutputStream(Common.getXMLOutPath()+filename+"2.xml");
			newRoot.toXML(null, new PrintStream(fos), true, null);
			fos.close();
			// The two XML files are not equal, because the internal ids
			// are different (write renumbers internal identifyers).
			
			System.out.println("Expected size of the tree, which was reread: "+converter.getSerializedSize(root));
			new File(Common.getXMLOutPath()+filename+"2.bin").delete();
			Common.writeTree(newRoot,Common.getXMLOutPath()+filename+"2.bin", converter);
			
			equal = InputStreams.compareFiles(Common.getXMLOutPath()+filename+".bin",Common.getXMLOutPath()+filename+"2.bin");
		}
		catch (Exception e) {
			throw new WrappingRuntimeException(e);
		}

		if (equal)
			System.out.println("NodeTest was successful");
		else
			throw new RuntimeException("NodeTest ERROR: Files are not equal!");
	}
}
