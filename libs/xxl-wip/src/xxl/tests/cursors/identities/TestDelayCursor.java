package xxl.tests.cursors.identities;

import xxl.core.cursors.identities.DelayCursor;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class DelayCursor.
 */
public class TestDelayCursor {
	
	/**
	 * The main method contains some examples to demonstrate the usage of the
	 * delay cursor. An emumerator constructs a sequence of 100 Integers that are
	 * consumed by a delay cursor.
	 *
	 * @param args array of <tt>String</tt> arguments. It can be used to submit
	 *        parameters when the main method is called.
	 */
	public static void main(String[] args) {

		/*********************************************************************/
		/*                            Example                                */
		/*********************************************************************/
		
		System.out.println("Example for the DelayCursor");
		
		DelayCursor delayCursor = new DelayCursor(
			new xxl.core.cursors.sources.Enumerator(10),
			1000,
			true
		);

		delayCursor.open();
		
		while (delayCursor.hasNext())
			System.out.println(delayCursor.next());
		
		delayCursor.close();

		System.out.println("Example for the DelayCursor");
		delayCursor = new DelayCursor(
			new xxl.core.cursors.sources.Enumerator(10),
			new xxl.core.cursors.sources.DiscreteRandomNumber(new xxl.core.util.random.JavaDiscreteRandomWrapper(1000)),
			true
		);

		delayCursor.open();
		
		while (delayCursor.hasNext())
			System.out.println(delayCursor.next());
		
		delayCursor.close();
	}

}
