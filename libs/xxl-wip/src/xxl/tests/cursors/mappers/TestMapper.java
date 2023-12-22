package xxl.tests.cursors.mappers;

import java.util.Iterator;
import java.util.List;

import xxl.core.cursors.Cursor;
import xxl.core.cursors.Cursors;
import xxl.core.cursors.mappers.Mapper;
import xxl.core.functions.AbstractFunction;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class Mapper.
 */
public class TestMapper {
	
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
		
		Mapper<Integer, Integer> mapper = new Mapper<Integer, Integer>(
			new AbstractFunction<Integer, Integer>() {
				public Integer invoke(Integer arguments) {
					return arguments * 2;
				}
			},
			new xxl.core.cursors.sources.Enumerator(21)
		);
		
		mapper.open();
		
		while (mapper.hasNext())
			System.out.print(mapper.next() + "; ");
		System.out.flush();
		System.out.println();
		
		mapper.close();

		/*********************************************************************/
		/*                            Example 2                              */
		/*********************************************************************/
		
		xxl.core.cursors.groupers.HashGrouper<Integer> hashGrouper = new xxl.core.cursors.groupers.HashGrouper<Integer>(
			new xxl.core.cursors.sources.Enumerator(21),
			new AbstractFunction<Integer, Integer>() {
				public Integer invoke(Integer next) {
					return next % 5;
				}
			}
		);
		
		hashGrouper.open();
		
		Cursor[] cursors = new Cursor[5];
		for (int i = 0; hashGrouper.hasNext() && i < 5; i++)
			cursors[i] = hashGrouper.next();
		
		mapper = new Mapper<Integer, Integer>(
			new AbstractFunction<Integer, Integer>() {
				public Integer invoke(List<? extends Integer> arguments) {
					return Cursors.minima(arguments.iterator()).getFirst();
				}
			},
			(Iterator<? extends Integer>[])cursors
		);
		
		mapper.open();
		
		while (mapper.hasNext())
			System.out.print(mapper.next() + "; ");
		System.out.flush();
		
		mapper.close();
	}

}
