package xxl.tests.functions;

import java.util.List;

import xxl.core.functions.AbstractFunction;
import xxl.core.functions.ArrayFactory;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class ArrayFactory.
 */
public class TestArrayFactory {

	/**
	 * The main method contains some examples to demonstrate the usage and the
	 * functionality of this class.
	 *
	 * @param args array of <code>String</code> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main(String[] args){
		ArrayFactory<List<Integer>> f = new ArrayFactory<List<Integer>>(
			new AbstractFunction<Object, List<Integer>[]>() {
				@Override
				public List<Integer>[] invoke(List<? extends Object> arguments){
					return new List[(Integer)arguments.get(0)];
				}
			},
			new AbstractFunction<Object, List<Integer>>() {
				@Override
				public List<Integer> invoke(List<? extends Object> arguments){
					java.util.ArrayList<Integer> list = new java.util.ArrayList<Integer>(1);
					list.add((Integer)arguments.get(0));
					return list;
				}
			}
		);

		List<Integer>[] array = f.invoke(10, new xxl.core.cursors.sources.Enumerator());
		int i = 0;
		for (List<Integer> list : array)
			System.out.println(i++ + "\t" + list);
	}

}
