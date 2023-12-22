package xxl.tests.io.converters;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import xxl.core.io.converters.Converter;
import xxl.core.io.converters.FixedSizeBlockConverter;
import xxl.core.io.converters.IntegerConverter;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class FixedSizeBlockConverter.
 */
public class TestFixedSizeBlockConverter {

	/**
	 * A test method which is used inside the main method.
	 * 
	 * @param c a converter used inside the example.
	 */
	private static void performTest(Converter<Integer> c) {
		try {
			xxl.core.io.Block b = new xxl.core.io.Block(20);
			DataOutput dout = b.dataOutputStream();
			
			c.write(dout, 13);
			c.write(dout, 42);
			
			DataInput din = b.dataInputStream();
			while (true)
				System.out.println(c.read(din));
		}
		catch (IOException ioe) {
			// ignore
		}
	}
	
	/**
	 * Shows a simple example.
	 * 
	 * @param args the command line arguments are ignored here.
	 */
	public static void main(String[] args) {

		//////////////////////////////////////////////////////////////////
		//                      Usage example (1).                      //
		//////////////////////////////////////////////////////////////////
		IntegerConverter ic = IntegerConverter.DEFAULT_INSTANCE;
		
		System.out.println("Test with an IntegerConverter (outputs three values too much)");
		performTest(ic);
		
		System.out.println("Test with a wrapped IntegerConverter (output which is wanted, two values)");
		performTest(new FixedSizeBlockConverter<Integer>(ic, 20));
	}

}
