package xxl.tests.cursors;

import java.util.LinkedList;
import java.util.Map;

import xxl.core.cursors.Cursor;
import xxl.core.cursors.Cursors;
import xxl.core.functions.AbstractFunction;
import xxl.core.functions.Identity;
import xxl.core.predicates.AbstractPredicate;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class Cursors.
 */
public class TestCursors {

	/**
	 * The main method contains some examples to demonstrate the usage and the
	 * functionality of this class.
	 *
	 * @param args array of <code>String</code> arguments. It can be used to
	 *        submit parameters when the main method is called.
	 */
	public static void main(String[] args) {

		/*********************************************************************/
		/*                            Example 1                              */
		/*********************************************************************/
		
		Cursor<Integer> cursor = new xxl.core.cursors.sources.Enumerator(11);
		cursor.open();
		
		System.out.println("First element:"+ Cursors.first(cursor));
		
		cursor.reset();
		System.out.println("Third element: "+ Cursors.nth(cursor, 3));
		
		cursor.reset();
		System.out.println("Last element: "+ Cursors.last(cursor));
		
		cursor.reset();
		System.out.println("Length: "+ Cursors.count(cursor));
		
		cursor.close();

		/*********************************************************************/
		/*                            Example 2                              */
		/*********************************************************************/
		
		Map.Entry<?, LinkedList<Integer>> entry = Cursors.maximize(
			cursor = new xxl.core.cursors.sources.DiscreteRandomNumber(
				new xxl.core.util.random.JavaDiscreteRandomWrapper(100),
				50
			),
			new Identity<Integer>()
		);
		System.out.println("Maximal value is : " + entry.getKey());
		
		System.out.print("Maxima : ");
		for (Integer next : entry.getValue())
			System.out.print(next + "; ");
		System.out.flush();
		
		cursor.close();

		/*********************************************************************/
		/*                            Example 3                              */
		/*********************************************************************/
		
		Cursors.forEach(
			new AbstractFunction<Integer, Integer>() {
				@Override
				public Integer invoke(Integer object) {
					Integer result = (int)Math.pow(object, 2);
					System.out.println("Number : " + object + " ; Number^2 : " + result);
					return result;
				}
			},
			cursor = new xxl.core.cursors.sources.Enumerator(11)
		);
		cursor.close();

		/*********************************************************************/
		/*                            Example 4                              */
		/*********************************************************************/
		
		System.out.println(
			"Is the number '13' contained in the discrete random numbers' cursor: " +
			Cursors.any(
				new AbstractPredicate<Integer>() {
					@Override
					public boolean invoke(Integer object) {
						return object == 13;
					}
				},
				cursor = new xxl.core.cursors.sources.DiscreteRandomNumber(
					new xxl.core.util.random.JavaDiscreteRandomWrapper(1000),
					200
				)
			)
		);
		cursor.close();
	}

}
