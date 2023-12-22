package xxl.tests.spatial.converters;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import xxl.core.cursors.Cursor;
import xxl.core.cursors.sources.io.FileInputCursor;
import xxl.core.spatial.KPE;
import xxl.core.spatial.converters.AsciiKPEConverter;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class AsciiKPEConverter.
 */
public class TestAsciiKPEConverter {

	/**
	 * Use-case for converting an ascii file containing rectangles to an Iterator of KPEs.
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 * 		   submit parameters when the main method is called.
	 * @throws IOException in case of I/O Error 
	 */
	public static void main(String[] args) throws IOException{

		if(args.length != 2){
			System.out.println("usage: java xxl.core.spatial.converters.AsciiKPEConverter <input file-name> <dim>");
			return;	
		}

		Cursor c = new FileInputCursor(
			new AsciiKPEConverter(
				Integer.parseInt(args[1])
			),
			new File(args[0]),
			4096*1024
		);

		DataOutputStream output = null;

		File outputFile = new File(args[1]);
		try{
			output = 	new DataOutputStream(
							new BufferedOutputStream(
								new FileOutputStream(outputFile),
							4096*1024
							)
						);
		}
		catch(IOException e){System.out.println(e);}

		int count = 0;

		while(c.hasNext()){
			KPE k = (KPE) c.next();
			k.write(output);
			count++;
		}
		c.close();
		output.close();
		System.out.println(count);
	}

}
