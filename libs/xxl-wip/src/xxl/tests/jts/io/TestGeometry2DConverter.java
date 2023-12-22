package xxl.tests.jts.io;

import java.io.IOException;

import xxl.connectivity.jts.Geometry2DAdapter;
import xxl.connectivity.jts.io.Geometry2DConverter;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class Geometry2DConverter.
 */
public class TestGeometry2DConverter {
	
	
	
	/** USE CASE: The main method contains some examples how to use a geometry converter. It
	 *  can also be used to test the functionality of a geometry converter.
	 *
	 * @param args array of <code>String</code> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 * @throws IOException includes any I/O exceptions that may occur.
	 */
	public static void main(String[] args) throws IOException{
		
		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////

		// create a byte array output stream
		java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
		// create a Geometry
		xxl.connectivity.jts.Point2DAdapter geometry0 = xxl.connectivity.jts.Geometry2DFactory.createPoint(5,5,5);
						
		// serialize the Geometry object to the given outputstream
		Geometry2DConverter.DEFAULT_INSTANCE.write( new java.io.DataOutputStream( output ), geometry0);
		
		// create a byte array input stream on the output stream
		java.io.ByteArrayInputStream input = new java.io.ByteArrayInputStream(output.toByteArray());
		
		// read a Geometry from the input stream
		Geometry2DAdapter geometry1 = Geometry2DConverter.DEFAULT_INSTANCE.read( new java.io.DataInputStream( input ));
		
		// print the original and the deserialized object
		System.out.println(geometry0);
		System.out.println(geometry1);
		// close the streams after use
		input.close();
		output.close();
		
		System.out.println();
	}	

}
