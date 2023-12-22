package xxl.tests.util.reflect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Stack;

import xxl.core.util.reflect.RuntimeExtender;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class RuntimeExtender.
 */
public class TestRuntimeExtender {
	
	/** Main method demonstrating the usage of the static methods of this class.
	 * 
	 * @param args command line parameters - not used
	 * @throws SecurityException can be caused by usage of reflection
	 * @throws NoSuchMethodException can be caused by usage of reflection
	 * @throws IllegalArgumentException can be caused by usage of reflection
	 * @throws IllegalAccessException can be caused by usage of reflection
	 * @throws InvocationTargetException can be caused by usage of reflection
	 */
	public static void main(String[] args) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		// Create a HashMap and insert a value
		HashMap hashmap = new HashMap();
		hashmap.put("XXL","XXL Library in Hashmap");

		// Extend hashmap object at runtime und retrieve inserted value
		HashMap extendedhashmap = (HashMap) RuntimeExtender.extendObject(hashmap);
		System.out.println(extendedhashmap.get("XXL"));
	
		// Append some metadata to the hashmap and get it back
		Method set1 = extendedhashmap.getClass().getDeclaredMethod("setMetaData", new Class [] {Object.class});
		set1.invoke(extendedhashmap, new Object [] {"Some MetaData for HashMap"} );		
		Method get1 = extendedhashmap.getClass().getDeclaredMethod("getMetaData");
		System.out.println(get1.invoke(extendedhashmap));
		
		// Show that the extended HashMap object equals the original one
		System.out.println(hashmap.equals(extendedhashmap));

		// Create a Stack and insert a value
		Stack stack = new Stack();
		stack.add("XXL Library on Stack");

		// Extend stack object at runtime und retrieve inserted value
		Stack extendedstack = (Stack) RuntimeExtender.extendObject(stack);
		System.out.println(extendedstack.firstElement());

		// Append some metadata to the stack and get it back	
		Method set2 = extendedstack.getClass().getDeclaredMethod("setMetaData", new Class [] {Object.class});
		set2.invoke(extendedstack, new Object [] {"Some MetaData for Stack"} );		
		Method get2 = extendedstack.getClass().getDeclaredMethod("getMetaData");
		System.out.println(get2.invoke(extendedstack));

		// Show that the extended Stack object equals the original one
		System.out.println(stack.equals(extendedstack));
	}

}
