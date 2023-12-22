package xxl.tests.xml.operators;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import javax.xml.parsers.SAXParserFactory;

import xxl.core.cursors.mappers.Mapper;
import xxl.core.cursors.sources.Enumerator;
import xxl.core.cursors.sources.io.ChannelCursor;
import xxl.core.util.WrappingRuntimeException;
import xxl.core.util.XXLSystem;
import xxl.core.util.concurrency.AsynchronousChannel;
import xxl.core.xml.operators.DOMTreeContentHandler;
import xxl.core.xml.operators.Operators;
import xxl.core.xml.operators.Sinks;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class DOMTreeContentHandler.
 */
public class TestDOMTreeContentHandler {

	/**
	 * Simple example using this handler.
	 * @param args The first argument must be the filename of an XML
	 * 	file inside the data directory, the second parameter must be
	 *  a tag name inside this document.
	 */
	public static void main (String args[]) {

		if (args.length==2) {
			final String fileName = args[0];
			final String tagName = args[1];
			
			// Example 1:
			// final String fileName = "nexmarktext.xml";
			// final String tagName = "people";
			// Example 2:
			// final String fileName = "actors.xml";
			// final String tagName = "row";
			System.out.println("Parsing the document with SAX ...... ");
			
			final AsynchronousChannel channel = new AsynchronousChannel();
			ChannelCursor cursor = new ChannelCursor (channel);
			new Thread() {
				public void run() {
					try {
						SAXParserFactory spf = SAXParserFactory.newInstance();
						spf.newSAXParser().parse(
							new BufferedInputStream(new FileInputStream(
								XXLSystem.getDataPath(new String[]{"xml"})+File.separator+fileName
							)),
							new DOMTreeContentHandler(tagName, channel)
						);
					}
					catch (Exception e) {
						throw new WrappingRuntimeException(e);
					}
				}
			}.start();
			
			Sinks.writeToFiles(cursor,
				new Mapper(
						Operators.getPrefixSuffixMapStringFunction(XXLSystem.getOutPath()+File.separator,".xml"),
					new Mapper(
						Operators.TO_STRING_FUNCTION,
						new Enumerator()
					)
				)
			);
			// System.out.println(Cursors.count(cursor));
			System.out.println("Test finished successfully");
		}
	}

}
