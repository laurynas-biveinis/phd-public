package xxl.tests.util;

import java.util.TreeSet;

import xxl.core.util.LongIdGenerator;

/**
 * Some examples to demonstrate the functionality and the usage
 * of the class LongIdGenerator.
 */
public class TestLongIdGenerator {

	/**
	 * Contains a simple example of this class. Start it 
	 * without any parameter.
	 * 
	 * @param args array of <tt>String</tt> arguments. Is not used
	 */
	public static void main(String args[]) {
		LongIdGenerator idgen = new LongIdGenerator(0,255);
		TreeSet ids = new TreeSet();
		long id;
		
		for (int i=0; i<=255; i++) {
			id = idgen.getIdentifyer(new xxl.core.functions.Constant(ids.iterator()));
			ids.add(new Short((short) id));
			System.out.print(id+" ");
		}
		System.out.println();

		id = idgen.getIdentifyer(new xxl.core.functions.Constant(ids.iterator()));
		System.out.println("Identifyer number 256: "+id);
		
		System.out.println("Remove identifyer 0");
		idgen.removeIdentifyer(0);

		id = idgen.getIdentifyer(new xxl.core.functions.Constant(ids.iterator()));
		System.out.println("New Identifyer: "+id);		

		id = idgen.getIdentifyer(new xxl.core.functions.Constant(ids.iterator()));
		System.out.println("New Identifyer: "+id);		

		System.out.println("\nNew very large Generator");
		idgen = new LongIdGenerator();
		ids = new TreeSet();
		
		System.out.println("Performing five reservations");
		for (int i=0; i<5; i++) {
			id = idgen.getIdentifyer(new xxl.core.functions.Constant(ids.iterator()));
			ids.add(new Long(id));
			System.out.println(id+" ");
		}
		System.out.println();

		System.out.println("\nNew very small generator");
		idgen = new LongIdGenerator(41,46);
		ids = new TreeSet();
		
		System.out.println("Performing external reservations (42+44)");
		idgen.makeExternalReservation(42);
		ids.add(new Short((short)42));
		idgen.makeExternalReservation(44);
		ids.add(new Short((short)44));
		System.out.println("Performing five reservations");
		for (int i=0; i<5; i++) {
			id = idgen.getIdentifyer(new xxl.core.functions.Constant(ids.iterator()));
			ids.add(new Short((short) id));
			System.out.println(id+" ");
		}
		System.out.println();
	}

}
