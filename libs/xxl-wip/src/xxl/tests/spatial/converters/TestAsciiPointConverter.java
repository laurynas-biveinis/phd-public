package xxl.tests.spatial.converters;

import java.io.File;

import xxl.core.cursors.Cursor;
import xxl.core.cursors.sources.io.FileInputCursor;
import xxl.core.io.Convertables;
import xxl.core.spatial.converters.AsciiPointConverter;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class AsciiPointConverter.
 */
public class TestAsciiPointConverter {

	/**
	 *	Convertes an input-file of Ascii-points to an output-file of ConvertableTuple.
	 *  @param args array of <tt>String</tt> arguments. It can be used to
	 * 		   submit parameters when the main method is called.
	 *	       args[0] is the input-file name, args[1] - dimensionality, 
	 *         args[2] - output-file name
	 */
	public static void main(String[] args) {

		if(args.length != 3){
			System.out.println("usage: java xxl.core.spatial.converts.AsciiPointConverter <input file-name> <dim> <output file-name>");
			return;	
		}

		Cursor c = new FileInputCursor(new AsciiPointConverter(Integer.parseInt(args[1])), new File(args[0]), 4096*1024);

		System.out.println("No. of elements converted:\t"+  Convertables.write( args[2], c ) );
		c.close();
	}

}
