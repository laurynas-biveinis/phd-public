package xxl.tests.util.metaData;

import xxl.core.util.metaData.CompositeMetaData;
import xxl.core.util.metaData.MetaDataException;
import xxl.core.util.metaData.TransparentCompositeMetaData;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class TransparentCompositeMetaData.
 * 
 * @param <I> the type of the identifiers.
 * @param <M> the type of the metadata fragments.
 */
public class TestTransparentCompositeMetaData<I, M> extends TransparentCompositeMetaData<I, M> {
	
	/**
	 * Creates a new transparent composite metadata that wraps the given
	 * composite metadata. The wrapped composite metadata's fragments are made
	 * visible from the surrounding transparent composite metadata, but cannot
	 * be changed.
	 * 
	 * @param compositeMetaData the composite metadata to be wrapped.
	 */
	public TestTransparentCompositeMetaData(CompositeMetaData<I, M> compositeMetaData) {
		super(compositeMetaData);
	}
	
	/**
	 * Shows and tests the functionality of a transparent composite metadata.
	 * 
	 * @param args the arguments to the <tt>main</tt> method. Arguments
	 *        specified to this method are ignored.
	 */
	public static void main(String[] args) {
		CompositeMetaData<String, String> inner = new CompositeMetaData<String, String>();
		TestTransparentCompositeMetaData<String, String> outer = new TestTransparentCompositeMetaData<String, String>(inner);
		
		System.out.println("inner  = " + inner);
		System.out.println("outer- = " + outer.metaDataMap);
		System.out.println("outer+ = " + outer);
		
		System.out.println("inner.add(\"inner1\", \"inner1\")");
		inner.add("inner1", "inner1");
		System.out.println("inner.add(\"inner2\", \"inner2\")");
		inner.add("inner2", "inner2");
		System.out.println("inner.add(\"inner3\", \"inner3\")");
		inner.add("inner3", "inner3");
		
		System.out.println("outer.add(\"outer1\", \"outer1\")");
		outer.add("outer1", "outer1");
		System.out.println("outer.add(\"outer2\", \"outer2\")");
		outer.add("outer2", "outer2");
		
		System.out.println("inner  = " + inner);
		System.out.println("outer- = " + outer.metaDataMap);
		System.out.println("outer+ = " + outer);
		
		try {
			System.out.print("outer.add(\"inner1\", \"inner1*\") ");
			outer.add("inner1", "inner1*");
			
			throw new IllegalStateException();
		}
		catch (MetaDataException mde) {
			System.out.println(mde.getMessage());
		}
		
		try {
			System.out.print("outer.add(\"outer1\", \"outer1*\") ");
			outer.add("outer1", "outer1*");
			
			throw new IllegalStateException();
		}
		catch (MetaDataException mde) {
			System.out.println(mde.getMessage());
		}
		
		System.out.println("outer.add(\"outer3\", \"outer3*\")");
		outer.add("outer3", "outer3*");
		
		System.out.println("inner  = " + inner);
		System.out.println("outer- = " + outer.metaDataMap);
		System.out.println("outer+ = " + outer);
		
		System.out.print("outer.put(\"inner1\", \"inner1**\") = ");
		System.out.println(outer.put("inner1", "inner1**"));
		
		System.out.print("outer.put(\"outer1\", \"outer1**\") = ");
		System.out.println(outer.put("outer1", "outer1**"));
		
		System.out.print("outer.put(\"outer4\", \"outer4**\") = ");
		System.out.println(outer.put("outer4", "outer4**"));
		
		System.out.println("inner  = " + inner);
		System.out.println("outer- = " + outer.metaDataMap);
		System.out.println("outer+ = " + outer);
		
		System.out.print("outer.replace(\"inner2\", \"inner2***\") = ");
		System.out.println(outer.replace("inner2", "inner2***"));
		
		System.out.print("outer.replace(\"outer1\", \"outer1***\") = ");
		System.out.println(outer.replace("outer1", "outer1***"));
		
		try {
			System.out.print("outer.replace(\"outer5\", \"outer5***\") = ");
			System.out.println(outer.replace("outer5", "outer5***"));
			
			throw new IllegalStateException();
		}
		catch (MetaDataException mde) {
			System.out.println(mde.getMessage());
		}
		
		System.out.println("inner  = " + inner);
		System.out.println("outer- = " + outer.metaDataMap);
		System.out.println("outer+ = " + outer);
		
		try {
			System.out.print("outer.remove(\"inner3\") = ");
			System.out.println(outer.remove("inner3"));
			
			throw new IllegalStateException();
		}
		catch (MetaDataException mde) {
			System.out.println(mde.getMessage());
		}
		
		System.out.print("outer.remove(\"outer1\") = ");
		System.out.println(outer.remove("outer1"));
		
		try {
			System.out.print("outer.remove(\"outer6\") = ");
			System.out.println(outer.remove("outer6"));
			
			throw new IllegalStateException();
		}
		catch (MetaDataException mde) {
			System.out.println(mde.getMessage());
		}
		
		System.out.println("inner  = " + inner);
		System.out.println("outer- = " + outer.metaDataMap);
		System.out.println("outer+ = " + outer);
	}

}
