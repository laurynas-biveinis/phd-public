package xxl.tests.util.random;

import xxl.core.math.functions.RealFunction;
import xxl.core.math.functions.RealFunctions;
import xxl.core.util.random.ContinuousRandomWrapper;
import xxl.core.util.random.InversionDistributionBasedPRNG;
import xxl.core.util.random.JavaContinuousRandomWrapper;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class InversionDistributionBasedPRNG.
 */
public class TestInversionDistributionBasedPRNG {

	/**
	 * The main method contains some examples to demonstrate the usage
	 * and the functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 * 		submit parameters when the main method is called.
	 */
	public static void main( String [] args){
		ContinuousRandomWrapper jcrw = new JavaContinuousRandomWrapper();
		RealFunction invG = RealFunctions.invDistCont01();
		// ---
		InversionDistributionBasedPRNG inv = new InversionDistributionBasedPRNG( jcrw, invG);
		java.util.Iterator iterator = new xxl.core.cursors.sources.ContinuousRandomNumber( inv, 10000);
		System.out.println("# Random numbers distributed like G(y) = y^2 with y in [0,1] ( g(x) = 2x , x in [0,1]");
		while( iterator.hasNext())
			System.out.println( iterator.next());
	}

}
