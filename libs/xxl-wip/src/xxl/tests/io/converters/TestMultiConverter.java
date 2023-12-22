package xxl.tests.io.converters;

import java.io.IOException;

import xxl.core.io.converters.Converter;
import xxl.core.io.converters.IntegerConverter;
import xxl.core.io.converters.MultiConverter;
import xxl.core.io.converters.StringConverter;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class MultiConverter.
 */
public class TestMultiConverter {
	
	/**
	 * Simple example using a multi converter converting the elements of a map.
	 * 
	 * @param args the command line arguments are ignored here.
	 * @throws IOException includes any I/O exceptions that may occur.
	 */
	public static void main(String[] args) throws IOException {
		java.util.Map<String, Integer> map = new java.util.HashMap<String, Integer>();
		map.put("Audi",     27000);
		map.put("Mercedes", 30000);
		map.put("BMW",      29000);
		map.put("VW",       24000);
		
		System.out.println("Example");
		System.out.println("=======");

		java.util.Iterator<java.util.Map.Entry<String, Integer>> it = map.entrySet().iterator();

		Converter<java.util.Map.Entry<?, ?>> conv = new MultiConverter<java.util.Map.Entry<?, ?>>(
			xxl.core.collections.MapEntry.FACTORY_METHOD,
			xxl.core.collections.MapEntry.TO_OBJECT_ARRAY_FUNCTION,
			StringConverter.DEFAULT_INSTANCE,
			IntegerConverter.DEFAULT_INSTANCE
		);
		
		java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
		java.io.DataOutputStream dos = new java.io.DataOutputStream(baos);
		
		while (it.hasNext())
			conv.write(dos, it.next());
		dos.flush();
			
		java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(baos.toByteArray());
		java.io.DataInputStream dis = new java.io.DataInputStream(bais);
			
		for (int i = 0; i < 4; i++)
			System.out.println(conv.read(dis));
	}

}
