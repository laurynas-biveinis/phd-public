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

package xxl.core.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import xxl.core.io.NullOutputStream;
import xxl.core.util.metaData.CompositeMetaData;
import xxl.core.util.metaData.MetaDataException;

/**
 * The <code>XXLSystem</code> class contains 
 * system related methods.
 *
 * For example there are two static
 * methods to determine the memory size of an object.
 * It cannot be instantiated.
 *
 * @see java.util.HashSet
 * @see java.util.Set
 * @see java.lang.reflect.Array
 * @see java.lang.reflect.Field
 * @see java.lang.reflect.Modifier
 */

public class XXLSystem {

	/** Don't let anyone instantiate this class. 
	 */
	private XXLSystem() {}

	/** The "standard" null stream. This stream is already open and ready to accept output data.
	 * Typically this stream corresponds to suppress any display output. This stream fills the 
	 * gap in {@link java.lang.System} providing "only" standard {@link java.io.PrintStream print streams}
	 * for standard out and errors but no "null sink".
	 * See the println methods in class PrintStream.
	 */
	public static final PrintStream NULL = new PrintStream( NullOutputStream.NULL);

	/** The size of a reference in memory. */
	public final static int REFERENCE_MEM_SIZE = 8;

	/**
	 * This class is a Wrapper for objects
	 * with the intention to distinguish
	 * two objects if and only if they
	 * refer to the same object.
	 * That means for any reference values <code>x</code> and
	 * <code>y</code>, the method <code>equals()</code>
	 * returns <code>true</code> if and only if <code>x</code> and
	 * <code>y</code> refer to the same object
	 * (<code>x==y</code> has the value <code>true</code>).
	 * <p>
	 * It is used in the method {@link XXLSystem#getObjectSize(Object, Class, Set, Set, Set)} 
	 * where attributes of the object to be analyzed
	 * are inserted in a HashSet
	 * with the intention to create an exact
	 * image of the memory allocated for this given object.
	 * Each non-primitive attribute is wrapped
	 * using this class before inserting it into
	 * the HashSet, so the HashSet
	 * contains only one instance of
	 * each traversed class during determining
	 * the object size.
	 * Because of wrapping the non-primitive attributes the
	 * <code>equals</code>-method of this class
	 * is used, in contradiction to inserting
	 * the objects directly in the HashSet,
	 * the attributes's own <code>equals</code>-method
	 * will be used, but this may lead to duplicates,
	 * like the following example demonstrates:
	 * <br><br>
	 * <code><pre>
	 * 	class Test {
	 * 		public Integer a, b;
	 *
	 * 		public Test(Integer a, Integer b) {
	 * 			this.a = a;
	 * 			this.b = b;
	 * 		}
	 *
	 * 		public static void main(String[] args) throws Exception {
	 * 			Test test = new Test(new Integer(7), new Integer(7));
	 * 			System.out.println("a==b ? "+(test.a==test.b));
	 * 			System.out.println("a.equals(b) ? "+(test.a.equals(test.b)));
	 * 			System.out.println("getObjectSize(test) = "+getObjectSize(test));
	 * 		}
	 * 	}
	 * </code></pre>
	 * The output of this short example is:
	 * <br><pre>
	 * 	a==b ? false
	 * 	a.equals(b) ? true
	 * 	getObjectSize(test) = 32
	 * </pre>
	 * If the attributes inserted into the HashSet were not wrapped,
	 * the returned object size would only be 28, because
	 * the attribute <tt>a</tt> was detected to be equal to attribute <tt>b</tt>,
	 * and therefore only a reference pointing to the same object would be
	 * saved in memory.
	 *
	 * @see XXLSystem#getObjectSize(Object, Class, Set, Set, Set)
	 */
	protected static final class Wrapper {

		/** The wrapped object. */
		public final Object object;

		/**
		 * Creates a new Wrapper
		 * by wrapping the given object.
		 *
		 * @param object the object to be wrapped.
		 */
		public Wrapper (Object object) {
			this.object = object;
		}

		/**
		 * The <tt>equals</tt> method for class <code>Object</code> implements
		 * the most discriminating possible equivalence relation on objects;
		 * that is, for any reference values <code>x</code> and <code>y</code>,
		 * this method returns <code>true</code> if and only if <code>x</code> and
		 * <code>y</code> refer to the same object (<code>x==y</code> has the
		 * value <code>true</code>).
		 *
		 * @param wrapper the reference object with which to compare.
		 * @return  <code>true</code> if this object is the same as
		 * 		the wrapper argument; <code>false</code> otherwise.
		 */
		public boolean equals (Object wrapper) {
			return (object == ((Wrapper)wrapper).object);
		}

		/**
		 * Returns a hash code value for the wrapped object.
		 * This method is supported for the benefit
		 * of hashtables such as those provided by
		 * <code>java.util.Hashtable</code>.
		 *
		 * @return a hash code value for this object.
		 * @see java.lang.Object#hashCode()
		 * @see java.util.Hashtable
		 */
		public int hashCode () {
			return object.hashCode();
		}
	}

	/**
	 * This method computes the memory size
	 * of the given object.
	 * To get the memory size of the specified
	 * object, a reference to this object is
	 * needed. If this reference is <code>null</code>,
	 * the trivial case,
	 * the returned object size is 8 byte, because
	 * that is only the size of a reference in
	 * memory. Otherwise {@link #getObjectSize(Object, Class, Set, Set, Set)}
	 * is called, which anaylizes the class this object
	 * is an instance of, the superclasses and especially the
	 * memory size allocated for each non-static and non-transient
	 * attribute of these classes is determined
	 * and saved in a HashSet.
	 * For further information of saving the objects
	 * in the HashSet see {@link Wrapper}.
	 * @see #getObjectSize(Object, Class, Set, Set, Set)
	 *
	 * @param object the object the memory size is to be determined.
	 * @param classesExcl Set of classes which are excluded during counting.
	 * @param fieldsExcl Sets of field names which are excluded during counting.
	 * @return the memory size of the specified object.
	 * @throws IllegalAccessException
	 */
	public static int getObjectSize(Object object, Set classesExcl, Set fieldsExcl) throws IllegalAccessException {
		return (object==null) ? REFERENCE_MEM_SIZE :
			getObjectSize(object, object.getClass(), new HashSet(), classesExcl, fieldsExcl);
	}

	/**
	 * This method computes the memory size
	 * of the given object.
	 * To get the memory size of the specified
	 * object, a reference to this object is
	 * needed. If this reference is <code>null</code>,
	 * the trivial case,
	 * the returned object size is 8 byte, because
	 * that is only the size of a reference in
	 * memory. Otherwise {@link #getObjectSize(Object, Class, Set, Set, Set)}
	 * is called, which anaylizes the class this object
	 * is an instance of, the superclasses and especially the
	 * memory size allocated for each non-static and non-transient
	 * attribute of these classes is determined
	 * and saved in a HashSet.
	 * For further information of saving the objects
	 * in the HashSet see {@link Wrapper}.
	 * @see #getObjectSize(Object, Class, Set, Set, Set)
	 *
	 * @param object the object the memory size is to be determined.
	 * @return the memory size of the specified object.
	 * @throws IllegalAccessException
	 */
	public static int getObjectSize(Object object) throws IllegalAccessException {
		return getObjectSize(object, null, null);
	}

	/**
	 * The size of an object is computed recursively, by
	 * determining the attributes of the class the object
	 * is an instance of.
	 * The algorithm starts with the reference memory size
	 * of 8 byte that is allocated for each object to hold
	 * the reference on it.
	 * Then the attributes (fields) of the class and the
	 * super classes are determined with regard to the
	 * object to be analyzed.
	 * If an attribute is a primitive type the size
	 * is added to the reference memory size. <br>
	 * If it is an array the size of the array's
	 * component type is computed and then multiplied
	 * with the length of the array. The resulting
	 * value is added to the reference size allocated
	 * for this array and at last 4 bytes are added
	 * needed to save the length information of an
	 * array. <br>
	 * Otherwise the attribute is an object
	 * and the method is called recursively adding
	 * the size of this attribute to the previous
	 * computed object size.
	 * <p>
	 * <b>Overview:</b> <br><br>
	 * primitive types:<p>
	 * <ul>
	 * <li> int, float : 	4 byte </li>
	 * <li> byte, boolean:	1 byte </li>
	 * <li> char, short: 	2 byte </li>
	 * <li> long, double: 	8 byte </li>
	 * </ul>
	 * <br>
	 * size of a reference in memory: 8 byte
	 * <p>
	 * size of an array: <br>
	 * <pre>
	 * 8 byte (reference)
	 * + 4 byte (length information (int))
	 * + (array.length * bytes allocated component type)
	 * </pre>
	 * All traversed attributes are inserted in a HashSet,
	 * which represents an exact image of the allocated
	 * memory concerning the given object.<p>
	 * <b>Note:</b> This HashSet is a non-cyclic graph.
	 *
	 * @param object the object the memory size is to be determined.
	 * @param cl the class the object to be analyzed is an instance of.
	 * @param hs the HashSet used to store the traversed attributes.
	 * @param classesExcl Set of classes which are excluded during counting.
	 * @param fieldsExcl Sets of field names which are excluded during counting.
	 * @return the memory size of the specified object.
	 * @throws IllegalAccessException
	 */
	protected static int getObjectSize(Object object, Class cl, Set hs, Set classesExcl, Set fieldsExcl) throws IllegalAccessException {
		int size = 0;
		
		// Circle detection
		if ((object==null) || !hs.add(new Wrapper(object)))
			return size;
		
		cl = object.getClass(); // refresh object-reference
		if (cl.isArray()) {
			int length = Array.getLength(object);
			size += 4; // allocated for saving length
			cl = cl.getComponentType();
			for (int i=0; i<length; i++)
				size += getObjectSize(Array.get(object, i), cl, hs, classesExcl, fieldsExcl);
			
			return size;
		}
		for (; cl!=null; cl=cl.getSuperclass()) {
			Field[] fields = cl.getDeclaredFields();
			for (int i=0; i<fields.length; i++) {
				Field f = fields[i];
				if (!Modifier.isStatic(f.getModifiers()) && !Modifier.isTransient(f.getModifiers())) {
					Class clField = f.getType();
					if (classesExcl==null || !classesExcl.contains(clField)) {
						if (fieldsExcl==null || !fieldsExcl.contains(f.getName())) {
							f.setAccessible(true);
							if (clField.isPrimitive()) {
								if (clField.equals(Integer.TYPE) || clField.equals(Float.TYPE))
									size += 4;
								else if (clField.equals(Byte.TYPE) || clField.equals(Boolean.TYPE))
									size ++;
								else if (clField.equals(Character.TYPE) || clField.equals(Short.TYPE))
									size += 2;
								else
									size += 8; // Double and Long
							}
							else {
								size += REFERENCE_MEM_SIZE;
								size += getObjectSize(f.get(object), clField, hs, classesExcl, fieldsExcl);
							}
						}
					}
				}
			}
		}
		return size;
	}

	/**
	 * Serializes an object and returns its byte representation.
	 * @param o input object to be serialized.
	 * @return byte array containing the byte representation.
	 */
	public static byte[] serializeObject(Object o) {
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(output);
			out.writeObject(o);
			out.flush();
			return output.toByteArray();
		}
		catch (IOException e) {
			throw new WrappingRuntimeException(e);
		}
	}
	
	/**
	 * Deserializes a byte array to an object and return it.
	 * @param b byte array containing the byte representation of an object.
	 * @return deserialized object.
	 */
	public static Object deserializeObject(byte b[]) {
		try {
			return new ObjectInputStream(new ByteArrayInputStream(b)).readObject();
		}
		catch (IOException e) {
			throw new WrappingRuntimeException(e);
		}
		catch (ClassNotFoundException e) {
			throw new WrappingRuntimeException(e);
		}
	}

	/**
	 * Clones an object wheather or not the clone method is protected (via reflection).
	 * If a clone is not possible, then a RuntimeException is thrown.
	 * @param o to be cloned.
	 * @return the cloned Object.
	 */
	public static Object cloneObject(Object o) {
		try {
			Method m = o.getClass().getMethod("clone");
			m.setAccessible(true);
			return m.invoke(o);
		}
		catch (Exception e) {
			throw new WrappingRuntimeException(e);
		}
	}

	/**
	 * Determines iff the mainmaker called the current class. 
	 * @return true iff the mainmaker called the current class.
	 */
	public static boolean calledFromMainMaker() {
		try {
			throw new RuntimeException();
		}
		catch (Exception e) {
			StackTraceElement st[] = e.getStackTrace();
			for (int i=0; i<st.length; i++)
				if (st[i].getClassName().toLowerCase().indexOf("mainmaker")>=0)
					return true;
			return false;
		}
	}

	/**
	 * Returns the outpath that has been passed to java via the -D Option.
	 * @return String - the outpath
	 */
	public static String getOutPath() {
		String s = System.getProperty("xxloutpath");
		if (s==null)
			throw new RuntimeException("xxloutpath has not been given as a parameter. Use java -Dxxloutpath=...");
		else
			return s; 
	}

	/**
	 * Returns the rootpath that has been passed to java via the -D Option.
	 * @return String - the outpath
	 */
	public static String getRootPath() {
		String s = System.getProperty("xxlrootpath");
		if (s==null)
			throw new RuntimeException("xxlrootpath has not been given as a parameter. Use java -Dxxlrootpath=...");
		else
			return s; 
	}

	/**
	 * Constructs a directory inside the outpath with the desired subdirectory.
	 * The partial names of the subdirectory have to be passed inside the subdirs
	 * array.
	 * The path does not have a file separator at the end.
	 * @param subdirs partial names of the path
	 * @return whole path (contains a file separator at the end).
	 */
	public static String getOutPath(String subdirs[]) {
		StringBuffer sb = new StringBuffer(XXLSystem.getOutPath());
		for (int i=0; i<subdirs.length; i++)
			sb.append(File.separator + subdirs[i]);
		String s = sb.toString();
		File f = new File(s);
		f.mkdirs();
		return s;
	}

	/** 
	 * Returns a data path inside xxl/data, which points to
	 * a certain subdirectory.
	 * The partial names of the subdirectory have to be passed inside the subdirs
	 * array.
	 * The path does not have a file separator at the end.
	 * 
	 * @param subdirs string containing subpath
	 * @return whole path (contains a file separator at the end).
	 */
	public static String getDataPath(String subdirs[]) {
		StringBuffer sb = new StringBuffer(XXLSystem.getRootPath());
		sb.append(File.separator + "data");
		for (int i=0; i<subdirs.length; i++)
			sb.append(File.separator + subdirs[i]);
		return sb.toString();
	}


	/**
	 * Returns the version of the Java RTE (only the major version
	 * number).
	 *
	 * @return returns the Java RTE major version number
	 */
	public static double getJavaVersion() {
		String s = System.getProperty("java.version");
		int pos = s.indexOf('.');
		if (pos>=0) {
			pos = s.indexOf('.',pos+1);
			if (pos>0)
				s = s.substring(0,pos);
		}
		return Double.parseDouble(s);
	}
	
	/**
	 * Tries to get a value from the meta data of given object, specified by the key. 
	 * If the object does not implement the CompositeMetaData interface or the key
	 * is not found, null is returned. 
	 * @see xxl.core.util.metaData.CompositeMetaData
	 *  
	 * @param key the specified key
	 * @param object an object
	 * @return the value specified by the key.
	 */
	public static Object getValueFromMD (String key, Object object) {
		if (object == null || !(object instanceof CompositeMetaData))
			return null;
		try {
			return ((CompositeMetaData)object).get(key);	
		}
		catch (MetaDataException e) {
			return null;
		}		
	}

	/**
	 * The main method contains some examples for methods of this class.
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 *	submit parameters when the main method is called.
	 */
	public static void main (String args[]) {
		System.out.println("Object serialization/deserialization");
		byte b1[] = serializeObject(new String("hello world"));
		byte b2[] = serializeObject(new Integer(4711));
		String s = (String) deserializeObject(b1);
		Integer i = (Integer) deserializeObject(b2);
		
		System.out.println("String: "+s);
		System.out.println("Integer: "+i);

		System.out.println();
		System.out.println("Rootpath: "+getRootPath());
		System.out.println("Outpath: "+getOutPath());
		
		try {
			double d[] = new double[10];
			int length;
			length = XXLSystem.getObjectSize(d);
			System.out.println(length+" (should be 84)");
			if (length!=84)
				throw new RuntimeException("Size not correct");
			
			Double d2 = new Double(2);
			length = XXLSystem.getObjectSize(d2);
			System.out.println(length+" (should be 8)");
			if (length!=8)
				throw new RuntimeException("Size not correct");
		}
		catch (Exception e) {
			throw new WrappingRuntimeException(e);
		}
		
	}
}
