package xxl.tests.util.random;

import xxl.core.math.functions.RealFunction;
import xxl.core.math.functions.RealFunctions;
import xxl.core.util.random.ContinuousRandomWrapper;
import xxl.core.util.random.InversionDistributionBasedPRNG;
import xxl.core.util.random.JavaContinuousRandomWrapper;
import xxl.core.util.random.RejectionDistributionBasedPRNG;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class RejectionDistributionBasedPRNG.
 */
public class TestRejectionDistributionBasedPRNG {

	/**
	 * The main method contains some examples to demonstrate the usage
	 * and the functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 * 		submit parameters when the main method is called.
	 */
	public static void main( String[] args) {
		// prob. density function of the distribution F with x ~ F
		// pdf f(x) = x^4 + I(x)_[.3,1], x in [0,1]
		RealFunction f = RealFunctions.pdfCP00();
		/* probability density function to G(x): g(x) = 2x * I(x)[0,1]*/
		RealFunction g = RealFunctions.pdfCont01();
		/* Inversal function of G:  G^{-1}(y) = \sqrt(y) * I(y)[0,1] */
		RealFunction invG = RealFunctions.invDistCont01();
		// ---
		double c = 2.65;
		// ---
		ContinuousRandomWrapper jcrw = new JavaContinuousRandomWrapper();
		ContinuousRandomWrapper gRnd = new InversionDistributionBasedPRNG( jcrw, invG);
		RejectionDistributionBasedPRNG rb = new RejectionDistributionBasedPRNG( jcrw, f, g, c, gRnd);
		java.util.Iterator iterator = new xxl.core.cursors.sources.ContinuousRandomNumber( rb, 10000);
		System.out.println("# Random numbers distributed with the pdf f(x) = x^4 + I(x)_[.3,1], x in [0,1]");
		while( iterator.hasNext())
			System.out.println( iterator.next());
	}

}
