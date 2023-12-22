package xxl.tests.xml.operators;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import xxl.core.cursors.Cursor;
import xxl.core.cursors.mappers.Mapper;
import xxl.core.cursors.sources.io.FileNameCursor;
import xxl.core.functions.Functions;
import xxl.core.util.WrappingRuntimeException;
import xxl.core.xml.operators.Operators;
import xxl.core.xml.operators.Sinks;
import xxl.core.xml.operators.Sources;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class Operators.
 */
public class TestOperators {

	/**
	 * Test case which transforms the documents inside data/xml/weather
	 * according to a xslt-description data/xml/weather/weather.xsl.
	 * The output files are written to output/xml/weather.
	 * @param args Command line arguments are ignored here.
	 */
	public static void main(String args[]) {
		DocumentBuilder dbuild;
		
		try {
			dbuild = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		}
		catch (ParserConfigurationException e) {
			throw new WrappingRuntimeException(e);
		}
		
		String inPath = xxl.core.util.XXLSystem.getDataPath(new String[]{"xml","weather"});
		String outPath = xxl.core.util.XXLSystem.getOutPath(new String[]{"output","xml","weather"});
		System.out.println("inPath: "+inPath);
		System.out.println("outPath: "+outPath);
		
		// String path=".";
		Cursor filenamesCursor =
			new Mapper(
				Operators.getPrefixSuffixMapStringFunction(inPath,""),
				new xxl.core.cursors.filters.Filter(
					new FileNameCursor(inPath, false),
					Operators.getStringMatchingPredicate(".*[xX][mM][lL]$") // xml at the end
				)
			);

		// xxl.core.cursors.Cursors.println(filenamesCursor);
		// System.out.println(xxl.core.cursors.Cursors.count(filenamesCursor));

		Cursor c = Sources.readXMLFiles(filenamesCursor,dbuild);

		c = new Mapper(
			Operators.getXSLTMapFunction(inPath+java.io.File.separatorChar+"weather.xsl"),
			c
		);
		
		// xxl.cursors.Cursors.count(c);
		Sinks.writeToFiles(
			c,
			Functions.compose(
				Operators.getPrefixSuffixMapStringFunction(outPath+java.io.File.separatorChar,".html"),
				Operators.FILENAME_FROM_PATH_FUNCTION
			)
		);

		// different possibility: write into files with increasing number as file name.
		// writeToFiles (
		//	c,
		//	new Mapper(
		//		new xxl.cursors.Enumerator(),
		//		getPrefixSuffixMapStringFunction(".\\weather",".html")
		//	)
		// );
	}

}
