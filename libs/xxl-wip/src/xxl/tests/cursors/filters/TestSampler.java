package xxl.tests.cursors.filters;

import xxl.core.cursors.filters.Sampler;
import xxl.core.cursors.sources.ContinuousRandomNumber;
import xxl.core.util.random.JavaContinuousRandomWrapper;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class Sampler.
 */
public class TestSampler {

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
		
		Sampler<Integer> sampler = new Sampler<Integer>(
			new xxl.core.cursors.sources.Enumerator(21),
			new ContinuousRandomNumber(new JavaContinuousRandomWrapper()),
			0.5 // Bernoulli probability
		);
		
		sampler.open();
		
		while (sampler.hasNext())
			System.out.println(sampler.next());
		
		sampler.close();
	}

}
