package xxl.tests.colt;

import xxl.connectivity.colt.Samplers;
import xxl.core.cursors.sources.Enumerator;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class Samplers.
 */
public class TestSamplers {
	
	/**
	 * The main method contains some examples to demonstrate the usage
	 * and the functionality of this class.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to
	 * 		submit parameters when the main method is called.
	 */
	public static void main(String[] args) {

		/*********************************************************************/
		/*                            Example 1                              */
		/*********************************************************************/
		
		Object[] sample=new Samplers().sample(0,99,20,new Enumerator(0,100));
		for(int i=0;i<sample.length;i++)
			System.out.println((i+1)+"-th sample element: "+sample[i]);
	}

}
