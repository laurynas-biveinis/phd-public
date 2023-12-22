package xxl.tests.io.converters;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import xxl.core.functions.AbstractFunction;
import xxl.core.io.converters.Converter;
import xxl.core.io.converters.IntegerConverter;
import xxl.core.io.converters.MapConverter;
import xxl.core.io.converters.StringConverter;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class MapConverter.
 */
public class TestMapConverter {
	
	/**
	 * Simple example using the map converter.
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

		Converter<Map<String, Integer>> conv = new MapConverter<String, Integer>(
			StringConverter.DEFAULT_INSTANCE,
			IntegerConverter.DEFAULT_INSTANCE,
			new AbstractFunction<Object, HashMap<String, Integer>>() {
				@Override
				public HashMap<String, Integer> invoke() {
					return new HashMap<String, Integer>();
				}
			}
		);
		
		java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
		java.io.DataOutputStream dos = new java.io.DataOutputStream(baos);
		
		conv.write(dos, map);
		dos.flush();
		map = null;
		
		java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(baos.toByteArray());
		java.io.DataInputStream dis = new java.io.DataInputStream(bais);
		
		map = conv.read(dis);
		
		for (Map.Entry<String, Integer> me : map.entrySet())
			System.out.println(me.getKey() + ": " + me.getValue());
		
		System.out.println();
	}

}
