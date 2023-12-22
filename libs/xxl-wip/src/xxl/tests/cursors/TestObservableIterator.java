package xxl.tests.cursors;

import java.util.Observable;

import xxl.core.cursors.ObservableIterator;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class ObservableIterator.
 */
public class TestObservableIterator {

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
		
		// counting the objects
		ObservableIterator<Integer> iterator = new ObservableIterator<Integer>(
			new xxl.core.cursors.sources.DiscreteRandomNumber(new xxl.core.util.random.JavaDiscreteRandomWrapper(10), 100),
			xxl.core.functions.Functions.aggregateUnaryFunction(
				new xxl.core.math.statistics.parametric.aggregates.CountAll()
			)
		);
		java.util.Observer observer = new java.util.Observer() {
			public void update(Observable observable, Object object) {
				System.out.println("getting " + object + " from " + observable);
			}
		};
		iterator.addObserver(observer);

		while (iterator.hasNext())
			System.out.println("next=" + iterator.next());
		System.out.println("---");
	}

}
